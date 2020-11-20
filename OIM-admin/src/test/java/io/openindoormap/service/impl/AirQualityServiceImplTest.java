package io.openindoormap.service.impl;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.EntityType;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import io.openindoormap.OIMAdminApplication;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.sensor.AirQualityObservedProperty;
import io.openindoormap.domain.sensor.TimeType;
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
    private SensorThingsUtils sta;

    @BeforeAll
    void setup() throws MalformedURLException {
        sensorThingsService = new SensorThingsService(new URL(propertiesConfig.getSensorThingsApiServer()));
        sta = new SensorThingsUtils();
        sta.init(propertiesConfig.getSensorThingsApiServer());
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
    void 데이터스트림_정렬() throws ServiceFailureException {

        var thing = sta.hasThingWithRelationEntities(null, "금천구");
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

    @Test
    void 시간_범위_검색() {
        //http://localhost:8888/FROST-Server/v1.0/Observations?$filter=resultTime ge 2020-11-16T15:00:00.000Z and
        // resultTime le 2020-11-17T14:00:00.000Z and Datastreams/Things/name eq '금천구' and Datastreams/ObservedProperties/name eq 'pm10Value'
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        ZonedDateTime now = ZonedDateTime.now().minusDays(1L);
        ZonedDateTime start = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 0, 0, 0, 0, zoneId);
        ZonedDateTime end = ZonedDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 23, 0, 0, 0, zoneId);
        log.info("start ============ {} ", start.toInstant());
        log.info("end ================ {} ", end.toInstant());

        String stationName = "금천구";
        String filter = "resultTime ge " + start.toInstant() + " and resultTime le " + end.toInstant() +
                " and Datastreams/Things/name eq '" + stationName + "' and Datastreams/ObservedProperties/name eq '" +
                AirQualityObservedProperty.PM10.getName() + "'";

        log.info("filter ===================== {} ", filter);

        EntityList<Observation> observations = sta.hasObservations(filter, null);

        log.info("Observations size =========== {} ", observations.size());

        observations.forEach(f -> log.info(f.getResultTime().toString()));
    }

    @Test
    void 데이터스트림_24시간() {
        //http://localhost:8888/FROST-Server/v1.0/Datastreams?$filter=Thing/name eq '금천구' and Datastream/ObservedProperty/name eq 'pm10ValueDaily'
        String stationName = "금천구";
        for (AirQualityObservedProperty type : AirQualityObservedProperty.values()) {
            if(type.getTimeType().equals(TimeType.DAILY)){
                String filter = "Thing/name eq '" + stationName + "' and Datastream/ObservedProperty/name eq '" + type.getName() + "'";
                Datastream datastream = sta.hasDatastream(filter, null);
                log.info(datastream.getName());
            }
        }
    }

    @Test
    void 미세먼지_전체_thing_조회() {
        String filter = "Datastreams/ObservedProperties/name eq " + "'" + AirQualityObservedProperty.PM10.getName() + "'" +
                " or name eq " + "'" + AirQualityObservedProperty.PM25.getName() + "'" +
                " or name eq " + "'" + AirQualityObservedProperty.SO2.getName() + "'" +
                " or name eq " + "'" + AirQualityObservedProperty.CO.getName() + "'" +
                " or name eq " + "'" + AirQualityObservedProperty.O3.getName() + "'" +
                " or name eq " + "'" + AirQualityObservedProperty.NO2.getName() + "'";

        EntityList<Thing> things = sta.hasThingsFindAll(filter);

        log.info("things size ================= {} ", things.size());

        things.forEach(f -> log.info(String.valueOf(f.getId())));
    }

    @Test
    void test() {
        log.info("test =============== {} ", sta.hasThingsWithObservation(null, "금천구"));
    }
}