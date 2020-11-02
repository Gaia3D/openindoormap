package io.openindoormap.service.impl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractFeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractLocationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractSensorBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ext.UnitOfMeasurementBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.service.OccupancyService;
import io.openindoormap.utils.SensorThingsUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("occupancyService")
public class OccupancyServiceImpl implements OccupancyService {

    @Autowired
    private PropertiesConfig propertiesConfig;

    private long interval = 60;
    private SensorThingsUtils sta;
    private SensorThingsService service;

    @PostConstruct
    public void postConstruct() {
        sta = new SensorThingsUtils();
        sta.init(propertiesConfig.getSensorThingsApiServer());
        service = sta.getService();
    }
    
    /**
     * STA 초기 데이터 생성
     */
    @Override
    public void initSensorData() {
        // ObservedProperty(재실자) 생성
        ObservedProperty observedProperty = sta.createObservedProperty(null, "occupancy", "https://en.wikipedia.org/wiki/Occupancy", "The occupancy of each cell based on the number of people");

        // UnitOfMeasurement 생성
        UnitOfMeasurement unitOfMeasurement = UnitOfMeasurementBuilder.builder()
            .symbol("명")
            .name("Count")
            .definition("https://en.wikipedia.org/wiki/Counting")
            .build();

        String buildId = "Alphadom";
        JSONObject cells = getListCell();
        Iterator<?> cellIds = cells.keySet().iterator();

        while(cellIds.hasNext() ) {
            String cellId = (String)cellIds.next();
            if ( cells.get(cellId) instanceof JSONObject ) {
                JSONObject cell = (JSONObject)cells.get(cellId);

                long floor = Long.parseLong(cell.get("floor").toString());
                double lon = Double.parseDouble(cell.get("x").toString());
                double lat = Double.parseDouble(cell.get("y").toString());
                double alt = Double.parseDouble(cell.get("z").toString());

                Feature feature = new Feature();
                GeoJsonObject geometry = new Point(lon, lat, alt);
                feature.setGeometry(geometry);

                String cellName = buildId + ":" + cellId;
                String cellDescription = "Cell " + cellId + " of building " + buildId;

                // Location 생성
                String locationName = cellName;
                String locationDescription = "The location of cell "+ cellName;
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
                String foiDescription = "The cell "+ cellName + " of interest";
                sta.createFeatureOfInterest(null, foiName, foiDescription, AbstractFeatureOfInterestBuilder.ValueCode.GeoJSON.getValue(), feature);
            }
        }
    }

    /**
     * STA 관측 데이터 생성
     */
    public void insertSensorData() {
        ZonedDateTime nowTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime resultTime = correctTime(nowTime, interval);

        String buildId = "Alphadom";
        JSONObject cells = getListCell();
        Iterator<?> cellIds = cells.keySet().iterator();

        while(cellIds.hasNext() ) {
            String cellId = (String)cellIds.next();
            if ( cells.get(cellId) instanceof JSONObject ) {
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
    }

    /**
     * 재실자용 CELL 목록 조회
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
     * 주어진 Datastream 에 하나의 Observation 을 생성한다.
     * 
     * @param dataStreamId Datastream ID
     * @param foiId FeatureOfInterest ID
     * @param resultTime ZonedDateTime 형식의 결과값 산출 시간
     * @param result 해당 Observation 에서 ObservedProperty 에 대한 결과값
     * @throws ServiceFailureException
     */
    public void generateObservation(final Id dataStreamId, final Id foiId, ZonedDateTime resultTime, Object result) throws ServiceFailureException {
        if(dataStreamId == null)    return;

        Datastream dataStream = service.datastreams().find(dataStreamId);
        FeatureOfInterest foi;

        if(foiId != null) {
            foi = service.featuresOfInterest().find(foiId);
        }
        else {
            foi = new FeatureOfInterest();
            log.info("Creating FeatureOfInterest {}.", foi);
        }

        ZonedDateTime zonedDateTime = resultTime;
        if(zonedDateTime == null) {
            zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        }

        sta.createObservation(result, resultTime, resultTime, 0, dataStream, foi);
    }

    /**
     * 주어진 Datastream 에 인원 계수에 대한 하나의 Observation 을 생성한다. 
     * 
     * @param dataStreamId Datastream ID
     * @param foiId FeatureOfInterest ID
     * @param min 최소 인원
     * @param max 최대 인원
     */
    public void generatePeopleObservation(Datastream dataStream, FeatureOfInterest featureOfInterest, ZonedDateTime resultTime, int min, int max) {
        int result = ThreadLocalRandom.current().nextInt(min, max + 1);
        // Id dataStreamId = dataStream.getId();
        // Id foiId = featureOfInterest.getId();
        // generateObservation(dataStreamId, foiId, resultTime, new BigDecimal(result));

        sta.createObservation(result, resultTime, resultTime, 0, dataStream, featureOfInterest);
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
}
