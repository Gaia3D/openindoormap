package io.openindoormap.service;

/**
 * 미세 먼지 관련 인터페이스
 */
public interface AirQualityService extends SensorService {

    @Override
    void initSensorData();

    @Override
    void insertSensorData();

    /**
     * 하루(1시간 간격 24시간) mock 데이터 생성
     */
    void initDailyMockData();

}
