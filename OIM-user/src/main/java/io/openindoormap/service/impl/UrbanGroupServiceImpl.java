package io.openindoormap.service.impl;

import io.openindoormap.domain.urban.UrbanGroup;
import io.openindoormap.persistence.UrbanGroupMapper;
import io.openindoormap.service.UrbanGroupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UrbanGroupServiceImpl implements UrbanGroupService {

    private final UrbanGroupMapper urbanGroupMapper;

    /**
     * 도시 그룹 목록
     * @param urbanGroup 도시그룹
     */
    @Transactional(readOnly = true)
    public List<UrbanGroup> getListUrbanGroup(UrbanGroup urbanGroup) {
        return urbanGroupMapper.getListUrbanGroup(urbanGroup);
    }

    /**
     * 도시 그룹 정보 조회
     *
     * @param urbanGroupId
     * @return
     */
    @Transactional(readOnly = true)
    public UrbanGroup getUrbanGroup(Integer urbanGroupId) {
        return urbanGroupMapper.getUrbanGroup(urbanGroupId);
    }

    /**
     * depth 에 해당하는 도시그룹 정보 목록
     * @param depth
     * @return
     */
    @Transactional(readOnly = true)
    public List<UrbanGroup> getListUrbanGroupByDepth(Integer depth) {
        return urbanGroupMapper.getListUrbanGroupByDepth(depth);
    }

    /**
     * parent 에 해당하는 도시그룹 정보 목록
     * @param urbanGroupId
     * @return
     */
    @Transactional(readOnly = true)
    public List<UrbanGroup> getListUrbanGroupByParent(Integer urbanGroupId) {
        return urbanGroupMapper.getListUrbanGroupByParent(urbanGroupId);
    }
}
