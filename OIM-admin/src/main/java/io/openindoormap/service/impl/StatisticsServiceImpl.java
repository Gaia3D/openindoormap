package io.openindoormap.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.openindoormap.domain.statistics.StatisticsMonth;
import io.openindoormap.persistence.StatisticsMapper;
import io.openindoormap.service.StatisticsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper mapper;

    @Override
    public List<StatisticsMonth> getStatisticsDataInfo(StatisticsMonth years) {
        return mapper.getStatisticsDataInfo(years);
    }

    @Override
    public List<StatisticsMonth> getStatisticsConverter(StatisticsMonth years) {
        return mapper.getStatisticsConverter(years);
    }

    @Override
    public List<StatisticsMonth> getStatisticsUploadData(StatisticsMonth years) {
        return mapper.getStatisticsUploadData(years);
    }

    @Override
    public List<StatisticsMonth> getStatisticsAccess(StatisticsMonth years) {
        return mapper.getStatisticsAccess(years);
    }

}
