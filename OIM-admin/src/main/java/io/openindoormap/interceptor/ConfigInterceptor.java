package io.openindoormap.interceptor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import io.openindoormap.domain.Key;
import io.openindoormap.domain.YOrN;
import io.openindoormap.domain.cache.CacheManager;
import io.openindoormap.domain.menu.Menu;
import io.openindoormap.domain.policy.Policy;
import io.openindoormap.domain.user.UserGroupMenu;
import io.openindoormap.domain.user.UserSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 사이트 전체 설정 관련 처리를 담당
 *  
 * @author jeongdae
 *
 */
@Slf4j
@Component
public class ConfigInterceptor extends HandlerInterceptorAdapter {
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//		log.info("**** 버그 추적용 ConfigInterceptor ****");
    	
    	String uri = request.getRequestURI();
    	HttpSession session = request.getSession();
    	
    	Policy policy = CacheManager.getPolicy();
    	
    	// TODO 너무 비 효율 적이다. 좋은 방법을 찾자.
    	// 세션이 존재하지 않는 경우
    	UserSession userSession = (UserSession)session.getAttribute(Key.USER_SESSION.name());
		if(userSession != null && userSession.getUserId() != null && !"".equals(userSession.getUserId())) {
	    	List<UserGroupMenu> userGroupMenuList = CacheManager.getUserGroupMenuList(userSession.getUserGroupId());
	    	Integer clickParentId = null;
			Integer clickMenuId = null;
			Integer clickDepth = null;

			for(UserGroupMenu userGroupMenu : userGroupMenuList) {
				if(uri.equals(userGroupMenu.getUrl())) {
					clickMenuId = userGroupMenu.getMenuId();
					if(userGroupMenu.getDepth() == 1) {
						clickParentId = userGroupMenu.getMenuId();
					} else {
						clickParentId = Integer.valueOf(userGroupMenu.getParent().toString());
					}
					clickDepth = userGroupMenu.getDepth();
					
					if( userGroupMenu.getDepth() == 1 && (uri.indexOf("/main/index")>=0) ) {
						break;
					} else if( userGroupMenu.getDepth() == 2) {
						break;
					} else {
						// pass
					}
				}
			}
			
			Menu menu = CacheManager.getMenuMap().get(clickMenuId);
			Menu parentMenu = CacheManager.getMenuMap().get(clickParentId);
			if(menu != null) {
				if(YOrN.Y == YOrN.valueOf(menu.getDisplayYn())) {
					menu.setAliasName(null);
					parentMenu.setAliasName(null);
				} else {
					Integer aliasMenuId = CacheManager.getMenuUrlMap().get(menu.getUrlAlias());
					Menu aliasMenu = CacheManager.getMenuMap().get(aliasMenuId);
					menu.setAliasName(aliasMenu.getName());
					menu.setAliasMenuId(aliasMenuId);
					parentMenu.setAliasName(aliasMenu.getName());
				}
			}
			
//			Integer contentLoadBalancingIntervalValue = policy.getContent_load_balancing_interval().intValue() * 1000;
//			request.setAttribute("contentLoadBalancingInterval", contentLoadBalancingIntervalValue);
			
			request.setAttribute("clickMenuId", clickMenuId);
//			request.setAttribute("clickParentId", clickParentId);
//			request.setAttribute("clickDepth", clickDepth);
			request.setAttribute("menu", menu);
			request.setAttribute("parentMenu", parentMenu);

			log.info("+++++++++++++++++++++++ clickMenuId = {}", clickMenuId);
			log.info("+++++++++++++++++++++++ menu = {}", menu);
			log.info("+++++++++++++++++++++++ parentMenu = {}", parentMenu);
			
			request.setAttribute("cacheUserGroupMenuList", userGroupMenuList);
			request.setAttribute("cacheUserGroupMenuListSize", userGroupMenuList.size());
			request.setAttribute("contentCacheVersion", policy.getContentCacheVersion());
    	}
    	
        return true;
    }
}
