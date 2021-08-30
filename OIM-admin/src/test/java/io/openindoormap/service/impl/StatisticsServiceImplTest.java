package io.openindoormap.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.openindoormap.domain.statistics.StatisticsForYear;
import io.openindoormap.service.StatisticsService;

@SpringBootTest
@ActiveProfiles("dev")
class StatisticsServiceImplTest {

    @Autowired
    StatisticsService service;

    @Test
    void testGetStatisticsDataInfo() {

        int st = 2019, ed = 2024;
        int count = (ed - st + 1);
        StatisticsForYear year = StatisticsForYear.builder()
                                 .year(String.valueOf(st))
                                 .count(count * 12)
                                 .build();
        List<StatisticsForYear> l = service.getStatisticsDataInfo(year);
        assertEquals(count * 13 + 1, l.size());

    }

    @Test
    void testGetStatisticsConverter() {
        int st = 2019, ed = 2024;
        int count = (ed - st + 1);
        StatisticsForYear year = StatisticsForYear.builder()
                                 .year(String.valueOf(st))
                                 .count(count * 12)
                                 .build();
        List<StatisticsForYear> l = service.getStatisticsConverter(year);
        assertEquals(count * 13 + 1, l.size());
    }

    @Test
    void testGetStatisticsUploadData() {
        int st = 2019, ed = 2024;
        int count = (ed - st + 1);
        StatisticsForYear year = StatisticsForYear.builder()
                                 .year(String.valueOf(st))
                                 .count(count * 12)
                                 .build();
        List<StatisticsForYear> l = service.getStatisticsUploadData(year);
        assertEquals(count * 13 + 1, l.size());
    }

    @Test
    void testGetStatisticsAccess() {
        int st = 2019, ed = 2024;
        int count = (ed - st + 1);
        StatisticsForYear year = StatisticsForYear.builder()
                                 .year(String.valueOf(st))
                                 .count(count * 12)
                                 .build();
        List<StatisticsForYear> l = service.getStatisticsAccess(year);
        assertEquals(count * 13 + 1, l.size());
    }


}
