package io.openindoormap.service;

import io.openindoormap.domain.layer.LayerGroup;

import java.util.List;

public interface LayerGroupService {

    /**
     * 레이어 그룹 목록 및 하위 레이어 조회
     * @return
     */
    List<LayerGroup> getListLayerGroupAndLayer();
    
    /**
     * 레이어 그룹 목록
	 * @param  layerGroup 레이어 그룹
     * @return
     */
    List<LayerGroup> getListLayerGroup();
    
    /**
	 * 레이어 그룹 정보 조회
	 * @param layerGroupId
	 * @return
	 */
	LayerGroup getLayerGroup(Integer layerGroupId);

}
