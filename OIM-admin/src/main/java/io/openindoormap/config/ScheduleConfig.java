package io.openindoormap.config;

import io.openindoormap.service.AirQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class ScheduleConfig {

    @Qualifier("airQualityService")
    @Autowired
    private AirQualityService airQualityService;
    @Autowired
    private PropertiesConfig propertiesConfig;

    @PostConstruct
    public void ScheduleInit() {
        log.info("*************************************************");
        log.info("************** Schedule Init Start **************");
        log.info("*************************************************");

        // 실제 운영 서버 일때만 실행(mock-enable=false)
        if(!propertiesConfig.isMockEnable()) {
            airQualityService.initSensorData();
            airQualityService.insertSensorData();
        }

        log.info("*************************************************");
        log.info("************** Schedule Init End ****************");
        log.info("*************************************************");
    }
}
