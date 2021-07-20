package io.openindoormap.geoserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openindoormap.OIMSensorthingsApplication;
import io.openindoormap.geoserver.domain.ConnectionParameter;
import io.openindoormap.geoserver.domain.ConnectionParameters;
import io.openindoormap.geoserver.domain.DataStore;
import io.openindoormap.geoserver.domain.WorkSpace;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = OIMSensorthingsApplication.class)
public class DataStoresTest {

    @Autowired
    private RestTemplate geoserverRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {"contour", "air-quality-contour"})
    public void 지오서버_데이터_저장소_존재_여부(String store) {
        // given
        String workspace = "oim";
        String url = String.format("/workspaces/%s/datastores/%s", workspace, store);
        try {
            // when
            ResponseEntity<?> response = geoserverRestTemplate.getForEntity(url, String.class);
            log.info("----------------------- statusCode = {}, response = {}", response.getStatusCodeValue(), response);
            // than
            assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        } catch (RestClientResponseException e) {
            // then
            log.info("----------------------- statusCode = {}, message = {}", e.getRawStatusCode(), e.getMessage());
            assertThat(e.getRawStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        }
    }

    @Test
    public void 지오서버_데이터_저장소_삭제() {
        // given
        String workspace = "oim";
        String store = "air-quality-contour";
        String url = String.format("/workspaces/%s/datastores/%s", workspace, store);
        try {
            // when
            ResponseEntity<?> response = geoserverRestTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
            log.info("----------------------- statusCode = {}, response = {}", response.getStatusCodeValue(), response);
            // then
            assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        } catch (HttpClientErrorException e) {
            // then
            log.info("----------------------- statusCode = {}, message = {}", e.getRawStatusCode(), e.getMessage());
            assertThat(e.getRawStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        }
    }

    @Test
    public void 지오서버_데이터_저장소_생성() {

        // given
        String workspace = "oim";
        String store = "air-quality-contour";

        List<ConnectionParameter> entry = new ArrayList<>();
        ConnectionParameter connectionParameter = ConnectionParameter.builder()
                .key("url")
                .value("file:/geoserver-data/sensorthings/contour/contour.shp")
                .build();
        entry.add(connectionParameter);
        connectionParameter = ConnectionParameter.builder()
                .key("charset")
                .value(StandardCharsets.UTF_8.name())
                .build();
        entry.add(connectionParameter);

        WorkSpace workSpace = WorkSpace.builder()
                .name(workspace)
                .build();
        ConnectionParameters connectionParameters = ConnectionParameters.builder()
                .entry(entry)
                .build();
        DataStore dataStore = DataStore.builder()
                .name(store)
                .description("공기질 등치선 저장소")
                .enabled(true)
                .workspace(workSpace)
                .connectionParameters(connectionParameters)
                .build();

        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataStore);
            log.info("----------------------- dataStore = {}", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // when
        String url = String.format("/workspaces/%s/datastores", workspace);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DataStore> httpEntity = new HttpEntity<>(dataStore, headers);
        ResponseEntity<?> response = geoserverRestTemplate.postForEntity(url, httpEntity, String.class);

        // then
        log.info("----------------------- statusCode = {}, response = {}", response.getStatusCodeValue(), response);
        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

    }
    
}
