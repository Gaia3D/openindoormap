package io.openindoormap.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import io.openindoormap.domain.Key;

import java.util.Locale;

@Slf4j
public class LocaleUtils {

    public static Locale getUserLocale(HttpServletRequest request) {
        String lang = (String)request.getSession().getAttribute(Key.LANG.name());
        log.info("@@@@@@@@@@@ lang = {}", lang);
        if(lang == null || "".equals(lang)) {
            Locale myLocale = request.getLocale();
            lang = myLocale.getLanguage();
        }
        return new Locale(lang);
    }

}
