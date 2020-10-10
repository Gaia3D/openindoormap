package io.openindoormap.service.impl;

import io.openindoormap.domain.extrusionmodel.DesignLayer;
import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;
import io.openindoormap.persistence.DesignLayerGroupMapper;
import io.openindoormap.service.DesignLayerGroupService;
import io.openindoormap.service.DesignLayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DesignLayerGroupServiceImpl implements DesignLayerGroupService {

	private final DesignLayerService designLayerService;
	private final DesignLayerGroupMapper designLayerGroupMapper;

	public DesignLayerGroupServiceImpl(DesignLayerService designLayerService, DesignLayerGroupMapper designLayerGroupMapper) {
		this.designLayerService = designLayerService;
		this.designLayerGroupMapper = designLayerGroupMapper;
	}

	/**
	 * 디자인 레이어 그룹 목록
	 * @param  designLayerGroup 디자인 레이어 그룹
	 */
	@Transactional(readOnly = true)
	public List<DesignLayerGroup> getListDesignLayerGroup(DesignLayerGroup designLayerGroup) {
		return designLayerGroupMapper.getListDesignLayerGroup(designLayerGroup);
	}

	/**
     * 디자인 레이어 그룹 정보 조회
     * @param designLayerGroupId
     * @return
     */
	@Transactional(readOnly = true)
    public DesignLayerGroup getDesignLayerGroup(Integer designLayerGroupId) {
		return designLayerGroupMapper.getDesignLayerGroup(designLayerGroupId);
	}

	/**
	 * 디자인 레이어 그룹 목록 및 하위 레이어를 조회
     * @return
     */
	@Transactional(readOnly = true)
	public List<DesignLayerGroup> getListDesignLayerGroupAndLayer() {
		List<DesignLayerGroup> designLayerGroupList = designLayerGroupMapper.getListDesignLayerGroup(new DesignLayerGroup());
		for(DesignLayerGroup designLayerGroup : designLayerGroupList) {
			DesignLayer designLayer = new DesignLayer();
			designLayer.setDesignLayerGroupId(designLayerGroup.getDesignLayerGroupId());
			designLayerGroup.setDesignLayerList(designLayerService.getListDesignLayer(designLayer));
		}

		return designLayerGroupList;
	}
}
