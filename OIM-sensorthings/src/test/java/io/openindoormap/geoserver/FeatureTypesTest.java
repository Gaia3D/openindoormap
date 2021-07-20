package io.openindoormap.geoserver;

import io.openindoormap.OIMSensorthingsApplication;
import io.openindoormap.geoserver.domain.FeatureType;
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
public class FeatureTypesTest {

    @Autowired
    private RestTemplate geoserverRestTemplate;

    @ParameterizedTest
    @ValueSource(strings = {"contour", "air-quality-contour"})
    void 지오서버_피쳐타입_레이어_존재_여부(String featuretype) {
        // given
        String workspace = "oim";
        String datastore = "contour";
        String url = String.format("/workspaces/%s/datastores/%s/featuretypes/%s", workspace, datastore, featuretype);
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
    void 지오서버_피쳐타입_레이어_삭제() {
        // given
        String workspace = "oim";
        String datastore = "air-quality-contour";
        String featuretype = "air-quality-contour";
        String url = String.format("/workspaces/%s/datastores/%s/featuretypes/%s", workspace, datastore, featuretype);
        url = UriComponentsBuilder.fromPath(url)
                //.queryParam("recurse", false)
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
    void 지오서버_피쳐타입_레이어_생성() {

        // given
        String workspace = "oim";
        String datastore = "air-quality-contour";
        String url = String.format("/workspaces/%s/datastores/%s/featuretypes", workspace, datastore);

        FeatureType featureType = FeatureType.builder()
                .name(datastore)
                .nativeName("contour")
                .build();

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FeatureType> httpEntity = new HttpEntity<>(featureType, headers);
        ResponseEntity<?> response = geoserverRestTemplate.postForEntity(url, httpEntity, String.class);

        // then
        log.info("----------------------- statusCode = {}, response = {}", response.getStatusCodeValue(), response);
        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

    }
}
