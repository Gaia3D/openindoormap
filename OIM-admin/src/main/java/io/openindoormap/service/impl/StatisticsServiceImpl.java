package io.openindoormap.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.openindoormap.domain.statistics.StatisticsForYear;
import io.openindoormap.persistence.StatisticsMapper;
import io.openindoormap.service.StatisticsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper mapper;

    @Override
    public List<StatisticsForYear> getStatisticsDataInfo(StatisticsForYear years) {
        return mapper.getStatisticsDataInfo(years);
    }

    @Override
    public List<StatisticsForYear> getStatisticsConverter(StatisticsForYear years) {
        return mapper.getStatisticsConverter(years);
    }

    @Override
    public List<StatisticsForYear> getStatisticsUploadData(StatisticsForYear years) {
        return mapper.getStatisticsUploadData(years);
    }

    @Override
    public List<StatisticsForYear> getStatisticsAccess(StatisticsForYear years) {
        return mapper.getStatisticsAccess(years);
    }

}
