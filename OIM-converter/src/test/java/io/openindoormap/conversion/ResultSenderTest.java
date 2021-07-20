package io.openindoormap.conversion;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.openindoormap.OIMConverterApplication;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OIMConverterApplication.class)
@SpringBootTest
class ResultSenderTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PropertiesConfig propertiesConfig;

    @Autowired
    private RestTemplate restTemplate;

    private ConverterJob converterJob;
    
    @BeforeEach
    void setUp() {
        converterJob = new ConverterJob();
        converterJob.setConverterJobFileId(1L);
        converterJob.setUserId("admin");
        converterJob.setConverterJobId(1L);
        converterJob.setStatus("success");
        converterJob.setErrorCode(null);
    }

    @Test
    void sendLog() {

//        String outFolder = "src/test/resources/";
//        String logPath = outFolder + "logTest_32.txt";
//        ServerTarget target = ServerTarget.ADMIN;
//
//        QueueMessage queueMessage = new QueueMessage();
//        queueMessage.setOutputFolder(outFolder);
//        queueMessage.setLogPath(logPath);
//        queueMessage.setServerTarget(target);
//        queueMessage.setUploadDataType(UploadDataType.CITYGML);
//
////        생성자 대신 정적 팩터리 매서드를 고려하라. (EffectiveJava 8page)
////        static 메서드와 인스턴스 메서드. (Java의 정석 1, 188~191page)
//        try {
//            // 로그파일 전송
//            PostProcess.execute(converterJob, objectMapper, propertiesConfig, restTemplate, queueMessage);
//        } catch (IOException | URISyntaxException e) {
//            // 로그파일 전송 오류 시 변환 실패 전송
//            converterJob.setStatus(ConverterJobStatus.FAIL.name().toLowerCase());
//            converterJob.setErrorCode(e.getMessage());
//            PostProcess.executeException(converterJob, propertiesConfig, restTemplate, target);
//            LogMessageSupport.printMessage(e);
//        }

    }

    @Test
    void sendConverterJobStatus() {
//        ServerTarget target = ServerTarget.ADMIN;
//        PostProcess.sendConverterJobStatus(converterJob, propertiesConfig, restTemplate, target);
    }

}