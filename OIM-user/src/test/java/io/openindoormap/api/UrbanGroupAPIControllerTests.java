package io.openindoormap.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.openindoormap.api.UrbanGroupAPIController;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.urban.UrbanGroup;
import io.openindoormap.service.UrbanGroupService;

import java.math.BigDecimal;
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

@WebMvcTest(UrbanGroupAPIController.class)
class UrbanGroupAPIControllerTests extends BaseControllerTest {

    @MockBean
    private UrbanGroupService urbanGroupService;

    @Test
    @DisplayName("도시 그룹 목록 조회 하기")
    public void getUrbanGroups() throws Exception {
        given(urbanGroupService.getListUrbanGroup(any())).willReturn(getUrbanGroupList());

        this.mockMvc.perform(get("/api/urban-groups"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.urbanGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("urban-group-list"));
    }

    @Test
    @DisplayName("도시 그룹 depth 로 조회")
    public void getUrbanGroupsByDepth() throws Exception {
        given(urbanGroupService.getListUrbanGroupByDepth(any())).willReturn(getUrbanGroupList());

        this.mockMvc.perform(get("/api/urban-groups/depth/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.urbanGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("urban-group-list-by-depth"));
    }

    @Test
    @DisplayName("도시 그룹 parent 로 조회 하기")
    public void getUrbanGroupsByParent() throws Exception {
        given(urbanGroupService.getListUrbanGroup(any())).willReturn(getUrbanGroupList());

        this.mockMvc.perform(get("/api/urban-groups/parent/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.urbanGroups[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("urban-group-list-by-parent"));
    }

    @Test
    @DisplayName("도시 그룹 단일 조회 하기")
    public void getUrbanGroup() throws Exception {
        UrbanGroup mock = getUrbanGroupById();
        given(urbanGroupService.getUrbanGroup(any())).willReturn(getUrbanGroupById());

        this.mockMvc.perform(get("/api/urban-groups/{id}", mock.getUrbanGroupId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("urban-group-get",
                        relaxedResponseFields(
                                fieldWithPath("urbanGroupId").description("고유 번호"),
                                fieldWithPath("urbanGroupKey").description("링크 활용 등을 위한 확장 컬럼"),
                                fieldWithPath("urbanGroupName").description("그룹명"),
                                fieldWithPath("userId").description("사용자 아이디"),
                                fieldWithPath("ancestor").description("조상 고유번호"),
                                fieldWithPath("parent").description("부모 고유번호"),
                                fieldWithPath("depth").description("깊이"),
                                fieldWithPath("viewOrder").description("나열 순서"),
                                fieldWithPath("children").description("자식 존재 유무"),
                                fieldWithPath("basic").description("true : 기본(초기 등록), false : 선택"),
                                fieldWithPath("available").description("사용유무, true : 사용, false : 사용안함"),
                                fieldWithPath("startDate").description("시작일"),
                                fieldWithPath("endDate").description("종료일"),
                                fieldWithPath("location").description("POINT(위도, 경도)"),
                                fieldWithPath("area").description("면적"),
                                fieldWithPath("receivingPopulation").description("수용 인구"),
                                fieldWithPath("receivingHousehold").description("수용 세대"),
                                fieldWithPath("projectOperator").description("사업 시행자"),
                                fieldWithPath("transferLocalGovernment").description("지자체로 양도 시기"),
                                fieldWithPath("latitude").description("위도"),
                                fieldWithPath("longitude").description("경도"),
                                fieldWithPath("altitude").description("높이"),
                                fieldWithPath("duration").description("이동 시간"),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("updateDate").description("수정일"),
                                fieldWithPath("insertDate").description("등록일")
                        )
                ));
    }

    private List<UrbanGroup> getUrbanGroupList() {
        List<UrbanGroup> mockList = new ArrayList<>();
        IntStream.range(1, 4).forEach(i -> {
            mockList.add(UrbanGroup.builder()
                    .urbanGroupId(i)
                    .urbanGroupKey("test"+i)
                    .urbanGroupName("test")
                    .userId("admin")
                    .ancestor(1)
                    .parent(0)
                    .depth(0)
                    .viewOrder(1)
                    .children(1)
                    .basic(true)
                    .available(true)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now())
                    .location("POINT(127.262219 36.497006)")
                    .area(0)
                    .receivingPopulation(0)
                    .receivingHousehold(0)
                    .projectOperator("test")
                    .transferLocalGovernment("test")
                    .description("test")
                    .updateDate(LocalDateTime.now())
                    .insertDate(LocalDateTime.now())
                    .build());
        });
        return mockList;
    }

    private UrbanGroup getUrbanGroupById() {
        return UrbanGroup.builder()
                .urbanGroupId(1)
                .urbanGroupKey("test")
                .urbanGroupName("test")
                .userId("admin")
                .ancestor(1)
                .parent(0)
                .depth(0)
                .viewOrder(1)
                .children(1)
                .basic(true)
                .available(true)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .longitude(BigDecimal.valueOf(36.497006))
                .latitude(BigDecimal.valueOf(127.262219))
                .location("POINT(127.262219 36.497006)")
                .altitude(BigDecimal.valueOf(3000.000000))
                .duration(3)
                .area(0)
                .receivingPopulation(0)
                .receivingHousehold(0)
                .projectOperator("test")
                .transferLocalGovernment("test")
                .description("test")
                .updateDate(LocalDateTime.now())
                .insertDate(LocalDateTime.now())
                .build();
    }
}