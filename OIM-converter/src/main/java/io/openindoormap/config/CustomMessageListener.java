package io.openindoormap.config;

import ch.qos.logback.core.net.SyslogOutputStream;
import io.openindoormap.conversion.PostProcess;
import io.openindoormap.domain.*;
import io.openindoormap.support.LogMessageSupport;
import io.openindoormap.support.ProcessBuilderSupport;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@EnableAsync
@PropertySource("classpath:openindoormap.properties")
@Component
public class CustomMessageListener {

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private PropertiesConfig propertiesConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Async
    @RabbitListener(queues = {"${openindoormap.queue-name}"})
    public void receiveMessage(final QueueMessage queueMessage) {
		log.info("--------------- queueMessage = {}", queueMessage);

		String osType = propertiesConfig.getOsType();
		final String PREFIX;
		if(OSType.WINDOW.name().equalsIgnoreCase(osType)) {
			PREFIX = "#";
		} else {
			PREFIX = "-";
		}

		CompletableFuture.supplyAsync( () -> {
			List<String> command = new ArrayList<>();
			command.add(propertiesConfig.getConverterDir());
			command.add(PREFIX + "inputFolder");
			command.add(queueMessage.getInputFolder());
			command.add(PREFIX + "outputFolder");
			command.add(queueMessage.getOutputFolder());
			command.add(PREFIX + "meshType");
			command.add(queueMessage.getMeshType());
			if (!StringUtils.isEmpty(queueMessage.getSkinLevel())) {
				command.add(PREFIX + "skinLevel");
				command.add(queueMessage.getSkinLevel());
			}
			command.add(PREFIX + "log");
			command.add(queueMessage.getLogPath());
			command.add(PREFIX + "indexing");
			command.add(queueMessage.getIndexing());
			command.add(PREFIX + "usf");
			command.add(queueMessage.getUsf().toString());
			command.add(PREFIX + "isYAxisUp");
			command.add(queueMessage.getIsYAxisUp());

			log.info(" >>>>>> command = {}", command.toString());

			String result;
			try {
				int exitCode = ProcessBuilderSupport.execute(command);
				if(exitCode == 0) result = ConverterJobStatus.SUCCESS.name().toLowerCase();
				else result = ConverterJobStatus.FAIL.name().toLowerCase();
			} catch(InterruptedException e1) {
				result = ConverterJobStatus.FAIL.name().toLowerCase();
				LogMessageSupport.printMessage(e1);
			} catch(IOException e1) {
				result = ConverterJobStatus.FAIL.name().toLowerCase();
				LogMessageSupport.printMessage(e1);
			} catch (Exception e1) {
				result = ConverterJobStatus.FAIL.name().toLowerCase();
				LogMessageSupport.printMessage(e1);
			}
			return result;
        })
		.exceptionally(e -> {
			handlerException(queueMessage, e.getMessage());
			log.info("exceptionally exception = {}", e.getMessage());
        	return null;

        })
		// 앞의 비동기 작업의 결과를 받아 사용하며 return이 없다.
		.thenAccept(status -> {
			log.info("thenAccept status = {}", status);
			try {
				if(ConverterType.DATA == queueMessage.getConverterType()) {
					// 데이터 변환
					ConverterJob converterJob = new ConverterJob();
					converterJob.setUserId(queueMessage.getUserId());
					converterJob.setConverterJobId(queueMessage.getConverterJobId());
					converterJob.setStatus(status);
					converterJob.setErrorCode(null);
					// 로그파일 전송
					PostProcess.execute(converterJob, objectMapper, propertiesConfig, restTemplate, queueMessage);
				} else {
					DataLibraryConverterJob dataLibraryConverterJob = new DataLibraryConverterJob();
					dataLibraryConverterJob.setUserId(queueMessage.getUserId());
					dataLibraryConverterJob.setDataLibraryConverterJobId(queueMessage.getDataLibraryConverterJobId());
					dataLibraryConverterJob.setStatus(status);
					dataLibraryConverterJob.setErrorCode(null);
					PostProcess.execute(dataLibraryConverterJob, objectMapper, propertiesConfig, restTemplate, queueMessage);
				}
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();

				// 로그파일 전송 오류 시 변환 실패 전송
				handlerException(queueMessage, e.getMessage());
				LogMessageSupport.printMessage(e);
			}

			log.info("thenAccept end");
		});
		log.info("receiveMessage end");
	}

	/**
	 * 예외 처리
	 * @param queueMessage
	 * @param message
	 */
	private void handlerException(QueueMessage queueMessage, String message) {
		if(ConverterType.DATA == queueMessage.getConverterType()) {
			// 데이터 변환
			ConverterJob converterJob = new ConverterJob();
			converterJob.setServerTarget(queueMessage.getServerTarget());
			converterJob.setUserId(queueMessage.getUserId());
			converterJob.setConverterJobId(queueMessage.getConverterJobId());
			converterJob.setStatus(ConverterJobStatus.FAIL.name().toLowerCase());
			converterJob.setErrorCode(message);

			PostProcess.executeException(converterJob, propertiesConfig, restTemplate);
		} else {
			// 데이터 라이브러리 변환
			DataLibraryConverterJob dataLibraryConverterJob = new DataLibraryConverterJob();
			dataLibraryConverterJob.setServerTarget(queueMessage.getServerTarget());
			dataLibraryConverterJob.setUserId(queueMessage.getUserId());
			dataLibraryConverterJob.setDataLibraryConverterJobId(queueMessage.getDataLibraryConverterJobId());
			dataLibraryConverterJob.setStatus(ConverterJobStatus.FAIL.name().toLowerCase());
			dataLibraryConverterJob.setErrorCode(message);

			PostProcess.executeException(dataLibraryConverterJob, propertiesConfig, restTemplate);
		}
	}
}
