package io.openindoormap.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.openindoormap.domain.extrusionmodel.DesignLayer;
import io.openindoormap.persistence.DesignLayerMapper;
import io.openindoormap.service.DesignLayerService;

import java.util.List;

/**
 * @author Cheon JeongDae
 *
 */
@Slf4j
@Service
public class DesignLayerServiceImpl implements DesignLayerService {

    private final DesignLayerMapper designLayerMapper;

	public DesignLayerServiceImpl(DesignLayerMapper designLayerMapper) {
		this.designLayerMapper = designLayerMapper;
	}

	/**
	 * Design Layer 총 건수
	 * @param designLayer
	 * @return
	 */
    @Transactional(readOnly=true)
	public Long getDesignLayerTotalCount(DesignLayer designLayer) {
    	return designLayerMapper.getDesignLayerTotalCount(designLayer);
    }
    
    /**
    * design layer 목록
    * @return
    */
    @Transactional(readOnly=true)
    public List<DesignLayer> getListDesignLayer(DesignLayer designLayer) {
        return designLayerMapper.getListDesignLayer(designLayer);
    }
    
    /**
    * design layer 정보 취득
    * @param designLayerId
    * @return
    */
    @Transactional(readOnly=true)
    public DesignLayer getDesignLayer(Long designLayerId) {
        return designLayerMapper.getDesignLayer(designLayerId);
    }
}
