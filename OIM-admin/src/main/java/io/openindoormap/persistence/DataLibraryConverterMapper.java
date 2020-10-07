package io.openindoormap.persistence;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.extrusionmodel.*;

import java.util.List;

/**
 * 데이터 라이브러리 변환
 * @author jeongdae
 *
 */
@Repository
public interface DataLibraryConverterMapper {

    /**
     * 데이터 라이브러리 converter job 총 건수
     * @param dataLibraryConverterJob
     * @return
     */
    Long getDataLibraryConverterJobTotalCount(DataLibraryConverterJob dataLibraryConverterJob);

    /**
     * 데이터 라이브러리 converter job file 총 건수
     * @param dataLibraryConverterJobFile
     * @return
     */
    Long getDataLibraryConverterJobFileTotalCount(DataLibraryConverterJobFile dataLibraryConverterJobFile);

    /**
     * 데이터 라이브러리  converter job 목록
     * @param dataLibraryConverterJob
     * @return
     */
    List<DataLibraryConverterJob> getListDataLibraryConverterJob(DataLibraryConverterJob dataLibraryConverterJob);

    /**
     * 데이터 라이브러리  converter job 파일 목록
     * @param dataLibraryConverterJobFile
     * @return
     */
    List<DataLibraryConverterJobFile> getListDataLibraryConverterJobFile(DataLibraryConverterJobFile dataLibraryConverterJobFile);

    /**
     * 데이터 라이브러리 converter job에 해당하는 f4d converter job 파일 목록
     * @param dataLibraryConverterJob
     * @return
     */
    List<DataLibraryConverterJobFile> getListDataLibraryConverterJobFileByParent(DataLibraryConverterJob dataLibraryConverterJob);

    /**
     * 데이터 라이브러리  converter job 등록
     * @param dataLibraryConverterJob
     * @return
     */
    Long insertDataLibraryConverterJob(DataLibraryConverterJob dataLibraryConverterJob);

    /**
     * 데이터 라이브러리  converter job file 등록
     * @param dataLibraryConverterJobFile
     * @return
     */
    Long insertDataLibraryConverterJobFile(DataLibraryConverterJobFile dataLibraryConverterJobFile);

    /**
     * 데이터 라이브러리  converter job 수정
     * @param dataLibraryConverterJob
     * @return
     */
    int updateDataLibraryConverterJob(DataLibraryConverterJob dataLibraryConverterJob);

    /**
     * 데이터 라이브러리  converter job file 수정
     * @param dataLibraryConverterJobFile
     * @return
     */
    int updateDataLibraryConverterJobFile(DataLibraryConverterJobFile dataLibraryConverterJobFile);
}
