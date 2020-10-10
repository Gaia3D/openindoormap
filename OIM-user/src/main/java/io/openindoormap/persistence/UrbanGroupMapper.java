package io.openindoormap.persistence;

import io.openindoormap.domain.urban.UrbanGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrbanGroupMapper {

    /**
     * 도시 그룹 목록
     * @param urbanGroup 도시그룹
     * @return
     */
    List<UrbanGroup> getListUrbanGroup(UrbanGroup urbanGroup);

    /**
     * 도시 정보 조회
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
