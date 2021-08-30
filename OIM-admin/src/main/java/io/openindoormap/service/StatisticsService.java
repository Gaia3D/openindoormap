package io.openindoormap.service;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.statistics.StatisticsForYear;


/**
 * Sign in
 * @author jeongdae
 *
 */
@Repository
public interface StatisticsService {


    /**
     * 3D 데이터 등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsForYear> getStatisticsDataInfo(StatisticsForYear years);

    /**
     * 변환  등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsForYear> getStatisticsConverter(StatisticsForYear years);

    /**
     * upload  등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsForYear> getStatisticsUploadData(StatisticsForYear years);

    /**
     * 접근 이력
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsForYear> getStatisticsAccess(StatisticsForYear years);



}
