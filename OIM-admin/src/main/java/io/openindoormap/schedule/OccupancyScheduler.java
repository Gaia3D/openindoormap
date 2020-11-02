package io.openindoormap.schedule;

import io.openindoormap.service.AirQualityService;
import io.openindoormap.service.OccupancyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 재실자 가상 데이터 갱신 스케줄러
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "openindoormap.mock-enable", havingValue = "false")
public class OccupancyScheduler {

    @Qualifier("occupancyService")
    @Autowired
    private OccupancyService occupancyService;

    /**
     * 1분 주기로 재실자 데이터 추가
     */
    @Scheduled(cron = "${openindoormap.schedule.every-mins}")
    public void everyHoursScheduler() {
        occupancyService.setDryRun(true);
        occupancyService.insertSensorData();
    }

    /**
     * 하루 1번 Cell 정보 갱신
     */
    @Scheduled(cron = "${openindoormap.schedule.every-days}")
    public void everyDaysScheduler() {
        occupancyService.setDryRun(true);
        occupancyService.initSensorData();
    }
}
