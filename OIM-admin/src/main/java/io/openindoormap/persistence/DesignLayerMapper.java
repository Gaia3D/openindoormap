package io.openindoormap.persistence;

import io.openindoormap.domain.extrusionmodel.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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

    /**
     * design Layer land extent 취득
     * @param designLayerId
     * @return
     */
    String getDesignLayerLandExtent(Long designLayerId);

    /**
     * design Layer building extent 취득
     * @param designLayerId
     * @return
     */
    String getDesignLayerBuildingExtent(Long designLayerId);

    /**
     * design Layer building height extent 취득
     * @param designLayerId
     * @return
     */
    String getDesignLayerBuildingHeightExtent(Long designLayerId);
    
    /**
     * designLayerKey 중복 체크
     * @param designLayerKey
     * @return
     */
    Boolean isDesignLayerKeyDuplication(String designLayerKey);

    /**
    * 자식 Design Layer 중 순서가 최대인 Design Layer를 검색
    * @param designLayerId
    * @return
    */
    DesignLayer getMaxViewOrderChildDesignLayer(Long designLayerId);

    /**
    * 자식 Design Layer 개수
    * @param designLayerId
    * @return
    */
    int getChildDesignLayerCount(Long designLayerId);

    /**
    * Design Layer 트리 부모와 순서로 그룹 정보 취득
    * @param designLayer
    * @return
    */
    DesignLayer getDesignLayerByParentAndViewOrder(DesignLayer designLayer);

    /**
    * Design Layer 테이블의 컬럼 타입이 어떤 geometry 타입인지를 구함
    * @param designLayerKey
    * @return
    */
    String getGeometryType(String designLayerKey);

    /**
     * Design Layer 존재 하는지 확인
     * @param designLayerKey
     * @return
     */
    String isDesignLayerExists(String designLayerKey);

    /**
    * Design Layer 등록
    * @param designLayer
    * @return
    */
    int insertDesignLayer(DesignLayer designLayer);

    /**
     * land info insert
     * @param designLayer
     * @return
     */
    int insertGeometryLand(DesignLayerLand designLayer);

    /**
     * building info insert
     * @param designLayer
     * @return
     */
    int insertGeometryBuilding(DesignLayerBuilding designLayer);

    /**
     * building info insert
     * @param designLayer
     * @return
     */
    int insertGeometryBuildingHeight(DesignLayerBuildingHeight designLayer);

    /**
    * Design Layer 트리 정보 수정
    * @param designLayer
    * @return
    */
    int updateTreeDesignLayer(DesignLayer designLayer);

    /**
    * Design Layer 트리 순서 수정
    * @param designLayer
    * @return
    */
    int updateViewOrderDesignLayer(DesignLayer designLayer);

    /**
    * Design Layer 정보 수정
    * @param designLayer
    * @return
    */
    int updateDesignLayer(DesignLayer designLayer);

    /**
     * design layer land 속성 정보 업데이트
     * @param designLayerLand
     * @return
     */
    int updateDesignLayerLandAttributes(DesignLayerLand designLayerLand);

    /**
     * design layer building 속성 정보 업데이트
     * @param designLayerBuilding
     * @return
     */
    int updateDesignLayerBuildingAttributes(DesignLayerBuilding designLayerBuilding);

    /**
     * design layer building 속성 정보 업데이트
     * @param designLayerBuildingHeight
     * @return
     */
    int updateDesignLayerBuildingHeightAttributes(DesignLayerBuildingHeight designLayerBuildingHeight);

    /**
    * Design Layer 삭제
    * @param designLayerId
    * @return
    */
    int deleteDesignLayer(Long designLayerId);

    /**
     * land geometry delete
     * @param map
     * @return
     */
    int deleteGeometryLand(Map<String,Object> map);

    /**
     * building geometry delete
     * @param map
     * @return
     */
    int deleteGeometryBuilding(Map<String,Object> map);

    /**
     * building geometry delete
     * @param map
     * @return
     */
    int deleteGeometryBuildingHeight(Map<String,Object> map);
    
    /**
     * 디자인 레이어 그룹 고유번호를 이용한 삭제
     * @param designLayerGroup
     * @return
     */
    int deleteDesignLayerByDesignLayerGroupId(DesignLayerGroup designLayerGroup);
}
