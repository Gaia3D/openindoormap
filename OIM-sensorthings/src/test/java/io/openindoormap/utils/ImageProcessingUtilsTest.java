package io.openindoormap.utils;

import io.openindoormap.OIMSensorthingsApplication;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.GdalContourCommandParams;
import io.openindoormap.domain.GdalGridCommandParams;
import io.openindoormap.sensor.AirQualityObservedProperty;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OIMSensorthingsApplication.class)
class ImageProcessingUtilsTest {

    @Autowired
    private PropertiesConfig propertiesConfig;

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    private String pgInfo;
    private AirQualityObservedProperty observedProperty;
    private String dataTime;

    @BeforeEach
    void setUp() {
        pgInfo = ImageProcessingUtils.getPgInfo(propertiesConfig, url, username, password);
        observedProperty = AirQualityObservedProperty.PM10;
        dataTime = "2021-01-14 01:00";
    }

    @Disabled
    @DisplayName("미세먼지(점 데이터)를 격자로 만든다.")
    void rasterize() {
        String utcDateTime = 한국시간_세계표준시_변환(dataTime);
        GdalGridCommandParams params = 그리드_커맨드_파라미터(utcDateTime);
        List<String> commandList = ImageProcessingUtils.rasterize(propertiesConfig, params);
        log.info("@@@@@ commandList : {}", String.join(" ", commandList));
        //ProcessBuilderSupport.execute(commandList);
    }

    @Disabled
    @DisplayName("미세먼지 격자 데이터의 등치선을 만든다.")
    void vectorize() {
        GdalContourCommandParams params = 등치선_커맨드_파라미터();
        List<String> commandList = ImageProcessingUtils.vectorize(propertiesConfig, params);
        log.info("@@@@@ commandList : {}", String.join(" ", commandList));
        //ProcessBuilderSupport.execute(commandList);
    }

    @Disabled
    @DisplayName("미세먼지를 격자로 만든 후 등치선을 만든다.")
    void executeRasterizeAndVectorize() {
        String utcDateTime = 한국시간_세계표준시_변환(dataTime);

        GdalGridCommandParams gridParams = 그리드_커맨드_파라미터(utcDateTime);
        List<String> gridCommand = ImageProcessingUtils.rasterize(propertiesConfig, gridParams);

        GdalContourCommandParams contourParams = 등치선_커맨드_파라미터();
        List<String> contourCommand = ImageProcessingUtils.vectorize(propertiesConfig, contourParams);

        ImageProcessingUtils.executeRasterizeAndVectorize(propertiesConfig, gridCommand, contourCommand);
    }

    @Disabled
    @DisplayName("만들어진 자료를 저장소 경로에 복사한다.")
    void copyToDataSource() {
        ImageProcessingUtils.copyToDataSource(propertiesConfig);
    }

    private String 한국시간_세계표준시_변환(String dataTime) {
        LocalDateTime t = LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        ZonedDateTime zonedDateTime = ZonedDateTime.of(t.getYear(), t.getMonthValue(), t.getDayOfMonth(), t.getHour(), 0, 0, 0, ZoneId.of("Asia/Seoul"));
        String utcDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        log.info("@@@ utcDateTime : {}", utcDateTime);
        return utcDateTime;
    }

    private GdalGridCommandParams 그리드_커맨드_파라미터(String utcDateTime) {
        return new GdalGridCommandParams(utcDateTime, observedProperty, pgInfo);
    }

    private GdalContourCommandParams 등치선_커맨드_파라미터() {
        return new GdalContourCommandParams();
    }

}