package io.openindoormap.schedule;

import io.openindoormap.service.OccupancyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 재실자 가상 데이터 갱신 스케줄러
 */
@EnableAsync
@EnableScheduling
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
    //@Async
    @Scheduled(fixedDelay = 30000)
    //@Scheduled(cron = "${openindoormap.schedule.every-mins}")
    public void everyHoursScheduler() {
        occupancyService.setDryRun(false);
        occupancyService.insertSensorData();
    }

    /**
     * 하루 1번 Cell 정보 갱신
     */
    //@Scheduled(cron = "${openindoormap.schedule.every-days}")
    public void everyDaysScheduler() {
        occupancyService.setDryRun(false);
        occupancyService.initSensorData();
    }
}
