package io.openindoormap.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@MapperScan("io.openindoormap.persistence")
public class RootConfig {
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		log.info(" ### RootConfig sqlSessionFactory ### ");
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/*.xml"));
		factory.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("mybatis-config.xml"));
		return factory.getObject();
    }
}