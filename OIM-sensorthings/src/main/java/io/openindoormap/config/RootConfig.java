package io.openindoormap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.openindoormap.service.OccupancyService;
import io.openindoormap.service.impl.OccupancyServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RootConfig {

	@Bean(name="OccupancyService1")
	public OccupancyService occupancyService1() {
		return new OccupancyServiceImpl();
	}

	@Bean(name="OccupancyService2")
	public OccupancyService occupancyService2() {
		return new OccupancyServiceImpl();
	}
}
