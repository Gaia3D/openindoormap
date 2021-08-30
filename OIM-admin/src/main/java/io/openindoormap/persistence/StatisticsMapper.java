package io.openindoormap.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.statistics.StatisticsForYear;


/**
 * Sign in
 * @author jeongdae
 *
 */
@Repository
public interface StatisticsMapper {


    /**
     * 3D 데이터 등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsForYear> getStatisticsDataInfo(StatisticsForYear year);

    /**
     * 변환  등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsForYear> getStatisticsConverter(StatisticsForYear year);

    /**
     * upload  등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsForYear> getStatisticsUploadData(StatisticsForYear year);

    /**
     * 접근 이력
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsForYear> getStatisticsAccess(StatisticsForYear year);



}
