package io.openindoormap.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.CacheManager;
import io.openindoormap.domain.Project;
import io.openindoormap.domain.UserPolicy;
import io.openindoormap.domain.UserSession;
import io.openindoormap.service.UserPolicyService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/settings/")
public class SettingsController {
	
	@Autowired
	private PropertiesConfig propertiesConfig;
	
	@Autowired
	private UserPolicyService userPolicyService;
	
	/**
	 * userPolicy 수정화면
	 * @param model
	 * @return
	 */
	@GetMapping(value = "modify-membership")
	public String modifyMemberShip(HttpServletRequest request, Model model) {
		return "settings/modify-membership";
	}
	
	/**
	 * userPolicy 수정화면
	 * @param model
	 * @return
	 */
	@GetMapping(value = "modify-user-policy")
	public String modifyUserPolicy(HttpServletRequest request, Model model) {
		
		UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
		// UserPolicy userPolicy = userPolicyService.getUserPolicy(userSession.getUser_id());
		UserPolicy userPolicy = userPolicyService.getUserPolicy("guest");
		
		String defaultProjects = userPolicy.getGeo_data_default_projects();
		if(defaultProjects != null && !"".equals(defaultProjects)) {
			String[] projectIds = defaultProjects.split(",");
			Map<Integer, Project> projectMap = CacheManager.getProjectMap();
			String defaultProjectsView = "";
			for(String projectId : projectIds) {
				Project project = projectMap.get(Integer.valueOf(projectId));
				if("".equals(defaultProjectsView)) {
					defaultProjectsView += project.getProject_name();
				} else {
					defaultProjectsView += "," + project.getProject_name();
				}
			}
			userPolicy.setGeo_data_default_projects_view(defaultProjectsView);
			
		}
		log.debug(userPolicy.toString());;
		model.addAttribute("userPolicy", userPolicy);
		
		return "settings/modify-user-policy";
	}
	
	/**
	 * userPolicy update
	 * @param request
	 * @param userPolicy
	 * @return
	 */
	@PostMapping(value = "ajax-update-user-policy")
	@ResponseBody
	public Map<String, String> ajaxUpdateUserPolicy(HttpServletRequest request, UserPolicy userPolicy) {
		log.info("@@ userPolicy = {} ", userPolicy);
		
		Map<String, String> map = new HashMap<>();
		String result = "success";
		try {
			UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
			//userPolicy.setUser_id(userSession.getUser_id());
			userPolicy.setUser_id("guest");
						
			userPolicyService.updateUserPolicy(userPolicy);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
	
		map.put("result", result);
		return map;
	}
	
}