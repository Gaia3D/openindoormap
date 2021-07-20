package io.openindoormap.service;

import io.openindoormap.domain.layer.Layer;

import java.util.List;

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
