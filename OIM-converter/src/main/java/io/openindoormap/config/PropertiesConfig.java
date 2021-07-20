package io.openindoormap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Data
@Configuration
@PropertySource("classpath:openindoormap.properties")
@ConfigurationProperties(prefix = "openindoormap")
public class PropertiesConfig {
	
	/**
	 * 로컬 : local, 개발 : develop, 운영 : product
	 */
	private String profile;
	
	private String osType;
	private boolean callRemoteEnable;
	private String serverIp;
	private String serverPort;
	private String restAuthKey;
	
	// 사용자 서버
	private String cmsUserRestServer;
	// 관리자 서버
	private String cmsAdminRestServer;
	
	private String queueServerHost;
	private String queueServerPort;
	private String queueUser;
	private String queuePassword;
	private String queueName;
	private String exchange;
	private String routingKey;

	// User excel batch registration
	private String dataUploadDir;
	
	// F4D Converter.exe 가 있는 경로
	private String converterDir;	
	
}