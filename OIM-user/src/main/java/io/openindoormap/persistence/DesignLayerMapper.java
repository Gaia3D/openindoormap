package io.openindoormap.persistence;

import io.openindoormap.domain.extrusionmodel.DesignLayer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignLayerMapper {
	
	/**
	 * Design Layer 총 건수
	 * @param designLayer
	 * @return
	 */
	Long getDesignLayerTotalCount(DesignLayer designLayer);

    /**
    * Design Layer 목록
    * @param designLayer
    * @return
    */
    List<DesignLayer> getListDesignLayer(DesignLayer designLayer);

    /**
    * Design Layer 정보 취득
    * @param designLayerId
    * @return
    */
    DesignLayer getDesignLayer(Long designLayerId);
}
