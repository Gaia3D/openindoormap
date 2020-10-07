package io.openindoormap.service;

import java.util.List;

import io.openindoormap.domain.extrusionmodel.DesignLayer;

public interface DesignLayerService {

	/**
	 * design layer 총 건수
	 * @param designLayer
	 * @return
	 */
	Long getDesignLayerTotalCount(DesignLayer designLayer);
	
    /**
    * design layer 목록
    * @return
    */
    List<DesignLayer> getListDesignLayer(DesignLayer designLayer);
    
    /**
    * design layer 정보 취득
    * @param designLayerId
    * @return
    */
    DesignLayer getDesignLayer(Long designLayerId);
    
}
