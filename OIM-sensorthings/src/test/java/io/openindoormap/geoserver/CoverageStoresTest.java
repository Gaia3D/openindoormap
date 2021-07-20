package io.openindoormap.geoserver;

import io.openindoormap.OIMSensorthingsApplication;
import io.openindoormap.geoserver.domain.CoverageStore;
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
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = OIMSensorthingsApplication.class)
public class CoverageStoresTest {

    @Autowired
    private RestTemplate geoserverRestTemplate;

    @ParameterizedTest
    @ValueSource(strings = {"dust", "air-quality"})
    void 지오서버_커버리지_저장소_존재_여부(String store) {
        // given
        String workspace = "oim";
        String url = String.format("/workspaces/%s/coveragestores/%s", workspace, store);
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
    void 지오서버_커버리지_저장소_삭제() {
        // given
        String workspace = "oim";
        String store = "air-quality";
        String url = String.format("/workspaces/%s/coveragestores/%s", workspace, store);
        url = UriComponentsBuilder.fromPath(url)
                .queryParam("recurse", false)
                .build(false)
                .toUriString();
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
    void 지오서버_커버리지_저장소_생성() {

        // given
        String workspace = "oim";
        String store = "air-quality";

        WorkSpace workSpace = WorkSpace.builder()
                .name(workspace)
                .build();
        CoverageStore coverageStore = CoverageStore.builder()
                .name(store)
                .description("공기질 커버리지 저장소")
                .type("ImageMosaic")
                .enabled(true)
                .workspace(workSpace)
                .url("file:/geoserver-data/sensorthings/grid")
                .build();
        log.info("----------------------- coverageStore = {}", coverageStore);

        // when
        String url = String.format("/workspaces/%s/coveragestores", workspace);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CoverageStore> httpEntity = new HttpEntity<>(coverageStore, headers);
        ResponseEntity<?> response = geoserverRestTemplate.postForEntity(url, httpEntity, String.class);

        // then
        log.info("----------------------- statusCode = {}, response = {}", response.getStatusCodeValue(), response);
        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

    }


}