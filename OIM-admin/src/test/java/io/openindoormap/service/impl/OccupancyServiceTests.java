package io.openindoormap.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.openindoormap.OIMAdminApplication;
import io.openindoormap.service.OccupancyService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OIMAdminApplication.class)
public class OccupancyServiceTests {
    @Qualifier("occupancyService")
    @Autowired
    private OccupancyService sensorService;

    @Test
    public void testInitSensorData() {
        sensorService.setDryRun(false);
        sensorService.initSensorData();
    }

    @Test
    public void testInsertSensorData() {
        sensorService.setDryRun(true);
        sensorService.insertSensorData();
    }
}
