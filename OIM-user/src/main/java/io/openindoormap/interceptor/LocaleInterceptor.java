package io.openindoormap.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import io.openindoormap.domain.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Locale 관련 설정
 * Enum은 성능을 위해 사용하지 않음
 *  
 * @author jeongdae
 *
 */
@Slf4j
@Component
public class LocaleInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	String lang = (String)request.getSession().getAttribute(Key.LANG.name());
		if(lang == null || "".equals(lang)) {
			Locale myLocale = request.getLocale();
			lang = myLocale.getLanguage();
		}

		String accessibility = "ko-KR";
		if("ko".equals(lang)) {
			accessibility = "ko-KR";
		} else if("en".equals(lang)) {
			accessibility = "en-US";
		} else if("ja".equals(lang)) {
			accessibility = "ja-JP";
		} else {
			// TODO Because it does not support multilingual besides English and Japanese Based on English
			lang = "en";
			accessibility = "en-US";
		}

		request.setAttribute("lang", lang);
		request.setAttribute("accessibility", accessibility);

        return true;
    }
}
