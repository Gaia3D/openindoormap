package io.openindoormap.geoserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openindoormap.OIMSensorthingsApplication;
import io.openindoormap.geoserver.domain.Style;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = OIMSensorthingsApplication.class)
public class StylesTest {

    @Autowired
    private RestTemplate geoserverRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {"air-quality", "air-quality-contour"})
    void 지오서버_스타일_존재_여부(String styleFileName) {
        // given
        String workspace = "oim";
        String url = String.format("/workspaces/%s/styles/%s.sld", workspace, styleFileName);
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

    @ParameterizedTest
    @ValueSource(strings = {"air-quality", "air-quality-contour"})
    void 지오서버_스타일_등록_여부(String layerKey) {
        // given
        String workspace = "oim";
        String url = String.format("/workspaces/%s/styles/%s", workspace, layerKey);
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

    @ParameterizedTest
    @ValueSource(strings = {"air-quality", "air-quality-contour"})
    void 지오서버_스타일_등록(String styleName) {

        // step 1 스키마 등록
        String workspace = "oim";
        String url = String.format("/workspaces/%s/styles", workspace);
        Style style = Style.builder()
                .name(styleName)
                .fileName(styleName + ".sld")
                .build();
        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Style> httpEntity = new HttpEntity<>(style, headers);
        ResponseEntity<?> response = geoserverRestTemplate.postForEntity(url, httpEntity, String.class);
        log.info("----------------------- statusCode = {}, response = {}", response.getStatusCodeValue(), response);

        // step 2 파일 등록
        ClassPathResource resource = new ClassPathResource("geoserver/" + styleName + ".sld");
        Path path = null;
        try {
            path = Paths.get(resource.getURI());
            String sldContents = Files.readString(path);
            // when
            url = String.format("/workspaces/%s/styles/%s", workspace, styleName);
            HttpHeaders headersSld = new HttpHeaders();
            headersSld.setContentType(new MediaType("application", "vnd.ogc.sld+xml"));
            HttpEntity<String> httpEntitySld = new HttpEntity<>(sldContents, headersSld);
            ResponseEntity<?> responseSld = geoserverRestTemplate.exchange(url, HttpMethod.PUT, httpEntitySld, String.class);
            log.info("----------------------- statusCode = {}, response = {}", responseSld.getStatusCodeValue(), responseSld);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // step 3 레이어에 스타일 등록
        url = String.format("/workspaces/%s/layers/%s", workspace, styleName);

        StringBuilder builder = new StringBuilder()
                .append("<layer>")
                .append("<enabled>true</enabled>")
                .append("<defaultStyle>")
                .append("<name>" + styleName + "</name>")
                .append("</defaultStyle>")
                .append("</layer>");

        // when
        HttpHeaders headersLayers = new HttpHeaders();
        headersLayers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> httpEntityLayers = new HttpEntity<>(builder.toString(), headersLayers);
        ResponseEntity<?> responseLayers = geoserverRestTemplate.exchange(url, HttpMethod.PUT, httpEntityLayers, String.class);
        log.info("----------------------- statusCode = {}, response = {}", responseLayers.getStatusCodeValue(), responseLayers);

    }

    @Test
    void 지오서버_그룹_레이어_등록() {

        String workspace = "oim";
        String groupLayerName = "air-quality-group";
        String url = String.format("/workspaces/%s/layergroups", workspace);

        StringBuilder builder = new StringBuilder()
                .append("<layerGroup>")
                .append("<name>" + groupLayerName + "</name>")
                .append("<layers>")
                .append("<layer>air-quality</layer>")
                .append("<layer>air-quality-contour</layer>")
                .append("</layers>")
                .append("<styles>")
                .append("<style>air-quality</style>")
                .append("<style>air-quality-contour</style>")
                .append("</styles>")
                .append("</layerGroup>");

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> httpEntity = new HttpEntity<>(builder.toString(), headers);
        ResponseEntity<?> response = geoserverRestTemplate.postForEntity(url, httpEntity, String.class);
        log.info("----------------------- statusCode = {}, response = {}", response.getStatusCodeValue(), response);

    }

}
