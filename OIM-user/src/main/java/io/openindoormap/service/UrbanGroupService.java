package io.openindoormap.service;

import java.util.List;

import io.openindoormap.domain.urban.UrbanGroup;

public interface UrbanGroupService {

    /**
     * 도시 그룹 목록
     * @param urbanGroup 도시그룹
     * @return
     */
    List<UrbanGroup> getListUrbanGroup(UrbanGroup urbanGroup);

    /**
     * 도시 그룹 정보 조회
     *
     * @param urbanGroupId
     * @return
     */
    UrbanGroup getUrbanGroup(Integer urbanGroupId);

    /**
     * depth 에 해당하는 도시 그룹 목록
     * @param depth
     * @return
     */
    List<UrbanGroup> getListUrbanGroupByDepth(Integer depth);

    /**
     * parent 에 해당하는 도시 그룹 목록
     * @param urbanGroupId
     * @return
     */
    List<UrbanGroup> getListUrbanGroupByParent(Integer urbanGroupId);
}
