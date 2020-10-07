package io.openindoormap.service;

import java.util.List;

import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;

public interface DesignLayerGroupService {

	/**
     * 디자인 레이어 그룹 목록
     * @return
     */
    List<DesignLayerGroup> getListDesignLayerGroup();

	/**
	 * 디자인 레이어 그룹 정보 조회
	 * @param designLayerGroup
	 * @return
	 */
	DesignLayerGroup getDesignLayerGroup(DesignLayerGroup designLayerGroup);

    /**
     * 디자인 레이어 그룹 목록 및 하위 레이어 조회
     * @return
     */
    List<DesignLayerGroup> getListDesignLayerGroupAndLayer();

	/**
	 * 디자인 레이어 그룹 등록
	 *
	 * @param designLayerGroup
	 * @return
	 */
	int insertDesignLayerGroup(DesignLayerGroup designLayerGroup);

	/**
	 * 디자인 레이어 그룹 수정
	 * @param designLayerGroup
	 * @return
	 */
	int updateDesignLayerGroup(DesignLayerGroup designLayerGroup);

    /**
	 * 디자인 레이어 그룹 표시 순서 수정 (up/down)
	 * @param designLayerGroup
	 * @return
	 */
	int updateDesignLayerGroupViewOrder(DesignLayerGroup designLayerGroup);

	/**
	 * 디자인 레이어 그룹 삭제
	 * @param designLayerGroup
	 * @return
	 */
	int deleteDesignLayerGroup(DesignLayerGroup designLayerGroup);
}
