package io.openindoormap.api;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.openindoormap.domain.layer.Layer;
import io.openindoormap.domain.layer.LayerDto;
import io.openindoormap.service.LayerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;


@Slf4j
@Api(tags = {"LayerAPI"})
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/layers", produces = MediaTypes.HAL_JSON_VALUE)
public class LayerAPIController {

    private final LayerService layerService;
    private final ModelMapper modelMapper;

    /**
     * 레이어 목록 조회
     *
     * @return
     */
    @ApiOperation(value = "레이어 목록 조회")
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<CollectionModel<EntityModel<LayerDto>>> getLayers(@RequestParam(defaultValue = "0") Integer layerGroupId) {
        List<Layer> layerList = layerService.getListLayer(Layer.builder().layerGroupId(layerGroupId).build());
        List<EntityModel<LayerDto>> layerDtoList = layerList.stream()
                .map(f -> EntityModel.of(modelMapper.map(f, LayerDto.class))
                        .add(linkTo(LayerAPIController.class).slash(f.getLayerId()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<LayerDto>> model = CollectionModel.of(layerDtoList);

        model.add(linkTo(LayerAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-layer-list").withRel("profile"));

        return ResponseEntity.ok(model);
    }

    /**
     * 레이어 한건 조회
     *
     * @param id 레이어 아이디
     * @return
     */
    @ApiOperation(value = "레이어 한건 조회")
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8")
    @ApiImplicitParam(name = "id", value = "아이디")
    public ResponseEntity<EntityModel<LayerDto>> getLayerById(@PathVariable("id") Long id) {
        LayerDto dto = modelMapper.map(layerService.getLayer(id), LayerDto.class);
        EntityModel<LayerDto> layer = EntityModel.of(dto);
        layer.add(linkTo(LayerAPIController.class).slash(id).withSelfRel());
        layer.add(Link.of("/docs/index.html#resources-layer-get").withRel("profile"));

        return ResponseEntity.ok(layer);
    }
}
