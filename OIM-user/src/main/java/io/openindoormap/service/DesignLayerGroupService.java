package io.openindoormap.service;

import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;

import java.util.List;

public interface DesignLayerGroupService {

	/**
     * 디자인 레이어 그룹 목록
	 * @param  designLayerGroup 디자인 레이어 그룹
     * @return
     */
    List<DesignLayerGroup> getListDesignLayerGroup(DesignLayerGroup designLayerGroup);

	/**
	 * 디자인 레이어 그룹 정보 조회
	 * @param designLayerGroupId
	 * @return
	 */
	DesignLayerGroup getDesignLayerGroup(Integer designLayerGroupId);

    /**
     * 디자인 레이어 그룹 목록 및 하위 레이어 조회
     * @return
     */
    List<DesignLayerGroup> getListDesignLayerGroupAndLayer();
}
