package io.openindoormap.persistence;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;

import java.util.List;

@Repository
public interface DesignLayerGroupMapper {

	/**
     * 디자인 레이어 그룹 목록
     * @return
     */
    List<DesignLayerGroup> getListDesignLayerGroup();

    /**
     * 디자인 레이어 정보 조회
     * @param designLayerGroup
     * @return
     */
    DesignLayerGroup getDesignLayerGroup(DesignLayerGroup designLayerGroup);

    /**
     * 부모와 표시 순서로 메뉴 조회
     * @param designLayerGroup
     * @return
     */
    DesignLayerGroup getDesignLayerGroupByParentAndViewOrder(DesignLayerGroup designLayerGroup);

	/**
	 * 나를 부모로 가지는 자식 데이터 그룹 목록을 취득
	 * @param designLayerGroup
	 * @return
	 */
	List<DesignLayerGroup> getChildrenDesignLayerGroupListByParent(DesignLayerGroup designLayerGroup);

    /**
     * 디자인 레이어 그룹 등록
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
	 * 자식의 수를 + 또는 - 연산
	 */
	int updateDesignLayerGroupChildren(DesignLayerGroup designLayerGroup);

	/**
	 * 디자인 레이어 그룹 삭제
	 * @param designLayerGroup
	 * @return
	 */
	int deleteDesignLayerGroup(DesignLayerGroup designLayerGroup);

	/**
	 * ancestor를 이용하여 디자인 레이어 그룹 삭제
	 * @param designLayerGroup
	 * @return
	 */
	int deleteDesignLayerGroupByAncestor(DesignLayerGroup designLayerGroup);

	/**
	 * parent를 이용하여 디자인 레이어 그룹 삭제
	 * @param designLayerGroup
	 * @return
	 */
	int deleteDesignLayerGroupByParent(DesignLayerGroup designLayerGroup);
}
