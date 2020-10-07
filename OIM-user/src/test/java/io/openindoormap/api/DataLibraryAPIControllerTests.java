package io.openindoormap.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;

import io.openindoormap.api.DataLibraryAPIController;
import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.extrusionmodel.DataLibrary;
import io.openindoormap.service.DataLibraryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

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

@WebMvcTest(DataLibraryAPIController.class)
class DataLibraryAPIControllerTests extends BaseControllerTest {

    @MockBean
    private DataLibraryService dataLibraryService;

    @Test
    @DisplayName("데이터 라이브러리 목록 조회 하기")
    public void getDataLibraries() throws Exception {
        // given
        given(dataLibraryService.getListDataLibrary(any())).willReturn(getDataLibraryList());

        this.mockMvc.perform(get("/api/data-libraries")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .param("dataLibraryGroupId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.dataLibraries[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-library-list",
                        requestParameters(parameterWithName("dataLibraryGroupId").description("데이터 라이브러리 그룹 고유번호"))
                ));
    }

    @Test
    @DisplayName("데이터 라이브러리 단일 조회 하기")
    public void getDataLibrary() throws Exception {
        DataLibrary mock = getDataLibraryById();
        given(dataLibraryService.getDataLibrary(any())).willReturn(mock);

        this.mockMvc.perform(get("/api/data-libraries/{id}", mock.getDataLibraryId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("data-library-get",
                        relaxedResponseFields(
                                fieldWithPath("dataLibraryId").description("data library 고유번호"),
                                fieldWithPath("dataLibraryGroupId").description("data library 그룹 고유번호"),
                                fieldWithPath("dataLibraryConverterJobId").description("converter job 고유번호"),
                                fieldWithPath("dataLibraryKey").description("data library 고유키(API용)"),
                                fieldWithPath("dataLibraryName").description("data library명"),
                                fieldWithPath("dataLibraryThumbnail").description("data library 썸네일"),
                                fieldWithPath("dataLibraryPath").description("data library 경로"),
                                fieldWithPath("dataType").description("데이터 타입(중복). 3ds,obj,dae,collada,ifc,las,citygml,indoorgml,etc"),
                                fieldWithPath("userId").description("사용자명"),
                                fieldWithPath("serviceType").description("서비스 타입 (정적, 동적)"),
                                fieldWithPath("viewOrder").description("나열 순서"),
                                fieldWithPath("available").description("사용유무"),
                                fieldWithPath("status").description("상태. processing : 변환중, use : 사용중, unused : 사용중지(관리자), delete : 삭제(비표시)"),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("updateDate").description("수정일"),
                                fieldWithPath("insertDate").description("등록일")
                        )
                ));
    }

    private List<DataLibrary> getDataLibraryList() {
        List<DataLibrary> mockList = new ArrayList<>();
        LongStream.range(1, 4).forEach(i -> {
            mockList.add(DataLibrary.builder()
                    .dataLibraryId(i)
                    .dataLibraryGroupId(1)
                    .dataLibraryConverterJobId(1L)
                    .dataLibraryKey("test"+i)
                    .dataLibraryName("test")
                    .dataLibraryPath("/")
                    .dataType("3ds")
                    .userId("admin")
                    .serviceType("정적")
                    .viewOrder(1)
                    .available(true)
                    .status("use")
                    .description("test")
                    .updateDate(LocalDateTime.now())
                    .insertDate(LocalDateTime.now())
                    .build());
        });
        return mockList;
    }

    private DataLibrary getDataLibraryById() {
        return DataLibrary.builder()
                .dataLibraryId(1L)
                .dataLibraryGroupId(1)
                .dataLibraryConverterJobId(1L)
                .dataLibraryKey("test")
                .dataLibraryName("test")
                .dataLibraryPath("/")
                .dataLibraryThumbnail("/path/thumbnail.png")
                .dataType("3ds")
                .userId("admin")
                .serviceType("정적")
                .viewOrder(1)
                .available(true)
                .status("use")
                .description("test")
                .updateDate(LocalDateTime.now())
                .insertDate(LocalDateTime.now())
                .build();
    }

}