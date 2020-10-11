package io.openindoormap.persistence;

import io.openindoormap.domain.layer.Layer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LayerMapper {
	
	/**
	 * Layer 총 건수
	 * @param layer
	 * @return
	 */
	Long getLayerTotalCount(Layer layer);
	
    /**
    * layer 목록
    * @param layer
    * @return
    */
    List<Layer> getListLayer(Layer layer);
    
    /**
     * 기본 사용 레이어 목록 
     * @param layer
     * @return
     */
    List<String> getListDefaultDisplayLayer(Layer layer);
    
    /**
     * Layer 정보 취득
     * @param layerId
     * @return
     */
     Layer getLayer(Long layerId);
}
