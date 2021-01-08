package io.openindoormap.service;

/**
 * 미세 먼지 관련 인터페이스
 */
public interface AirQualityService extends SensorService {

    @Override
    void initSensorData();

    @Override
    void insertSensorData();

    void insertStatisticsDaily();

    public Boolean getDryRun();

    public void setDryRun(Boolean dryRun);
}
