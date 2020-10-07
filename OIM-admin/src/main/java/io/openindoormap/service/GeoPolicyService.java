package io.openindoormap.service;

import io.openindoormap.domain.policy.GeoPolicy;

/**
 * 2D, 3D 운영 정책
 * @author jeongdae
 *
 */
public interface GeoPolicyService {
	
	/**
	 * 운영 정책 정보
	 * @return
	 */
	GeoPolicy getGeoPolicy();
	
	/**
	 * 공간 정보 기본 수정
	 * @param geoPolicy
	 * @return
	 */
	int updateGeoPolicy(GeoPolicy geoPolicy);
	
	/**
	 * Geo Server 수정
	 * @param geoPolicy
	 * @return
	 */
	int updateGeoPolicyGeoServer(GeoPolicy geoPolicy);

	/**
	 * 디자인 레이어 관련 정책 수정
	 * @param geoPolicy
	 * @return
	 */
	int updateGeoPolicyDesignLayer(GeoPolicy geoPolicy);
}
