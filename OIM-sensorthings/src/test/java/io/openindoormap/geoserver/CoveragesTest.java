package io.openindoormap.geoserver;

import io.openindoormap.OIMSensorthingsApplication;
import io.openindoormap.geoserver.domain.Coverage;
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
public class CoveragesTest {

    @Autowired
    private RestTemplate geoserverRestTemplate;

    @ParameterizedTest
    @ValueSource(strings = {"grid", "air-quality"})
    void 지오서버_커버리지_레이어_존재_여부(String coverage) {
        // given
        String workspace = "oim";
        String url = String.format("/workspaces/%s/coverages/%s", workspace, coverage);
        url = UriComponentsBuilder.fromPath(url)
                //.queryParam("quietOnNotFound", false)
                .build(false)
                .toUriString();
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
    void 지오서버_커버리지_레이어_삭제() {
        // given
        String workspace = "oim";
        String store = "air-quality";
        String coverage = "air-quality";
        String url = String.format("/workspaces/%s/coveragestores/%s/coverages/%s", workspace, store, coverage);
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
    void 지오서버_커버리지_레이어_생성() {

        // given
        String workspace = "oim";
        String store = "air-quality";
        String url = String.format("/workspaces/%s/coveragestores/%s/coverages", workspace, store);

        Coverage coverage = Coverage.builder()
                .name("air-quality")
                .nativeName("grid")
                .build();
        log.info("----------------------- coverage = {}", coverage);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Coverage> httpEntity = new HttpEntity<>(coverage, headers);
        ResponseEntity<?> response = geoserverRestTemplate.postForEntity(url, httpEntity, String.class);

        // then
        log.info("----------------------- statusCode = {}, response = {}", response.getStatusCodeValue(), response);
        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

    }


}
