package io.openindoormap.service;

import io.openindoormap.domain.agent.ConverterResultLog;
import io.openindoormap.domain.agent.DataLibraryConverterResultLog;
import io.openindoormap.domain.extrusionmodel.DataLibraryConverterJob;
import io.openindoormap.domain.extrusionmodel.DataLibraryConverterJobFile;

import java.util.List;

/**
 * 자동 변환
 * @author Cheon JeongDae
 *
 */
public interface DataLibraryConverterService {
	
	/**
	 * 자동 변환 job 총 건수
	 * @param dataLibraryConverterJob dataLibraryConverterJob
	 * @return converter job 총 건수
	 */
	Long getDataLibraryConverterJobTotalCount(DataLibraryConverterJob dataLibraryConverterJob);
	
	/**
	 * 자동 변환 job file 총 건수
	 * @param dataLibraryConverterJobFile dataLibraryConverterJobFile
	 * @return converter job file 총 건수
	 */
	Long getDataLibraryConverterJobFileTotalCount(DataLibraryConverterJobFile dataLibraryConverterJobFile);
	
	/**
	 * 자동 변환 job 목록
	 * @param dataLibraryConverterJob dataLibraryConverterJob
	 * @return f4d converter job 목록
	 */
	List<DataLibraryConverterJob> getListDataLibraryConverterJob(DataLibraryConverterJob dataLibraryConverterJob);
	
	/**
	 * 자동 변환 job file 목록
	 * @param dataLibraryConverterJobFile dataLibraryConverterJobFile
	 * @return f4d converter job file 목록
	 */
	List<DataLibraryConverterJobFile> getListDataLibraryConverterJobFile(DataLibraryConverterJobFile dataLibraryConverterJobFile);

	/**
	 * 자동 변환 job 등록
	 * @param dataLibraryConverterJob    dataLibraryConverterJob
	 */
	void insertDataLibraryConverter(DataLibraryConverterJob dataLibraryConverterJob);

	/**
	 *
	 * @param dataLibraryConverterJob
	 */
	void updateDataLibraryConverterJob(DataLibraryConverterJob dataLibraryConverterJob);

	/**
	 * 로그파일을 통한 데이터 변환 작업 상태를 갱신
	 * @param dataLibraryConverterResultLog
	 */
	void updateDataLibraryConverterJobStatus(DataLibraryConverterResultLog dataLibraryConverterResultLog);

}
