package io.openindoormap.service;

import io.openindoormap.domain.common.SpatialOperationInfo;
import io.openindoormap.domain.data.DataInfo;

import java.util.List;

public interface GeometryService {

    /**
     * geometry intersection 데이터 정보 리턴
     * @param spatialOperationInfo geometry 정보
     * @return
     */
    List<DataInfo> getIntersectionDatas(SpatialOperationInfo spatialOperationInfo);
}
