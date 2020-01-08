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
	 * 관리자 서버
	 */
	private String restServer;
}