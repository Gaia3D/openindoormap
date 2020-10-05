package io.openindoormap.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
// @EnableWebMvc
@Configuration
public class ServletConfig implements WebMvcConfigurer {
    @Autowired
    private PropertiesConfig propertiesConfig;

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        log.info(" @@@ ServletConfig addResourceHandlers @@@");
        
        registry.addResourceHandler("/f4d/**").addResourceLocations("file:" + propertiesConfig.getUserConverterDir());
        // super.addResourceHandlers(registry);
    }
}