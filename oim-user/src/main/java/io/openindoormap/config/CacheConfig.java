package io.openindoormap.config;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CacheConfig {
	@PostConstruct
	public void init() {
		log.info("*************************************************");
		log.info("****** OpenIndoorMap USER Cache Init Start ******");
		log.info("*************************************************");
		
		policy();
		
		log.info("*************************************************");
		log.info("***** OpenIndoorMap USER Cache Init Finish ******");
		log.info("*************************************************");
    }
    
	/**
	 * 관리 정책을 5분 단위로 조회한다.
	 */
	private void policy() {
    }
}