package io.openindoormap.service.impl;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.*;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractFeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractLocationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractSensorBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ext.UnitOfMeasurementBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.data.DataGroup;
import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.service.DataGroupService;
import io.openindoormap.service.DataService;
import io.openindoormap.service.OccupancyService;
import io.openindoormap.utils.FileUtils;
import io.openindoormap.utils.SensorThingsUtils;
import lombok.extern.slf4j.Slf4j;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Map.entry;

@Slf4j
@Service("occupancyService")
public class OccupancyServiceImpl implements OccupancyService {

    @Autowired
    private PropertiesConfig propertiesConfig;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataGroupService dataGroupService;

    private long interval = 60;
    private long minFloor = 0;
    private long maxFloor = 0;
    private SensorThingsUtils sta;
    private SensorThingsService service;

    @PostConstruct
    public void postConstruct() {
        sta = new SensorThingsUtils();
        sta.init(propertiesConfig.getSensorThingsApiServer());
        service = sta.getService();
    }

    private void init(String buildId, JSONObject cells) {

        this.minFloor = 0;
        this.maxFloor = 0;

        // ObservedProperty(재실자) 생성
        ObservedProperty observedProperty = sta.createObservedProperty(null, "occupancy", "https://en.wikipedia.org/wiki/Occupancy", "The occupancy of each cell based on the number of people");

        // UnitOfMeasurement 생성
        UnitOfMeasurement unitOfMeasurement = UnitOfMeasurementBuilder.builder()
                .symbol("명")
                .name("Count")
                .definition("https://en.wikipedia.org/wiki/Counting")
                .build();

        Iterator<?> cellIds = cells.keySet().iterator();

        while (cellIds.hasNext()) {
            String cellId = (String) cellIds.next();
            if (cells.get(cellId) instanceof JSONObject) {
                JSONObject cell = (JSONObject) cells.get(cellId);

                long floor = Long.parseLong(cell.get("floor").toString());
                double lon = Double.parseDouble(cell.get("x").toString());
                double lat = Double.parseDouble(cell.get("y").toString());
                double alt = Double.parseDouble(cell.get("z").toString());
                this.minFloor = Math.min(minFloor, floor);
                this.maxFloor = Math.max(maxFloor, floor);

                Feature feature = new Feature();
                GeoJsonObject geometry = new Point(lon, lat, alt);
                feature.setGeometry(geometry);

                String cellName = buildId + ":" + cellId;
                String cellDescription = "Cell " + cellId + " of building " + buildId;

                // Location 생성
                String locationName = cellName;
                String locationDescription = "The location of cell " + cellName;
                Location location = sta.createLocation(null, locationName, locationDescription, AbstractLocationBuilder.ValueCode.GeoJSON.getValue(), feature);

                // Thing 생성(with Location)
                Map<String, Object> properties = new HashMap<>();
                properties.put("floor", floor);
                properties.put("cell", cellId);

                String filter = "name eq '" + cellName + "'";
                Thing thing = sta.createThing(filter, cellName, cellDescription, properties);
                // EntityList<Location> locations = thing.getLocations();
                // locations.add(location);
                sta.updateThingWithLocation(thing, location);

                // Sensor 생성
                Sensor sensor = sta.createSensor(null, "인원 계수 센서", "재실자 파악을 위한 가상 센서", AbstractSensorBuilder.ValueCode.PDF.getValue(), "https://en.wikipedia.org/wiki/Occupancy");

                // Datastream 생성
                String dsName = "Cell " + cellName + " Occupancy";
                String dsDescription = "The occupancy of cell " + cellName;
                String dsObservationType = AbstractDatastreamBuilder.ValueCode.OM_CountObservation.getValue();

                sta.createDatastream(null, dsName, dsDescription, dsObservationType, unitOfMeasurement, thing, sensor, observedProperty);

                // FeatureOfInterest 생성
                String foiName = cellName;
                String foiDescription = "The cell " + cellName + " of interest";
                sta.createFeatureOfInterest(null, foiName, foiDescription, AbstractFeatureOfInterestBuilder.ValueCode.GeoJSON.getValue(), feature);
            }
        }

        // 각층, 빌딩정보 생성
        initStatisticsEntity(buildId);
    }

    @Override
    public void initSensorData(Long dataId) {

        DataInfo dataInfo = new DataInfo();
        dataInfo.setDataId(dataId);
        DataInfo selectedDataInfo = dataService.getData(dataInfo);

        log.info("@@@@@@@ dataInfo = {}", selectedDataInfo);

        String buildId = selectedDataInfo.getDataKey();
        JSONObject cells = getListCell(selectedDataInfo);
        init(buildId, cells);

    }

    /**
     * STA 초기 데이터 생성
     */
    @Override
    public void initSensorData() {
        String buildId = "Alphadom";
        JSONObject cells = getListCell();
        init(buildId, cells);
    }

    private void insert(String buildId, JSONObject cells) {

        this.minFloor = 0;
        this.maxFloor = 0;

        ZonedDateTime nowTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime resultTime = correctTime(nowTime, interval);

        Iterator<?> cellIds = cells.keySet().iterator();

        while (cellIds.hasNext()) {
            String cellId = (String) cellIds.next();
            if (cells.get(cellId) instanceof JSONObject) {
                JSONObject cell = (JSONObject) cells.get(cellId);
                long floor = Long.parseLong(cell.get("floor").toString());
                this.minFloor = Math.min(minFloor, floor);
                this.maxFloor = Math.max(maxFloor, floor);
                String cellName = buildId + ":" + cellId;
                String dsName = "Cell " + cellName + " Occupancy";
                Datastream datastream = sta.hasDatastream(null, dsName);
                log.info("Using Datastream {}.", datastream);

                String foiName = cellName;
                FeatureOfInterest foi = sta.hasFeatureOfInterest(null, foiName);
                log.info("Using FeatureOfInterest {}.", foi);

                generatePeopleObservation(datastream, foi, resultTime, 0, 10);
            }
        }

        // 각층, 빌딩정보 합계
        generateStatisticsObservation(buildId);
    }

    public void insertSensorData(Long dataId) {
        DataInfo dataInfo = new DataInfo();
        dataInfo.setDataId(dataId);
        DataInfo selectedDataInfo = dataService.getData(dataInfo);

        log.info("@@@@@@@ dataInfo = {}", selectedDataInfo);

        String buildId = selectedDataInfo.getDataKey();
        JSONObject cells = getListCell(selectedDataInfo);
        insert(buildId, cells);
    }

    /**
     * STA 관측 데이터 생성
     */
    public void insertSensorData() {
        String buildId = "Alphadom";
        JSONObject cells = getListCell();
        insert(buildId, cells);
    }

    /**
     * 재실자용 CELL 목록 조회
     *
     * @return JSONObject cell 목록
     */
    private JSONObject getListCell() {
        log.info("재실자용 CELL 목록 조회");
        JSONParser parser = new JSONParser();

        JSONObject cellJson = null;
        try {
            cellJson = (JSONObject) parser.parse(new FileReader(
                    this.getClass().getClassLoader().getResource("sample/IndoorGML_cellspacelist.json").getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cellJson;
    }

    /**
     * F4D 재실자용 CELL 목록 조회
     *
     * @return JSONObject cell 목록
     */
    private JSONObject getListCell(DataInfo dataInfo) {

        String dataGroupRootPath = propertiesConfig.getDataServiceDir();

        DataGroup dataGroup = new DataGroup();
        //dataGroup.setUserId(userId);
        dataGroup.setDataGroupId(dataInfo.getDataGroupId());
        dataGroup = dataGroupService.getDataGroup(dataGroup);

        String dataGroupFilePath = FileUtils.getFilePath(dataGroup.getDataGroupPath());
        String fileName = dataInfo.getDataKey() + "_cellspacelist.json";
        log.info("----------- output = {}", dataGroupRootPath + dataGroupFilePath + fileName);

        File cellSpaceListJsonFile = new File(dataGroupRootPath + dataGroupFilePath + fileName);

        JSONParser parser = new JSONParser();
        JSONObject cellJson = null;
        try {
            cellJson = (JSONObject) parser.parse(new FileReader(cellSpaceListJsonFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cellJson;

    }

    /**
     * 층별, 빌딩별 엔티티 생성
     *
     * @param buildId 건물 아이디
     */
    private void initStatisticsEntity(String buildId) {
        Sensor sensor = sta.hasSensor(null, "인원 계수 센서");
        UnitOfMeasurement unitOfMeasurement = UnitOfMeasurementBuilder.builder()
                .symbol("명")
                .name("Count")
                .definition("https://en.wikipedia.org/wiki/Counting")
                .build();
        ObservedProperty observedProperty = sta.hasObservedProperty(null, "occupancy");
        String opDefinition = observedProperty.getDefinition();
        String opDescription = observedProperty.getDescription();
        ObservedProperty observedPropertyBuilding = sta.createObservedProperty(null, "occupancyBuild", opDefinition, opDescription);
        ObservedProperty observedPropertyFloor = sta.createObservedProperty(null, "occupancyFloor", opDefinition, opDescription);
        String dsObservationType = AbstractDatastreamBuilder.ValueCode.OM_CountObservation.getValue();

        for (long i = minFloor; i <= maxFloor; i++) {
            Map<String, Object> properties = Map.of("floor", i);
            String cellDescription = "Floor " + i + " of building " + buildId;
            String filter = "name eq '" + buildId + "' and properties/floor eq " + i;
            Thing thing = sta.createThing(filter, buildId, cellDescription, properties);

            // Datastream 생성
            String dsName = "Floor " + buildId + ":" + i + " Occupancy";
            String dsDescription = "The occupancy of floor " + i;

            sta.createDatastream(null, dsName, dsDescription, dsObservationType, unitOfMeasurement, thing, sensor, observedPropertyFloor);

            // FeatureOfInterest 생성
            String foiName = buildId + ":" + i;
            String foiDescription = "The floor " + buildId + ":" + i + " of interest";
            sta.createFeatureOfInterest(null, foiName, foiDescription, AbstractFeatureOfInterestBuilder.ValueCode.GeoJSON.getValue(), new Feature());
        }

        String cellDescription = "building " + buildId;
        String filter = "Datastreams/ObservedProperties/name eq 'occupancyBuilding'";
        Thing thing = sta.createThing(filter, buildId, cellDescription, null);

        // Datastream 생성
        String dsName = buildId + " Occupancy";
        String dsDescription = "The occupancy of " + buildId;
        sta.createDatastream(null, dsName, dsDescription, dsObservationType, unitOfMeasurement, thing, sensor, observedPropertyBuilding);

        // FeatureOfInterest 생성
        String foiName = buildId;
        String foiDescription = buildId + " of interest";
        sta.createFeatureOfInterest(null, foiName, foiDescription, AbstractFeatureOfInterestBuilder.ValueCode.GeoJSON.getValue(), new Feature());
    }


    /**
     * 주어진 Datastream 에 하나의 Observation 을 생성한다.
     *
     * @param dataStreamId Datastream ID
     * @param foiId        FeatureOfInterest ID
     * @param resultTime   ZonedDateTime 형식의 결과값 산출 시간
     * @param result       해당 Observation 에서 ObservedProperty 에 대한 결과값
     * @throws ServiceFailureException
     */
    public void generateObservation(final Id dataStreamId, final Id foiId, ZonedDateTime resultTime, Object result) throws ServiceFailureException {
        if (dataStreamId == null) return;

        Datastream dataStream = service.datastreams().find(dataStreamId);
        FeatureOfInterest foi;

        if (foiId != null) {
            foi = service.featuresOfInterest().find(foiId);
        } else {
            foi = new FeatureOfInterest();
            log.info("Creating FeatureOfInterest {}.", foi);
        }

        ZonedDateTime zonedDateTime = resultTime;
        if (zonedDateTime == null) {
            zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        }

        sta.createObservation(result, resultTime, resultTime, 0, dataStream, foi);
    }


    /**
     * 주어진 Datastream 에 인원 계수에 대한 하나의 Observation 을 생성한다.
     *
     * @param dataStream        Datastream
     * @param featureOfInterest FeatureOfInterest
     * @param min               최소 인원
     * @param max               최대 인원
     */
    public void generatePeopleObservation(Datastream dataStream, FeatureOfInterest featureOfInterest, ZonedDateTime resultTime, int min, int max) {
        int result = ThreadLocalRandom.current().nextInt(min, max + 1);
        int grade = getGrade(result);
        Map<String, Object> resultMap = Map.ofEntries(
                entry("grade", grade),
                entry("value", result)
        );
        // Id dataStreamId = dataStream.getId();
        // Id foiId = featureOfInterest.getId();
        // generateObservation(dataStreamId, foiId, resultTime, new BigDecimal(result));

        sta.createObservation(resultMap, resultTime, resultTime, 0, dataStream, featureOfInterest);
    }

    private void generateStatisticsObservation(String buildId) {
        int buildSum = 0;
        ZonedDateTime resultTime = null;
        for (long i = minFloor; i <= maxFloor; i++) {
            int floorSum = 0;
            String thingFilter = "startswith(name, '" + buildId + "') and properties/floor eq " + i + " and Datastreams/ObservedProperties/name eq 'occupancy'";
            String datastreamName = "Floor " + buildId + ":" + i + " Occupancy";
            String featureOfInterestName = buildId + ":" + i;
            EntityList<Thing> things = sta.hasThingsWithObservation(thingFilter, null);
            Datastream datastream = sta.hasDatastream(null, datastreamName);
            FeatureOfInterest featureOfInterest = sta.hasFeatureOfInterest(null, featureOfInterestName);

            if (things.size() > 0) {
                resultTime = things.toList().get(0).getDatastreams().toList().get(0).getObservations().toList().get(0).getResultTime();
                floorSum = things.stream()
                        .mapToInt(f -> {
                            Map<String, Object> map  = (Map<String, Object>) f.getDatastreams().toList().get(0).getObservations().toList().get(0).getResult();
                            return Integer.parseInt(map.get("value").toString());
                        })
                        .sum();

                buildSum += floorSum;
            }
            Map<String, Object> resultMap = Map.ofEntries(
                    entry("grade", getGrade(floorSum)),
                    entry("value", floorSum)
            );
            sta.createObservation(resultMap, resultTime, resultTime, 0, datastream, featureOfInterest);
        }
        String datastreamFilter = "startswith(name, '" + buildId + "')";
        Datastream datastream = sta.hasDatastream(datastreamFilter, null);
        FeatureOfInterest featureOfInterest = sta.hasFeatureOfInterest(null, buildId);
        Map<String, Object> resultMap = Map.ofEntries(
                entry("grade", getGrade(buildSum)),
                entry("value", buildSum)
        );
        sta.createObservation(resultMap, resultTime, resultTime, 0, datastream, featureOfInterest);

    }

    /**
     * 주어진 날짜/시간에서 interval 만큼의 시간을 보정하는 함수
     *
     * @param dateTime A date-time with a time-zone in the ISO-8601 calendar system
     * @param interval unit(second)
     * @return
     */
    public ZonedDateTime correctTime(ZonedDateTime dateTime, long interval) {
        long time = dateTime.getMinute() * 60 + dateTime.getSecond();
        long diff = time - time / interval * interval;

        return dateTime.minusSeconds(diff);
    }

    public Boolean getDryRun() {
        return sta.getDryRun();
    }

    public void setDryRun(Boolean dryRun) {
        sta.setDryRun(dryRun);
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    private int getGrade(int value) {
        int grade = 0;
        if (value >= 0 && value <= 2) {
            grade = 1;
        } else if (value >= 3 && value <= 5) {
            grade = 2;
        } else if (value >= 6 && value <= 8) {
            grade = 3;
        } else if (value >= 9) {
            grade = 4;
        } else {
            // cell 이 아닌 각층과 빌딩은 임시로 랜덤한 등급
            grade = ThreadLocalRandom.current().nextInt(1, 5);
        }

        return grade;
    }
}
