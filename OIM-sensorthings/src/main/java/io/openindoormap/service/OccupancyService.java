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

    void initSensorData(String dataKey);

    void insertSensorData(String dataKey);

    public long getInterval();

    public void setInterval(long interval);
}
