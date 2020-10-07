package io.openindoormap.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.openindoormap.domain.extrusionmodel.DataLibrary;
import io.openindoormap.domain.extrusionmodel.DataLibraryDto;
import io.openindoormap.service.DataLibraryService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/data-libraries", produces = MediaTypes.HAL_JSON_VALUE)
public class DataLibraryAPIController {

    private final DataLibraryService dataLibraryService;
    private final ModelMapper modelMapper;

    /**
     * 데이터 라이브러리 목록 조회
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DataLibraryDto>>> getDataLibraries(@RequestParam(defaultValue = "0") Integer dataLibraryGroupId) {
        List<DataLibrary> dataLibraryList = dataLibraryService.getListDataLibrary(DataLibrary.builder().dataLibraryGroupId(dataLibraryGroupId).build());
        List<EntityModel<DataLibraryDto>> dataLibraryDtoList = dataLibraryList.stream()
                .map(f -> EntityModel.of(modelMapper.map(f, DataLibraryDto.class))
                        .add(linkTo(DesignLayerAPIController.class).slash(f.getDataLibraryId()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<DataLibraryDto>> model = CollectionModel.of(dataLibraryDtoList);

        model.add(linkTo(DataLibraryAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-data-library-list").withRel("profile"));

        return ResponseEntity.ok(model);
    }

    /**
     * 데이터 라이브러리 한건 조회
     *
     * @param id 데이터 라이브러리 아이디
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DataLibraryDto>> getDataLibraryById(@PathVariable("id") Long id) {
        DataLibraryDto dto = modelMapper.map(dataLibraryService.getDataLibrary(DataLibrary.builder().dataLibraryId(id).build()), DataLibraryDto.class);
        EntityModel<DataLibraryDto> dataLibrary = EntityModel.of(dto);
        dataLibrary.add(linkTo(DataLibraryAPIController.class).slash(id).withSelfRel());
        dataLibrary.add(Link.of("/docs/index.html#resources-data-library-get").withRel("profile"));

        return ResponseEntity.ok(dataLibrary);
    }
}
