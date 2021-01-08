package io.openindoormap.schedule;

import io.openindoormap.service.OccupancyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 재실자 가상 데이터 갱신 스케줄러
 */
@EnableScheduling
@Slf4j
@Component
@ConditionalOnProperty(name = "openindoormap.mock-enable", havingValue = "true")
public class OccupancyScheduler {

    @Qualifier("OccupancyService1")
    @Autowired
    private OccupancyService occupancyService1;

    @Qualifier("OccupancyService2")
    @Autowired
    private OccupancyService occupancyService2;

    /**
     * 1분 주기로 재실자 데이터 추가(알파돔)
     */
    @Scheduled(cron = "${openindoormap.schedule.every-mins}")
    public void everyHoursScheduler1() {
        String dataKey = "Alphadom_IndoorGML";
        occupancyService1.setDryRun(false);
        occupancyService1.setInterval(60);
        occupancyService1.insertSensorData(dataKey);
    }

    /**
     * 10초 주기로 재실자 데이터 추가(시립대)
     */
    @Scheduled(cron = "${openindoormap.schedule.every-mins}")
    public void everyHoursScheduler2() {
        String dataKey = "UOS21C_IndoorGML";
        occupancyService2.setDryRun(false);
        occupancyService2.setInterval(60);
        occupancyService2.insertSensorData(dataKey);
    }

    /**
     * 하루 1번 Cell 정보 갱신(알파돔)
     */
    @Scheduled(cron = "${openindoormap.schedule.every-days}")
    public void everyDaysScheduler1() {
        String dataKey = "Alphadom_IndoorGML";
        occupancyService1.setDryRun(false);
        occupancyService1.initSensorData(dataKey);
    }

    /**
     * 하루 1번 Cell 정보 갱신(시립대)
     */
    @Scheduled(cron = "${openindoormap.schedule.every-days}")
    public void everyDaysScheduler2() {
        String dataKey = "UOS21C_IndoorGML";
        occupancyService2.setDryRun(false);
        occupancyService2.initSensorData(dataKey);
    }
}
