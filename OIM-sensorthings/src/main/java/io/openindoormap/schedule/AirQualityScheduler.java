package io.openindoormap.schedule;

import io.openindoormap.service.AirQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 미세먼지 데이터 갱신 스케줄러
 */
@EnableScheduling
@Slf4j
@Component
@ConditionalOnProperty(name = "openindoormap.schedule-enable", havingValue = "true")
public class AirQualityScheduler {

    @Qualifier("airQualityService")
    @Autowired
    private AirQualityService airQualityService;

    /**
     * 1시간 주기로 미세먼지 데이터 추가
     */
    @Scheduled(cron = "${openindoormap.schedule.every-hours}")
    public void everyHoursScheduler() {
        airQualityService.setDryRun(false);
        airQualityService.insertSensorData();
    }

    /**
     * 매일 00:30 에 측정소 정보 갱신
     */
    @Scheduled(cron = "${openindoormap.schedule.every-days}")
    public void everyDaysScheduler() {
        airQualityService.setDryRun(false);
        airQualityService.initSensorData();
        airQualityService.insertStatisticsDaily();
    }

    /**
     * 매일 00:30에 Observation 정보 삭제
     */
    @Scheduled(cron = "${openindoormap.schedule.every-days}")
    public void deleteScheduler() {
        airQualityService.setDryRun(false);
        airQualityService.deleteSensorData();
    }

}
