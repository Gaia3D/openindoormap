package io.openindoormap.service;

import io.openindoormap.domain.extrusionmodel.DesignLayerFileInfo;

import java.util.List;
import java.util.Map;

public interface DesignLayerFileInfoService {

	/**
	 * design layer shape 파일 목록
	 * @return
	 */
	List<DesignLayerFileInfo> getListDesignLayerFileInfo(Long designLayerId);
	
	/**
	 * designLayerId에 해당하는 모든 파일 경로 목록
	 * @param designLayerId
	 * @return
	 */
	List<String> getListDesignLayerFilePath(Long designLayerId);
	
	/**
	 * design layer 파일 정보 취득
	 * @param designLayerFileInfoId
	 * @return
	 */
	DesignLayerFileInfo getDesignLayerFileInfo(Long designLayerFileInfoId);
	
	/**
	 * design layer shape 파일 그룹 정보 취득
	 * @param designLayerFileInfoTeamId
	 * @return
	 */
	List<DesignLayerFileInfo> getDesignLayerFileInfoTeam(Long designLayerFileInfoTeamId);
	
	/**
	 * design layer shape 파일 version
	 * @param designLayerFileInfoId
	 * @return
	 */
	Integer getDesignLayerShapeFileVersion(Long designLayerFileInfoId);
	
	/**
	 * design 레이어 이력이 존재하는지 검사
	 * @param designLayerId
	 * @return
	 */
	boolean isDesignLayerFileInfoExist(Long designLayerId);
	
	/**
	 * design 레이어 이력내 활성화 된 데이터 정보를 취득
	 * @param designLayerId
	 * @return
	 */
	DesignLayerFileInfo getEnableDesignLayerFileInfo(Long designLayerId);

	/**
	 * design layer shape 파일 정보 수정
	 * @param designLayerFileInfo
	 * @return
	 * @throws Exception 
	 */
	int updateDesignLayerFileInfo(DesignLayerFileInfo designLayerFileInfo);
	
	/**
	 * org2org를 이용해서 생성한 테이블을 데이터 version 갱신
	 * @param map
	 * @return
	 */
	int updateDataFileVersion(Map<String, String> map);

	/**
	 * team id 로 design 레이어 파일 이력을 삭제
	 * @param deleteDesignLayerFileInfoTeamId
	 * @return
	 */
	int deleteDesignLayerFileInfoByTeamId(Long deleteDesignLayerFileInfoTeamId);
	
}
