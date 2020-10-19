package io.openindoormap.service.impl;

import io.openindoormap.OIMAdminApplication;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.service.SensorService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OIMAdminApplication.class)
class AirQualityServiceImplTests {

    @Qualifier("airQualityService")
    @Autowired
    private SensorService sensorService;
    @Autowired
    private PropertiesConfig propertiesConfig;
    @Autowired
    private JSONParser parser;

    @Test
    void test() {
        sensorService.initSensorData();
    }

    @Test
    void countTest() throws URISyntaxException, UnsupportedEncodingException, ParseException {
        //http://localhost:8888/FROST-Server/v1.0/ObservedProperties?$count=true&$filter=name eq 'pm10Value' or name eq 'pm25Value'
        String url = "http://localhost:8888/FROST-Server/v1.0/ObservedProperties";
        String filter = URLEncoder.encode("name eq 'pm10Value' or name eq 'pm25Value'", StandardCharsets.UTF_8);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UriComponents builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("$count", true)
                .queryParam("$filter", filter)
                .build(false);    //자동으로 encode해주는 것을 막기 위해 false
        ResponseEntity<?> response = restTemplate.exchange(new URI(builder.toString()), HttpMethod.GET, entity, String.class);
        log.info("-------- statusCode = {}, body = {}", response.getStatusCodeValue(), response.getBody());

        JSONObject json = (JSONObject) parser.parse(response.getBody().toString());
        Long count = (Long)json.get("@iot.count");

        log.info("count ================ {} ", count);
    }
}