package io.openindoormap.utils;

import io.openindoormap.OIMSensorthingsApplication;
import io.openindoormap.config.PropertiesConfig;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OIMSensorthingsApplication.class)
public class SensorThingsUtilsTest {

    @Autowired
    private PropertiesConfig propertiesConfig;

    private String airkoreaApiServiceUrl;
    private String airkoreaAuthKey;

    @BeforeAll
    void init() {
        airkoreaApiServiceUrl = propertiesConfig.getAirkoreaApiServiceUrl();
        airkoreaAuthKey = propertiesConfig.getAirkoreaAuthKey();
    }

    @Test
    void getListStation() {
        long startTime;
        long endTime;
        startTime = System.currentTimeMillis();
        JSONObject stationJson = null;
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
        endTime = System.currentTimeMillis();
        log.info("api 연동 [한국환경공단_에어코리아_측정소정보] exection time in millisecond : " + (endTime - startTime));
    }

    @Test
    void getObservations() {
        long startTime;
        long endTime;
        startTime = System.currentTimeMillis();
        JSONObject observationJson = null;
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
                    .queryParam("sidoName", UriUtils.encode("전국", "UTF-8"))
                    .queryParam("ver", 1.3)
                    .queryParam("returnType", "json")
                    .build(false);    //자동으로 encode해주는 것을 막기 위해 false
        }
        observationJson = getAPIResult(builder.toString());
        endTime = System.currentTimeMillis();
        log.info("api 연동 [한국환경공단_에어코리아_대기오염정보] exection time in millisecond : " + (endTime - startTime));
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

}
