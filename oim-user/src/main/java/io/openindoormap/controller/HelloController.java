package io.openindoormap.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/hello")
public class HelloController
{

    @Autowired
    LocaleResolver localeResolver;

    @Autowired
    MessageSource messageSource;

    @GetMapping("/")
    public String hello()
    {
        return "index";
    }

    @GetMapping("/i18n")
    public String i18n(Locale locale, HttpServletRequest request)
    {
        // RequestMapingHandler로 부터 받은 Locale 객체를 출력
        log.info("Welcome i18n! The client locale is {}.", locale);

        // localeResolver 로부터 Locale 을 출력
        log.info("Session locale is {}.", localeResolver.resolveLocale(request));

        //log.info(messageSource.getMessage("greeting", null, locale));
        return "message/index";
    }
}