package io.openindoormap.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.openindoormap.api.DesignLayerGroupAPIController;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;
import io.openindoormap.service.DesignLayerGroupService;

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

@WebMvcTest(DesignLayerGroupAPIController.class)
class DesignLayerGroupAPIControllerTests extends BaseControllerTest {

    @MockBean
    private DesignLayerGroupService designLayerGroupService;

    @Test
    @DisplayName("DesignLayerGroup 목록 조회 하기")
    public void getDesignLayerGroups() throws Exception {
        given(designLayerGroupService.getListDesignLayerGroup(any())).willReturn(getDesignGroupList());

        this.mockMvc.perform(get("/api/design-layer-groups"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.designLayerGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("design-layer-group-list"));
    }

    @Test
    @DisplayName("디자인 레이어 그룹 parent 로 조회 하기")
    public void getDesignLayerGroupsByParent() throws Exception {
        given(designLayerGroupService.getListDesignLayerGroup(any())).willReturn(getDesignGroupList());

        this.mockMvc.perform(get("/api/design-layer-groups/parent/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.designLayerGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("design-layer-group-list-by-parent"));
    }

    @Test
    @DisplayName("DesignLayerGroup 단일 조회 하기")
    public void getDesignLayerGroup() throws Exception {
        DesignLayerGroup mock = getDesignLayerGroupById();
        given(designLayerGroupService.getDesignLayerGroup(any())).willReturn(mock);

        this.mockMvc.perform(get("/api/design-layer-groups/{id}", mock.getDesignLayerGroupId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("designLayerGroupId").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("design-layer-group-get",
                        relaxedResponseFields(
                                fieldWithPath("designLayerGroupId").description("design layer 그룹 고유번호"),
                                fieldWithPath("designLayerGroupName").description("design layer 그룹명"),
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
                                fieldWithPath("designLayerList").description("자식 desing 레이어 목록")
                        )
                ));
    }

    private List<DesignLayerGroup> getDesignGroupList() {
        List<DesignLayerGroup> mockList = new ArrayList<>();
        IntStream.range(1, 4).forEach(i -> {
            mockList.add(DesignLayerGroup.builder()
                    .designLayerGroupId(i)
                    .designLayerGroupName("groupName" + i)
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

    private DesignLayerGroup getDesignLayerGroupById() {
        return DesignLayerGroup.builder()
                .designLayerGroupId(1)
                .designLayerGroupName("groupName")
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