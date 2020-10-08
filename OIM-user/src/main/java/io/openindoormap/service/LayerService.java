package io.openindoormap.service;

import java.util.List;

import io.openindoormap.domain.extrusionmodel.DesignLayer;
import io.openindoormap.domain.layer.Layer;

public interface LayerService {

    /**
    * layer 목록
    * @return
    */
    List<Layer> getListLayer(Layer layer);
    
    /**
     * layer 정보 취득
     * @param layerId
     * @return
     */
     Layer getLayer(Long layerId);
    
}
