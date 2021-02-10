package io.openindoormap.service.impl;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.*;
import de.fraunhofer.iosb.ilt.sta.model.builder.*;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractFeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractLocationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractSensorBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.sensor.AirQualityDatastream;
import io.openindoormap.domain.sensor.AirQualityObservedProperty;
import io.openindoormap.domain.sensor.TimeType;
import io.openindoormap.service.AirQualityService;
import io.openindoormap.support.LogMessageSupport;
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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.annotation.PostConstruct;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private String airkoreaApiServiceUrl;
    private String airkoreaAuthKey;

    @PostConstruct
    public void postConstruct() {
        sta = new SensorThingsUtils();
        sta.init(propertiesConfig.getSensorThingsApiServer());
        // 인덱스 계산
        indexCalculator = new IndexCalculator(ConcentrationsGenerator.createAQIConcentrations());
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
            String dmX = json.get("dmX").toString().trim();
            String dmY = json.get("dmY").toString().trim();
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
            Point point = null;
            if (!"".equals(dmX) && !"".equals(dmY)) {
                point = new Point(Double.parseDouble(dmY), Double.parseDouble(dmX));
            }
            Feature feature = new Feature();
            feature.setGeometry(point);
            List<Location> locationList = new ArrayList<>();
            List<Entity> entityList = new ArrayList<>();
            // Location
            Location locationEntity = initLocation(feature, addr);

            if (point != null) {
                entityList.add(locationEntity);
                locationList.add(locationEntity);
            }

            Thing thingEntity = ThingBuilder.builder().id(thing != null ? thing.getId() : null).name(stationName)
                    .description("한국환경공단 측정소").properties(thingProperties).locations(locationList).build();

            entityList.add(thingEntity);
            entityList.addAll(
                    initDatastreamAndSensor(thingEntity, datastreams, observedPropertyMap, stationName, mangName));
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

        for (var station : stationList) {
            var jsonObject = (JSONObject) station;
            var stationName = (String) jsonObject.get("stationName");
            JSONObject json = new JSONObject();
            // JSONObject result = getHourValue(stationName);

            JSONObject result = null;
            for (var observation : observationList) {
                if(((JSONObject)observation).get("stationName").toString().equals(stationName)) {
                    result = (JSONObject)observation;
                    break;
                }
            }

            if(result == null || result.get("dataTime") == null) {
                continue;
            }
            
            LocalDateTime t = LocalDateTime.parse((String) result.get("dataTime"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            ZonedDateTime zonedDateTime = ZonedDateTime.of(t.getYear(), t.getMonthValue(), t.getDayOfMonth(),
                    t.getHour(), 0, 0, 0, ZoneId.of("Asia/Seoul"));
            EntityList<Thing> things = sta.hasThingsWithObservation(null, stationName);
            // 일치하는 thing 이 없을경우 skip
            if (things.size() == 0)
                continue;

            Thing thing = things.toList().get(0);
            EntityList<Datastream> datastreamList = thing.getDatastreams();
            for (Datastream datastream : datastreamList) {
                double scaleFactor = 1.0;
                AirQuality airQuality = null;
                String name = datastream.getName();
                if (name.equals(AirQualityDatastream.PM10.getName())) {
                    scaleFactor = 1.0;
                    airQuality = AirQuality.PM10;
                    json.put("value", result.get("pm10Flag") == null ? result.get("pm10Value") : null);
                    // json.put("grade", result.get("pm10Grade"));
                } else if (name.equals(AirQualityDatastream.PM25.getName())) {
                    scaleFactor = 1.0;
                    airQuality = AirQuality.PM25;
                    json.put("value", result.get("pm25Flag") == null ? result.get("pm25Value") : null);
                    // json.put("grade", result.get("pm25Grade"));
                } else if (name.equals(AirQualityDatastream.SO2.getName())) {
                    scaleFactor = 1000.0;
                    airQuality = AirQuality.SO2;
                    json.put("value", result.get("so2Flag") == null ? result.get("so2Value") : null);
                    // json.put("grade", result.get("so2Grade"));
                } else if (name.equals(AirQualityDatastream.CO.getName())) {
                    scaleFactor = 1.0;
                    airQuality = AirQuality.CO;
                    json.put("value", result.get("coFlag") == null ? result.get("coValue") : null);
                    // json.put("grade", result.get("coGrade"));
                } else if (name.equals(AirQualityDatastream.O3.getName())) {
                    scaleFactor = 1000.0;
                    airQuality = AirQuality.O3;
                    json.put("value", result.get("o3Flag") == null ? result.get("o3Value") : null);
                    // json.put("grade", result.get("o3Grade"));
                } else if (name.equals(AirQualityDatastream.NO2.getName())) {
                    scaleFactor = 1000.0;
                    airQuality = AirQuality.NO2;
                    json.put("value", result.get("no2Flag") == null ? result.get("no2Value") : null);
                    // json.put("grade", result.get("no2Grade"));
                } else {
                    // 24시간 datastream 에는 여기서 값을 넣지 않는다.
                    continue;
                }

                // Calculate AQI
                if(airQuality != null) {
                    double concentration = -1.0;
                    if(json.get("value") != null) {
                        try {
                            concentration = Double.parseDouble(json.get("value").toString());
                        } catch (Exception e) {
                            concentration = -1.0;
                            log.debug(e.getMessage());
                        }
                    }

                    int aqi = indexCalculator.getAQI(airQuality, concentration * scaleFactor);
                    json.put("index", Integer.toString(aqi));
                }

                Observation observation = ObservationBuilder.builder()
                        .phenomenonTime(new TimeObject(ZonedDateTime.now())).resultTime(zonedDateTime).result(json)
                        .datastream(DatastreamBuilder.builder().id(Id.tryToParse(String.valueOf(datastream.getId())))
                                .build())
                        .featureOfInterest(FeatureOfInterestBuilder.builder()
                                .id(Id.tryToParse(String.valueOf(thing.getId()))).build())
                        .build();

                int observationCount = datastream.getObservations().size();
                var lastObservation = observationCount > 0 ? datastream.getObservations().toList().get(0) : null;
                var lastTime = lastObservation != null
                        ? lastObservation.getResultTime().withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                        : null;

                if (zonedDateTime.equals(lastTime)) {
                    observation.setId(lastObservation.getId());
                    sta.update(observation);
                } else {
                    sta.create(observation);
                }
            }
        }
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
        ZonedDateTime start = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 0, 0, 0, 0,
                now.getZone());
        ZonedDateTime end = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 23, 0, 0, 0,
                now.getZone());
        List<AirQualityObservedProperty> typeList = AirQualityObservedProperty.getObservedPropertyByType(TimeType.HOUR);
        for (AirQualityObservedProperty type : typeList) {
            String observationFilter = "resultTime ge " + start.toInstant() + " and resultTime le " + end.toInstant()
                    + " and Datastreams/Things/name eq '" + stationName
                    + "' and Datastreams/ObservedProperties/name eq '" + type.getName() + "'";
            EntityList<Observation> observations = sta.hasObservations(observationFilter, null);
            if (observations.size() == 0) {
                continue;
            }
            JSONObject json = getObservationAverage(observations, type);
            String timeName = TimeType.DAILY.getValue();
            String dailyName = type.getName() + timeName.charAt(0) + timeName.toLowerCase().substring(1);
            String datastreamFilter = "Thing/name eq '" + stationName + "' and Datastream/ObservedProperty/name eq '"
                    + dailyName + "'";
            Datastream datastream = sta.hasDatastream(datastreamFilter, null);
            if (datastream == null) {
                continue;
            }
            ZonedDateTime insertTime = ZonedDateTime.now();

            Observation observation = ObservationBuilder.builder().phenomenonTime(new TimeObject(insertTime))
                    .resultTime(start).result(json).datastream(datastream)
                    .featureOfInterest(
                            FeatureOfInterestBuilder.builder().id(Id.tryToParse(String.valueOf(thing.getId()))).build())
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
            var observedProperty = sta.createObservedProperty(null, entity.getName(), entity.getDescription(),
                    entity.getDefinition());
            map.put(entity.getName(), observedProperty);
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

        return LocationBuilder.builder().id(location != null ? location.getId() : null).name(address)
                .encodingType(AbstractLocationBuilder.ValueCode.GeoJSON).description("대기질 측정소 위치").location(feature)
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
    private List<Entity> initDatastreamAndSensor(Thing thing, List<Datastream> datastreams,
            Map<String, ObservedProperty> observedPropertyMap, String stationName, String mangName) {
        List<Entity> entityList = new ArrayList<>();
        int i = 0;
        if (datastreams != null) {
            // datastreams sort
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
                e.printStackTrace();
            }
            Sensor sensorEntity = SensorBuilder.builder().id(sensor != null ? sensor.getId() : null)
                    .name(stationName + ":" + datastreamType.getName()).description("미세먼지 측정소")
                    .encodingType(AbstractSensorBuilder.ValueCode.SensorML).metadata(mangName).build();
            entityList.add(sensorEntity);

            Datastream datastreamEntity = DatastreamBuilder.builder().id(datastream != null ? datastream.getId() : null)
                    .name(datastreamType.getName()).description(datastreamType.getName())
                    .observationType(AbstractDatastreamBuilder.ValueCode.OM_Observation)
                    .unitOfMeasurement(datastreamType.getUnitOfMeasurement()).sensor(sensorEntity)
                    .observedProperty(observedPropertyMap.get(entity.getName())).thing(thing).build();
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

        return FeatureOfInterestBuilder.builder().id(foi != null ? foi.getId() : null).name(foiName)
                .description("한국환경공단 대기질 측정소").encodingType(AbstractFeatureOfInterestBuilder.ValueCode.GeoJSON)
                .feature(feature).build();
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
            LogMessageSupport.printMessage(e, "-------- AirQualityService create & update Error = {}", e.getMessage());
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
        JSONParser parser = new JSONParser();
        JSONObject stationJson = null;
        // 테스트
        if (mockEnable) {
            log.info("mock 미세먼지 저장소 목록");
            try {
                stationJson = (JSONObject) parser.parse(new FileReader(
                        this.getClass().getClassLoader().getResource("sample/airQualityStation.json").getFile()));
            } catch (IOException | ParseException e) {
                LogMessageSupport.printMessage(e, "-------- AirQualityService getListStation = {}", e.getMessage());
            }
        } else {
            // 운영시 api 연동
            log.info("api 연동 [한국환경공단_에어코리아_측정소정보]");
            String url = airkoreaApiServiceUrl + "/MsrstnInfoInqireSvc/getMsrstnList";
            UriComponents builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("ServiceKey", UriUtils.encode(airkoreaAuthKey, "UTF-8"))
                    .queryParam("numOfRows", 10000)
                    .queryParam("pageNo", 1)
                    .queryParam("returnType", "json")
                    .build(false);    //자동으로 encode해주는 것을 막기 위해 false

            stationJson = getAPIResult(builder.toString());
        }

        return stationJson;
    }

    /**
     * 측정소에 해당하는 미세먼지 데이터 조회
     *
     * @param sidoName 시도명
     * @return JSONObejct
     */
    private JSONObject getHourValue(String sidoName) {
        boolean mockEnable = propertiesConfig.isMockEnable();
        JSONObject json = new JSONObject();
        // 테스트
        if (mockEnable) {
            Random random = new Random();
            // 미세먼지 pm10
            int pm10Value = random.nextInt(601);
            int pm10Grade = getGrade(pm10Value, AirQualityObservedProperty.PM10);
            // 미세먼지 pm2.5
            int pm25Value = random.nextInt(501);
            int pm25Grade = getGrade(pm25Value, AirQualityObservedProperty.PM25);
            // 아황산가스 농도
            double so2Value = NumberUtils.round(5, random.nextFloat());
            int so2Grade = getGrade(so2Value, AirQualityObservedProperty.SO2);
            // 일산화탄소 농도
            double coValue = NumberUtils.round(5, random.nextFloat() * 50);
            int coGrade = getGrade(coValue, AirQualityObservedProperty.CO);
            // 오존 농도
            double o3Value = NumberUtils.round(5, random.nextFloat() * 0.6);
            int o3Grade = getGrade(o3Value, AirQualityObservedProperty.O3);
            // 이산화질소 농도
            double no2Value = NumberUtils.round(5, random.nextFloat() * 2);
            int no2Grade = getGrade(no2Value, AirQualityObservedProperty.NO2);

            json.put("pm10Value", pm10Value);
            json.put("pm25Value", pm25Value);
            json.put("so2Value", so2Value);
            json.put("coValue", coValue);
            json.put("o3Value", o3Value);
            json.put("no2Value", no2Value);
            json.put("pm10Grade", pm10Grade);
            json.put("pm25Grade", pm25Grade);
            json.put("so2Grade", so2Grade);
            json.put("coGrade", coGrade);
            json.put("o3Grade", o3Grade);
            json.put("no2Grade", no2Grade);
            json.put("dataTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00")));
        } else {
            // 운영시 api 연동
            log.info("api 연동 [한국환경공단_에어코리아_대기오염정보]");
            String url = airkoreaApiServiceUrl + "/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty";
            UriComponents builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("ServiceKey", UriUtils.encode(airkoreaAuthKey, "UTF-8"))
                    .queryParam("numOfRows", 10000)
                    .queryParam("pageNo", 1)
                    .queryParam("sidoName", UriUtils.encode(sidoName, "UTF-8"))
                    .queryParam("ver", 1.3)
                    .queryParam("returnType", "json")
                    .build(false);    //자동으로 encode해주는 것을 막기 위해 false

            json = getAPIResult(builder.toString());
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
        // TODO ServiceKey 는 발급받은 키로 해야함. 개발용 api key 는 하루 request 500건으로 제한
        RestTemplate restTemplate = new RestTemplate();
        JSONObject json = new JSONObject();
        JSONParser parser = new JSONParser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<?> response = restTemplate.exchange(new URI(requestURI), HttpMethod.GET, entity, String.class);
            JSONObject apiResultJson = (JSONObject) (((JSONObject) parser.parse(response.getBody().toString())).get("response"));
            JSONObject apiResultHeader = (JSONObject) apiResultJson.get("header");
            JSONObject apiResultBody = (JSONObject) apiResultJson.get("body");
            if(!apiResultHeader.get("resultCode").toString().equals("00"))
            {
                return null;
            }
            List<?> resultList = (List<?>) apiResultBody.get("items");
            if (resultList.size() <= 0) {
                return null;  
            } 
            json = apiResultBody;
            log.debug("-------- statusCode = {}, body = {}", response.getStatusCodeValue(), response.getBody());
        } catch (URISyntaxException | ParseException e) {
            LogMessageSupport.printMessage(e, "-------- AirQualityService getAirQualityData = {}", e.getMessage());
        }
        return json;
    }

    /**
     * 에어코리아 기준에 해당하는 grade return
     *
     * @param value 측정데이터 값
     * @param type  측정데이터 타입
     * @return String
     */
    private int getGrade(int value, AirQualityObservedProperty type) {
        int grade = 0;
        if (AirQualityObservedProperty.PM10 == type) {
            if (value >= 0 && value <= 30) {
                grade = 1;
            } else if (value >= 31 & value <= 80) {
                grade = 2;
            } else if (value >= 81 && value <= 150) {
                grade = 3;
            } else if (value >= 151 && value <= 600) {
                grade = 4;
            }
        } else if (AirQualityObservedProperty.PM25 == type) {
            if (value >= 0 && value <= 15) {
                grade = 1;
            } else if (value >= 16 && value <= 35) {
                grade = 2;
            } else if (value >= 36 && value <= 75) {
                grade = 3;
            } else if (value >= 76 && value <= 500) {
                grade = 4;
            }
        }

        return grade;
    }

    private int getGrade(double value, AirQualityObservedProperty type) {
        int grade = 0;
        if (AirQualityObservedProperty.SO2 == type) {
            if (value >= 0 && value <= 0.02) {
                grade = 1;
            } else if (value >= 0.021 && value <= 0.05) {
                grade = 2;
            } else if (value >= 0.051 && value <= 0.15) {
                grade = 3;
            } else if (value >= 0.151 && value <= 1) {
                grade = 4;
            }
        } else if (AirQualityObservedProperty.CO == type) {
            if (0 >= value && value <= 2) {
                grade = 1;
            } else if (value >= 2.01 && value <= 9) {
                grade = 2;
            } else if (value >= 9.01 && value <= 15) {
                grade = 3;
            } else if (value >= 15.01 && value <= 50) {
                grade = 4;
            }
        } else if (AirQualityObservedProperty.O3 == type) {
            if (0 >= value && value <= 0.03) {
                grade = 1;
            } else if (value >= 0.031 && value <= 0.09) {
                grade = 2;
            } else if (value >= 0.091 && value <= 0.15) {
                grade = 3;
            } else if (value >= 0.151 && value <= 0.6) {
                grade = 4;
            }
        } else if (AirQualityObservedProperty.NO2 == type) {
            if (value >= 0 && value <= 0.03) {
                grade = 1;
            } else if (value >= 0.031 && value <= 0.06) {
                grade = 2;
            } else if (value >= 0.061 && value <= 0.2) {
                grade = 3;
            } else if (value >= 0.201 && value <= 2) {
                grade = 4;
            }
        }

        return grade;
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
        try {
            stationJson = (JSONObject) parser.parse(new FileReader(
                    this.getClass().getClassLoader().getResource("sample/airQualityStationForDodam.json").getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

            Map<String, Object> thingProperties = new HashMap<>();
            thingProperties.put("stationName", stationName);
            thingProperties.put("sid", json.get("sid"));
            thingProperties.put("available", true);

            var thing = sta.hasThingWithRelationEntities(null, stationName);
            var datastreams = thing != null ? thing.getDatastreams().toList() : null;
            Point point = null;
            if (!"".equals(lon) && !"".equals(lat)) {
                point = new Point(Double.parseDouble(lon), Double.parseDouble(lat));
            }
            Feature feature = new Feature();
            feature.setGeometry(point);
            List<Location> locationList = new ArrayList<>();
            List<Entity> entityList = new ArrayList<>();
            // Location
            Location locationEntity = initLocation(feature, addr);

            if (point != null) {
                entityList.add(locationEntity);
                locationList.add(locationEntity);
            }

            Thing thingEntity = ThingBuilder.builder().id(thing != null ? thing.getId() : null).name(stationName)
                    .description("미세먼지 측정기").properties(thingProperties).locations(locationList).build();

            entityList.add(thingEntity);
            entityList.addAll(
                    initDatastreamAndSensor(thingEntity, datastreams, observedPropertyMap, stationName, "미세먼지 측정기"));
            entityList.add(initFeatureOfInterest(feature, stationName));

            initEntity(entityList, thing != null);
        }
    }

    public void importSensorData() {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(
                    this.getClass().getClassLoader().getResource("sample/airQualityDataForDodam.csv").getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String[] contents;
        long count = 0L;

        try {
            // 날짜,시간,센서,PM2.5,PM10,,,
            while ((contents = reader.readNext()) != null) { // 2
                count++;

                String sensorDate = contents[0] + " " + contents[1];
                String stationName = contents[2];
                String pm25Value = contents[3];
                String pm10Value = contents[4];

                EntityList<Thing> things = sta.hasThingsWithObservation(null, stationName);
                // 일치하는 thing 이 없을경우 skip
                if (things.size() == 0)
                {
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
                    if(airQuality != null) {
                        double concentration = -1.0;
                        try {
                            concentration = Double.parseDouble(result.get("value").toString());
                        } catch (Exception e) {
                            concentration = -1.0;
                            log.debug(e.getMessage());
                        }
    
                        int aqi = indexCalculator.getAQI(airQuality, concentration);
                        result.put("index", Integer.toString(aqi));
                    }

                    Observation observation = ObservationBuilder.builder()
                            .phenomenonTime(new TimeObject(ZonedDateTime.now())).resultTime(zonedDateTime).result(result)
                            .datastream(DatastreamBuilder.builder().id(Id.tryToParse(String.valueOf(datastream.getId())))
                                    .build())
                            .featureOfInterest(FeatureOfInterestBuilder.builder()
                                    .id(Id.tryToParse(String.valueOf(thing.getId()))).build())
                            .build();
    
                    var observationCount = datastream.getObservations().size();
                    var lastObservation = observationCount > 0 ? datastream.getObservations().toList().get(0) : null;
                    var lastTime = lastObservation != null
                            ? lastObservation.getResultTime().withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                            : null;
    
                    if (zonedDateTime.equals(lastTime)) {
                        observation.setId(lastObservation.getId());
                        sta.update(observation);
                    } else {
                        sta.create(observation);
                    }
                }
                log.debug("[ImportSensorData] count = " + count);
            }
        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
        }
    }
}
