package io.openindoormap.service;

/**
 * sensor data 처리를 위한 공통 인터페이스
 */
public interface SensorService {

    /**
     * 초기 센서 정보 init
     */
    void initSensorData();

    /**
     * 센서 데이터를 parsing 해서 insert
     */
    void insertSensorData();
}
