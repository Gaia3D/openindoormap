package io.openindoormap.service;

import io.openindoormap.domain.common.SpatialOperationInfo;
import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.domain.extrusionmodel.DesignLayerBuildingDto;
import io.openindoormap.domain.extrusionmodel.DesignLayerBuildingHeightDto;
import io.openindoormap.domain.extrusionmodel.DesignLayerLandDto;

import java.util.List;

public interface GeometryService {

    /**
     * geometry intersection 필지 정보 리턴
     * @param spatialOperationInfo geometry 정보
     * @return
     */
    List<DesignLayerLandDto> getIntersectionDesignLayerLands(SpatialOperationInfo spatialOperationInfo);

    /**
     * geometry intersection 빌딩 정보 리턴
     * @param spatialOperationInfo geometry 정보
     * @return
     */
    List<DesignLayerBuildingDto> getIntersectionDesignLayerBuildings(SpatialOperationInfo spatialOperationInfo);

    /**
     * geometry intersection 빌딩 높이 정보 리턴
     * @param spatialOperationInfo geometry 정보
     * @return
     */
    List<DesignLayerBuildingHeightDto> getIntersectionDesignLayerBuildingHeights(SpatialOperationInfo spatialOperationInfo);

    /**
     * geometry intersection 데이터 정보 리턴
     * @param spatialOperationInfo geometry 정보
     * @return
     */
    List<DataInfo> getIntersectionDatas(SpatialOperationInfo spatialOperationInfo);
}
