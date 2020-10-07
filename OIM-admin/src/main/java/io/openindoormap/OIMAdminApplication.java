package io.openindoormap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import io.openindoormap.filter.XSSFilter;
import io.openindoormap.listener.OIMHttpSessionBindingListener;

import javax.servlet.http.HttpSessionBindingListener;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootApplication
public class OIMAdminApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(OIMAdminApplication.class, args);
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(OIMAdminApplication.class);
	}

	@Bean
	public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter() {
		FilterRegistrationBean<HiddenHttpMethodFilter> registrationBean = new FilterRegistrationBean<>(new HiddenHttpMethodFilter());

		//List<String> urls = getUrlList();
		//registrationBean.setUrlPatterns(urls);
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}

	@Bean
    public FilterRegistrationBean<XSSFilter> xSSFilter() {
		FilterRegistrationBean<XSSFilter> registrationBean = new FilterRegistrationBean<>(new XSSFilter());
		
		List<String> urls = getUrlList();
		
		registrationBean.setUrlPatterns(urls);
		//registrationBean.addUrlPatterns(/*);
		return registrationBean;
    }
	
	@Bean
	public HttpSessionBindingListener httpSessionBindingListener() {
		log.info(" $$$ OIMAdminApplication registerListener $$$ ");
		return new OIMHttpSessionBindingListener();
	}
	
	private List<String> getUrlList() {
		List<String> urls = new ArrayList<>();
		
		urls.add("/user/*");
		urls.add("/users/*");
		urls.add("/user-group/*");
		urls.add("/user-groups/*");
		urls.add("/data-group/*");
		urls.add("/data-groups/*");
		urls.add("/data/*");
		urls.add("/datas/*");
		urls.add("/upload-data/*");
		urls.add("/upload-datas/*");
		urls.add("/converter/*");
		urls.add("/converters/*");
		urls.add("/data-adjust-log/*");
		urls.add("/data-adjust-logs/*");
		urls.add("/data-log/*");
		urls.add("/data-logs/*");
		urls.add("/layer-group/*");
		urls.add("/layer-groups/*");
		urls.add("/layer/*");
		urls.add("/layers/*");
		urls.add("/policy/*");
		urls.add("/geopolicy/*");
		urls.add("/menus/*");
		urls.add("/widget/*");
		urls.add("/role/*");
		
		return urls;
	}
}
