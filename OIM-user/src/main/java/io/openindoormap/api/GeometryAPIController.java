package io.openindoormap.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import io.openindoormap.domain.common.SpatialOperationInfo;
import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.domain.data.DataInfoDto;
import io.openindoormap.domain.extrusionmodel.DesignLayer;
import io.openindoormap.service.GeometryService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/geometry", produces = MediaTypes.HAL_JSON_VALUE)
public class GeometryAPIController {

    private final GeometryService geometryService;
    private final ModelMapper modelMapper;

    /**
     * geoemtry 정보와 intersection 필지, 빌딩 정보 리턴
     * @param spatialOperationInfo
     * @return
     */
    @PostMapping("/intersection/design-layers")
    public ResponseEntity<?> getIntersectionDesignLayers(@RequestBody @Valid SpatialOperationInfo spatialOperationInfo, Errors errors) {
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        String type = spatialOperationInfo.getType().toUpperCase();
        List<?> designLayerList = new ArrayList<>();
        if(DesignLayer.DesignLayerType.LAND == DesignLayer.DesignLayerType.valueOf(type)) {
            designLayerList = geometryService.getIntersectionDesignLayerLands(spatialOperationInfo);
        } else if(DesignLayer.DesignLayerType.BUILDING == DesignLayer.DesignLayerType.valueOf(type)) {
            designLayerList = geometryService.getIntersectionDesignLayerBuildings(spatialOperationInfo);
        } else if(DesignLayer.DesignLayerType.BUILDING_HEIGHT == DesignLayer.DesignLayerType.valueOf(type)) {
            designLayerList = geometryService.getIntersectionDesignLayerBuildingHeights(spatialOperationInfo);
        }
        List<EntityModel<?>> designLayerEntity = designLayerList.stream().map(EntityModel::of).collect(Collectors.toList());

        CollectionModel<EntityModel<?>> model = CollectionModel.of(designLayerEntity);

        model.add(linkTo(GeometryAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-geometry-intersection-design-layer-list").withRel("profile"));

        return ResponseEntity.ok(model);
    }

    @PostMapping("/intersection/datas")
    public ResponseEntity<?> getIntersectionDatas(@RequestBody @Valid SpatialOperationInfo spatialOperationInfo, Errors errors) {
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        List<DataInfo> dataList = geometryService.getIntersectionDatas(spatialOperationInfo);
        List<EntityModel<DataInfoDto>> dataInfoEntity = dataList.stream()
                .map(f -> EntityModel.of(modelMapper.map(f, DataInfoDto.class)))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<DataInfoDto>> model = CollectionModel.of(dataInfoEntity);

        model.add(linkTo(GeometryAPIController.class).withSelfRel());
        model.add(Link.of("/docs/index.html#resources-geometry-intersection-data-list").withRel("profile"));

        return ResponseEntity.ok(model);
    }

    private ResponseEntity<?> badRequest(Errors errors) {
        Map<String, Object> result = new HashMap<>();
        int statusCode = HttpStatus.BAD_REQUEST.value();
        String field = errors.getFieldErrors().get(0).getField();
        String message = errors.getFieldErrors().get(0).getDefaultMessage();

        result.put("statusCode", statusCode);
        result.put("errorCode", errors.getFieldErrors().get(0).getCode());
        result.put("message", "field: " + field + ", message: " + message);

        return ResponseEntity.badRequest().body(result);
    }
}
