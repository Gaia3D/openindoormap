package io.openindoormap.interceptor;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import io.openindoormap.domain.Key;

/**
 * Locale 관련 설정
 * Enum은 성능을 위해 사용하지 않음
 *
 * @author jeongdae
 *
 */
@Component
public class LocaleInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private LocaleResolver localeResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	String lang = (String)request.getParameter("lang");
		if(lang != null && !"".equals(lang)) {
			localeResolver.setLocale(request, response, new Locale(lang));
		} else {
			Locale local = localeResolver.resolveLocale(request);
			lang = local.getLanguage();
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
