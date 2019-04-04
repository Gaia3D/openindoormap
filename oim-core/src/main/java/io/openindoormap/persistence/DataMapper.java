package io.openindoormap.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.DataInfo;
import io.openindoormap.domain.DataInfoAttribute;
import io.openindoormap.domain.DataInfoObjectAttribute;
import io.openindoormap.domain.Project;

/**
 * Data
 * @author jeongdae
 *
 */
@Repository
public interface DataMapper {

	/**
	 * Data ?��
	 * @param dataInfo
	 * @return
	 */
	Long getDataTotalCount(DataInfo dataInfo);
	
	/**
	 * ?��?��?�� ?��?���? ?���? ?���?
	 * @param dataInfo
	 * @return
	 */
	Long getDataTotalCountByStatus(DataInfo dataInfo);
	
	/**
	 * Data Object 총건?��
	 * @param dataInfoObjectAttribute
	 * @return
	 */
	Long getDataObjectAttributeTotalCount(DataInfoObjectAttribute dataInfoObjectAttribute);
	
	/**
	 * Data 목록
	 * @param dataInfo
	 * @return
	 */
	List<DataInfo> getListData(DataInfo dataInfo);
	
	/**
	 * ?��로젝?���? Data 목록
	 * @param dataInfo
	 * @return
	 */
	List<DataInfo> getListDataByProjectId(DataInfo dataInfo);
	
	/**
	 * data_group_id�? ?��?��?�� Data 목록
	 * @param dataInfo
	 * @return
	 */
	List<DataInfo> getListExceptDataGroupDataByGroupId(DataInfo dataInfo);
	
	/**
	 * Data Key 중복 건수
	 * @param dataInfo
	 * @return
	 */
	Integer getDuplicationKeyCount(DataInfo dataInfo);
	
	/**
	 * Data ?���? 취득
	 * @param data_id
	 * @return
	 */
	DataInfo getData(Long data_id);
	
	/**
	 * Data ?���? 취득
	 * @param dataInfo
	 * @return
	 */
	DataInfo getDataByDataKey(DataInfo dataInfo);
	
	/**
	 * 최상?�� root dataInfo ?���? 취득
	 * @param projectId
	 * @return
	 */
	DataInfo getRootDataByProjectId(Integer projectId);
	
	/**
	 * Data Attribute ?���? 취득
	 * @param data_id
	 * @return
	 */
	DataInfoAttribute getDataAttribute(Long data_id);
	
	/**
	 * Data Object Attribute ?���? 취득
	 * @param data_object_attribute_id
	 * @return
	 */
	DataInfoObjectAttribute getDataObjectAttribute(Long data_object_attribute_id);
	
	/**
	 * ?��?�� ?��?��
	 * @param dataInfo
	 * @return
	 */
	Integer getViewOrderByParent(DataInfo dataInfo);
	
	/**
	 * ?�� ?��로젝?�� ?�� Root Parent 개수�? 체크
	 * @param dataInfo
	 * @return
	 */
	Integer getRootParentCount(DataInfo dataInfo);
	
	/**
	 * data_key �? ?��?��?��?�� data_attribute_id �? ?��?��
	 * TODO 9.6 ?��?��?�� merge�? �?�? ?��?�� 
	 * @param data_key
	 * @return
	 */
	DataInfoAttribute getDataIdAndDataAttributeIDByDataKey(String data_key);
	
	/**
	 * Data Object 조회
	 * @param dataInfoObjectAttribute
	 * @return
	 */
	List<DataInfoObjectAttribute> getListDataObjectAttribute(DataInfoObjectAttribute dataInfoObjectAttribute);
	
	/**
	 * Data ?���?
	 * @param dataInfo
	 * @return
	 */
	int insertData(DataInfo dataInfo);
	
	/**
	 * Data ?��?�� ?���?
	 * @param dataInfoAttribute
	 * @return
	 */
	int insertDataAttribute(DataInfoAttribute dataInfoAttribute);
	
	/**
	 * Data Object ?��?�� ?���?
	 * @param dataInfoObjectAttribute
	 * @return
	 */
	int insertDataObjectAttribute(DataInfoObjectAttribute dataInfoObjectAttribute);
	
	/**
	 * Data ?��?��
	 * @param dataInfo
	 * @return
	 */
	int updateData(DataInfo dataInfo);
	
	/**
	 * Data ?��?�� ?��?��
	 * @param dataInfoAttribute
	 * @return
	 */
	int updateDataAttribute(DataInfoAttribute dataInfoAttribute);
	
	/**
	 * Data ?��?��블의 Data 그룹 ?���? �?�?
	 * @param dataInfo
	 * @return
	 */
	int updateDataGroupData(DataInfo dataInfo);
	
	/**
	 * Data ?��?�� ?��?��
	 * @param dataInfo
	 * @return
	 */
	int updateDataStatus(DataInfo dataInfo);
	
	/**
	 * Data ?��?��
	 * @param dataInfo
	 * @return
	 */
	int deleteData(DataInfo dataInfo);
	
	/**
	 * Data ?�� ?��?��?�� 모든 Object ID�? ?��?��
	 * @param dataId
	 * @return
	 */
	int deleteDataObjects(Long data_id);
	
	/**
	 * TODO ?��로젝?��?�� ?��?�� ?��?��?��?��?? ?��?��?��?�� ?��?��?
	 * @param project
	 * @return
	 */
	int deleteDataByProjectId(Project project);
}
