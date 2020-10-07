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

import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;
import io.openindoormap.domain.extrusionmodel.DesignLayerGroupDto;
import io.openindoormap.service.DesignLayerGroupService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/design-layer-groups", produces = MediaTypes.HAL_JSON_VALUE)
public class DesignLayerGroupAPIController {

    private final DesignLayerGroupService designLayerGroupService;
    private final ModelMapper modelMapper;

    /**
     * 디자인 레이어 그룹 목록 조회
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DesignLayerGroupDto>>> getDesignLayerGroups() {
        List<EntityModel<DesignLayerGroupDto>> designLayerGroupList = designLayerGroupService.getListDesignLayerGroup(new DesignLayerGroup())
                .stream()
                .map(f -> EntityModel.of(modelMapper.map(f, DesignLayerGroupDto.class))
                        .add(linkTo(DesignLayerGroupAPIController.class).slash(f.getDesignLayerGroupId()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<DesignLayerGroupDto>> model = CollectionModel.of(designLayerGroupList);

        model.add(linkTo(DesignLayerGroupAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-design-layer-group-list").withRel("profile"));

        return ResponseEntity.ok(model);
    }

    /**
     * 디자인 레이어 그룹 한건 조회
     *
     * @param id 디자인 레이어 그룹 아이디
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DesignLayerGroupDto>> getDesignLayerGroupById(@PathVariable("id") Integer id) {
        DesignLayerGroupDto dto = modelMapper.map(designLayerGroupService.getDesignLayerGroup(id), DesignLayerGroupDto.class);
        EntityModel<DesignLayerGroupDto> designLayerGroup = EntityModel.of(dto);
        designLayerGroup.add(linkTo(DesignLayerGroupAPIController.class).slash(id).withSelfRel());
        designLayerGroup.add(Link.of("/docs/index.html#resources-design-layer-group-get").withRel("profile"));

        return ResponseEntity.ok(designLayerGroup);
    }

    /**
     * parent 에 대한 디자인 레이어 그룹 목록 조회
     * @param id
     * @return
     */
    @GetMapping("/parent/{id}")
    public ResponseEntity<CollectionModel<EntityModel<DesignLayerGroupDto>>> getDesignLayerGroupByParent(@PathVariable("id") Integer id) {
        List<EntityModel<DesignLayerGroupDto>> designLayerGroupList = designLayerGroupService.getListDesignLayerGroup(
                DesignLayerGroup.builder().designLayerGroupId(id).build())
                .stream()
                .map(f -> EntityModel.of(modelMapper.map(f, DesignLayerGroupDto.class))
                        .add(linkTo(DesignLayerGroupAPIController.class).slash(f.getDesignLayerGroupId()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<DesignLayerGroupDto>> model = CollectionModel.of(designLayerGroupList);

        model.add(linkTo(DesignLayerGroupAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-design-layer-group-parent").withRel("profile"));

        return ResponseEntity.ok(model);
    }
}
