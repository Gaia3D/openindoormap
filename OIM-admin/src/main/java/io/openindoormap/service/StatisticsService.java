package io.openindoormap.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import io.openindoormap.domain.statistics.StatisticsMonth;


/**
 * Sign in
 * @author jeongdae
 *
 */
@Transactional(readOnly = true)
public interface StatisticsService {


    /**
     * 3D 데이터 등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsMonth> getStatisticsDataInfo(StatisticsMonth years);

    /**
     * 변환  등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsMonth> getStatisticsConverter(StatisticsMonth years);

    /**
     * upload  등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsMonth> getStatisticsUploadData(StatisticsMonth years);

    /**
     * 접근 이력
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsMonth> getStatisticsAccess(StatisticsMonth years);



}
