package io.openindoormap.service.impl;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.*;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import io.openindoormap.OIMAdminApplication;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.OrderBy;
import io.openindoormap.domain.sensor.AirQualityObservedProperty;
import io.openindoormap.service.AirQualityService;
import io.openindoormap.utils.SensorThingsUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OIMAdminApplication.class)
class AirQualityServiceImplTest {

    @Qualifier("airQualityService")
    @Autowired
    private AirQualityService sensorService;
    @Autowired
    private PropertiesConfig propertiesConfig;

    private SensorThingsService sensorThingsService;

    @BeforeAll
    void setup() throws MalformedURLException {
        sensorThingsService = new SensorThingsService(new URL(propertiesConfig.getSensorThingsApiServer()));
    }

    @Test
    void 측정소_데이터_넣기() {
        sensorService.initSensorData();
    }

    @Test
    void 미세먼지_데이터_넣기() {
        sensorService.insertSensorData();
    }

    @Test
    void 측정소별_데이터스트림() throws ServiceFailureException {
        //http://localhost:8888/FROST-Server/v1.0/Things?$filter= name eq '반송로'&$expand=Datastreams
        EntityList<Thing> things = sensorThingsService.things()
                .query()
                .filter("name eq '반송로'")
                .expand("Datastreams($orderby=id desc)")
                .list();

        EntityList<Datastream> datastreams = things.toList().get(0).getDatastreams();
        for (var datastream : datastreams) {
            log.info("datastream ================== {} ", datastream);
        }
    }

    @Test
    void getThingId() throws ServiceFailureException {
        long idCount = 0;
        EntityList<Thing> things = sensorThingsService.things()
                .query()
                .orderBy("id " + OrderBy.DESC.getValue())
                .top(1)
                .list();

        List<Thing> thingList = things.toList();
        idCount = thingList.size() > 0 ? Long.parseLong((thingList.get(0).getId().toString())) : 0;

        log.info("id ========================== {} ", idCount);
    }

    @Test
    void 시간_테스트() {
        String time = "2020-10-21 17:00";
        for (int i = 0; i < 24; i++) {
            LocalDateTime t = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            t = t.minusDays(1);
            t = t.plusHours(i);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(t.getYear(), t.getMonthValue(), t.getDayOfMonth(), t.getHour(), 0, 0, 0, ZoneId.of("Asia/Seoul"));

            log.info("zonedDateTime =========================== {} ", zonedDateTime);
            log.info("getYear =========================== {} ", t.getYear());
            log.info("getMonthValue =========================== {} ", t.getMonthValue());
            log.info("getDayOfMonth =========================== {} ", t.getDayOfMonth());
            log.info("getHour =========================== {} ", t.getHour());
        }
    }

    @Test
    void ObservedProperty() throws ServiceFailureException {
        EntityList<ObservedProperty> list = sensorThingsService.observedProperties()
                .query()
                .filter("name eq " + "'" + AirQualityObservedProperty.PM10.getName() + "'")
                .list();

        log.info("ObservedProperty ========================= {} ", list.size());
    }

    @Test
    void Thing() throws ServiceFailureException {
        EntityList<Thing> thing = sensorThingsService.things()
                .query()
                .filter("name eq " + "'" + "반송로" + "'")
                .list();

        log.info("thing ============================ {} ", thing.size());
    }

    @Test
    void 미세먼지_things_필터() throws ServiceFailureException {
        List<Thing> list = new ArrayList<>();
        boolean nextLinkCheck = true;
        int skipCount = 0;
        while (nextLinkCheck) {
            EntityList<Thing> things = sensorThingsService.things()
                    .query()
                    .skip(skipCount)
                    .filter("Datastreams/ObservedProperties/name eq " + "'" + AirQualityObservedProperty.PM10.getName() + "'" +
                            " or name eq " + "'" + AirQualityObservedProperty.PM25.getName() + "'" +
                            " or name eq " + "'" + AirQualityObservedProperty.SO2.getName() + "'" +
                            " or name eq " + "'" + AirQualityObservedProperty.CO.getName() + "'" +
                            " or name eq " + "'" + AirQualityObservedProperty.O3.getName() + "'" +
                            " or name eq " + "'" + AirQualityObservedProperty.NO2.getName() + "'"
                    )
                    .list();
            list.addAll(things.toList());
            nextLinkCheck = things.getNextLink() != null;
            skipCount = skipCount + 100;
        }

        log.info("things count =================== {}", list.size());
        for (var thing : list) {
            log.info("thing info ==================id:{} name:{} ", thing.getId(), thing.getName());
        }
    }

    @Test
    void 시간_비교() throws ServiceFailureException {
        EntityList<Thing> things = sensorThingsService.things()
                .query()
                .filter("name eq " + "'" + "반송로" + "'")
                .expand("Datastreams($orderby=id asc)/Observations($orderby=id desc)")
                .list();
        EntityList<Datastream> datastreamList = things.toList().get(0).getDatastreams();
        Datastream datastream = datastreamList.toList().get(0);
        Observation observation = datastream.getObservations().toList().get(0);
        log.info("datastream ================= {} ", datastream);
        log.info("observation ================= {} ", observation);
        LocalDateTime t = LocalDateTime.parse("2020-10-28 20:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        ZonedDateTime zonedDateTime = ZonedDateTime.of(t.getYear(), t.getMonthValue(), t.getDayOfMonth(), t.getHour(), 0, 0, 0, ZoneId.of("Asia/Seoul"));
        ZonedDateTime resultTime = observation.getResultTime().withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        log.info("time ===================== {} ", zonedDateTime);
        log.info("resultTime ==============={}", resultTime);
        log.info("equals ==================== {} ", zonedDateTime.equals(resultTime));

    }

    @Test
    void datastreamSort() throws ServiceFailureException {
        SensorThingsUtils sta = new SensorThingsUtils();
        sta.init(propertiesConfig.getSensorThingsApiServer());

        var thing = sta.hasThingWithAllEntity(null, "금천구");
        thing.getDatastreams().toList().sort((o1, o2) -> {
            var o1Value = Integer.parseInt(String.valueOf(o1.getId()));
            var o2Value = Integer.parseInt(String.valueOf(o2.getId()));

            return o1Value - o2Value;
        });
        var datastream = thing.getDatastreams().toList().get(0);
        var location = thing.getLocations().toList().get(0);
        var sensor = datastream.getSensor();

        assertThat(thing.getType()).isEqualTo(EntityType.THING);
        assertThat(datastream.getType()).isEqualTo(EntityType.DATASTREAM);
        assertThat(location.getType()).isEqualTo(EntityType.LOCATION);
        assertThat(sensor.getType()).isEqualTo(EntityType.SENSOR);
    }
}