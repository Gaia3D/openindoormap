package io.openindoormap.service.impl;

import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractFeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ext.UnitOfMeasurementBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import io.openindoormap.OIMAdminApplication;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.service.OccupancyService;
import io.openindoormap.utils.SensorThingsUtils;
import lombok.extern.slf4j.Slf4j;
import org.geojson.Feature;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@SpringBootTest(classes = OIMAdminApplication.class)
public class OccupancyServiceTests {

    @Qualifier("occupancyService")

    @Autowired
    private OccupancyService sensorService;
    @Autowired
    private PropertiesConfig propertiesConfig;

    @Ignore
    public void testInitSensorData() {
        sensorService.setDryRun(false);
        sensorService.initSensorData();
    }

    @Ignore
    public void testInsertSensorData() {
        sensorService.setDryRun(false);
        sensorService.insertSensorData();
    }

    @Ignore
    public void testInitSensorDataByDataId() {
        Long dataId = 200002L;  // UOS21C-indoorgml(시립대)
        //Long dataId = 200003L;  // Alphadom(알파돔)
        sensorService.setDryRun(false);
        sensorService.initSensorData(dataId);
    }

    @Ignore
    public void testInsertSensorDataByDataId() {
        Long dataId = 200002L;  // UOS21C-indoorgml(시립대)
        //Long dataId = 200003L;  // Alphadom(알파돔)
        sensorService.setDryRun(false);
        sensorService.insertSensorData(dataId);
    }

    @Ignore
    public void 최소_최대_층정보_구하기() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject cells = (JSONObject) parser.parse(new FileReader(this.getClass().getClassLoader().getResource("sample/IndoorGML_cellspacelist.json").getFile()));
        Iterator<?> cellIds = cells.keySet().iterator();
        long max = 0;
        long min = 0;
        while (cellIds.hasNext()) {
            String cellId = (String) cellIds.next();
            if (cells.get(cellId) instanceof JSONObject) {
                JSONObject cell = (JSONObject) cells.get(cellId);
                long floor = Long.parseLong(cell.get("floor").toString());
                max = Math.max(max, floor);
                min = Math.min(min, floor);
            }
        }
        log.info("min value ========= {}, max value ================ {} ", min, max);
    }

    @Ignore
    public void 층별_빌딩별_데이터_생성() throws Exception {
        SensorThingsUtils sta = new SensorThingsUtils();
        sta.init(propertiesConfig.getSensorThingsApiServer());
        sta.setDryRun(false);
        int min = 0;
        int max = 21;
        // ObservedProperty(재실자) 생성
        String opDefinition = "https://en.wikipedia.org/wiki/Occupancy";
        String opDescription = "The occupancy of each cell based on the number of people";
        String buildId = "Alphadom";
        ObservedProperty observedPropertyBuilding = sta.createObservedProperty(null, "occupancyBuilding", opDefinition, opDescription);
        ObservedProperty observedPropertyFloor = sta.createObservedProperty(null, "occupancyFloor", opDefinition, opDescription);
        Sensor sensor = sta.hasSensor(null, "인원 계수 센서");
        UnitOfMeasurement unitOfMeasurement = UnitOfMeasurementBuilder.builder()
                .symbol("명")
                .name("Count")
                .definition("https://en.wikipedia.org/wiki/Counting")
                .build();

        for (long i = min; i <= max; i++) {
            Map<String, Object> properties = Map.of("floor", i);
            String cellDescription = "Floor " + i + " of building " + buildId;
            String filter = "name eq '" + buildId + "' and properties/floor eq " + i;
            Thing thing = sta.createThing(filter, buildId, cellDescription, properties);

            // Datastream 생성
            String dsName = "Floor " + i +" "+ buildId + " Occupancy";
            String dsDescription = "The occupancy of floor " + i;
            String dsObservationType = AbstractDatastreamBuilder.ValueCode.OM_CountObservation.getValue();

            sta.createDatastream(null, dsName, dsDescription, dsObservationType, unitOfMeasurement, thing, sensor, observedPropertyFloor);

            // FeatureOfInterest 생성
            String foiName = buildId+":"+i;
            String foiDescription = "The floor " + i + buildId + " of interest";
            sta.createFeatureOfInterest(null, foiName, foiDescription, AbstractFeatureOfInterestBuilder.ValueCode.GeoJSON.getValue(), new Feature());
        }
    }

    @Ignore
    void 층별_합계() {
//        http://localhost:8888/FROST-Server/v1.0/Things?$filter=properties/floor%20eq%201&$count=true
//        http://localhost:8888/FROST-Server/v1.0/Things?$filter=properties/floor eq 0 and Datastreams/ObservedProperties/name eq 'occupancy'&$expand=Datastreams/Observations($orderby=id desc)
        SensorThingsUtils sta = new SensorThingsUtils();
        sta.init(propertiesConfig.getSensorThingsApiServer());
        sta.setDryRun(false);

//        EntityList<Thing> things = service.things()
//                .query()
//                .filter("startswith(name, 'Alphadom') and properties/floor eq 0 and Datastreams/ObservedProperties/name eq 'occupancy'")
//                .expand("Datastreams/Observations($orderby=id desc)")
//                .list();

        String filter = "startswith(name, 'Alphadom') and properties/floor eq 0 and Datastreams/ObservedProperties/name eq 'occupancy'";
        String expand = "Datastreams/Observations($orderby=id desc)";
        EntityList<Thing> things = sta.hasThingsWithObservation(filter, null);

        long count = things.stream()
                .mapToLong(f-> Long.parseLong(f.getDatastreams().toList().get(0).getObservations().toList().get(0).getResult().toString()))
                .sum();


        log.info("thing==================== {} ", count);
    }

}
