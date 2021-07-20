package io.openindoormap.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.openindoormap.domain.layer.Layer;
import io.openindoormap.persistence.LayerMapper;
import io.openindoormap.service.LayerService;

/**
 * 여기서는 Geoserver Rest API 결과를 가지고 파싱 하기 때문에 RestTemplate을 커스트마이징하면 안됨
 * @author Cheon JeongDae
 *
 */
@Service
public class LayerServiceImpl implements LayerService {

    private final LayerMapper layerMapper;
    
    public LayerServiceImpl(LayerMapper layerMapper) {
    	this.layerMapper = layerMapper;
    }
    /**
    * layer 목록
    * @param layer
    * @return
    */
    @Transactional(readOnly=true)
    public List<Layer> getListLayer(Layer layer) {
        return layerMapper.getListLayer(layer);
    }

    /**
     * layer 정보 취득
     * @param layerId
     * @return
     */
     @Transactional(readOnly=true)
	public Layer getLayer(Long layerId) {
    	 return layerMapper.getLayer(layerId);
	}
}
