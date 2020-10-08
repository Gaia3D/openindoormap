package io.openindoormap.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.openindoormap.api.LayerGroupAPIController;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.layer.LayerGroup;
import io.openindoormap.service.LayerGroupService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LayerGroupAPIController.class)
class LayerGroupAPIControllerTests extends BaseControllerTest {

    @MockBean
    private LayerGroupService layerGroupService;

    @Test
    @DisplayName("LayerGroup 목록 조회 하기")
    public void getLayerGroups() throws Exception {
        given(layerGroupService.getListLayerGroup()).willReturn(getLayerGroupList());
        
        this.mockMvc.perform(get("/api/layer-groups"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.layerGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("layer-group-list"));
    }

    @Test
    @DisplayName("레이어 그룹 parent 로 조회 하기")
    public void getLayerGroupsByParent() throws Exception {
        given(layerGroupService.getListLayerGroup()).willReturn(getLayerGroupList());

        this.mockMvc.perform(get("/api/layer-groups/parent/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.layerGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("layer-group-list-by-parent"));
    }

    @Test
    @DisplayName("LayerGroup 단일 조회 하기")
    public void getLayerGroup() throws Exception {
        LayerGroup mock = getLayerGroupById();
        given(layerGroupService.getLayerGroup(any())).willReturn(mock);

        this.mockMvc.perform(get("/api/layer-groups/{id}", mock.getLayerGroupId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("layerGroupId").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("layer-group-get",
                        relaxedResponseFields(
                                fieldWithPath("layerGroupId").description("layer 그룹 고유번호"),
                                fieldWithPath("layerGroupName").description("layer 그룹명"),
                                fieldWithPath("userId").description("아이디"),
                                fieldWithPath("ancestor").description("조상"),
                                fieldWithPath("parent").description("부모"),
                                fieldWithPath("parentName").description("부모명"),
                                fieldWithPath("depth").description("깊이"),
                                fieldWithPath("viewOrder").description("나열 순서"),
                                fieldWithPath("children").description("자식 존재 유무"),
                                fieldWithPath("available").description("사용 유무"),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("updateDate").description("수정일"),
                                fieldWithPath("insertDate").description("등록일"),
                                fieldWithPath("layerList").description("자식 레이어 목록")
                        )
                ));
    }

    private List<LayerGroup> getLayerGroupList() {
        List<LayerGroup> mockList = new ArrayList<>();
        IntStream.range(1, 4).forEach(i -> {
            mockList.add(LayerGroup.builder()
                    .layerGroupId(i)
                    .layerGroupName("groupName" + i)
                    .userId("admin")
                    .ancestor(1)
                    .parent(0)
                    .parentName("parentName")
                    .depth(1)
                    .viewOrder(1)
                    .children(1)
                    .available(true)
                    .description("test")
                    .build());
        });
        return mockList;
    }

    private LayerGroup getLayerGroupById() {
        return LayerGroup.builder()
                .layerGroupId(1)
                .layerGroupName("groupName")
                .userId("admin")
                .ancestor(1)
                .parent(0)
                .parentName("parentName")
                .depth(1)
                .viewOrder(1)
                .children(1)
                .available(true)
                .description("test")
                .build();
    }
}