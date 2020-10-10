package io.openindoormap.service.impl;

import io.openindoormap.domain.extrusionmodel.DataLibrary;
import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;
import io.openindoormap.persistence.DataLibraryGroupMapper;
import io.openindoormap.service.DataLibraryGroupService;
import io.openindoormap.service.DataLibraryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class DataLibraryGroupServiceImpl implements DataLibraryGroupService {

	@Autowired
	private DataLibraryService dataLibraryService;
	private final DataLibraryGroupMapper dataLibraryGroupMapper;


	@Transactional(readOnly = true)
	public List<DataLibraryGroup> getListDataLibraryGroup(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.getListDataLibraryGroup(dataLibraryGroup);
	}

	/**
	 * 모든 데이러 라이브러리 그룹에 속하는 데이터 라이브러리 목록
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<DataLibraryGroup> getListDataLibraryGroupAndDataLibrary() {
		List<DataLibraryGroup> dataLibraryGroupList = getListDataLibraryGroup(new DataLibraryGroup());
		for(DataLibraryGroup dataLibraryGroup : dataLibraryGroupList) {
			List<DataLibrary> dataLibraryList = dataLibraryService.getListDataLibrary(DataLibrary.builder().dataLibraryGroupId(dataLibraryGroup.getDataLibraryGroupId()).build());
			dataLibraryGroup.setDataLibraryList(dataLibraryList);
		}
		return dataLibraryGroupList;
	}

	@Transactional(readOnly = true)
	public DataLibraryGroup getDataLibraryGroup(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.getDataLibraryGroup(dataLibraryGroup);
	}

	@Transactional(readOnly = true)
	public List<DataLibraryGroup> getChildrenDataLibraryGroupListByParent(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.getChildrenDataLibraryGroupListByParent(dataLibraryGroup);
	}

	@Transactional(readOnly = true)
	public List<DataLibraryGroup> getDataLibraryGroupListByDepth(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryGroupMapper.getDataLibraryGroupListByDepth(dataLibraryGroup);
	}
}
