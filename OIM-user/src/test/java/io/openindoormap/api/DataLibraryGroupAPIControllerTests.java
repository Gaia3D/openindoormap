package io.openindoormap.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.openindoormap.api.DataLibraryGroupAPIController;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;
import io.openindoormap.service.DataLibraryGroupService;

import java.time.LocalDateTime;
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

@WebMvcTest(DataLibraryGroupAPIController.class)
class DataLibraryGroupAPIControllerTests extends BaseControllerTest {

    @MockBean
    private DataLibraryGroupService dataLibraryGroupService;

    @Test
    @DisplayName("데이터 라이브러리 그룹 목록 조회 하기")
    public void getDataLibraryGroups() throws Exception {
        given(dataLibraryGroupService.getListDataLibraryGroup(any())).willReturn(getDataLibraryGroupList());

        this.mockMvc.perform(get("/api/data-library-groups"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.dataLibraryGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-library-group-list"));
    }

    @Test
    @DisplayName("데이터 라이브러리 그룹 depth 로 조회")
    public void getDataLibraryGroupsByDepth() throws Exception {
        given(dataLibraryGroupService.getDataLibraryGroupListByDepth(any())).willReturn(getDataLibraryGroupList());

        this.mockMvc.perform(get("/api/data-library-groups/depth/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.dataLibraryGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-library-group-list-by-depth"));
    }

    @Test
    @DisplayName("데이터 라이브러리 그룹 parent 로 조회 하기")
    public void getDataLibraryGroupsByParent() throws Exception {
        given(dataLibraryGroupService.getListDataLibraryGroup(any())).willReturn(getDataLibraryGroupList());

        this.mockMvc.perform(get("/api/data-library-groups/parent/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.dataLibraryGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-library-group-list-by-parent"));
    }

    @Test
    @DisplayName("데이터 라이브러리 그룹 단일 조회 하기")
    public void getDataLibraryGroup() throws Exception {
        DataLibraryGroup mock = getDataLibraryGroupById();
        given(dataLibraryGroupService.getDataLibraryGroup(any())).willReturn(mock);

        this.mockMvc.perform(get("/api/data-library-groups/{id}", mock.getDataLibraryGroupId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-library-group-get",
                        relaxedResponseFields(
                                fieldWithPath("dataLibraryGroupId").description("data library 그룹 고유번호"),
                                fieldWithPath("dataLibraryGroupKey").description("링크 활용 등을 위한 확장 컬럼"),
                                fieldWithPath("dataLibraryGroupName").description("data library 그룹 그룹명"),
                                fieldWithPath("dataLibraryGroupPath").description("서비스 경로"),
                                fieldWithPath("dataLibraryGroupTarget").description("admin : 관리자용 data library 그룹, user : 일반 사용자용 data library 그룹"),
                                fieldWithPath("sharing").description("common : 공통, public : 공개, private : 비공개, group : 그룹"),
                                fieldWithPath("userId").description("사용자 아이디"),
                                fieldWithPath("ancestor").description("조상 고유번호"),
                                fieldWithPath("parent").description("부모 고유번호"),
                                fieldWithPath("depth").description("깊이"),
                                fieldWithPath("viewOrder").description("나열 순서"),
                                fieldWithPath("children").description("자식 존재 유무"),
                                fieldWithPath("basic").description("true : 기본(초기 등록), false : 선택"),
                                fieldWithPath("available").description("사용유무, true : 사용, false : 사용안함"),
                                fieldWithPath("dataLibraryCount").description("데이터 라이브러리 총 건수"),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("updateDate").description("수정일"),
                                fieldWithPath("insertDate").description("등록일")
                        )
                ));
    }

    private List<DataLibraryGroup> getDataLibraryGroupList() {
        List<DataLibraryGroup> mockList = new ArrayList<>();
        IntStream.range(1, 4).forEach(i -> {
            mockList.add(DataLibraryGroup.builder()
                    .dataLibraryGroupId(i)
                    .dataLibraryGroupKey("test"+i)
                    .dataLibraryGroupName("test")
                    .dataLibraryGroupPath("/")
                    .dataLibraryGroupTarget("target")
                    .sharing("public")
                    .userId("admin")
                    .dataLibraryCount(1)
                    .ancestor(0)
                    .parent(0)
                    .depth(0)
                    .viewOrder(1)
                    .children(1)
                    .basic(true)
                    .available(true)
                    .description("test")
                    .updateDate(LocalDateTime.now())
                    .insertDate(LocalDateTime.now())
                    .build());
        });
        return mockList;
    }

    private DataLibraryGroup getDataLibraryGroupById() {
        return DataLibraryGroup.builder()
                .dataLibraryGroupId(1)
                .dataLibraryGroupKey("test")
                .dataLibraryGroupName("test")
                .dataLibraryGroupPath("/")
                .dataLibraryGroupTarget("target")
                .sharing("public")
                .userId("admin")
                .dataLibraryCount(1)
                .ancestor(0)
                .parent(0)
                .depth(0)
                .viewOrder(1)
                .children(1)
                .basic(true)
                .available(true)
                .description("test")
                .updateDate(LocalDateTime.now())
                .insertDate(LocalDateTime.now())
                .build();
    }

}