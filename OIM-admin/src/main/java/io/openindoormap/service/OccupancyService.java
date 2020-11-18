package io.openindoormap.service;

/**
 * 재실자 관련 인터페이스
 */
public interface OccupancyService extends SensorService {

    @Override
    void initSensorData();

    @Override
    void insertSensorData();

    public Boolean getDryRun();

    public void setDryRun(Boolean dryRun);

    void initSensorData(Long dataId);

    void insertSensorData(Long dataId);
}
