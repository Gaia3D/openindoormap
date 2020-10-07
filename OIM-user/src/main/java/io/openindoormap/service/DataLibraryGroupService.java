package io.openindoormap.service;

import java.util.List;

import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;

public interface DataLibraryGroupService {

	/**
	 * 데이터 라이브러리 그룹 목록
	 * @param dataLibraryGroup 데이터라이브러리 그룹
	 * @return
	 */
	List<DataLibraryGroup> getListDataLibraryGroup(DataLibraryGroup dataLibraryGroup);

	/**
	 * 모든 데이러 라이브러리 그룹에 속하는 데이터 라이브러리 목록
 	 * @return
	 */
	List<DataLibraryGroup> getListDataLibraryGroupAndDataLibrary();

	/**
	 * 데이터 라이브러리 그룹 정보
	 * @param dataLibraryGroup
	 * @return
	 */
	DataLibraryGroup getDataLibraryGroup(DataLibraryGroup dataLibraryGroup);

	/**
	 * 나를 부모로 가지는 자식 데이터 그룹 목록을 취득
	 * @param dataLibraryGroup
	 * @return
	 */
	List<DataLibraryGroup> getChildrenDataLibraryGroupListByParent(DataLibraryGroup dataLibraryGroup);

	/**
	 * depth 에 해당하는 데이터 그룹 목록 정보
	 * @param dataLibraryGroup
	 * @return
	 */
	List<DataLibraryGroup> getDataLibraryGroupListByDepth(DataLibraryGroup dataLibraryGroup);
}
