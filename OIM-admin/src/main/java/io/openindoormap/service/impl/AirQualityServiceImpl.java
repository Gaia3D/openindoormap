package io.openindoormap.service.impl;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.builder.*;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractFeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.api.AbstractLocationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.service.AirQualityService;
import io.openindoormap.support.LogMessageSupport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geojson.Point;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sensor 초기 데이터 생성 및 갱신
 */
@Service("airQualityService")
@AllArgsConstructor
@Slf4j
public class AirQualityServiceImpl implements AirQualityService {

    private final PropertiesConfig propertiesConfig;
    private final JSONParser parser;

    @Override
    public void initSensorData() {
        // sensorThingsAPI server
        SensorThingsService service = null;
        JSONObject stationJson = null;
        try {
            service = new SensorThingsService(new URL(propertiesConfig.getSensorThingsApiServer()));
            stationJson = getListStation();
        } catch (Exception e) {
            LogMessageSupport.printMessage(e, "-------- AirQualityService Error = {}", e.getMessage());
        }
        // 저장소 목록
        List<?> stationList = (List<?>) stationJson.get("list");
        int id = 0;
        try {
            // ObservedProperty PM10
            service.create(ObservedPropertyBuilder.builder()
                    .name("pm10Value")
                    .description("미세먼지(PM10) Particulates")
                    .definition("https://en.wikipedia.org/wiki/Particulates")
                    .build());

            // ObservedProperty PM2.5
            service.create(ObservedPropertyBuilder.builder()
                    .name("pm25Value")
                    .description("미세먼지(PM2.5) Particulates")
                    .definition("https://en.wikipedia.org/wiki/Particulates")
                    .build());

            // ObservedProperty 아황산가스 농도
            service.create(ObservedPropertyBuilder.builder()
                    .name("so2Value")
                    .description("아황산가스 농도 Sulfur_dioxide")
                    .definition("https://en.wikipedia.org/wiki/Sulfur_dioxide")
                    .build());

            // ObservedProperty 일산화탄소 농도
            service.create(ObservedPropertyBuilder.builder()
                    .name("coValue")
                    .description("일산화탄소 농도 Carbon_monoxide")
                    .definition("https://en.wikipedia.org/wiki/Carbon_monoxide")
                    .build());

            // ObservedProperty 오존 농도
            service.create(ObservedPropertyBuilder.builder()
                    .name("o3Value")
                    .description("오존 농도 Ozone")
                    .definition("https://en.wikipedia.org/wiki/Ozone")
                    .build());

            // ObservedProperty 이산화질소 농도
            service.create(ObservedPropertyBuilder.builder()
                    .name("no2Value")
                    .description("이산화질소 Nitrogen_dioxide")
                    .definition("https://en.wikipedia.org/wiki/Nitrogen_dioxide")
                    .build());

            for (var station : stationList) {
                var json = (JSONObject) station;
                var stationName = (String) json.get("stationName");
                var dmX = json.get("dmX").toString().trim();
                var dmY = json.get("dmY").toString().trim();
                // 위치 정보가 없는 측정소의 경우 1,1 로 좌표 넣어줌
                var point = "".equals(dmX) || "".equals(dmY) ? new Point(1, 1) : new Point(Double.parseDouble(dmY), Double.parseDouble(dmX));
                ++id;

                // Thing
                Map<String, Object> thingProperties = new HashMap<>();
                thingProperties.put("stationName", stationName);
                thingProperties.put("year", json.get("year"));
                thingProperties.put("oper", json.get("oper"));
                thingProperties.put("photo", json.get("photo"));
                thingProperties.put("vrml", json.get("vrml"));
                thingProperties.put("map", json.get("map"));
                thingProperties.put("mangName", json.get("mangName"));
                thingProperties.put("item", json.get("item"));

                service.create(ThingBuilder.builder()
                        .name(stationName)
                        .description("한국환경공단 측정소")
                        .properties(thingProperties)
                        .build());

                // Location
                service.create(LocationBuilder.builder()
                        .name((String) json.get("addr"))
                        .encodingType(AbstractLocationBuilder.ValueCode.GeoJSON)
                        .description("대기질 측정소 위치")
                        .location(point)
                        .build());

                // DataStream PM10
                service.create(DatastreamBuilder.builder()
                        .name("미세먼지(PM10)")
                        .description("미세먼지(PM10)")
                        .observationType(AbstractDatastreamBuilder.ValueCode.OM_Observation)
                        .unitOfMeasurement(new UnitOfMeasurement(
                                "microgram per cubic meter",
                                "ug/m3",
                                "https://www.eea.europa.eu/themes/air/air-quality/resources/glossary/g-m3"
                        ))
                        .sensor(new Sensor(
                                stationName + ":" + "미세먼지(PM10)",
                                "미세먼지 측정소",
                                "http://schema.org/description",
                                json.get("mangName")
                        ))
                        .observedProperty(ObservedPropertyBuilder.builder().id(Id.tryToParse("1")).build())
                        .thing(ThingBuilder.builder().id(Id.tryToParse(String.valueOf(id))).build())
                        .build());

                // DataStream PM2.5
                service.create(DatastreamBuilder.builder()
                        .name("미세먼지(PM2.5)")
                        .description("미세먼지(PM2.5)")
                        .observationType(AbstractDatastreamBuilder.ValueCode.OM_Observation)
                        .unitOfMeasurement(new UnitOfMeasurement(
                                "microgram per cubic meter",
                                "ug/m3",
                                "https://www.eea.europa.eu/themes/air/air-quality/resources/glossary/g-m3"
                        ))
                        .sensor(new Sensor(
                                stationName + ":" + "미세먼지(PM2.5)",
                                "미세먼지 측정소",
                                "http://schema.org/description",
                                json.get("mangName")
                        ))
                        .observedProperty(ObservedPropertyBuilder.builder().id(Id.tryToParse("2")).build())
                        .thing(ThingBuilder.builder().id(Id.tryToParse(String.valueOf(id))).build())
                        .build());

                // DataStream 아황산가스 농도
                service.create(DatastreamBuilder.builder()
                        .name("아황산가스 농도")
                        .description("아황산가스 농도")
                        .observationType(AbstractDatastreamBuilder.ValueCode.OM_Observation)
                        .unitOfMeasurement(new UnitOfMeasurement(
                                "parts per million",
                                "ppm",
                                "https://en.wikipedia.org/wiki/Parts-per_notation"
                        ))
                        .sensor(new Sensor(
                                stationName + ":" + "아황산가스 농도",
                                "아황산가스 측정소",
                                "http://schema.org/description",
                                json.get("mangName")
                        ))
                        .observedProperty(ObservedPropertyBuilder.builder().id(Id.tryToParse("3")).build())
                        .thing(ThingBuilder.builder().id(Id.tryToParse(String.valueOf(id))).build())
                        .build());

                // DataStream 일산화탄소 농도
                service.create(DatastreamBuilder.builder()
                        .name("일산화탄소 농도")
                        .description("일산화탄소 농도")
                        .observationType(AbstractDatastreamBuilder.ValueCode.OM_Observation)
                        .unitOfMeasurement(new UnitOfMeasurement(
                                "parts per million",
                                "ppm",
                                "https://en.wikipedia.org/wiki/Parts-per_notation"
                        ))
                        .sensor(new Sensor(
                                stationName + ":" + "일산화탄소 농도",
                                "일산화탄소 측정소",
                                "http://schema.org/description",
                                json.get("mangName")
                        ))
                        .observedProperty(ObservedPropertyBuilder.builder().id(Id.tryToParse("4")).build())
                        .thing(ThingBuilder.builder().id(Id.tryToParse(String.valueOf(id))).build())
                        .build());

                // DataStream 오존 농도
                service.create(DatastreamBuilder.builder()
                        .name("오존 농도")
                        .description("오존 농도")
                        .observationType(AbstractDatastreamBuilder.ValueCode.OM_Observation)
                        .unitOfMeasurement(new UnitOfMeasurement(
                                "parts per million",
                                "ppm",
                                "https://en.wikipedia.org/wiki/Parts-per_notation"
                        ))
                        .sensor(new Sensor(
                                stationName + ":" + "오존 농도",
                                "오존 측정소",
                                "http://schema.org/description",
                                json.get("mangName")
                        ))
                        .observedProperty(ObservedPropertyBuilder.builder().id(Id.tryToParse("5")).build())
                        .thing(ThingBuilder.builder().id(Id.tryToParse(String.valueOf(id))).build())
                        .build());

                // DataStream 이산화질소 농도
                service.create(DatastreamBuilder.builder()
                        .name("이산화질소 농도")
                        .description("이산화질소 농도")
                        .observationType(AbstractDatastreamBuilder.ValueCode.OM_Observation)
                        .unitOfMeasurement(new UnitOfMeasurement(
                                "parts per million",
                                "ppm",
                                "https://en.wikipedia.org/wiki/Parts-per_notation"
                        ))
                        .sensor(new Sensor(
                                stationName + ":" + "이산화질소 농도",
                                "이산화질소 측정소",
                                "http://schema.org/description",
                                json.get("mangName")
                        ))
                        .observedProperty(ObservedPropertyBuilder.builder().id(Id.tryToParse("6")).build())
                        .thing(ThingBuilder.builder().id(Id.tryToParse(String.valueOf(id))).build())
                        .build());

                // FeatureOfInterest
                service.create(FeatureOfInterestBuilder.builder()
                        .name(stationName + " 측정소")
                        .description("한국환경공단 대기질 측정소")
                        .encodingType(AbstractFeatureOfInterestBuilder.ValueCode.GeoJSON)
                        .feature(point)
                        .build());
            }
        } catch (ServiceFailureException e) {
            LogMessageSupport.printMessage(e, "-------- AirQualityService ServiceFailureException = {}", e.getMessage());
        }
    }

    @Override
    public void insertSensorData() {
        boolean mockEnable = propertiesConfig.isMockEnable();
        // 테스트
        if (mockEnable) {

        } else {
            // 운영시 api 연동
        }
    }

    private JSONObject getListStation() throws Exception {
        boolean mockEnable = propertiesConfig.isMockEnable();
        JSONObject stationJson;
        // 테스트
        if (mockEnable) {
            log.info("mock 미세먼지 저장소 목록");
            stationJson = (JSONObject) parser.parse(new FileReader(this.getClass().getClassLoader().getResource("sample/airQualityStation.json").getFile()));
        } else {
            // 운영시 api 연동
            log.info("api 연동 미세먼지 저장소 목록");
            String url = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getMsrstnList";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            UriComponents builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("ServiceKey", "4EA8xQz4hBCUI0azTs4P6Xznia8j5fjbeA%2F33IADvvdxt2MkVGsjVzU4yjn2tjyrjkww73GoOncpjz5L4nKdvg%3D%3D")
                    .queryParam("numOfRows", 10000)
                    .queryParam("pageNo", 1)
                    .queryParam("_returnType", "json")
                    .build(false);    //자동으로 encode해주는 것을 막기 위해 false
            // TODO ServiceKey 는 발급받은 키로 해야함. 개발용 api key 는 하루 request 500건으로 제한
            ResponseEntity<?> response = restTemplate.exchange(new URI(builder.toString()), HttpMethod.GET, entity, String.class);
            log.info("-------- statusCode = {}, body = {}", response.getStatusCodeValue(), response.getBody());

            stationJson = (JSONObject) parser.parse(response.toString());
        }

        return stationJson;
    }

    private boolean initDataExistCheck() {

        return true;
    }
}
