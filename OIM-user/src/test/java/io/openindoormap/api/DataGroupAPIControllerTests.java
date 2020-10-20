package io.openindoormap.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.data.DataGroup;
import io.openindoormap.service.DataGroupService;
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

@WebMvcTest(DataGroupAPIController.class)
class DataGroupAPIControllerTests extends BaseControllerTest {

    @MockBean
    private DataGroupService dataGroupService;

    @Test
    @DisplayName("DataGroup 목록 조회 하기")
    public void getDataGroups() throws Exception {
        given(dataGroupService.getListDataGroup(any())).willReturn(getDataGroupList());
        
        this.mockMvc.perform(get("/api/data-groups"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-group-list"));
    }

    @Test
    @DisplayName("데이터 그룹 parent 로 조회 하기")
    public void getDataGroupsByParent() throws Exception {
        given(dataGroupService.getListDataGroup(any())).willReturn(getDataGroupList());

        this.mockMvc.perform(get("/api/data-groups/parent/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-group-list-by-parent"));
    }

    @Test
    @DisplayName("DataGroup 단일 조회 하기")
    public void getDataGroup() throws Exception {
    	DataGroup mock = getDataGroupById();
        given(dataGroupService.getDataGroup(any())).willReturn(mock);

        this.mockMvc.perform(get("/api/data-groups/{id}", mock.getDataGroupId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("dataGroupId").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-group-get",
                        relaxedResponseFields(
                                fieldWithPath("dataGroupId").description("고유번호"),
                                fieldWithPath("dataGroupKey").description("링크 활용 등을 위한 확장 컬럼"),
                                fieldWithPath("dataGroupName").description("그룹명"),
                                fieldWithPath("dataGroupPath").description("서비스 경로"),
                                fieldWithPath("dataGroupTarget").description("admin : 관리자용 데이터 그룹, user : 일반 사용자용 데이터 그룹"),
                                fieldWithPath("sharing").description("공유 타입. common : 공통, public : 공개, private : 개인, group : 그룹"),
                                fieldWithPath("userId").description("사용자명"),
                                fieldWithPath("ancestor").description("조상"),
                                fieldWithPath("parent").description("부모"),
                                fieldWithPath("depth").description("깊이"),
                                fieldWithPath("viewOrder").description("순서"),
                                fieldWithPath("children").description("자식 존재 유무"),
                                fieldWithPath("basic").description("true : 기본, false : 선택"),
                                fieldWithPath("available").description("true : 사용, false : 사용안함"),
                                fieldWithPath("tiling").description("스마트 타일링 사용유무. true : 사용, false : 사용안함(기본)"),
                                fieldWithPath("dataCount").description("데이터 총 건수"),
                                fieldWithPath("location").description("POINT(위도, 경도). 공간 검색 속도 때문에 altitude는 분리"),
                                fieldWithPath("locationUpdateType").description("location 업데이트 방법. auto : data 입력시 자동, user : 사용자가 직접 입력"),
                                fieldWithPath("metainfo").description("데이터 그룹 메타 정보. 그룹 control을 위해 인위적으로 만든 속성"),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("updateDate").description("수정일"),
                                fieldWithPath("insertDate").description("등록일")
                        )
                ));
    }

    private List<DataGroup> getDataGroupList() {
        List<DataGroup> mockList = new ArrayList<>();
        IntStream.range(1, 4).forEach(i -> {
            mockList.add(DataGroup.builder()
            		.dataGroupId(1)
                    .dataGroupKey("groupKey")
                    .dataGroupName("basic")
                    .dataGroupPath("path")
                    .dataGroupTarget("admin")
                    .sharing("common")   
                    .userId("admin")
                    .ancestor(1)
                    .parent(1)
                    .depth(1)
                    .viewOrder(1)
                    .children(1)
                    .basic(true)
                    .available(true)
                    .tiling(true)
                    .dataCount(0)
                    .location("POINT(위도, 경도)")
                    .locationUpdateType("auto")
                    .metainfo("데이터 그룹 메타 정보")
                    .description("설명")
                    .build());
        });
        return mockList;
    }

    private DataGroup getDataGroupById() {
        return DataGroup.builder()
                .dataGroupId(1)
                .dataGroupKey("groupKey")
                .dataGroupName("basic")
                .dataGroupPath("path")
                .dataGroupTarget("admin")
                .sharing("common")   
                .userId("admin")
                .ancestor(1)
                .parent(1)
                .depth(1)
                .viewOrder(1)
                .children(1)
                .basic(true)
                .available(true)
                .tiling(true)
                .dataCount(0)
                .location("POINT(위도, 경도)")
                .locationUpdateType("auto")
                .metainfo("데이터 그룹 메타 정보")
                .description("설명")
                .build();
    }
    
 
}