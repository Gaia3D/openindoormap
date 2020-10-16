package io.openindoormap.service;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * sensor 초기 데이터 생성 및 갱신
 */
public interface SensorService {

    /**
     * 초기 센서 정보 init
     * @throws IOException
     * @throws ParseException
     * @throws URISyntaxException
     */
    void initSensorData() throws IOException, ParseException, URISyntaxException;

    /**
     * 센서 정보를 parsing 해서 insert
     */
    void insertSensorData();
}
