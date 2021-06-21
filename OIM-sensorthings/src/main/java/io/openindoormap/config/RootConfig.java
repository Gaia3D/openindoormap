package io.openindoormap.config;

import io.openindoormap.service.AirQualityService;
import io.openindoormap.service.OccupancyService;
import io.openindoormap.service.impl.OccupancyServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;


@Slf4j
@Configuration
public class RootConfig {

	@Bean(name = "OccupancyService1")
	public OccupancyService occupancyService1() {
		return new OccupancyServiceImpl();
	}

	@Bean(name = "OccupancyService2")
	public OccupancyService occupancyService2() {
		return new OccupancyServiceImpl();
	}

	@Bean
	public RestTemplate geoserverRestTemplate(PropertiesConfig propertiesConfig, RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.rootUri("http://" + propertiesConfig.getGeoserverUrl() + "/geoserver/rest")
				.basicAuthentication(propertiesConfig.getGeoserverUser(), propertiesConfig.getGeoserverPassword())
				.build();
	}

	@Autowired
	private AirQualityService airQualityService;

	@Qualifier("OccupancyService1")
	@Autowired
	private OccupancyService occupancyService1;

	@Qualifier("OccupancyService2")
	@Autowired
	private OccupancyService occupancyService2;

	@PostConstruct
	public void ScheduleInit() {
		log.info("*************************************************");
		log.info("************** Schedule Init Start **************");
		log.info("*************************************************");
		/*
		airQualityService.setDryRun(false);
		airQualityService.initSensorData();

		String dataKey = "Alphadom_IndoorGML";
		occupancyService1.setDryRun(false);
		occupancyService1.setInterval(600);
		occupancyService1.initSensorData(dataKey);

		dataKey = "UOS21C_IndoorGML";
		occupancyService2.setDryRun(false);
		occupancyService2.setInterval(600);
		occupancyService2.initSensorData(dataKey);
		*/
		log.info("*************************************************");
		log.info("************** Schedule Init End ****************");
		log.info("*************************************************");
	}

}