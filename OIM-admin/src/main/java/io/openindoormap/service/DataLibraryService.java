package io.openindoormap.service;

import io.openindoormap.domain.extrusionmodel.DataLibrary;
import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;
import io.openindoormap.domain.extrusionmodel.DataLibraryUpload;
import io.openindoormap.domain.extrusionmodel.DataLibraryUploadFile;
import io.openindoormap.domain.uploaddata.UploadData;
import io.openindoormap.domain.uploaddata.UploadDataFile;

import java.util.List;

/**
 * 데이터 라이브러리 관리
 * @author jeongdae
 *
 */
public interface DataLibraryService {

	/**
	 * 데이터 라이브러리 수
	 * @param dataLibrary
	 * @return
	 */
	Long getDataLibraryTotalCount(DataLibrary dataLibrary);

	/**
	 * 데이터 라이브러리 목록
	 * @param dataLibrary
	 * @return
	 */
	List<DataLibrary> getListDataLibrary(DataLibrary dataLibrary);

	/**
	 * 데이터 라이브러리 그룹에 포함되는 모든 데이터 라이브러리를 취득
	 * @param dataGroupId
	 * @return
	 */
	List<DataLibrary> getListAllDataLibraryByDataLibraryGroupId(Integer dataGroupId);
	
	/**
	 * 데이터 라이브러리 정보 취득
	 * @param dataLibrary
	 * @return
	 */
	DataLibrary getDataLibrary(DataLibrary dataLibrary);
	
	/**
	 * 데이터 라이브러리 정보 취득
	 * @param dataLibrary
	 * @return
	 */
	DataLibrary getDataLibraryByDataLibraryKey(DataLibrary dataLibrary);
	
	/**
	 * 데이터 라이브러리 정보 취득
	 * @param dataLibrary
	 * @return
	 */
	List<DataLibrary> getDataLibraryByDataLibraryConverterJob(DataLibrary dataLibrary);

	/**
	 * 데이터 라이브러리 등록
	 * @param dataLibrary
	 * @return
	 */
	int insertDataLibrary(DataLibrary dataLibrary);
	
	/**
	 * 데이터 라이브러리 수정
	 * @param dataLibrary
	 * @return
	 */
	int updateDataLibrary(DataLibrary dataLibrary);
	
	/**
	 * 데이터 라이브러리 상태 수정
	 * @param dataLibrary
	 * @return
	 */
	int updateDataLibraryStatus(DataLibrary dataLibrary);
	
	/**
	 * 데이터 라이브러리 삭제
	 * @param dataLibrary
	 * @return
	 */
	int deleteDataLibrary(DataLibrary dataLibrary);
	
	/**
	 * 일괄 데이터 라이브러리 삭제
	 * @param userId
	 * @param dataIds
	 * @return
	 */
	int deleteDataLibraryList(String userId, String dataIds);

	/**
	 * 데이터 라이브러리 그룹을 이용한 데이터 라이브러리 삭제
	 * @param dataLibraryGroup
	 * @return
	 */
	int deleteDataLibraryByDataLibraryGroupId(DataLibraryGroup dataLibraryGroup);
	
	/**
	 * 데이터 라이브러리 삭제
	 * @param dataLibrary
	 * @return
	 */
	int deleteDataLibraryByConverterJob(DataLibrary dataLibrary);
	
	/**
	 * 사용자 아이디를 이용한 데이터 라이브러리 삭제
	 * @param userId
	 * @return
	 */
	int deleteDataLibraryByUserId(String userId);
}
