package io.openindoormap.controller.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import io.openindoormap.controller.rest.DesignLayerRestController;
import io.openindoormap.domain.extrusionmodel.DesignLayer;
import io.openindoormap.service.AccessLogService;
import io.openindoormap.service.DesignLayerService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DesignLayerRestController.class)
class DesignLayerRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DesignLayerService designLayerService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    @DisplayName("디자인 레이어 등록")
    void designLayerInsert() throws Exception {
        // shpae 파일 업로드 및 압축 해제
        // shape 필수 칼럼 및 필수 파일 validation
        // geotools 로 shape 파일 정보 parsing
        // DesignLayer 등록
        // shape 속성정보 그룹에 따라 land, building 테이블에 insert
        // 필수 컬럼 외에 담긴 정보는 design_layer_attribute 테이블에 insert
        // DesignLayerFileInfo 등록
        // version_id, enable_yn 업데이트
        // 초기 한번은 geoserver 레이어 등록. 그 다음부터는 reload만
        DesignLayer mock = DesignLayer.builder().build();
        this.mockMvc.perform(get("/design-layers/insert")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}