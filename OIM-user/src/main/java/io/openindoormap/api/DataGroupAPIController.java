package io.openindoormap.api;

import io.openindoormap.domain.data.DataGroup;
import io.openindoormap.service.DataGroupService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.openindoormap.domain.data.DataGroupDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Api(tags = {"DataGroupAPI"})
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/data-groups", produces = MediaTypes.HAL_JSON_VALUE)
public class DataGroupAPIController {

	@Autowired
	private DataGroupService dataGroupService;

	
	private final ModelMapper modelMapper;

	
	
	/**
     * 데이터 그룹 목록 조회
     *
     * @return
     */
    @ApiOperation(value = "데이터 그룹 목록 조회")
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<CollectionModel<EntityModel<DataGroupDto>>> getDesignLayerGroups() {
        List<EntityModel<DataGroupDto>> dataGroupList = dataGroupService.getListDataGroup(new DataGroup())
                .stream()
                .map(f -> EntityModel.of(modelMapper.map(f, DataGroupDto.class))
                        .add(linkTo(DataGroupAPIController.class).slash(f.getDataGroupId()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<DataGroupDto>> model = CollectionModel.of(dataGroupList);

        model.add(linkTo(DataGroupAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-data-group-list").withRel("profile"));

        return ResponseEntity.ok(model);
    }

    /**
     * 데이터 그룹 한건 조회
     *
     * @param id 데이터 그룹 아이디
     * @return
     */
    @ApiOperation(value = "데이터 그룹 한건 조회")
    @ApiImplicitParam(name = "id", value = "아이디")
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<EntityModel<DataGroupDto>> getDataGroupById(@PathVariable("id") Integer id) {
    	
    	DataGroup dGroup = new DataGroup();
    	dGroup.setDataGroupId(id);
    	
        DataGroupDto dto = modelMapper.map(dataGroupService.getDataGroup(dGroup), DataGroupDto.class);
        EntityModel<DataGroupDto> dataGroup = EntityModel.of(dto);
        dataGroup.add(linkTo(DataGroupAPIController.class).slash(id).withSelfRel());
        dataGroup.add(Link.of("/docs/index.html#resources-data-group-get").withRel("profile"));

        return ResponseEntity.ok(dataGroup);
    }

    /**
     * parent 에 대한 데이터 그룹 목록 조회
     * @param id
     * @return
     */
    @ApiOperation(value = "parent 에 대한 데이터 그룹 목록 조회")
    @ApiImplicitParam(name = "id", value = "아이디")
    @GetMapping(value = "/parent/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<CollectionModel<EntityModel<DataGroupDto>>> getDataGroupByParent(@PathVariable("id") Integer id) {
        List<EntityModel<DataGroupDto>> dataGroupList = dataGroupService.getListDataGroupByPatent(
                DataGroup.builder().dataGroupId(id).build())
                .stream()
                .map(f -> EntityModel.of(modelMapper.map(f, DataGroupDto.class))
                        .add(linkTo(DataGroupAPIController.class).slash(f.getDataGroupId()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<DataGroupDto>> model = CollectionModel.of(dataGroupList);

        model.add(linkTo(DataGroupAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-data-group-parent").withRel("profile"));

        return ResponseEntity.ok(model);
    }
}
