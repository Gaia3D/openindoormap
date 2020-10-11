package io.openindoormap.persistence;

import io.openindoormap.domain.layer.LayerGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

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
