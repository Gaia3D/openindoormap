package io.openindoormap.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class MessageSourceConfig {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.KOREA);
        return localeResolver;
    }

    // @Bean("messageSource")
    // public MessageSource messageSource(@Value("${spring.messages.basename}") String basename,
    //         @Value("${spring.messages.encoding}") String encoding) {
    //     YamlMessageSource ms = new YamlMessageSource();
    //     ms.setBasename(basename);
    //     ms.setDefaultEncoding(encoding);
    //     ms.setAlwaysUseMessageFormat(true);
    //     ms.setUseCodeAsDefaultMessage(true);
    //     ms.setFallbackToSystemLocale(true);
    //     return ms;
    // }
}

// class YamlMessageSource extends ResourceBundleMessageSource {
//     @Override
//     protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
//         return ResourceBundle.getBundle(basename, locale, YamlResourceBundle.Control.INSTANCE);
//     }
// }