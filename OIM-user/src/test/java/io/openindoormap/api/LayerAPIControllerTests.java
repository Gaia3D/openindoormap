package io.openindoormap.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;

import io.openindoormap.api.LayerAPIController;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.layer.Layer;
import io.openindoormap.service.LayerService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LayerAPIController.class)
class LayerAPIControllerTests extends BaseControllerTest {

    @MockBean
    private LayerService layerService;

    @Test
    @DisplayName("Layer 목록 조회 하기")
    public void getLayers() throws Exception {
        // given
        given(layerService.getListLayer(any())).willReturn(getLayerList());

        this.mockMvc.perform(get("/api/layers")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .param("layerGroupId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.layers[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("layer-list", requestParameters(parameterWithName("layerGroupId").description("레이어 그룹 고유번호"))));
    }

    @Test
    @DisplayName("Layer 단일 조회 하기")
    public void getLayer() throws Exception {
        Layer mock = getLayerById();
        given(layerService.getLayer(any())).willReturn(mock);

        this.mockMvc.perform(get("/api/layers/{id}", mock.getLayerId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerId").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("layer-get",
                        /**
                         * relaxedResponseFields 를 쓰면 모든 필드를 기술할 필요가 없다.
                         * 하지만 모든 필드를 기술하지 않으므로 정확한 문서를 만들지 못한다.
                         * responseFields를 쓰면 모든 필드를 기술해야 한다.
                         */
                        relaxedResponseFields(
                                fieldWithPath("layerId").description("layer 고유번호"),
                                fieldWithPath("layerGroupId").description("layer 그룹 고유번호"),
                                fieldWithPath("layerKey").description("layer 고유키(API용)"),
                                fieldWithPath("layerName").description("layer 명"),
                                fieldWithPath("layerType").description("layer 분류. Raster, Vector"),
                                fieldWithPath("userId").description("사용자명"),
                                fieldWithPath("ogcWebServices").description("OGC Web Services (wms, wfs, wcs, wps)"),
                                fieldWithPath("geometryType").description("도형 타입"),
                                fieldWithPath("layerFillColor").description("외곽선 색상"),
                                fieldWithPath("layerLineColor").description("외곽선 두께"),
                                fieldWithPath("layerLineStyle").description("채우기 색상"),
                                fieldWithPath("layerAlphaStyle").description("투명도"),
                                fieldWithPath("viewOrder").description("나열 순서"),
                                fieldWithPath("zindex").description("지도위에 노출 순위(css z-index와 동일)"),
                                fieldWithPath("available").description("사용유무"),
                                fieldWithPath("cacheAvailable").description("캐시 사용 유무"),
                                fieldWithPath("coordinate").description("좌표계 정보"),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("updateDate").description("수정일"),
                                fieldWithPath("insertDate").description("등록일")
                        )
                ));
    }

    private List<Layer> getLayerList() {
        List<Layer> mockList = new ArrayList<>();
        IntStream.range(1, 4).forEach(i -> {
            mockList.add(Layer.builder()
                    .layerId((Integer) i)
                    .layerGroupId(1)
                    .layerKey("test" + i)
                    .layerName("testName" + i)
                    .layerType("Vector")
                    .userId("admin")
                    .ogcWebServices("wms")
                    .geometryType("polygon")
                    .layerFillColor("#000000")
                    .layerLineColor("#000000")
                    .layerLineStyle(1F)
                    .layerAlphaStyle(1F)
                    .viewOrder(1)
                    .zIndex(1)
                    .available(true)
                    .cacheAvailable(true)
                    .coordinate("EPSG:4326")
                    .description("test")
                    .build());
        });
        return mockList;
    }

    private Layer getLayerById() {
        return Layer.builder()
                .layerId((int) 1L)
                .layerGroupId(1)
                .layerKey("test")
                .layerName("testName")
                .layerType("Vector")
                .userId("admin")
                .ogcWebServices("wms")
                .geometryType("polygon")
                .layerFillColor("#000000")
                .layerLineColor("#000000")
                .layerLineStyle(1F)
                .layerAlphaStyle(1F)
                .viewOrder(1)
                .zIndex(1)
                .available(true)
                .cacheAvailable(true)
                .coordinate("EPSG:4326")
                .description("test")
                .build();
    }
}