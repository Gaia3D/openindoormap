package io.openindoormap.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.statistics.StatisticsMonth;


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
    List<StatisticsMonth> getStatisticsDataInfo(StatisticsMonth year);

    /**
     * 변환  등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsMonth> getStatisticsConverter(StatisticsMonth year);

    /**
     * upload  등록
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsMonth> getStatisticsUploadData(StatisticsMonth year);

    /**
     * 접근 이력
     * @param yyyy
     * @param count
     * @return
     */
    List<StatisticsMonth> getStatisticsAccess(StatisticsMonth year);



}
