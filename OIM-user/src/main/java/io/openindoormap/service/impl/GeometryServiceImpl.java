package io.openindoormap.service.impl;

import io.openindoormap.domain.common.SpatialOperationInfo;
import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.persistence.GeometryMapper;
import io.openindoormap.service.GeometryService;
import io.openindoormap.support.GeometrySupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GeometryServiceImpl implements GeometryService {

    private final GeometryMapper geometryMapper;

    public GeometryServiceImpl(GeometryMapper geometryMapper) {
        this.geometryMapper = geometryMapper;
    }

    @Transactional(readOnly=true)
    public List<DataInfo> getIntersectionDatas(SpatialOperationInfo spatialOperationInfo) {
        spatialOperationInfo.setWkt(GeometrySupport.toWKT(spatialOperationInfo.getGeometryInfo()));

        return geometryMapper.getIntersectionDatas(spatialOperationInfo);
    }
}
