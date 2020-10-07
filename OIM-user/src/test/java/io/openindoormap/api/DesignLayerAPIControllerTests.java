package io.openindoormap.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;

import io.openindoormap.api.DesignLayerAPIController;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.extrusionmodel.DesignLayer;
import io.openindoormap.service.DesignLayerService;

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

@WebMvcTest(DesignLayerAPIController.class)
class DesignLayerAPIControllerTests extends BaseControllerTest {

    @MockBean
    private DesignLayerService designLayerService;

    @Test
    @DisplayName("DesignLayer 목록 조회 하기")
    public void getDesignLayers() throws Exception {
        // given
        given(designLayerService.getListDesignLayer(any())).willReturn(getDesignLayerList());

        this.mockMvc.perform(get("/api/design-layers")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .param("urbanGroupId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.designLayers[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("design-layer-list", requestParameters(parameterWithName("urbanGroupId").description("도시 그룹 고유번호"))));
    }

    @Test
    @DisplayName("DesignLayer 단일 조회 하기")
    public void getDesignLayer() throws Exception {
        DesignLayer mock = getDesignLayerById();
        given(designLayerService.getDesignLayer(any())).willReturn(mock);

        this.mockMvc.perform(get("/api/design-layers/{id}", mock.getDesignLayerId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("designLayerId").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("design-layer-get",
                        /**
                         * relaxedResponseFields 를 쓰면 모든 필드를 기술할 필요가 없다.
                         * 하지만 모든 필드를 기술하지 않으므로 정확한 문서를 만들지 못한다.
                         * responseFields를 쓰면 모든 필드를 기술해야 한다.
                         */
                        relaxedResponseFields(
                                fieldWithPath("designLayerId").description("design layer 고유번호"),
                                fieldWithPath("urbanGroupId").description("도시그룹 고유번호"),
                                fieldWithPath("designLayerGroupType").description("design layer 그룹 타입. land : 땅, building : 빌딩"),
                                fieldWithPath("designLayerGroupId").description("design layer 그룹 고유번호"),
                                fieldWithPath("designLayerKey").description("design layer 고유키(API용)"),
                                fieldWithPath("designLayerName").description("design layer 명"),
                                fieldWithPath("designLayerType").description("design layer 분류. land : 땅, building : 빌딩"),
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

    private List<DesignLayer> getDesignLayerList() {
        List<DesignLayer> mockList = new ArrayList<>();
        IntStream.range(1, 4).forEach(i -> {
            mockList.add(DesignLayer.builder()
                    .designLayerId((long) i)
                    .urbanGroupId(1)
                    .designLayerGroupId(1)
                    .designLayerKey("test" + i)
                    .designLayerName("testName" + i)
                    .designLayerType("land")
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

    private DesignLayer getDesignLayerById() {
        return DesignLayer.builder()
                .designLayerId(1L)
                .urbanGroupId(1)
                .designLayerGroupId(1)
                .designLayerKey("test")
                .designLayerName("testName")
                .designLayerType("land")
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