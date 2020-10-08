package io.openindoormap.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import io.openindoormap.domain.layer.LayerGroup;
import io.openindoormap.domain.layer.LayerGroupDto;
import io.openindoormap.service.LayerGroupService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/layer-groups", produces = MediaTypes.HAL_JSON_VALUE)
public class LayerGroupAPIController {

    private final LayerGroupService layerGroupService;
    private final ModelMapper modelMapper;

    /**
     * 레이어 그룹 목록 조회
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<LayerGroupDto>>> getLayerGroups() {
        List<EntityModel<LayerGroupDto>> layerGroupList = layerGroupService.getListLayerGroup()
                .stream()
                .map(f -> EntityModel.of(modelMapper.map(f, LayerGroupDto.class))
                        .add(linkTo(LayerGroupAPIController.class).slash(f.getLayerGroupId()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<LayerGroupDto>> model = CollectionModel.of(layerGroupList);

        model.add(linkTo(LayerGroupAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-layer-group-list").withRel("profile"));

        return ResponseEntity.ok(model);
    }

    /**
     * 레이어 그룹 한건 조회
     *
     * @param id 레이어 그룹 아이디
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<LayerGroupDto>> getLayerGroupById(@PathVariable("id") Integer id) {
        LayerGroupDto dto = modelMapper.map(layerGroupService.getLayerGroup(id), LayerGroupDto.class);
        EntityModel<LayerGroupDto> layerGroup = EntityModel.of(dto);
        layerGroup.add(linkTo(LayerGroupAPIController.class).slash(id).withSelfRel());
        layerGroup.add(Link.of("/docs/index.html#resources-design-layer-group-get").withRel("profile"));

        return ResponseEntity.ok(layerGroup);
    }

    /**
     * parent 에 대한 레이어 그룹 목록 조회
     * @param id
     * @return
     */
    @GetMapping("/parent/{id}")
    public ResponseEntity<CollectionModel<EntityModel<LayerGroupDto>>> getLayerGroupByParent(@PathVariable("id") Integer id) {
        List<EntityModel<LayerGroupDto>> layerGroupList = layerGroupService.getListLayerGroup()
                .stream()
                .map(f -> EntityModel.of(modelMapper.map(f, LayerGroupDto.class))
                        .add(linkTo(LayerGroupAPIController.class).slash(f.getLayerGroupId()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<LayerGroupDto>> model = CollectionModel.of(layerGroupList);

        model.add(linkTo(LayerGroupAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-layer-group-parent").withRel("profile"));

        return ResponseEntity.ok(model);
    }
}
