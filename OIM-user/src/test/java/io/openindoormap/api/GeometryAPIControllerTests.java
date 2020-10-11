package io.openindoormap.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.common.GeometryInfo;
import io.openindoormap.domain.common.SpatialOperationInfo;
import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.service.GeometryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeometryAPIController.class)
class GeometryAPIControllerTests extends BaseControllerTest {

    @MockBean
    private GeometryService geometryService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("데이터 intersection point")
    void getDatasByPoint() throws Exception {
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(127.262038223551698, 36.49698802728868));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .buffer(0.002f)
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();

        given(geometryService.getIntersectionDatas(any())).willReturn(getDatasMock());

        this.mockMvc.perform(post("/api/geometry/intersection/datas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("_embedded.designLayerLands[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("geometry-intersection-data-point"));
    }

    @Test
    @DisplayName("데이터 intersection lineString")
    void getDatasByLineString() throws Exception {
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(127.262133962040522, 36.497193516804202));
        geometry.add(new GeometryInfo(127.262154933683433, 36.497120922655668));
        geometry.add(new GeometryInfo(127.262171065716444, 36.497001545611411));
        geometry.add(new GeometryInfo(127.262364650112531, 36.496930564666179));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .buffer(0.002f)
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();

        given(geometryService.getIntersectionDatas(any())).willReturn(getDatasMock());

        this.mockMvc.perform(post("/api/geometry/intersection/datas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("_embedded.designLayerLands[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("geometry-intersection-data-linestring"));
    }

    @Test
    @DisplayName("데이터 intersection polygon")
    void getDatasByPolygon() throws Exception {
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(127.261939578507366, 36.497348461104508));
        geometry.add(new GeometryInfo(127.261994592089778, 36.497006997489514));
        geometry.add(new GeometryInfo(127.262430906708943, 36.496855235882848));
        geometry.add(new GeometryInfo(127.262550418974186, 36.497179626317092));
        geometry.add(new GeometryInfo(127.262550418974186, 36.497179626317092));
        geometry.add(new GeometryInfo(127.261939578507366, 36.497348461104508));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();

        given(geometryService.getIntersectionDatas(any())).willReturn(getDatasMock());

        this.mockMvc.perform(post("/api/geometry/intersection/datas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("_embedded.designLayerLands[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("geometry-intersection-data-polygon",
                        relaxedResponseFields(
                                fieldWithPath("_embedded.dataInfos[0].dataId").description("고유번호"),
                                fieldWithPath("_embedded.dataInfos[0].dataGroupId").description("data_group 고유번호"),
                                fieldWithPath("_embedded.dataInfos[0].converterJobId").description("converter job 고유번호"),
                                fieldWithPath("_embedded.dataInfos[0].dataKey").description("data 고유 식별번호"),
                                fieldWithPath("_embedded.dataInfos[0].dataName").description("data 이름"),
                                fieldWithPath("_embedded.dataInfos[0].dataType").description("데이터 타입(중복). 3ds,obj,dae,collada,ifc,las,citygml,indoorgml,etc"),
                                fieldWithPath("_embedded.dataInfos[0].sharing").description("common : 공통, public : 공개, private : 비공개, group : 그룹"),
                                fieldWithPath("_embedded.dataInfos[0].userId").description("사용자 아이디"),
                                fieldWithPath("_embedded.dataInfos[0].updateUserId").description("수정한 사용자 아이디"),
                                fieldWithPath("_embedded.dataInfos[0].mappingType").description("기본값 origin : latitude, longitude, height를 origin에 맞춤. boundingboxcenter : latitude, longitude, height를 boundingboxcenter 맞춤"),
                                fieldWithPath("_embedded.dataInfos[0].location").description("POINT(위도, 경도)"),
                                fieldWithPath("_embedded.dataInfos[0].altitude").description("높이"),
                                fieldWithPath("_embedded.dataInfos[0].heading").description("heading"),
                                fieldWithPath("_embedded.dataInfos[0].pitch").description("ptich"),
                                fieldWithPath("_embedded.dataInfos[0].roll").description("roll"),
                                fieldWithPath("_embedded.dataInfos[0].metainfo").description("데이터 메타 정보. 데이터  control을 위해 인위적으로 만든 속성"),
                                fieldWithPath("_embedded.dataInfos[0].status").description("상태. processing : 변환중, use : 사용중, unused : 사용중지(관리자), delete : 삭제(비표시)"),
                                fieldWithPath("_embedded.dataInfos[0].attributeExist").description("속성 존재 유무. true : 존재, false : 존재하지 않음(기본값)"),
                                fieldWithPath("_embedded.dataInfos[0].objectAttributeExist").description("Object 속성 존재 유무. true : 존재, false : 존재하지 않음(기본값)"),
                                fieldWithPath("_embedded.dataInfos[0].description").description("설명"),
                                fieldWithPath("_embedded.dataInfos[0].updateDate").description("수정일"),
                                fieldWithPath("_embedded.dataInfos[0].insertDate").description("등록일")
                        )
                ));
    }

    private List<DataInfo> getDatasMock() {
        List<DataInfo> mockList = Arrays.asList(
                DataInfo.builder()
                        .dataId(1L)
                        .dataGroupId(1)
                        .converterJobId(1L)
                        .dataKey("data1")
                        .dataName("data1")
                        .dataType("ifc")
                        .sharing("public")
                        .userId("admin")
                        .updateUserId("test")
                        .mappingType("origin")
                        .location("POINT(127.262219 36.497006)")
                        .altitude(BigDecimal.valueOf(1))
                        .heading(BigDecimal.valueOf(1))
                        .pitch(BigDecimal.valueOf(1))
                        .roll(BigDecimal.valueOf(1))
                        .metainfo("test")
                        .status("use")
                        .attributeExist(false)
                        .objectAttributeExist(true)
                        .description("test")
                        .insertDate(LocalDateTime.now())
                        .updateDate(LocalDateTime.now())
                        .build(),
                DataInfo.builder()
                        .dataId(2L)
                        .dataGroupId(2)
                        .converterJobId(2L)
                        .dataKey("data2")
                        .dataName("data2")
                        .dataType("ifc")
                        .sharing("public")
                        .userId("admin")
                        .updateUserId("test")
                        .mappingType("origin")
                        .location("POINT(127.262219 36.497006)")
                        .altitude(BigDecimal.valueOf(1))
                        .heading(BigDecimal.valueOf(1))
                        .pitch(BigDecimal.valueOf(1))
                        .roll(BigDecimal.valueOf(1))
                        .metainfo("test")
                        .status("use")
                        .attributeExist(false)
                        .objectAttributeExist(true)
                        .description("test")
                        .insertDate(LocalDateTime.now())
                        .updateDate(LocalDateTime.now())
                        .build(),
                DataInfo.builder()
                        .dataId(3L)
                        .dataGroupId(3)
                        .converterJobId(3L)
                        .dataKey("data3")
                        .dataName("data3")
                        .dataType("ifc")
                        .sharing("public")
                        .userId("admin")
                        .updateUserId("test")
                        .mappingType("origin")
                        .location("POINT(127.262219 36.497006)")
                        .altitude(BigDecimal.valueOf(1))
                        .heading(BigDecimal.valueOf(1))
                        .pitch(BigDecimal.valueOf(1))
                        .roll(BigDecimal.valueOf(1))
                        .metainfo("test")
                        .status("use")
                        .attributeExist(false)
                        .objectAttributeExist(true)
                        .description("test")
                        .insertDate(LocalDateTime.now())
                        .updateDate(LocalDateTime.now())
                        .build()
        );

        return mockList;
    }

}