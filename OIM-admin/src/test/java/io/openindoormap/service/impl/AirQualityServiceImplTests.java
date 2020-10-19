package io.openindoormap.service.impl;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import io.openindoormap.OIMAdminApplication;
import io.openindoormap.service.SensorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OIMAdminApplication.class)
class AirQualityServiceImplTests {

    @Qualifier("airQualityService")
    @Autowired
    private SensorService sensorService;
    @Autowired
    private SensorThingsService sensorThingsService;

    @Test
    void 초기_데이터_넣기() {
        sensorService.initSensorData();
    }

    @Test
    void 미세먼지_데이터_유무_확인() throws MalformedURLException, ServiceFailureException {
        //http://localhost:8888/FROST-Server/v1.0/Things?$count=true&$filter=description eq '한국환경공단 측정소'&$orderBy=id desc&$top=1
        EntityList<Thing> things = sensorThingsService.things()
                .query()
                .filter("description eq '한국환경공단 측정소'")
                .orderBy("id desc")
                .top(1)
                .list();

        log.info("things ================== {} ", things.toList().get(0).getId());
        //http://localhost:8888/FROST-Server/v1.0/ObservedProperties?$count=true&$filter=name eq 'pm10Value' or name eq 'pm25Value'
//        String url = "http://localhost:8888/FROST-Server/v1.0/ObservedProperties";
//        String filter = URLEncoder.encode("name eq 'pm10Value' or name eq 'pm25Value'", StandardCharsets.UTF_8);
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        UriComponents builder = UriComponentsBuilder.fromHttpUrl(url)
//                .queryParam("$count", true)
//                .queryParam("$filter", filter)
//                .build(false);    //자동으로 encode해주는 것을 막기 위해 false
//        ResponseEntity<?> response = restTemplate.exchange(new URI(builder.toString()), HttpMethod.GET, entity, String.class);
//        log.info("-------- statusCode = {}, body = {}", response.getStatusCodeValue(), response.getBody());
//
//        JSONObject json = (JSONObject) parser.parse(response.getBody().toString());
//        Long count = (Long)json.get("@iot.count");
//
//        log.info("count ================ {} ", count);
    }

    @Test
    void 측정소별_데이터스트림() throws MalformedURLException, ServiceFailureException {
        //http://localhost:8888/FROST-Server/v1.0/Things?$filter= name eq '반송로'&$expand=Datastreams
        EntityList<Thing> things = sensorThingsService.things()
                .query()
                .filter("name eq '반송로'")
                .expand("Datastreams($orderby=id asc)")
                .list();

        EntityList<Datastream> datastreams = things.toList().get(0).getDatastreams();
        for(var datastream : datastreams) {
            log.info("datastream ================== {} ", datastream);
        }
    }
}