package io.openindoormap.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.*;
import de.fraunhofer.iosb.ilt.sta.model.builder.*;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractFeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractLocationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.GdalContourCommandParams;
import io.openindoormap.domain.GdalGridCommandParams;
import io.openindoormap.sensor.AirQualityDatastream;
import io.openindoormap.sensor.AirQualityObservedProperty;
import io.openindoormap.sensor.TimeType;
import io.openindoormap.service.AirQualityService;
import io.openindoormap.support.LogMessageSupport;
import io.openindoormap.utils.ImageProcessingUtils;
import io.openindoormap.utils.NumberUtils;
import io.openindoormap.utils.SensorThingsUtils;
import io.openindoormap.utils.airquality.AirQuality;
import io.openindoormap.utils.airquality.ConcentrationsGenerator;
import io.openindoormap.utils.airquality.IndexCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geojson.Feature;
import org.geojson.Point;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * sensor 초기 데이터 생성 및 갱신
 */
@Service("airQualityService")
@RequiredArgsConstructor
@Slf4j
public class AirQualityServiceImpl implements AirQualityService {

    private final PropertiesConfig propertiesConfig;
    private SensorThingsUtils sta;
    private IndexCalculator indexCalculator;

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    private String airkoreaApiServiceUrl;
    private String airkoreaAuthKey;

    @PostConstruct
    public void postConstruct() {
        sta = new SensorThingsUtils();
        sta.init(propertiesConfig.getSensorThingsApiServer());
        // 인덱스 계산
        indexCalculator = new IndexCalculator(ConcentrationsGenerator.createCAIConcentrations());
        // 에어코리아 API 관련
        airkoreaApiServiceUrl = propertiesConfig.getAirkoreaApiServiceUrl();
        airkoreaAuthKey = propertiesConfig.getAirkoreaAuthKey();
    }

    /**
     * 초기 저장소 데이터 insert & update
     */
    @Override
    public void initSensorData() {

        // 저장소 목록
        JSONObject stationJson = getListStation();
        // things 의 모든 available 값을 false 처리
        updateThingsStatus();
        List<?> stationList = (List<?>) stationJson.get("items");
        // ObservedProperty init
        Map<String, ObservedProperty> observedPropertyMap = initObservedProperty();
        for (var station : stationList) {

            var json = (JSONObject) station;
            var stationName = json.get("stationName").toString();
            String lat = json.get("dmX").toString().trim();
            String lon = json.get("dmY").toString().trim();
            var mangName = json.get("mangName").toString();
            var addr = json.get("addr").toString();

            Map<String, Object> thingProperties = new HashMap<>();
            thingProperties.put("stationName", stationName);
            thingProperties.put("year", json.get("year"));
            thingProperties.put("mangName", mangName);
            thingProperties.put("item", json.get("item"));
            thingProperties.put("available", true);

            var thing = sta.hasThingWithRelationEntities(null, stationName);
            var datastreams = thing != null ? thing.getDatastreams().toList() : null;

            Point point = createPoint(lon, lat);
            Feature feature = createPointFeature(point);

            // Location
            Location locationEntity = initLocation(feature, addr);

            List<Location> locationList = new ArrayList<>();
            List<Entity> entityList = new ArrayList<>();

            if (point != null) {
                entityList.add(locationEntity);
                locationList.add(locationEntity);
            }

            Thing thingEntity = ThingBuilder.builder()
                    .id(thing != null ? thing.getId() : null)
                    .name(stationName)
                    .description("한국환경공단 측정소")
                    .properties(thingProperties)
                    .locations(locationList)
                    .build();

            entityList.add(thingEntity);
            entityList.addAll(initDatastreamAndSensor(thingEntity, datastreams, observedPropertyMap, stationName, mangName));
            entityList.add(initFeatureOfInterest(feature, stationName));

            initEntity(entityList, thing != null);
        }
    }

    /**
     * 미세먼지 측정 데이터 insert
     */
    @Override
    public void insertSensorData() {

        JSONObject stationJson = getListStation();
        JSONObject observationJson = getHourValue("전국");

        List<?> stationList = (List<?>) stationJson.get("items");
        List<?> observationList = (List<?>) observationJson.get("items");
        log.info("================== stationList size = {}", stationList.size());
        log.info("================== observationList size = {}", stationList.size());

        AirQualityObservedProperty observedProperty = AirQualityObservedProperty.PM10;
        String pgInfo = ImageProcessingUtils.getPgInfo(propertiesConfig, url, username, password);
        String utcDateTime = null;

        for (var station : stationList) {
            var jsonObject = (JSONObject) station;
            var stationName = (String) jsonObject.get("stationName");
            JSONObject json = new JSONObject();
            // JSONObject result = getHourValue(stationName);

            JSONObject result = null;
            for (var observation : observationList) {
                if (((JSONObject) observation).get("stationName").toString().equals(stationName)) {
                    result = (JSONObject) observation;
                    break;
                }
            }

            String dataTime = (String) result.get("dataTime");
            if (result == null || dataTime == null) {
                continue;
            }

            LocalDateTime t = LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            ZonedDateTime zonedDateTime = ZonedDateTime.of(t.getYear(), t.getMonthValue(), t.getDayOfMonth(), t.getHour(), 0, 0, 0, ZoneId.of("Asia/Seoul"));
            EntityList<Thing> things = sta.hasThingsByName(null, stationName);

            // 일치하는 thing 이 없을경우 skip
            if (things.size() == 0) continue;

            Thing thing = things.toList().get(0);
            EntityList<Datastream> datastreamList = thing.getDatastreams();
            //log.info("================== datastreamList size = {}", datastreamList.size());

            for (Datastream datastream : datastreamList) {

                double scaleFactor = 1.0;
                Object value;
                AirQuality airQuality;
                String name = datastream.getName();

                if (name.equals(AirQualityDatastream.PM10.getName())) {
                    scaleFactor = 1.0;
                    airQuality = AirQuality.PM10;
                    value = (result.get("pm10Flag") == null) ? result.get("pm10Value") : null;
                } else if (name.equals(AirQualityDatastream.PM25.getName())) {
                    scaleFactor = 1.0;
                    airQuality = AirQuality.PM25;
                    value = (result.get("pm25Flag") == null) ? result.get("pm25Value") : null;
                } else if (name.equals(AirQualityDatastream.SO2.getName())) {
                    scaleFactor = 1000.0;
                    airQuality = AirQuality.SO2;
                    value = (result.get("so2Flag") == null) ? result.get("so2Value") : null;
                } else if (name.equals(AirQualityDatastream.CO.getName())) {
                    scaleFactor = 1.0;
                    airQuality = AirQuality.CO;
                    value = (result.get("coFlag") == null) ? result.get("coValue") : null;
                } else if (name.equals(AirQualityDatastream.O3.getName())) {
                    scaleFactor = 1000.0;
                    airQuality = AirQuality.O3;
                    value = (result.get("o3Flag") == null) ? result.get("o3Value") : null;
                } else if (name.equals(AirQualityDatastream.NO2.getName())) {
                    scaleFactor = 1000.0;
                    airQuality = AirQuality.NO2;
                    value = (result.get("no2Flag") == null) ? result.get("no2Value") : null;
                } else {
                    // 24시간 datastream 에는 여기서 값을 넣지 않는다.
                    continue;
                }

                // Calculate AQI
                double concentration = Double.NaN;
                if (value != null) {
                    try {
                        concentration = Double.parseDouble(value.toString());
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        LogMessageSupport.printMessage(e);
                    }
                }

                if (Double.isNaN(concentration)) {
                    json.put("value", Double.toString(Double.NaN));
                    json.put("index", Integer.toString(IndexCalculator.INDEX_MISSING));
                    json.put("grade", Integer.toString(0));
                } else {
                    int aqi = indexCalculator.getAQI(airQuality, concentration);
                    int grade = indexCalculator.getGrade(airQuality, concentration);
                    json.put("value", value);
                    json.put("index", Integer.toString(aqi));
                    json.put("grade", Integer.toString(grade));
                }

                //log.info("======================= RESULT_JSON = {}", json);

                Observation observation = ObservationBuilder.builder()
                        .phenomenonTime(new TimeObject(ZonedDateTime.now()))
                        .resultTime(zonedDateTime)
                        .result(json)
                        .datastream(DatastreamBuilder.builder().id(Id.tryToParse(String.valueOf(datastream.getId()))).build())
                        .featureOfInterest(FeatureOfInterestBuilder.builder().id(Id.tryToParse(String.valueOf(thing.getId()))).build())
                        .build();

                int observationCount = datastream.getObservations().size();
                Observation lastObservation = observationCount > 0 ? datastream.getObservations().toList().get(0) : null;
                var lastTime = lastObservation != null ? lastObservation.getResultTime().withZoneSameInstant(ZoneId.of("Asia/Seoul")) : null;

                if (zonedDateTime.equals(lastTime)) {
                    observation.setId(lastObservation.getId());
                    sta.update(observation);
                } else {
                    sta.create(observation);
                }

            }

            utcDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        }

        if (utcDateTime == null) return;

        GdalGridCommandParams gridParams = new GdalGridCommandParams(utcDateTime, observedProperty, pgInfo);
        List<String> gridCommand = ImageProcessingUtils.rasterize(propertiesConfig, gridParams);

        GdalContourCommandParams contourParams = new GdalContourCommandParams();
        List<String> contourCommand = ImageProcessingUtils.vectorize(propertiesConfig, contourParams);

        // GDAL command 실행
        ImageProcessingUtils.executeRasterizeAndVectorize(propertiesConfig, gridCommand, contourCommand);

    }

    @Override
    public void deleteSensorData() {

        // find
        ZonedDateTime now = correctTime(ZonedDateTime.now(), 3600);
        ZonedDateTime start = now.minusDays(2);
        ZonedDateTime end = now.minusDays(1);
        StringBuilder observationFilter = new StringBuilder("resultTime ge " + start.toInstant() + " and resultTime le " + end.toInstant() + " and (");

        int length = AirQualityObservedProperty.values().length;
        for (int i = 0; i < length; i++) {
            AirQualityObservedProperty observedProperty = AirQualityObservedProperty.values()[i];
            String appendOr = " or ";
            observationFilter.append("Datastreams/ObservedProperties/name eq '").append(observedProperty.getName()).append("'");
            if (i != length - 1) {
                observationFilter.append(appendOr);
            }
        }
        observationFilter.append(")");

        log.info("@@@ delete observationFilter = {}", observationFilter.toString());
        EntityList<Observation> observations = sta.hasObservations(observationFilter.toString(), null);

        log.info("@@@ delete observations size = {}", observations.size());
        if (observations.size() == 0) {
            return;
        }

        // delete
        for (Observation observation : observations) {
            try {
                sta.getService().delete(observation);
            } catch (ServiceFailureException e) {
                log.error(e.getMessage());
                LogMessageSupport.printMessage(e);
            }
        }

    }

    public ZonedDateTime correctTime(ZonedDateTime dateTime, long interval) {
        long time = dateTime.getHour() * 3600L + dateTime.getMinute() * 60L + dateTime.getSecond();
        long diff = time - time / interval * interval;
        return dateTime.minusSeconds(diff);
    }

    /**
     * 미세먼지 하루 통계 데이터 생성
     */
    @Override
    public void insertStatisticsDaily() {
        EntityList<Thing> thingList = sta.hasThingsFindAll(getFilter());
        for (Thing thing : thingList) {
            insertObservationDaily(thing);
        }
    }

    /**
     * 미세먼지 타입별 24시간 Observation 생성
     *
     * @param thing Thing Entity
     */
    private void insertObservationDaily(Thing thing) {
        String stationName = thing.getName();
        ZonedDateTime now = ZonedDateTime.now().minusDays(1L);
        ZonedDateTime start = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 0, 0, 0, 0, now.getZone());
        ZonedDateTime end = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 23, 0, 0, 0, now.getZone());
        List<AirQualityObservedProperty> typeList = AirQualityObservedProperty.getObservedPropertyByType(TimeType.HOUR);
        for (AirQualityObservedProperty type : typeList) {
            String observationFilter = "resultTime ge " + start.toInstant() + " and resultTime le " + end.toInstant() +
                    " and Datastreams/Things/name eq '" + stationName + "' and Datastreams/ObservedProperties/name eq '" + type.getName() + "'";
            EntityList<Observation> observations = sta.hasObservations(observationFilter, null);
            if (observations.size() == 0) {
                continue;
            }
            JSONObject json = getObservationAverage(observations, type);
            String timeName = TimeType.DAILY.getValue();
            String dailyName = type.getName() + timeName.charAt(0) + timeName.toLowerCase().substring(1);
            String datastreamFilter = "Thing/name eq '" + stationName + "' and Datastream/ObservedProperty/name eq '" + dailyName + "'";
            Datastream datastream = sta.hasDatastream(datastreamFilter, null);
            if (datastream == null) {
                continue;
            }
            ZonedDateTime insertTime = ZonedDateTime.now();

            Observation observation = ObservationBuilder.builder()
                    .phenomenonTime(new TimeObject(insertTime))
                    .resultTime(start)
                    .result(json)
                    .datastream(datastream)
                    .featureOfInterest(FeatureOfInterestBuilder.builder().id(Id.tryToParse(String.valueOf(thing.getId()))).build())
                    .build();

            sta.create(observation);
        }
    }

    /**
     * Observation 평균 구하기
     *
     * @param observations Observation list
     * @param type         미세먼지 타입
     * @return JSONObject
     */
    private JSONObject getObservationAverage(EntityList<Observation> observations, AirQualityObservedProperty type) {
        JSONObject json = new JSONObject();
        int size = observations.size();
        if (type.equals(AirQualityObservedProperty.PM10) || type.equals(AirQualityObservedProperty.PM25)) {
            int observationSum = observations.stream().mapToInt(f -> {
                        Map<String, Object> map = (Map<String, Object>) f.getResult();
                        return Integer.parseInt(map.get("value").toString());
                    }).sum();

            json.put("value", observationSum / size);

            return json;
        }

        double observationSum = observations.stream().mapToDouble(f -> {
                    Map<String, Object> map = (Map<String, Object>) f.getResult();
                    return Double.parseDouble(map.get("value").toString());
                }).sum();

        json.put("value", NumberUtils.round(5, observationSum / size));

        return json;
    }

    /**
     * observedProperty insert 후 map 에 담아서 리턴
     *
     * @return Map
     */
    private Map<String, ObservedProperty> initObservedProperty() {
        Map<String, ObservedProperty> map = new HashMap<>();
        for (AirQualityObservedProperty entity : AirQualityObservedProperty.values()) {
            map.put(entity.getName(), sta.createObservedProperty(null, entity.getName(), entity.getDescription(), entity.getDefinition()));
        }
        log.info("================== ObservedProperty insert success ==================");
        return map;
    }

    /**
     * location insert & update
     *
     * @param feature 위치정보
     * @param address 주소
     * @return Location
     */
    private Location initLocation(Feature feature, String address) {
        Location location = sta.hasLocation(null, address);

        return LocationBuilder.builder()
                .id(location != null ? location.getId() : null)
                .name(address)
                .encodingType(AbstractLocationBuilder.ValueCode.GeoJSON)
                .description("대기질 측정소 위치")
                .location(feature)
                .build();
    }

    /**
     * datastream, sensor insert & update
     *
     * @param thing               thing entity
     * @param datastreams         datastream entity
     * @param observedPropertyMap observedProperty into map
     * @param stationName         측정소 명
     * @param mangName            관리타입
     * @return List
     */
    private List<Entity> initDatastreamAndSensor(Thing thing, List<Datastream> datastreams, Map<String, ObservedProperty> observedPropertyMap, String stationName, String mangName) {
        List<Entity> entityList = new ArrayList<>();
        int i = 0;
        if (datastreams != null) {
            // sort
            datastreams.sort((o1, o2) -> {
                var o1Value = Integer.parseInt(String.valueOf(o1.getId()));
                var o2Value = Integer.parseInt(String.valueOf(o2.getId()));
                return o1Value - o2Value;
            });
        }

        for (AirQualityObservedProperty entity : AirQualityObservedProperty.values()) {
            AirQualityDatastream datastreamType = AirQualityDatastream.values()[i];
            Datastream datastream = datastreams != null ? datastreams.get(i) : null;
            Sensor sensor = null;
            try {
                sensor = datastream != null ? datastream.getSensor() : null;
            } catch (ServiceFailureException e) {
                log.info(" getSensor. message = {} ", e.getMessage());
            }
            Sensor sensorEntity = SensorBuilder.builder()
                    .id(sensor != null ? sensor.getId() : null)
                    .name(stationName + ":" + datastreamType.getName())
                    .description("미세먼지 측정소")
                    .encodingType("http://www.opengis.net/doc/IS/SensorML/2.0")
                    .metadata(mangName)
                    .build();
            entityList.add(sensorEntity);

            Datastream datastreamEntity = DatastreamBuilder.builder()
                    .id(datastream != null ? datastream.getId() : null)
                    .name(datastreamType.getName())
                    .description(datastreamType.getName())
                    .observationType(AbstractDatastreamBuilder.ValueCode.OM_Observation)
                    .unitOfMeasurement(datastreamType.getUnitOfMeasurement())
                    .sensor(sensorEntity)
                    .observedProperty(observedPropertyMap.get(entity.getName()))
                    .thing(thing)
                    .build();
            entityList.add(datastreamEntity);
            i++;
        }

        return entityList;
    }

    /**
     * featureOfInteres insert & update
     *
     * @param feature     위치정보
     * @param stationName 측정소명
     * @return FeatureOfInterest
     */
    private FeatureOfInterest initFeatureOfInterest(Feature feature, String stationName) {
        String foiName = stationName + " 측정소";
        FeatureOfInterest foi = sta.hasFeatureOfInterest(null, foiName);

        return FeatureOfInterestBuilder.builder()
                .id(foi != null ? foi.getId() : null)
                .name(foiName)
                .description("한국환경공단 대기질 측정소")
                .encodingType(AbstractFeatureOfInterestBuilder.ValueCode.GeoJSON)
                .feature(feature)
                .build();
    }

    /**
     * sta entity insert & update
     *
     * @param entityList  entityList
     * @param entityExist insert & update flag
     */
    private void initEntity(List<Entity> entityList, boolean entityExist) {
        try {
            for (var entity : entityList) {
                if (entityExist) {
                    sta.update(entity);
                } else {
                    sta.create(entity);
                }
            }
        } catch (Exception e) {
            log.error("-------- AirQualityService create & update Error = {}", e.getMessage());
            LogMessageSupport.printMessage(e);
        }
    }

    /**
     * 미세먼지에 해당하는 모든 thing 의 정보들의 status false 로 업데이트
     */
    private void updateThingsStatus() {
        EntityList<Thing> thingList = sta.hasThingsFindAll(getFilter());
        log.info("updateThingsStatus size ====================== {} ", thingList.size());
        for (var thing : thingList) {
            var properties = thing.getProperties();
            properties.put("available", false);
            thing.setProperties(properties);
            sta.update(thing);
        }
    }

    /**
     * 측정소 목록 조회
     *
     * @return JSONObject
     */
    private JSONObject getListStation() {
        boolean mockEnable = propertiesConfig.isMockEnable();
        JSONObject stationJson = null;
        if (mockEnable) {
            // mock 데이터 생성
            log.info("mock 미세먼지 저장소 목록");
            stationJson = parseMockFile("sample/airQualityStationNew.json");
            JSONObject apiResultJson = (JSONObject) stationJson.get("response");
            stationJson = parseBody(apiResultJson);
        } else {
            // api 연동
            log.info("api 연동 [한국환경공단_에어코리아_측정소정보]");
            String url = airkoreaApiServiceUrl + "/MsrstnInfoInqireSvc/getMsrstnList";
            UriComponents builder;
            if ("product".equalsIgnoreCase(propertiesConfig.getProfile())) {
                builder = UriComponentsBuilder.fromHttpUrl(url).build(false);
            } else {
                builder = UriComponentsBuilder.fromHttpUrl(url)
                        .queryParam("serviceKey", UriUtils.encode(airkoreaAuthKey, StandardCharsets.UTF_8))
                        .queryParam("numOfRows", 10000)
                        .queryParam("pageNo", 1)
                        .queryParam("returnType", "json")
                        .build(false);    //자동으로 encode해주는 것을 막기 위해 false
            }
            stationJson = getAPIResult(builder.toString());
        }
        return stationJson;
    }

    /**
     * 측정소에 해당하는 미세먼지 데이터 조회
     *
     * @param sidoName 측정소 이름
     * @return JSONObejct
     */
    private JSONObject getHourValue(String sidoName) {
        boolean mockEnable = propertiesConfig.isMockEnable();
        JSONObject observationJson = new JSONObject();
        // 테스트
        if (mockEnable) {
            log.info("mock 미세먼지 관측값");
            observationJson = parseMockFile("sample/airQualityObservationNew.json");
            JSONObject apiResultJson = (JSONObject) observationJson.get("response");
            observationJson = parseBody(apiResultJson);

            List<?> observationList = (List<?>) observationJson.get("items");

            for (Object observation : observationList) {

                Random random = new Random();
                // 미세먼지 pm10
                int pm10Value = random.nextInt(601);
                int pm10Grade = indexCalculator.getGrade(AirQuality.PM10, pm10Value);
                //int pm10Grade = getGrade(pm10Value, AirQualityObservedProperty.PM10);
                // 미세먼지 pm2.5
                int pm25Value = random.nextInt(501);
                int pm25Grade = indexCalculator.getGrade(AirQuality.PM25, pm25Value);
                //int pm25Grade = getGrade(pm25Value, AirQualityObservedProperty.PM25);
                // 아황산가스 농도
                double so2Value = NumberUtils.round(5, random.nextFloat());
                int so2Grade = indexCalculator.getGrade(AirQuality.SO2, so2Value);
                //int so2Grade = getGrade(so2Value, AirQualityObservedProperty.SO2);
                // 일산화탄소 농도
                double coValue = NumberUtils.round(5, random.nextFloat() * 50);
                int coGrade = indexCalculator.getGrade(AirQuality.CO, coValue);
                //int coGrade = getGrade(coValue, AirQualityObservedProperty.CO);
                // 오존 농도
                double o3Value = NumberUtils.round(5, random.nextFloat() * 0.6);
                int o3Grade = indexCalculator.getGrade(AirQuality.O3, o3Value);
                //int o3Grade = getGrade(o3Value, AirQualityObservedProperty.O3);
                // 이산화질소 농도
                double no2Value = NumberUtils.round(5, random.nextFloat() * 2);
                int no2Grade = indexCalculator.getGrade(AirQuality.NO2, no2Value);
                //int no2Grade = getGrade(no2Value, AirQualityObservedProperty.NO2);

                ((JSONObject) observation).put("pm10Value", pm10Value);
                ((JSONObject) observation).put("pm25Value", pm25Value);
                ((JSONObject) observation).put("so2Value", so2Value);
                ((JSONObject) observation).put("coValue", coValue);
                ((JSONObject) observation).put("o3Value", o3Value);
                ((JSONObject) observation).put("no2Value", no2Value);
                ((JSONObject) observation).put("pm10Grade", pm10Grade);
                ((JSONObject) observation).put("pm25Grade", pm25Grade);
                ((JSONObject) observation).put("so2Grade", so2Grade);
                ((JSONObject) observation).put("coGrade", coGrade);
                ((JSONObject) observation).put("o3Grade", o3Grade);
                ((JSONObject) observation).put("no2Grade", no2Grade);
                ((JSONObject) observation).put("dataTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00")));

            }

            observationJson.put("items", observationList);

        } else {
            // 운영시 api 연동
            log.info("api 연동 [한국환경공단_에어코리아_대기오염정보]");
            String url = airkoreaApiServiceUrl + "/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty";
            UriComponents builder;
            if ("product".equalsIgnoreCase(propertiesConfig.getProfile())) {
                builder = UriComponentsBuilder.fromHttpUrl(url).build(false);
            } else {
                builder = UriComponentsBuilder.fromHttpUrl(url)
                        .queryParam("serviceKey", UriUtils.encode(airkoreaAuthKey, "UTF-8"))
                        .queryParam("numOfRows", 10000)
                        .queryParam("pageNo", 1)
                        .queryParam("sidoName", UriUtils.encode(sidoName, "UTF-8"))
                        .queryParam("ver", 1.3)
                        .queryParam("returnType", "json")
                        .build(false);    //자동으로 encode해주는 것을 막기 위해 false
            }
            observationJson = getAPIResult(builder.toString());
        }

        return observationJson;
    }

    private JSONObject parseMockFile(String mockFileName) {
        JSONObject json = null;
        URL resource = Objects.requireNonNull(this.getClass().getClassLoader().getResource(mockFileName));
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(resource.toURI()), StandardCharsets.UTF_8)) {
            JSONParser jsonParser = new JSONParser();
            json = (JSONObject) jsonParser.parse(bufferedReader);
        } catch (IOException | ParseException | URISyntaxException e) {
            log.info("-------- AirQualityService parse MockFile Error = {}", e.getMessage());
        }
        return json;
    }

    /**
     * requestURI 에 해당하는 api result return
     *
     * @param requestURI api 요청 requestURI
     * @return JSONObject
     */
    private JSONObject getAPIResult(String requestURI) {
        log.info("---------------- requestURI = {}", requestURI);
        RestTemplate restTemplate = new RestTemplate();
        JSONObject json = new JSONObject();
        JSONParser parser = new JSONParser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<?> response = restTemplate.exchange(new URI(requestURI), HttpMethod.GET, entity, String.class);
            JSONObject apiResultJson = (JSONObject) (((JSONObject) parser.parse(response.getBody().toString())).get("response"));
            json = parseBody(apiResultJson);
            //log.info("-------- statusCode = {}, body = {}", response.getStatusCodeValue(), response.getBody());
        } catch (URISyntaxException | ParseException e) {
            log.info("-------- AirQualityService getAirQualityData = {}", e.getMessage());
        }
        return json;
    }

    private JSONObject parseBody(JSONObject apiResultJson) {
        JSONObject apiResultHeader = (JSONObject) apiResultJson.get("header");
        JSONObject apiResultBody = (JSONObject) apiResultJson.get("body");
        if (!apiResultHeader.get("resultCode").toString().equals("00")) {
            return null;
        }
        List<?> resultList = (List<?>) apiResultBody.get("items");
        if (resultList.size() <= 0) {
            return null;
        }
        return apiResultBody;
    }

    /**
     * 미세먼지 관련 thing 만을 조회하기 위한 필터
     *
     * @return
     */
    private String getFilter() {
        return "Datastreams/ObservedProperties/name eq " + "'" + AirQualityObservedProperty.PM10.getName() + "'"
                + " or name eq " + "'" + AirQualityObservedProperty.PM25.getName() + "'" + " or name eq " + "'"
                + AirQualityObservedProperty.SO2.getName() + "'" + " or name eq " + "'"
                + AirQualityObservedProperty.CO.getName() + "'" + " or name eq " + "'"
                + AirQualityObservedProperty.O3.getName() + "'" + " or name eq " + "'"
                + AirQualityObservedProperty.NO2.getName() + "'";
    }

    public Boolean getDryRun() {
        return sta.getDryRun();
    }

    public void setDryRun(Boolean dryRun) {
        sta.setDryRun(dryRun);
    }

    public void importSensorThing() {

        // 저장소 목록
        JSONParser parser = new JSONParser();
        JSONObject stationJson = null;
        stationJson = parseMockFile("sample/airQualityStationForDodam.json");

        // things 의 모든 available 값을 false 처리
        updateThingsStatus();
        List<?> stationList = (List<?>) stationJson.get("list");

        // ObservedProperty init
        Map<String, ObservedProperty> observedPropertyMap = initObservedProperty();
        for (var station : stationList) {

            var json = (JSONObject) station;
            var stationName = json.get("stationName").toString();
            var lon = json.get("lon").toString().trim();
            var lat = json.get("lat").toString().trim();
            var addr = json.get("addr").toString();
            var mangName = "미세먼지 측정기";

            Map<String, Object> thingProperties = new HashMap<>();
            thingProperties.put("stationName", stationName);
            thingProperties.put("sid", json.get("sid"));
            thingProperties.put("available", true);

            var thing = sta.hasThingWithRelationEntities(null, stationName);
            var datastreams = thing != null ? thing.getDatastreams().toList() : null;

            Point point = createPoint(lon, lat);
            Feature feature = createPointFeature(point);

            // Location
            Location locationEntity = initLocation(feature, addr);

            List<Location> locationList = new ArrayList<>();
            List<Entity> entityList = new ArrayList<>();

            if (point != null) {
                entityList.add(locationEntity);
                locationList.add(locationEntity);
            }

            Thing thingEntity = ThingBuilder.builder()
                    .id(thing != null ? thing.getId() : null)
                    .name(stationName)
                    .description(mangName)
                    .properties(thingProperties)
                    .locations(locationList)
                    .build();

            entityList.add(thingEntity);
            entityList.addAll(initDatastreamAndSensor(thingEntity, datastreams, observedPropertyMap, stationName, mangName));
            entityList.add(initFeatureOfInterest(feature, stationName));

            initEntity(entityList, thing != null);
        }
    }

    public void importSensorData() {

        CSVReader reader = null;
        URL resource = this.getClass().getClassLoader().getResource("sample/airQualityDataForDodam.csv");
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(resource.toURI()), StandardCharsets.UTF_8)) {
            reader = new CSVReader(bufferedReader);
        } catch (IOException | URISyntaxException e) {
            log.info("-------- importSensorData. message = {}", e.getMessage());
            LogMessageSupport.printMessage(e);
        }

        String[] contents;
        long count = 0L;

        try {
            // 날짜,시간,센서,PM2.5,PM10,,,
            while (true) {

                assert reader != null;
                if ((contents = reader.readNext()) == null) break; // 2
                count++;

                String sensorDate = contents[0] + " " + contents[1];
                String stationName = contents[2];
                String pm25Value = contents[3];
                String pm10Value = contents[4];

                EntityList<Thing> things = sta.hasThingsByName(null, stationName);

                // 일치하는 thing 이 없을경우 skip
                if (things.size() == 0) {
                    continue;
                }

                JSONObject result = new JSONObject();
                LocalDateTime localDateTime = LocalDateTime.parse(sensorDate, DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm"));
                ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("Asia/Seoul"));

                Thing thing = things.toList().get(0);
                EntityList<Datastream> datastreamList = thing.getDatastreams();
                for (var datastream : datastreamList) {
                    AirQuality airQuality = null;
                    var name = datastream.getName();
                    if (name.equals(AirQualityDatastream.PM10.getName())) {
                        airQuality = AirQuality.PM10;
                        result.put("value", pm10Value);
                    } else if (name.equals(AirQualityDatastream.PM25.getName())) {
                        airQuality = AirQuality.PM25;
                        result.put("value", pm25Value);
                    }  else {
                        continue;
                    }

                    // Calculate AQI
                    double concentration = -1.0;
                    try {
                        concentration = Double.parseDouble(result.get("value").toString());
                    } catch (Exception e) {
                        log.debug(e.getMessage());
                    }

                    int aqi = indexCalculator.getAQI(airQuality, concentration);
                    int grade = indexCalculator.getGrade(airQuality, concentration);
                    result.put("index", Integer.toString(aqi));
                    result.put("grade", Integer.toString(grade));

                    Observation observation = ObservationBuilder.builder()
                            .phenomenonTime(new TimeObject(ZonedDateTime.now()))
                            .resultTime(zonedDateTime)
                            .result(result)
                            .datastream(DatastreamBuilder.builder().id(Id.tryToParse(String.valueOf(datastream.getId()))).build())
                            .featureOfInterest(FeatureOfInterestBuilder.builder().id(Id.tryToParse(String.valueOf(thing.getId()))).build())
                            .build();

                    var observationCount = datastream.getObservations().size();
                    var lastObservation = observationCount > 0 ? datastream.getObservations().toList().get(0) : null;
                    var lastTime = lastObservation != null ? lastObservation.getResultTime().withZoneSameInstant(ZoneId.of("Asia/Seoul")) : null;

                    if (zonedDateTime.equals(lastTime)) {
                        observation.setId(lastObservation.getId());
                        sta.update(observation);
                    } else {
                        sta.create(observation);
                    }
                }
                log.info("-------- importSensorData count = " + count);
            }
        } catch (CsvValidationException | IOException e) {
            log.error("-------- importSensorData. message = {}", e.getMessage());
            LogMessageSupport.printMessage(e);
        }
    }

    private Point createPoint(String lon, String lat) {
        Point point = null;
        if (!"".equals(lon) && !"".equals(lat)) {
            // TODO : 3차원 공간정보
            // FROST-server에서 Geometry를 DB에 저장할 때 2차원으로 저장함.
            // 높이값을 사용하려면 geojson을 geometry로 변환해서 사용해야 함.
            // select ST_AsText(ST_GeomFromGeoJSON("LOCATION"::json->>'geometry')) from "LOCATIONS";
            point = new Point(Double.parseDouble(lon), Double.parseDouble(lat), 0.0);
        }
        return point;
    }

    private Feature createPointFeature(Point point) {
        Feature feature = new Feature();
        feature.setGeometry(point);
        return feature;
    }

}
