package io.openindoormap.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.domain.layer.Layer;
import io.openindoormap.service.DataService;

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@WebMvcTest(DataAPIController.class)
class DataAPIControllerTests extends BaseControllerTest {

    @MockBean
    private DataService dataService;

    @Test
    @DisplayName("데이터 목록 조회 하기")
    public void getDatas() throws Exception {
        // given
        given(dataService.getListData(any())).willReturn(getDataList());
        
        

        this.mockMvc.perform(get("/api/datas")
                .param("dataGroupId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-list", requestParameters(parameterWithName("dataGroupId").description("데이터 그룹 고유번호"))));
    }
    
    
    @Test
    @DisplayName("데이터 상세 정보 조회 하기")
    public void getDataById() throws Exception {
        // given
    	DataInfo mock = getdataById();
        given(dataService.getData(any())).willReturn(mock);

        this.mockMvc.perform(get("/api/datas/{id}", mock.getDataId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("dataId").exists())
        .andExpect(jsonPath("_links.self").exists())
        .andExpect(jsonPath("_links.profile").exists())
        .andDo(document("data-get",
                /**
                 * relaxedResponseFields 를 쓰면 모든 필드를 기술할 필요가 없다.
                 * 하지만 모든 필드를 기술하지 않으므로 정확한 문서를 만들지 못한다.
                 * responseFields를 쓰면 모든 필드를 기술해야 한다.
                 */
                relaxedResponseFields(
                        fieldWithPath("userId").description("사용자 고유번호"),
                        fieldWithPath("updateUserId").description("수정자 아이디"),
                        fieldWithPath("dataId").description("고유번호"),
                        fieldWithPath("dataGroupId").description("Data Group 고유번호"),
                        fieldWithPath("converterJobId").description("converter job 고유번호"),
                        fieldWithPath("dataGroupName").description("Data Group 이름"),
                        fieldWithPath("dataGroupTarget").description("admin : 관리자용 데이터 그룹, user : 일반 사용자용 데이터 그룹"),
                        fieldWithPath("dataGroupKey").description("data group key"),
                        fieldWithPath("tiling").description("smart"),
                        fieldWithPath("dataKey").description("data 고유 식별번호"),
                        fieldWithPath("oldDataKey").description("data 고유 식별번호"),
                        fieldWithPath("dataName").description("data 이름"),
                        fieldWithPath("dataType").description("데이터 타입(중복). 3ds,obj,dae,collada,ifc,las,citygml,indoorgml,etc"),
                        fieldWithPath("sharing").description("common : 공통, public : 공개, private : 개인, group : 그룹"),
                        fieldWithPath("parent").description("부모 고유번호"),
                        fieldWithPath("parentName").description("부모 이름(화면 표시용)"),
                        fieldWithPath("mappingType").description("origin : latitude, longitude, height 를 origin에 맟춤. boundingboxcenter : latitude, longitude, height 를 boundingboxcenter에 맟춤."),
                        fieldWithPath("location").description("POINT(위도, 경도). 공간 검색 속도 때문에 altitude는 분리"),
                        fieldWithPath("altitude").description("높이"),
                        fieldWithPath("heading").description("heading"),
                        fieldWithPath("pitch").description("pitch"),
                        fieldWithPath("roll").description("roll"),
                        fieldWithPath("childrenAncestor").description("조상"),
                        fieldWithPath("childrenParent").description("부모"),
                        fieldWithPath("childrenDepth").description("깊이"),
                        fieldWithPath("childrenViewOrder").description("순서"),
                        fieldWithPath("metainfo").description("기본정보"),
                        fieldWithPath("status").description("data 상태. processing : 변환중, use : 사용중, unused : 사용중지(관리자), delete : 삭제(비표시)"),
                        fieldWithPath("attributeExist").description("속성 존재 유무. true : 존재, false : 존재하지 않음(기본값)"),
                        fieldWithPath("objectAttributeExist").description("object 속성 존재 유무. true : 존재, false : 존재하지 않음(기본값)"),
                        fieldWithPath("description").description("설명"),
                        fieldWithPath("updateDate").description("수정일"),
                        fieldWithPath("insertDate").description("등록일")
                )
        ));
    }

    
    private List<DataInfo> getDataList() {
        List<DataInfo> mockList = new ArrayList<>();
        IntStream.range(1, 4).forEach(i -> {
            mockList.add(DataInfo.builder()
            		.userId("admin")
                    .updateUserId("admin")
                    .dataId((Long) 1L)
                    .dataGroupId((int) 1L)
                    .converterJobId((Long) 1L)
                    .dataGroupName("basic")
                    .dataGroupTarget("admin")                
                    .dataGroupKey("test")
                    .tiling(true)
                    .dataKey("test")
                    .oldDataKey("test")
                    .dataName("test-data")                
                    .sharing("common")
                    .parent((Long) 1L)
                    .parentName("test-parent")
                    .mappingType("origin")
                    .location("POINT(위도, 경도)")
                    .childrenAncestor((int) 1L)
                    .childrenParent((int) 1L)
                    .childrenDepth((int) 1L)                
                    .childrenViewOrder((int) 1L)
                    .metainfo("test-info")
                    .status("processing")
                    .attributeExist(true)
                    .objectAttributeExist(true)
                    .description("test-description")  
                    .build());
        });
        return mockList;
    }
    
	
    private DataInfo getdataById() {
        return DataInfo.builder()
                .userId("admin")
                .updateUserId("admin")
                .dataId((Long) 1L)
                .dataGroupId((int) 1L)
                .converterJobId((Long) 1L)
                .dataGroupName("basic")
                .dataGroupTarget("admin")                
                .dataGroupKey("test")
                .tiling(true)
                .dataKey("test")
                .oldDataKey("test")
                .dataName("test-data")                
                .sharing("common")
                .parent((Long) 1L)
                .parentName("test-parent")
                .mappingType("origin")
                .location("POINT(위도, 경도)")
                .childrenAncestor((int) 1L)
                .childrenParent((int) 1L)
                .childrenDepth((int) 1L)                
                .childrenViewOrder((int) 1L)
                .metainfo("test-info")
                .status("processing")
                .attributeExist(true)
                .objectAttributeExist(true)
                .description("test-description")  
                .build();
    }
    
}