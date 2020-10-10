package io.openindoormap.service.impl;

import io.openindoormap.domain.extrusionmodel.DataLibrary;
import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;
import io.openindoormap.domain.extrusionmodel.DataLibraryUpload;
import io.openindoormap.domain.extrusionmodel.DataLibraryUploadFile;
import io.openindoormap.domain.uploaddata.UploadDataFile;
import io.openindoormap.persistence.DataLibraryMapper;
import io.openindoormap.service.DataLibraryGroupService;
import io.openindoormap.service.DataLibraryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 데이터 라이브러리
 * @author jeongdae
 *
 */
@Slf4j
@Service
public class DataLibraryServiceImpl implements DataLibraryService {

	@Autowired
	private DataLibraryMapper dataLibraryMapper;
	
	@Autowired
	private DataLibraryGroupService dataLibraryGroupService;

	/**
	 * 데이터 라이브러리 수
	 * @param dataLibrary
	 * @return
	 */
	@Transactional(readOnly=true)
	public Long getDataLibraryTotalCount(DataLibrary dataLibrary) {
		return dataLibraryMapper.getDataLibraryTotalCount(dataLibrary);
	}
	
	/**
	 * 데이터 라이브러리 목록
	 * @param dataLibrary
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<DataLibrary> getListDataLibrary(DataLibrary dataLibrary) {
		return dataLibraryMapper.getListDataLibrary(dataLibrary);
	}
	
	/**
	 * 데이터 라이브러리 그룹에 포함되는 모든 데이터 라이브러리를 취득
	 * @param dataLibraryGroupId
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<DataLibrary> getListAllDataLibraryByDataLibraryGroupId(Integer dataLibraryGroupId) {
		return dataLibraryMapper.getListAllDataLibraryByDataLibraryGroupId(dataLibraryGroupId);
	}
	
	/**
	 * 데이터 라이브러리 정보 취득
	 * @param dataLibrary
	 * @return
	 */
	@Transactional(readOnly=true)
	public DataLibrary getDataLibrary(DataLibrary dataLibrary) {
		return dataLibraryMapper.getDataLibrary(dataLibrary);
	}
	
	/**
	 * 데이터 라이브러리 정보 취득
	 * @param dataLibrary
	 * @return
	 */
	@Transactional(readOnly=true)
	public DataLibrary getDataLibraryByDataLibraryKey(DataLibrary dataLibrary) {
		return dataLibraryMapper.getDataLibraryByDataLibraryKey(dataLibrary);
	}
	
	/**
	 * 최상위 root 데이터 라이브러리 정보 취득
	 * @param dataLibraryGroupId
	 * @return
	 */
	@Transactional(readOnly=true)
	public DataLibrary getRootDataLibraryByDataLibraryGroupId(Integer dataLibraryGroupId) {
		return dataLibraryMapper.getRootDataLibraryByDataLibraryGroupId(dataLibraryGroupId);
	}
	
	/**
	 * 데이터 라이브러리 정보 취득
	 * @param dataLibrary
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<DataLibrary> getDataLibraryByDataLibraryConverterJob(DataLibrary dataLibrary) {
		return dataLibraryMapper.getDataLibraryByDataLibraryConverterJob(dataLibrary);
	}

	/**
	 * 데이터 라이브러리 등록
	 * @param dataLibrary
	 * @return
	 */
	@Transactional
	public int insertDataLibrary(DataLibrary dataLibrary) {
		return dataLibraryMapper.insertDataLibrary(dataLibrary);
	}
	
	/**
	 * 데이터 라이브러리 수정
	 * @param dataLibrary
	 * @return
	 */
	@Transactional
	public int updateDataLibrary(DataLibrary dataLibrary) {
		return dataLibraryMapper.updateDataLibrary(dataLibrary);
	}
	
	/**
	 * 데이터 라이브러리 상태 수정
	 * @param dataLibrary
	 * @return
	 */
	@Transactional
	public int updateDataLibraryStatus(DataLibrary dataLibrary) {
		return dataLibraryMapper.updateDataLibraryStatus(dataLibrary);
	}
	
	/**
	 * 데이터 라이브러리 삭제
	 * @param dataLibrary
	 * @return
	 */
	@Transactional
	public int deleteDataLibrary(DataLibrary dataLibrary) {
		// 데이터 그룹 count -1
		dataLibrary = dataLibraryMapper.getDataLibrary(dataLibrary);
		
		DataLibraryGroup dataGroup = new DataLibraryGroup();
		dataGroup.setDataLibraryGroupId(dataLibrary.getDataLibraryGroupId());
		dataGroup = dataLibraryGroupService.getDataLibraryGroup(dataGroup);
		
		DataLibraryGroup tempDataLibraryGroup = DataLibraryGroup.builder()
				.dataLibraryGroupId(dataGroup.getDataLibraryGroupId())
				.dataLibraryCount(dataGroup.getDataLibraryCount() - 1).build();
		dataLibraryGroupService.updateDataLibraryGroup(tempDataLibraryGroup);
		
		return dataLibraryMapper.deleteDataLibrary(dataLibrary);
		// TODO 디렉토리도 삭제 해야 함
	}
	
	/**
	 * 일괄 데이터 라이브러리 삭제
	 * @param checkIds
	 * @return
	 */
	@Transactional
	public int deleteDataLibraryList(String userId, String checkIds) {
		// TODO sql in 으로 한번 query 가능 함. 수정해야 함
		
		String[] dataIds = checkIds.split(",");
		for(String dataId : dataIds) {
			DataLibrary dataLibrary = new DataLibrary();
			dataLibrary.setUserId(userId);
			dataLibrary.setDataLibraryId(Long.valueOf(dataId));
			return dataLibraryMapper.deleteDataLibrary(dataLibrary);
		}
		
		return checkIds.length();
	}

	/**
	 * 데이터 라이브러리 그룹을 이용한 데이터 라이브러리 삭제
	 * @param dataLibraryGroup
	 * @return
	 */
	@Transactional
	public int deleteDataLibraryByDataLibraryGroupId(DataLibraryGroup dataLibraryGroup) {
		return dataLibraryMapper.deleteDataLibraryByDataLibraryGroupId(dataLibraryGroup);
	}
	
	/**
	 * 데이터 라이브러리 삭제
	 * @param dataLibrary
	 * @return
	 */
	@Transactional
	public int deleteDataLibraryByConverterJob(DataLibrary dataLibrary) {
		return dataLibraryMapper.deleteDataLibraryByConverterJob(dataLibrary);
	}
	
	/**
	 * 사용자 아이디를 이용한 데이터 라이브러리 삭제
	 */
	@Transactional
	public int deleteDataLibraryByUserId(String userId) {
		return dataLibraryMapper.deleteDataLibraryByUserId(userId);
	}
}
