package io.openindoormap.service;

import io.openindoormap.domain.agent.ConverterResultLog;
import io.openindoormap.domain.converter.ConverterJob;
import io.openindoormap.domain.converter.ConverterJobFile;

import java.util.List;

/**
 * f4d converting manager
 * @author Cheon JeongDae
 *
 */
public interface ConverterService {
	
	/**
	 * converter job 총 건수
	 * @param converterJob converterJob
	 * @return converter job 총 건수
	 */
	Long getConverterJobTotalCount(ConverterJob converterJob);
	
	/**
	 * converter job file 총 건수
	 * @param converterJobFile converterJobFile
	 * @return converter job file 총 건수
	 */
	Long getConverterJobFileTotalCount(ConverterJobFile converterJobFile);
	
	/**
	 * f4d converter job 목록
	 * @param converterJob converterJob
	 * @return f4d converter job 목록
	 */
	List<ConverterJob> getListConverterJob(ConverterJob converterJob);
	
	/**
	 * f4d converter job file 목록
	 * @param converterJobFile converterJobFile
	 * @return f4d converter job file 목록
	 */
	List<ConverterJobFile> getListConverterJobFile(ConverterJobFile converterJobFile);

	/**
	 * 데이터 변환 현황
	 * @return 데이터 변환 현황
	 */
	List<ConverterJobFile> getConverterJobFileStatistics();

	/**
	 * f4d converter 변환 job 등록
	 * @param converterJob    converterJob
	 */
	void insertConverter(ConverterJob converterJob);

	/**
	 *
	 * @param converterJob
	 */
	void updateConverterJob(ConverterJob converterJob);

	/**
	 * 로그파일을 통한 데이터 변환 작업 상태를 갱신
	 * @param converterResultLog converterResultLog
	 */
	void updateConverterJobStatus(ConverterResultLog converterResultLog);

}
