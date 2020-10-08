package io.openindoormap.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;
import io.openindoormap.domain.layer.LayerGroup;

@Repository
public interface LayerGroupMapper {

	/**
     * 레이어 그룹 목록
     * @return
     */
    List<LayerGroup> getListLayerGroup();
    
    /**
     * 레이어 정보 조회
     * @param layerGroupId
     * @return
     */
    LayerGroup getLayerGroup(Integer layerGroupId);
}
