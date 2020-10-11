package io.openindoormap.service.impl;

import io.openindoormap.domain.layer.Layer;
import io.openindoormap.domain.layer.LayerGroup;
import io.openindoormap.persistence.LayerGroupMapper;
import io.openindoormap.service.LayerGroupService;
import io.openindoormap.service.LayerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LayerGroupServiceImpl implements LayerGroupService {

	private final LayerService layerService;

	private final LayerGroupMapper layerGroupMapper;
	
	public LayerGroupServiceImpl(LayerService layerService, LayerGroupMapper layerGroupMapper) {
		this.layerService = layerService;
		this.layerGroupMapper = layerGroupMapper;
	}
	
	
	/**
	 * 레이어 그룹 목록 및 하위 레이어를 조회
     * @return
     */
	@Transactional(readOnly = true)
	public List<LayerGroup> getListLayerGroupAndLayer() {
		List<LayerGroup> layerGroupList = layerGroupMapper.getListLayerGroup();
		layerGroupList.stream()
						.forEach(group -> {
							Layer layer = Layer.builder()
											.layerGroupId(group.getLayerGroupId())
											.build();
							group.setLayerList(layerService.getListLayer(layer));
						});
		
//		List<Layer> layerList = new ArrayList<>();
//		layerGroupList.stream()
//						.forEach(group -> {
//							Layer layer = Layer.builder()
//											.layerGroupId(group.getLayerGroupId())
//											.parent(group.getParent())
//											.parentName(group.getLayerGroupName())
//											.depth(group.getDepth())
//											.build();
//							layerList.add(layer);
//							List<Layer> childLayerList = layerService.getListLayer(layer);
//							if(childLayerList.size() > 0) {
//								layerList.addAll(childLayerList);
//							}
//						});

		return layerGroupList;
	}


	/**
	 * 레이어 그룹 목록
	 */
	@Transactional(readOnly = true)
	public List<LayerGroup> getListLayerGroup() {
		return layerGroupMapper.getListLayerGroup();
	}

	/**
     * 레이어 그룹 정보 조회
     * @param layerGroupId
     * @return
     */
	@Transactional(readOnly = true)
	public LayerGroup getLayerGroup(Integer layerGroupId) {
		return layerGroupMapper.getLayerGroup(layerGroupId);
	}

}
