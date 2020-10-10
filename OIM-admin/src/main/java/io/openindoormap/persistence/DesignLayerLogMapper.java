package io.openindoormap.persistence;

import io.openindoormap.domain.extrusionmodel.DesignLayerLog;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 디자인 레이어 로그 처리
 * @author jeongdae
 *
 */
@Repository
public interface DesignLayerLogMapper {
	
	/**
	 * 디자인 레이어 이력 총 건수
	 * @param designLayerLog
	 * @return
	 */
	Long getDesignLayerLogTotalCount(DesignLayerLog designLayerLog);
	
	/**
	 * 디자인 레이어 이력 목록
	 * @param designLayerLog
	 * @return
	 */
	List<DesignLayerLog> getListDesignLayerLog(DesignLayerLog designLayerLog);
	
	/**
	 * 디자인 레이어 정보 취득
	 * @param designLayerLogId
	 * @return
	 */
	DesignLayerLog getDesignLayerLog(Long designLayerLogId);

	/**
	 * 디자인 레이어 이력 등록
	 * @param designLayerLog
	 * @return
	 */
	int insertDesignLayerLog(DesignLayerLog designLayerLog);
}
