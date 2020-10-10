package io.openindoormap.service.impl;

import io.openindoormap.domain.Depth;
import io.openindoormap.domain.Move;
import io.openindoormap.domain.urban.UrbanGroup;
import io.openindoormap.domain.user.UserGroup;
import io.openindoormap.persistence.UrbanGroupMapper;
import io.openindoormap.service.UrbanGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UrbanGroupServiceImpl implements UrbanGroupService {

	@Autowired
	private UrbanGroupMapper urbanGroupMapper;

	/**
	 * 도시 그룹 목록
	 */
	@Transactional(readOnly = true)
	public List<UrbanGroup> getListUrbanGroup() {
		return urbanGroupMapper.getListUrbanGroup();
	}

	/**
     * 도시 그룹 정보 조회
     * @param urbanGroup
     * @return
     */
	@Transactional(readOnly = true)
    public UrbanGroup getUrbanGroup(UrbanGroup urbanGroup) {
		return urbanGroupMapper.getUrbanGroup(urbanGroup);
	}

	/**
	 * 부모와 표시 순서로 메뉴 조회
	 * @param urbanGroup
	 * @return
	 */
	private UrbanGroup getDataLayerByParentAndViewOrder(UrbanGroup urbanGroup) {
		return urbanGroupMapper.getUrbanGroupByParentAndViewOrder(urbanGroup);
	}

	/**
	 * 도시 그룹 Key 중복 확인
	 * @param urbanGroup
	 * @return
	 */
	@Transactional(readOnly = true)
	public Boolean isUrbanGroupKeyDuplication(UrbanGroup urbanGroup) {
		return urbanGroupMapper.isUrbanGroupKeyDuplication(urbanGroup);
	}

	/**
	 * 도시 그룹 등록
	 */
	@Transactional
	public int insertUrbanGroup(UrbanGroup urbanGroup) {
		
		Integer parentUrbanGroupId = 0;
    	
    	UrbanGroup parentUrbanGroup = new UrbanGroup();
    	int depth = 0;
    	if(urbanGroup.getParent() > 0) {
    		parentUrbanGroupId = urbanGroup.getParent();
    		parentUrbanGroup.setUrbanGroupId(parentUrbanGroupId);
    		parentUrbanGroup = urbanGroupMapper.getUrbanGroup(parentUrbanGroup);
	    	depth = parentUrbanGroup.getDepth() + 1;
    	}
    	
    	int result = urbanGroupMapper.insertUrbanGroup(urbanGroup); 
		
    	if(depth > 1) {
	    	// parent 의 children update
    		Integer children = parentUrbanGroup.getChildren();
    		if(children == null) children = 0;
    		children += 1;
    		
    		parentUrbanGroup = new UrbanGroup();
    		parentUrbanGroup.setUrbanGroupId(parentUrbanGroupId);
    		parentUrbanGroup.setChildren(children);
	    	return urbanGroupMapper.updateUrbanGroup(parentUrbanGroup);
    	}
    	
		return result; 
	}

	/**
	 * 데이터 그룹 표시 순서 수정 (up/down)
	 * @param urbanGroup
	 * @return
	 */
	@Transactional
	public int updateUrbanGroupViewOrder(UrbanGroup urbanGroup) {

		UrbanGroup dbUrbanGroup = urbanGroupMapper.getUrbanGroup(urbanGroup);
		dbUrbanGroup.setUpdateType(urbanGroup.getUpdateType());

		Integer modifyViewOrder = dbUrbanGroup.getViewOrder();
		UrbanGroup searchUrbanGroup = new UrbanGroup();
		searchUrbanGroup.setUpdateType(dbUrbanGroup.getUpdateType());
		searchUrbanGroup.setParent(dbUrbanGroup.getParent());

		if(Move.UP == Move.valueOf(dbUrbanGroup.getUpdateType())) {
			// 바로 위 메뉴의 view_order 를 +1
			searchUrbanGroup.setViewOrder(dbUrbanGroup.getViewOrder());
			searchUrbanGroup = getDataLayerByParentAndViewOrder(searchUrbanGroup);

			if(searchUrbanGroup == null) return 0;

			dbUrbanGroup.setViewOrder(searchUrbanGroup.getViewOrder());
			searchUrbanGroup.setViewOrder(modifyViewOrder);
		} else {
			// 바로 아래 메뉴의 view_order 를 -1 함
			searchUrbanGroup.setViewOrder(dbUrbanGroup.getViewOrder());
			searchUrbanGroup = getDataLayerByParentAndViewOrder(searchUrbanGroup);

			if(searchUrbanGroup == null) return 0;

			dbUrbanGroup.setViewOrder(searchUrbanGroup.getViewOrder());
			searchUrbanGroup.setViewOrder(modifyViewOrder);
		}

		updateViewOrderUrbanGroup(searchUrbanGroup);
		return updateViewOrderUrbanGroup(dbUrbanGroup);
	}

	/**
	 * 도시 그룹 수정
	 * @param urbanGroup
	 * @return
	 */
    @Transactional
	public int updateUrbanGroup(UrbanGroup urbanGroup) {
    	return urbanGroupMapper.updateUrbanGroup(urbanGroup);
    }

    /**
	 * 사용자 그룹 표시 순서 수정 (up/down)
	 * @param urbanGroup
	 * @return
	 */
	private int updateViewOrderUrbanGroup(UrbanGroup urbanGroup) {
		return urbanGroupMapper.updateUrbanGroupViewOrder(urbanGroup);
	}

    /**
	 * 도시 그룹 삭제
	 * TODO 넘 무식하다. recusive 로 바꿔라.
	 * @param urbanGroup
	 * @return
	 */
    @Transactional
	public int deleteUrbanGroup(UrbanGroup urbanGroup) {
    	// 삭제하고, children update

    	urbanGroup = urbanGroupMapper.getUrbanGroup(urbanGroup);

    	int result = 0;
    	if(Depth.ONE == Depth.findBy(urbanGroup.getDepth())) {
    		result = urbanGroupMapper.deleteUrbanGroupByAncestor(urbanGroup);
    	} else if(Depth.TWO == Depth.findBy(urbanGroup.getDepth())) {

    		UrbanGroup ancestorUrbanGroup = new UrbanGroup();
    		ancestorUrbanGroup.setUrbanGroupId(urbanGroup.getAncestor());
    		ancestorUrbanGroup = urbanGroupMapper.getUrbanGroup(ancestorUrbanGroup);
    		ancestorUrbanGroup.setChildren(ancestorUrbanGroup.getChildren() - 1);
	    	urbanGroupMapper.updateUrbanGroup(ancestorUrbanGroup);
	    	
	    	result = urbanGroupMapper.deleteUrbanGroupByParent(urbanGroup);
    		// ancestor - 1
    	} else if(Depth.THREE == Depth.findBy(urbanGroup.getDepth())) {

    		UrbanGroup parentDataGroup = new UrbanGroup();
	    	parentDataGroup.setUrbanGroupId(urbanGroup.getParent());
	    	parentDataGroup = urbanGroupMapper.getUrbanGroup(parentDataGroup);
	    	parentDataGroup.setChildren(parentDataGroup.getChildren() - 1);
	    	urbanGroupMapper.updateUrbanGroup(parentDataGroup);
	    	
	    	result = urbanGroupMapper.deleteUrbanGroup(urbanGroup);
    	} else {

    	}

    	return result;
    }
}
