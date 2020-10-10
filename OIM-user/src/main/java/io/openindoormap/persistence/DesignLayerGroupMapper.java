package io.openindoormap.persistence;

import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignLayerGroupMapper {

	/**
     * 디자인 레이어 그룹 목록
     * @param  designLayerGroup 디자인 레이어 그룹
     * @return
     */
    List<DesignLayerGroup> getListDesignLayerGroup(DesignLayerGroup designLayerGroup);

    /**
     * 디자인 레이어 정보 조회
     * @param designLayerGroupId
     * @return
     */
    DesignLayerGroup getDesignLayerGroup(Integer designLayerGroupId);
}
