package io.openindoormap.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.openindoormap.api.GeometryAPIController;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.common.GeometryInfo;
import io.openindoormap.domain.common.SpatialOperationInfo;
import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.domain.extrusionmodel.DesignLayerBuildingDto;
import io.openindoormap.domain.extrusionmodel.DesignLayerLandDto;
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
    @DisplayName("디자인 레이어 필지 intersection point(한점)")
    void getDesignLayerLandsByPoint() throws Exception {
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(127.0018109, 37.4440647));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .type("land")
                .buffer(0.002f)
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();

        given(geometryService.getIntersectionDesignLayerLands(any())).willReturn(getDesignLayerLandsMock());

        this.mockMvc.perform(post("/api/geometry/intersection/design-layers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("_embedded.designLayerLands[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("geometry-intersection-design-layer-land-point"));
    }


    @Test
    @DisplayName("디자인 레이어 필지 intersection lineString(처음 위치와 마지막 위치가 같지 않을 경우)")
    void getDesignLayerLandsByLineString() throws Exception {
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(126.997892834868054, 37.445044223911694));
        geometry.add(new GeometryInfo(126.999715202064294, 37.442834603686244));
        geometry.add(new GeometryInfo(127.000876961151903, 37.442379011887184));
        geometry.add(new GeometryInfo(127.002152618189271, 37.44215121598765));
        geometry.add(new GeometryInfo(127.002448752858669, 37.444019142363807));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .type("land")
                .buffer(0.002f)
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();

        given(geometryService.getIntersectionDesignLayerLands(any())).willReturn(getDesignLayerLandsMock());

        this.mockMvc.perform(post("/api/geometry/intersection/design-layers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("_embedded.designLayerLands[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("geometry-intersection-design-layer-land-linestring"));
    }

    @Test
    @DisplayName("디자인 레이어 필지 intersection polygon(처음 위치와 마지막 위치가 같은 경우)")
    void getDesignLayerLandsByPolygon() throws Exception {
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(126.998530663386731, 37.445613713660521));
        geometry.add(new GeometryInfo(126.997414463479032, 37.446342660539017));
        geometry.add(new GeometryInfo(126.99957852452458, 37.442219554757514));
        geometry.add(new GeometryInfo(127.000990859101663, 37.441946199678078));
        geometry.add(new GeometryInfo(127.003633291536218, 37.446843811517979));
        geometry.add(new GeometryInfo(127.003633291536218, 37.446843811517979));
        geometry.add(new GeometryInfo(126.998530663386731, 37.445613713660521));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .type("land")
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();

        given(geometryService.getIntersectionDesignLayerLands(any())).willReturn(getDesignLayerLandsMock());

        this.mockMvc.perform(post("/api/geometry/intersection/design-layers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("_embedded.designLayerLands[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("geometry-intersection-design-layer-land-polygon"));

    }

    @Test
    @DisplayName("buffer 입력범위 초과 테스트")
    void bufferValidTest() throws Exception {
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(127.0018109, 37.4440647));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .type("land")
                .buffer(10f)
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();
        this.mockMvc.perform(post("/api/geometry/intersection/design-layers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errorCode").exists())
                .andExpect(jsonPath("statusCode").exists())
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("디자인 레이어 빌딩 intersection point")
    void getDesignLayerBuildingsByPoint() throws Exception {
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(127.0018109, 37.4440647));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .type("building")
                .buffer(0.002f)
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();

        given(geometryService.getIntersectionDesignLayerBuildings(any())).willReturn(getDesignLayerBuildingsMock());

        this.mockMvc.perform(post("/api/geometry/intersection/design-layers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("_embedded.designLayerLands[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("geometry-intersection-design-layer-building-point"));
    }

    @Test
    @DisplayName("디자인 레이어 빌딩 intersection lineString")
    void getDesignLayerBuildingsByLineString() throws Exception{
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(126.997892834868054, 37.445044223911694));
        geometry.add(new GeometryInfo(126.999715202064294, 37.442834603686244));
        geometry.add(new GeometryInfo(127.000876961151903, 37.442379011887184));
        geometry.add(new GeometryInfo(127.002152618189271, 37.44215121598765));
        geometry.add(new GeometryInfo(127.002448752858669, 37.444019142363807));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .type("building")
                .buffer(0.002f)
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();

        given(geometryService.getIntersectionDesignLayerBuildings(any())).willReturn(getDesignLayerBuildingsMock());

        this.mockMvc.perform(post("/api/geometry/intersection/design-layers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("_embedded.designLayerLands[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("geometry-intersection-design-layer-building-linestring"));
    }

    @Test
    @DisplayName("디자인 레이어 빌딩 intersection polygon")
    void getDesignLayerBuildingsByPolygon() throws Exception{
        List<GeometryInfo> geometry = new ArrayList<>();
        geometry.add(new GeometryInfo(126.998530663386731, 37.445613713660521));
        geometry.add(new GeometryInfo(126.997414463479032, 37.446342660539017));
        geometry.add(new GeometryInfo(126.99957852452458, 37.442219554757514));
        geometry.add(new GeometryInfo(127.000990859101663, 37.441946199678078));
        geometry.add(new GeometryInfo(127.003633291536218, 37.446843811517979));
        geometry.add(new GeometryInfo(127.003633291536218, 37.446843811517979));
        geometry.add(new GeometryInfo(126.998530663386731, 37.445613713660521));

        SpatialOperationInfo info = SpatialOperationInfo.builder()
                .type("building")
                .buffer(0.002f)
                .geometryInfo(geometry)
                .maxFeatures(3)
                .build();

        given(geometryService.getIntersectionDesignLayerBuildings(any())).willReturn(getDesignLayerBuildingsMock());

        this.mockMvc.perform(post("/api/geometry/intersection/design-layers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("_embedded.designLayerLands[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("geometry-intersection-design-layer-building-polygon"));
    }

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

    private List<DesignLayerLandDto> getDesignLayerLandsMock() {
        List<DesignLayerLandDto> mockList = Arrays.asList(
                DesignLayerLandDto.builder()
                        .designLayerLandId(18292L)
                        .designLayerId(60L)
                        .designLayerGroupId(3)
                        .identificationCode(109L)
                        .insertDate(LocalDateTime.now())
                        .theGeom("MULTIPOLYGON(((127.00132442755 37.4457155424487,127.000666244946 37.4447691897486,127.000601065241 37.4447542969596,126.999894295752 37.4450667729373,126.99987561642 37.4451187414168,127.000541542411 37.4460762420315,127.00060672325 37.44609113485,127.001299522872 37.4457848340477,127.00132442755 37.4457155424487)))")
                        .build(),
                DesignLayerLandDto.builder()
                        .designLayerLandId(18293L)
                        .designLayerId(60L)
                        .designLayerGroupId(3)
                        .identificationCode(110L)
                        .insertDate(LocalDateTime.now())
                        .theGeom("MULTIPOLYGON(((127.000543021486 37.4445920126611,126.999884858563 37.4436456554291,126.999797953802 37.4436257977909,126.999105167966 37.4439320899057,126.999086488371 37.4439840582705,126.999752394251 37.4449415635157,126.999817573949 37.4449564567722,127.00052434272 37.4446439812474,127.000543021486 37.4445920126611)))")
                        .build(),
                DesignLayerLandDto.builder()
                        .designLayerLandId(18294L)
                        .designLayerId(60L)
                        .designLayerGroupId(3)
                        .identificationCode(111L)
                        .insertDate(LocalDateTime.now())
                        .theGeom("MULTIPOLYGON(((127.000497252992 37.4427190680493,127.000497252992 37.4427190680493,127.00041167056 37.4426185274772,127.000318403541 37.4425224304588,127.000217813841 37.4424311498533,127.000110291776 37.4423450398307,126.999996254556 37.442264434498,126.999876144661 37.4421896466029,126.999750428134 37.4421209663206,126.999619592764 37.4420586601282,126.999484146198 37.4420029697711,126.999484146198 37.4420029697711,126.998828226844 37.4417535821301,126.998743124912 37.4417778974539,126.998656466606 37.4419255593528,126.99854342765 37.4421086557589,126.998399982863 37.4423467957618,126.998310261423 37.442432091198,126.998292569477 37.4424616861172,126.999443098993 37.4429629384062,126.999450374574 37.4429555041322,126.99945827253 37.4429484852467,126.999466755662 37.4429419148073,126.999475784015 37.4429358237598,126.999485315068 37.4429302407917,126.999495303931 37.442925192198,126.999505703558 37.4429207017564,126.999516464969 37.4429167906163,126.99952753748 37.4429134771984,126.99953886894 37.4429107771082,126.999550405982 37.4429087030626,126.999562094267 37.4429072648302,126.999573878745 37.4429064691845,126.999585703915 37.4429063198732,126.999597514082 37.4429068175991,126.999609253622 37.4429079600184,126.999620867244 37.4429097417503,126.99963230025 37.4429121544032,126.999643498792 37.4429151866139,126.999654410128 37.4429188241015,126.999664982867 37.4429230497338,126.999675167214 37.4429278436089,126.999684915202 37.4429331831487,126.999694180919 37.4429390432048,126.999702920725 37.4429453961774,126.999711093459 37.4429522121451,126.999718660626 37.442959459006,126.999725586588 37.4429671026286,126.999731838723 37.4429751070128,126.999737387585 37.4429834344593,126.99974220704 37.4429920457473,126.999746274389 37.4430009003191,126.999749570475 37.4430099564712,126.999752079773 37.4430191715505,126.999753790464 37.4430285021556,126.999754694493 37.443037904341,126.999754787599 37.4430473338237,126.999754787599 37.4430473338237,127.000497252992 37.4427190680493)))")
                        .build()
        );
        return mockList;
    }

    private List<DesignLayerBuildingDto> getDesignLayerBuildingsMock() {
        List<DesignLayerBuildingDto> mockList = Arrays.asList(
                DesignLayerBuildingDto.builder()
                    .buildHeight("5")
                    .buildFloor("3")
                    .buildArea("100")
                    .buildComplex("true")
                    .parentId("1")
                    .buildUnitType("84D")
                    .buildUnitCount(3)
                    .insertDate(LocalDateTime.now())
                    .theGeom("MULTIPOLYGON(((127.00132442755 37.4457155424487,127.000666244946 37.4447691897486,127.000601065241 37.4447542969596,126.999894295752 37.4450667729373,126.99987561642 37.4451187414168,127.000541542411 37.4460762420315,127.00060672325 37.44609113485,127.001299522872 37.4457848340477,127.00132442755 37.4457155424487)))")
                    .build(),
                DesignLayerBuildingDto.builder()
                        .buildHeight("5")
                        .buildFloor("3")
                        .buildArea("100")
                        .buildComplex("true")
                        .parentId("1")
                        .buildUnitType("84A")
                        .buildUnitCount(3)
                        .insertDate(LocalDateTime.now())
                        .theGeom("MULTIPOLYGON(((127.000543021486 37.4445920126611,126.999884858563 37.4436456554291,126.999797953802 37.4436257977909,126.999105167966 37.4439320899057,126.999086488371 37.4439840582705,126.999752394251 37.4449415635157,126.999817573949 37.4449564567722,127.00052434272 37.4446439812474,127.000543021486 37.4445920126611)))")
                        .build(),
                DesignLayerBuildingDto.builder()
                        .buildHeight("10")
                        .buildFloor("3")
                        .buildArea("200")
                        .buildComplex("true")
                        .parentId("1")
                        .buildUnitType("84B")
                        .buildUnitCount(3)
                        .insertDate(LocalDateTime.now())
                        .theGeom("MULTIPOLYGON(((127.000497252992 37.4427190680493,127.000497252992 37.4427190680493,127.00041167056 37.4426185274772,127.000318403541 37.4425224304588,127.000217813841 37.4424311498533,127.000110291776 37.4423450398307,126.999996254556 37.442264434498,126.999876144661 37.4421896466029,126.999750428134 37.4421209663206,126.999619592764 37.4420586601282,126.999484146198 37.4420029697711,126.999484146198 37.4420029697711,126.998828226844 37.4417535821301,126.998743124912 37.4417778974539,126.998656466606 37.4419255593528,126.99854342765 37.4421086557589,126.998399982863 37.4423467957618,126.998310261423 37.442432091198,126.998292569477 37.4424616861172,126.999443098993 37.4429629384062,126.999450374574 37.4429555041322,126.99945827253 37.4429484852467,126.999466755662 37.4429419148073,126.999475784015 37.4429358237598,126.999485315068 37.4429302407917,126.999495303931 37.442925192198,126.999505703558 37.4429207017564,126.999516464969 37.4429167906163,126.99952753748 37.4429134771984,126.99953886894 37.4429107771082,126.999550405982 37.4429087030626,126.999562094267 37.4429072648302,126.999573878745 37.4429064691845,126.999585703915 37.4429063198732,126.999597514082 37.4429068175991,126.999609253622 37.4429079600184,126.999620867244 37.4429097417503,126.99963230025 37.4429121544032,126.999643498792 37.4429151866139,126.999654410128 37.4429188241015,126.999664982867 37.4429230497338,126.999675167214 37.4429278436089,126.999684915202 37.4429331831487,126.999694180919 37.4429390432048,126.999702920725 37.4429453961774,126.999711093459 37.4429522121451,126.999718660626 37.442959459006,126.999725586588 37.4429671026286,126.999731838723 37.4429751070128,126.999737387585 37.4429834344593,126.99974220704 37.4429920457473,126.999746274389 37.4430009003191,126.999749570475 37.4430099564712,126.999752079773 37.4430191715505,126.999753790464 37.4430285021556,126.999754694493 37.443037904341,126.999754787599 37.4430473338237,126.999754787599 37.4430473338237,127.000497252992 37.4427190680493)))")
                        .build()
        );
        return mockList;
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