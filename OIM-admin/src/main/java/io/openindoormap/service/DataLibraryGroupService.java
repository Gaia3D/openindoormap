package io.openindoormap.service;

import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;

import java.util.List;

public interface DataLibraryGroupService {
	
	/**
     * 데이터 라이브러리 그룹 목록
     * @param dataLibraryGroup
     * @return
     */
    List<DataLibraryGroup> getListDataLibraryGroup(DataLibraryGroup dataLibraryGroup);

    /**
     * 데이터 라이브러리 그룹 정보
     * @param dataLibraryGroup
     * @return
     */
    DataLibraryGroup getDataLibraryGroup(DataLibraryGroup dataLibraryGroup);

	/**
	 * 기본 데이터 라이브러리 그룹 정보 조회
	 * @return
	 */
	DataLibraryGroup getBasicDataLibraryGroup();
    
    /**
     * 부모와 표시 순서로 데이터 라이브러리 그룹 조회
     * @param dataLibraryGroup
     * @return
     */
    DataLibraryGroup getDataLibraryGroupByParentAndViewOrder(DataLibraryGroup dataLibraryGroup);

    /**
     * 데이터 라이브러리 그룹 Key 중복 확인
     * @param dataLibraryGroup
     * @return
     */
    Boolean isDataLibraryGroupKeyDuplication(DataLibraryGroup dataLibraryGroup);
    
    /**
     * 데이터 라이브러리 그룹 등록
     * @param dataLibraryGroup
     * @return
     */
    int insertDataLibraryGroup(DataLibraryGroup dataLibraryGroup);

	/**
	 * 기본 데이터 라이브러리 등록
	 * @param dataLibraryGroup
	 * @return
	 */
	int insertBasicDataLibraryGroup(DataLibraryGroup dataLibraryGroup);
    
    /**
	 * 데이터 라이브러리 그룹 수정
	 * @param dataLibraryGroup
	 * @return
	 */
	int updateDataLibraryGroup(DataLibraryGroup dataLibraryGroup);

	/**
	 * 데이터 라이브러리 그룹 표시 순서 수정. UP, DOWN
	 * @param dataLibraryGroup
	 * @return
	 */
	int updateDataLibraryGroupViewOrder(DataLibraryGroup dataLibraryGroup);

	/**
	 * 자식의 수를 + 또는 - 연산
	 */
	int updateDataLibraryGroupChildren(DataLibraryGroup dataLibraryGroup);

	/**
	 * 데이터 라이브러리 그룹 삭제
	 * @param dataLibraryGroup
	 * @return
	 */
	int deleteDataLibraryGroup(DataLibraryGroup dataLibraryGroup);
	
	/**
	 * ancestor를 이용하여 데이터 라이브러리 그룹 삭제
	 * @param dataLibraryGroup
	 * @return
	 */
	int deleteDataLibraryGroupByAncestor(DataLibraryGroup dataLibraryGroup);
	
	/**
	 * parent를 이용하여 데이터 라이브러리 그룹 삭제
	 * @param dataLibraryGroup
	 * @return
	 */
	int deleteDataLibraryGroupByParent(DataLibraryGroup dataLibraryGroup);
	
	/**
	 * 사용자 아이디를 이용한 데이터 라이브러리 삭제
	 * @param userId
	 * @return
	 */
	int deleteDataLibraryGroupByUserId(String userId);
}
