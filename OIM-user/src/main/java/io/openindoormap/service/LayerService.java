package io.openindoormap.service;

import java.util.List;

import io.openindoormap.domain.layer.Layer;

public interface LayerService {

    /**
    * layer 목록
    * @return
    */
    List<Layer> getListLayer(Layer layer);
    
}
