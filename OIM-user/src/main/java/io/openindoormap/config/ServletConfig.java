package io.openindoormap.config;

import io.openindoormap.domain.ProfileType;
import io.openindoormap.interceptor.*;
import lombok.extern.slf4j.Slf4j;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import java.util.Collections;

//import nz.net.ultraq.thymeleaf.LayoutDialect;

@Slf4j
@EnableSwagger2
@EnableWebMvc
@Configuration
@ComponentScan(basePackages = { "io.openindoormap.api", "io.openindoormap.config", "io.openindoormap.controller.view",
		"io.openindoormap.controller.rest", "io.openindoormap.interceptor" }, includeFilters = {
				@Filter(type = FilterType.ANNOTATION, value = Component.class),
				@Filter(type = FilterType.ANNOTATION, value = Controller.class),
				@Filter(type = FilterType.ANNOTATION, value = RestController.class) })

public class ServletConfig implements WebMvcConfigurer {

	@Autowired
	private PropertiesConfig propertiesConfig;
	@Value("${spring.profiles.active}")
	private String profile;
	@Autowired
	private LocaleInterceptor localeInterceptor;
	@Autowired
	private CSRFHandlerInterceptor cSRFHandlerInterceptor;
	@Autowired
	private ConfigInterceptor configInterceptor;
	@Autowired
	private LogInterceptor logInterceptor;
	@Autowired
	private SecurityInterceptor securityInterceptor;

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		log.info(" @@@ ServletConfig addInterceptors @@@@ ");
		registry.addInterceptor(localeInterceptor).addPathPatterns("/**").excludePathPatterns("/f4d/**", "/guide/**",
				"/sample/**", "/css/**", "/geopolicies/**", "/externlib/**", "favicon*", "/images/**", "/js/**");
		registry.addInterceptor(securityInterceptor).addPathPatterns("/user-policy/**", "/data-group/**", "/map/**",
				"/upload-data/**", "/upload-datas/**", "/converter/**", "/converters/**", "data/list",
				"/data-log/list");
//				.excludePathPatterns("/f4d/**", "/sign/**", "/cache/reload", "/guide/**", "/geopolicies/**", "/sample/**", "/css/**", "/externlib/**", "favicon*", "/images/**", "/js/**");
		registry.addInterceptor(cSRFHandlerInterceptor).addPathPatterns("/**").excludePathPatterns("/f4d/**",
				"/sign/**", "/cache/reload", "/data-groups/view-order/*", "/layer-groups/view-order/*", "/upload-datas",
				"/issues", "/datas/**", "/guide/**", "/geopolicies/**", "/css/**", "/externlib/**", "favicon*",
				"/swagger-ui/**", "/images/**", "/js/**");
		registry.addInterceptor(logInterceptor).addPathPatterns("/**").excludePathPatterns("/f4d/**", "/sign/**",
				"/cache/reload", "/guide/**", "/geopolicies/**", "/css/**", "/externlib/**", "favicon*", "/images/**",
				"/js/**");
		registry.addInterceptor(configInterceptor).addPathPatterns("/**").excludePathPatterns("/f4d/**", "/sign/**",
				"/cache/reload", "/guide/**", "/geopolicies/**", "/sample/**", "/css/**", "/externlib/**", "favicon*",
				"/images/**", "/js/**");
	}

	@Bean
	public LayoutDialect layoutDialect() {
		return new LayoutDialect();
	}

	@Bean
	public LocaleResolver localeResolver() {
		return new SessionLocaleResolver();
	}

	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:/messages/messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	@Bean
	public MessageSourceAccessor getMessageSourceAccessor() {
		ReloadableResourceBundleMessageSource m = messageSource();
		return new MessageSourceAccessor(m);
	}

	/**
	 * anotation @Valid 를 사용하려면 이 빈을 생성해 줘야 함
	 */
	@Bean
	public LocalValidatorFactoryBean getValidator() {
		LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
		bean.setValidationMessageSource(messageSource());
		return bean;
	}
	
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("redirect:/data/map");
		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		log.info(" @@@ ServletConfig addResourceHandlers @@@");

		// F4D converter file 경로
		registry.addResourceHandler("/f4d/**").addResourceLocations("file:" + propertiesConfig.getDataServiceDir());
		registry.addResourceHandler("/f4d/sample/**").addResourceLocations("file:" + propertiesConfig.getGuideDataServiceDir());
		registry.addResourceHandler("/sample/json/**").addResourceLocations("classpath:static/sample/json/");
		registry.addResourceHandler("/sample/images/**").addResourceLocations("classpath:static/sample/images/");
		registry.addResourceHandler("/sample/wind/**").addResourceLocations("classpath:static/sample/wind/");
		if (ProfileType.LOCAL.toString().equalsIgnoreCase(profile)) {
			log.info(" @@@ ServletConfig addResourceHandlers profile is LOCAL @@@");
			registry.addResourceHandler("/css/**").addResourceLocations("file:src/main/resources/static/css/");
			registry.addResourceHandler("/externlib/**").addResourceLocations("file:src/main/resources/static/externlib/");
			registry.addResourceHandler("/images/**").addResourceLocations("file:src/main/resources/static/images/");
			registry.addResourceHandler("/js/**").addResourceLocations("file:src/main/resources/static/js/");
			registry.addResourceHandler("/docs/**").addResourceLocations("file:src/main/resources/static/docs/");
		} else {
			log.info(" @@@ ServletConfig addResourceHandlers profile is {} @@@", profile);
			registry.addResourceHandler("/css/**").addResourceLocations("classpath:static/css/");
			registry.addResourceHandler("/externlib/**").addResourceLocations("classpath:static/externlib/");
			registry.addResourceHandler("/images/**").addResourceLocations("classpath:static/images/");
			registry.addResourceHandler("/js/**").addResourceLocations("classpath:static/js/");
			registry.addResourceHandler("/docs/**").addResourceLocations("classpath:static/docs/");
		}
	}
	

	@Bean
	public RequestDataValueProcessor requestDataValueProcessor() {
		log.info(" @@@ ServletConfig requestDataValueProcessor @@@ ");
		return new CSRFRequestDataValueProcessor();
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	
	

	@Bean
    public Docket apiDocket() {

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.openindoormap.api"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo getApiInfo() {

        return new ApiInfoBuilder()
                .title("OPENINDOORMAP API Doc")
                .description("More description about the API")
                .version("1.0.0")
                .build();
    }

	@Bean
	public ServletContextInitializer clearJsessionid() {
		return servletContext -> {
			servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
			SessionCookieConfig sessionCookieConfig=servletContext.getSessionCookieConfig();
			sessionCookieConfig.setHttpOnly(true);
		};
	}

}
