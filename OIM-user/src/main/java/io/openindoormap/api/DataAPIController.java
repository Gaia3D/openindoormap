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

import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.domain.data.DataInfoDto;
import io.openindoormap.service.DataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@Api(tags = {"DataAPI"})
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/datas", produces = MediaTypes.HAL_JSON_VALUE)
public class DataAPIController {

    private final DataService dataService;
    private final ModelMapper modelMapper;

    /**
     * 데이터 목록 조회
     *
     * @return
     */
    @ApiOperation(value = "데이터 목록 조회")
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<CollectionModel<EntityModel<DataInfoDto>>> getDatas(@RequestParam(defaultValue = "0") Integer dataGroupId) {
        List<DataInfo> dataInfoList = dataService.getAllListData(DataInfo.builder().dataGroupId(dataGroupId).build());
        List<EntityModel<DataInfoDto>> dataInfoDtoList = dataInfoList.stream()
                .map(f -> EntityModel.of(modelMapper.map(f, DataInfoDto.class))
                        .add(linkTo(DataAPIController.class).slash(f.getDataId()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<DataInfoDto>> model = CollectionModel.of(dataInfoDtoList);

        model.add(linkTo(DataAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-data-info-list").withRel("profile"));

        return ResponseEntity.ok(model);
    }

    /**
     * 데이터 한건 조회
     *
     * @param id 데이터 아이디
     * @return
     */
    @ApiOperation(value = "데이터 한건 조회")
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8")
    @ApiImplicitParam(name = "id", value = "아이디")
    public ResponseEntity<EntityModel<DataInfoDto>> getDataById(@PathVariable("id") Long id) {
    	DataInfo dInfo = new DataInfo();
		dInfo.setDataId(id);
        DataInfoDto dto = modelMapper.map(dataService.getData(dInfo), DataInfoDto.class);
        EntityModel<DataInfoDto> dataInfo = EntityModel.of(dto);
        dataInfo.add(linkTo(DataAPIController.class).slash(id).withSelfRel());
        dataInfo.add(Link.of("/docs/index.html#resources-data-info-get").withRel("profile"));

        return ResponseEntity.ok(dataInfo);
    }
}
