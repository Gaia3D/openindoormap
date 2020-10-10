package io.openindoormap.service.impl;

import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.Move;
import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;
import io.openindoormap.persistence.DataLibraryGroupMapper;
import io.openindoormap.service.DataLibraryGroupService;
import io.openindoormap.service.DataLibraryService;
import io.openindoormap.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DataLibraryGroupServiceImpl implements DataLibraryGroupService {

	@Autowired
	private DataLibraryService dataLibraryService;
	@Autowired
	private DataLibraryGroupMapper dataLibraryGroupMapper;
	@Autowired
	private PropertiesConfig propertiesConfig;

	/**
     * 데이터 라이브러리 그룹 목록
     * @return
     */
	@Transactional(readOnly = true)
	public List<DataLibraryGroup> getListDataLibraryGroup(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.getListDataLibraryGroup();
	}

	/**
     * 데이터 라이브러리 그룹 정보 조회
     * @param dataLibraryGroup
	 * @return
	 */
	@Transactional(readOnly = true)
	public DataLibraryGroup getDataLibraryGroup(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.getDataLibraryGroup(dataLibraryGroup);
	}

	/**
	 * 기본 데이터 라이브러리 그룹 정보 조회
	 * @return
	 */
	@Transactional(readOnly = true)
	public DataLibraryGroup getBasicDataLibraryGroup() {
		return dataLibraryGroupMapper.getBasicDataLibraryGroup();
	}
	
	/**
     * 데이터 라이브러리 그룹 Key 중복 확인
     * @param dataLibraryGroup
     * @return
     */
	@Transactional(readOnly = true)
	public Boolean isDataLibraryGroupKeyDuplication(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.isDataLibraryGroupKeyDuplication(dataLibraryGroup);
	}
	
	/**
     * 부모와 표시 순서로 데이터 라이브러리 그룹 조회
     * @param dataLibraryGroup
     * @return
     */
	@Transactional(readOnly = true)
    public DataLibraryGroup getDataLibraryGroupByParentAndViewOrder(DataLibraryGroup dataLibraryGroup) {
    	return dataLibraryGroupMapper.getDataLibraryGroupByParentAndViewOrder(dataLibraryGroup);
    }

    /**
     * 데이터 라이브러리 그룹 등록
     * @param dataLibraryGroup
     * @return
     */
    @Transactional
	public int insertDataLibraryGroup(DataLibraryGroup dataLibraryGroup) {
    	String userId = dataLibraryGroup.getUserId();
    	Integer parentDataLibraryGroupId = 0;
    	
    	DataLibraryGroup parentDataLibraryGroup = new DataLibraryGroup();
    	//parentDataLibraryGroup.setUserId(userId);
    	int depth = 0;
    	if(dataLibraryGroup.getParent() > 0) {
    		parentDataLibraryGroupId = dataLibraryGroup.getParent();
    		parentDataLibraryGroup.setDataLibraryGroupId(parentDataLibraryGroupId);
    		parentDataLibraryGroup = dataLibraryGroupMapper.getDataLibraryGroup(parentDataLibraryGroup);
	    	depth = parentDataLibraryGroup.getDepth() + 1;
    	}
	    
    	// 디렉토리 생성
    	String tempDataLibraryGroupPath = dataLibraryGroup.getDataLibraryGroupKey() + "/";
    	FileUtils.makeDirectoryByPath(propertiesConfig.getAdminDataLibraryServiceDir(), tempDataLibraryGroupPath);
    	dataLibraryGroup.setDataLibraryGroupPath(propertiesConfig.getAdminDataLibraryServicePath() + tempDataLibraryGroupPath);
    	int result = dataLibraryGroupMapper.insertDataLibraryGroup(dataLibraryGroup);

    	if(depth > 1) {
	    	// parent 의 children update
    		Integer children = parentDataLibraryGroup.getChildren();
    		if(children == null) children = 0;
    		children += 1;
    		
    		parentDataLibraryGroup = new DataLibraryGroup();
    		//parentDataLibraryGroup.setUserId(userId);
    		parentDataLibraryGroup.setDataLibraryGroupId(parentDataLibraryGroupId);
    		parentDataLibraryGroup.setChildren(children);
	    	return dataLibraryGroupMapper.updateDataLibraryGroup(parentDataLibraryGroup);
    	}

    	return result;
    }

	/**
	 * 기본 데이터 라이브러리 등록
	 * @param dataLibraryGroup
	 * @return
	 */
	@Transactional
	public int insertBasicDataLibraryGroup(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.insertBasicDataLibraryGroup(dataLibraryGroup);
	}
    
    /**
	 * 데이터 라이브러리 그룹 수정
	 * @param dataLibraryGroup
	 * @return
	 */
    @Transactional
	public int updateDataLibraryGroup(DataLibraryGroup dataLibraryGroup) {
    	return dataLibraryGroupMapper.updateDataLibraryGroup(dataLibraryGroup);
    }

    /**
	 * 데이터 라이브러리 그룹 표시 순서 수정. UP, DOWN
	 * @param dataLibraryGroup
	 * @return
	 */
    @Transactional
	public int updateDataLibraryGroupViewOrder(DataLibraryGroup dataLibraryGroup) {

    	DataLibraryGroup dbDataLibraryGroup = dataLibraryGroupMapper.getDataLibraryGroup(dataLibraryGroup);
    	dbDataLibraryGroup.setUpdateType(dataLibraryGroup.getUpdateType());

    	Integer modifyViewOrder = dbDataLibraryGroup.getViewOrder();
    	DataLibraryGroup searchDataLibraryGroup = new DataLibraryGroup();
    	//searchDataLibraryGroup.setUserId(dataLibraryGroup.getUserId());
    	searchDataLibraryGroup.setUpdateType(dbDataLibraryGroup.getUpdateType());
    	searchDataLibraryGroup.setParent(dbDataLibraryGroup.getParent());

    	if(Move.UP == Move.valueOf(dbDataLibraryGroup.getUpdateType())) {
    		// 바로 위 메뉴의 view_order 를 +1
    		searchDataLibraryGroup.setViewOrder(dbDataLibraryGroup.getViewOrder());
    		searchDataLibraryGroup = getDataLibraryGroupByParentAndViewOrder(searchDataLibraryGroup);

    		if(searchDataLibraryGroup == null) return 0;

	    	dbDataLibraryGroup.setViewOrder(searchDataLibraryGroup.getViewOrder());
	    	searchDataLibraryGroup.setViewOrder(modifyViewOrder);
    	} else {
    		// 바로 아래 메뉴의 view_order 를 -1 함
    		searchDataLibraryGroup.setViewOrder(dbDataLibraryGroup.getViewOrder());
    		searchDataLibraryGroup = getDataLibraryGroupByParentAndViewOrder(searchDataLibraryGroup);

    		if(searchDataLibraryGroup == null) return 0;

    		dbDataLibraryGroup.setViewOrder(searchDataLibraryGroup.getViewOrder());
    		searchDataLibraryGroup.setViewOrder(modifyViewOrder);
    	}

    	dataLibraryGroupMapper.updateDataLibraryGroupViewOrder(searchDataLibraryGroup);
		return dataLibraryGroupMapper.updateDataLibraryGroupViewOrder(dbDataLibraryGroup);
    }

	/**
	 * 자식의 수를 + 또는 - 연산
	 */
	@Transactional
	public int updateDataLibraryGroupChildren(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.updateDataLibraryGroupChildren(dataLibraryGroup);
	}

    /**
	 * 데이터 라이브러리 그룹 삭제
	 * @param dataLibraryGroup
	 * @return
	 */
    @Transactional
	public int deleteDataLibraryGroup(DataLibraryGroup dataLibraryGroup) {
		int result = 0;
		List<DataLibraryGroup> childrenDataLibraryGroupList = dataLibraryGroupMapper.getChildrenDataLibraryGroupListByParent(dataLibraryGroup);
		if(childrenDataLibraryGroupList == null || childrenDataLibraryGroupList.isEmpty()) {
			// 내 데이터 라이브러리 그룹에 있는 모든 데이터 라이브러를 삭제
			dataLibraryService.deleteDataLibraryByDataLibraryGroupId(dataLibraryGroup);

			// 부모의 children -1
			dataLibraryGroup.setChildren(-1);
			dataLibraryGroupMapper.updateDataLibraryGroupChildren(dataLibraryGroup);

			// 내 자신을 삭제
			dataLibraryGroupMapper.deleteDataLibraryGroup(dataLibraryGroup);
		} else {
			dataLibraryGroupMapper.deleteDataLibraryGroup(dataLibraryGroup);
			for(DataLibraryGroup childDataLibraryGroup : childrenDataLibraryGroupList) {
				childDataLibraryGroup.setUserId(null);
				deleteDataLibraryGroup(childDataLibraryGroup);
			}
		}

		return result;
    }
    
    @Transactional
	public int deleteDataLibraryGroupByAncestor(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.deleteDataLibraryGroupByAncestor(dataLibraryGroup);
	}
    
    @Transactional
	public int deleteDataLibraryGroupByParent(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.deleteDataLibraryGroupByParent(dataLibraryGroup);
	}
	
    @Transactional
	public int deleteDataLibraryGroupByUserId(String userId) {
		return dataLibraryGroupMapper.deleteDataLibraryGroupByUserId(userId);
	}
}
