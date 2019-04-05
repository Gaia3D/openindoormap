package io.openindoormap.controller;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.openindoormap.domain.DataSharingType;
import io.openindoormap.domain.Pagination;
import io.openindoormap.domain.Project;
import io.openindoormap.domain.UserSession;
import io.openindoormap.service.ProjectService;
import io.openindoormap.util.DateUtil;
import io.openindoormap.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Project
 *
 */
@Slf4j
@Controller
@RequestMapping("/project")
public class ProjectController {
		
	@Autowired
	private ProjectService projectService;

	// @Autowired
	// private RoleService roleService;
	
	/**
	 * Project 목록
	 * @param model
	 * @return
	 */
	@RequestMapping("/list-project")
	public String listProject(HttpServletRequest request, Project project, @RequestParam(defaultValue="1") String pageNo, Model model) {
		log.info("@@ project = {}", project);
		
		// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
		// project.setUser_id(userSession.getUser_id());
		project.setUser_id("guest");
		project.setUse_yn(Project.IN_USE);
		// if(StringUtils.isEmpty(project.getSharing_type())) {
		// 	project.setSharing_type(DataSharingType.PUBLIC.getValue());
		// }
		
		if(!StringUtils.isEmpty(project.getStart_date())) {
			project.setStart_date(project.getStart_date().substring(0, 8) + DateUtil.START_TIME);
		}
		if(!StringUtils.isEmpty(project.getEnd_date())) {
			project.setEnd_date(project.getEnd_date().substring(0, 8) + DateUtil.END_TIME);
		}

		long totalCount = projectService.getProjectTotalCount(project);
		Pagination pagination = new Pagination(request.getRequestURI(), getSearchParameters(project), totalCount, Long.valueOf(pageNo).longValue(), project.getList_counter());
		log.info("@@ pagination = {}", pagination);
		
		project.setOffset(pagination.getOffset());
		project.setLimit(pagination.getPageRows());
		List<Project> projectList = new ArrayList<>();
		if(totalCount > 0l) {
			projectList = projectService.getListProject(project);
			log.info("@@ projectList = {}", projectList);
		}
		
		model.addAttribute(pagination);
		model.addAttribute("projectList", projectList);
		return "/project/list-project";
	}
	
	/**
	 * Project map
	 * @param model
	 * @return
	 */
	@GetMapping("/map-project")
	public String mapProject(HttpServletRequest request, HttpServletResponse response, Project project, 
			@RequestParam(defaultValue="1") String pageNo, @RequestParam(defaultValue="cesium") String viewLibrary, Model model) throws Exception {
		
		log.info("@@ viewLibrary = {}", viewLibrary);
		UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
		project.setUser_id(userSession.getUser_id());
		project.setUse_yn(Project.IN_USE);
		if(StringUtils.isEmpty(project.getSharing_type())) {
			project.setSharing_type(DataSharingType.PUBLIC.getValue());
		}
		
		if(!StringUtils.isEmpty(project.getStart_date())) {
			project.setStart_date(project.getStart_date().substring(0, 8) + DateUtil.START_TIME);
		}
		if(!StringUtils.isEmpty(project.getEnd_date())) {
			project.setEnd_date(project.getEnd_date().substring(0, 8) + DateUtil.END_TIME);
		}
		
		long totalCount = projectService.getProjectTotalCount(project);
		Pagination pagination = new Pagination(request.getRequestURI(), getSearchParameters(project), totalCount, Long.valueOf(pageNo).longValue(), project.getList_counter());
		log.info("@@ pagination = {}", pagination);
		
		project.setOffset(pagination.getOffset());
		project.setLimit(pagination.getPageRows());
		List<Project> projectList = new ArrayList<>();
		if(totalCount > 0l) {
			projectList = projectService.getListProject(project);
		}
		
		model.addAttribute(pagination);
		model.addAttribute("projectList", projectList);
		
		return "/project/map-project";
	}
	
	/**
	 * Project 목록
	 * @param request
	 * @return
	 */
	@PostMapping("/ajax-list-project")
	@ResponseBody
	public Map<String, Object> ajaxListProject(HttpServletRequest request, Project project) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		try {
			// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
			// project.setUser_id(userSession.getUser_id());
			project.setUser_id("guest");
			project.setUse_yn(Project.IN_USE);
			project.setSharing_type(DataSharingType.PUBLIC.getValue());
			
			List<Project> projectList = projectService.getListProject(project);
			
			map.put("projectList", projectList);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
		
		map.put("result", result);
		return map;
	}
	
	/**
	 * Project 정보
	 * @param projectId
	 * @return
	 */
	@GetMapping("/ajax-project")
	@ResponseBody
	public Map<String, Object> getProject(HttpServletRequest request, Project project) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		try {
			log.info("@@ project = {} ", project);
			if(project.getProject_id() == null) {
				result = "input.invalid";
				map.put("result", result);
				return map;
			}
			
			UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
			project.setUser_id(userSession.getUser_id());
			project.setUse_yn(Project.IN_USE);
			project = projectService.getProject(project);
			
			map.put("project", project);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
		
		map.put("result", result);
		return map;
	}
	
	/**
	 * Project 등록 화면
	 * @param model
	 * @return
	 */
	@GetMapping("/input-project")
	public String inputProject(HttpServletRequest request, Model model) {
		model.addAttribute("project", new Project());
		
		return "/project/input-project";
	}
	
	/**
	 * Project key 중복 체크
	 * @param model
	 * @return
	 */
	@PostMapping("/ajax-project-key-duplication-check")
	@ResponseBody
	public Map<String, Object> ajaxProjectKeyDuplicationCheck(HttpServletRequest request, Project project) {
		
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		String duplication_value = "";
		
		log.info("@@ project = {}", project);
		try {
			if(project.getProject_key() == null || "".equals(project.getProject_key())) {
				result = "project.key.empty";
				map.put("result", result);
				return map;
			} else if(project.getOld_project_key() != null && !"".equals(project.getOld_project_key())) {
				if(project.getProject_key().equals(project.getOld_project_key())) {
					result = "project.key.same";
					map.put("result", result);
					return map;
				}
			}
			
			int count = projectService.getDuplicationKeyCount(project.getProject_key());
			log.info("@@ duplication_value = {}", count);
			duplication_value = String.valueOf(count);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
	
		map.put("result", result);
		map.put("duplication_value", duplication_value);
		
		return map;
	}
	
	/**
	 * Project 추가
	 * @param request
	 * @param project
	 * @return
	 */
	@PostMapping("/ajax-insert-project")
	@ResponseBody
	public Map<String, Object> ajaxInsertProject(HttpServletRequest request, Project project) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		try {
			log.info("@@ project = {} ", project);
			
			if(project.getProject_name() == null || "".equals(project.getProject_name())) {
				result = "input.invalid";
				map.put("result", result);
				return map;
			}
			
			// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
			// String id = userSession.getUser_id();
			String id = "guest";
			project.setUser_id(id);
			project.setProject_key(id + "_" + System.nanoTime());
			project.setProject_path("");
			projectService.insertProject(project);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
		
		map.put("result", result);
		return map;
	}
	
	/**
	 * Project 수정 화면
	 * @param model
	 * @return
	 */
	@GetMapping("/modify-project")
	public String modifyProject(HttpServletRequest request, Project project, Model model) {
		// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
		// String id = userSession.getUser_id();
		String id = "guest";
		project.setUser_id(id);
		project.setUse_yn(Project.IN_USE);
		project = projectService.getProject(project);
		model.addAttribute("project", project);
		
		return "/project/modify-project";
	}
	
	/**
	 * Project 수정
	 * @param request
	 * @param project
	 * @return
	 */
	@PostMapping("/ajax-update-project")
	@ResponseBody
	public Map<String, Object> ajaxUpdateProject(HttpServletRequest request, Project project) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		try {
						
			log.info("@@ project = {} ", project);
			if(project.getProject_id() == null || project.getProject_id().intValue() == 0l
					|| project.getProject_name() == null || "".equals(project.getProject_name())) {
				
				result = "input.invalid";
				map.put("result", result);
				return map;
			}
			
			// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
			// String id = userSession.getUser_id();
			String id = "guest";
			project.setUser_id(id);
			project.setUse_yn(Project.IN_USE);
			projectService.updateProject(project);
			
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
		
		map.put("result", result);
		return map;
	}
	
	/**
	 * Project 삭제
	 * @param request
	 * @param project_id
	 * @param model
	 * @return
	 */
	@PostMapping("/ajax-delete-project")
	@ResponseBody
	public Map<String, Object> ajaxDeleteProject(HttpServletRequest request, Project project) {
		log.info("@@@@@@@ project = {}", project);
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		try {
			if(project.getProject_id() == null || project.getProject_id().intValue() <=0) {
				map.put("result", "project.project_id.empty");
				return map;
			}
			/*
			UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
			// 사용자 그룹 ROLE 확인
			UserGroupRole userGroupRole = new UserGroupRole();
			userGroupRole.setUser_id(userSession.getUser_id());
			
			// // TODO get 방식으로 권한 오류를 넘겨준다.
			// if(!GroupRoleHelper.isUserGroupRoleValid(roleService.getListUserGroupRoleByUserId(userGroupRole), UserGroupRole.PROJECT_DELETE)) {
			// 	log.info("@@ 접근 권한이 없어 실행할 수 없습니다. RoleName = {}",  UserGroupRole.PROJECT_DELETE);
			// 	map.put("result", "user.group.role.invalid");
			// 	return map;
			// }
	
			project.setUser_id(userSession.getUser_id());
			*/
			String id = "guest";
			project.setUser_id(id);
			project.setUse_yn(Project.IN_USE);
			projectService.deleteProject(project);
		} catch(Exception e) {
			e.printStackTrace();
			map.put("result", "db.exception");
			return map;
		}
		
		map.put("result", result	);
		return map;
	}
	
	/**
	 * 검색 조건
	 * @param project
	 * @return
	 */
	private String getSearchParameters(Project project) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("&");
		buffer.append("search_word=" + StringUtil.getDefaultValue(project.getSearch_word()));
		buffer.append("&");
		buffer.append("search_option=" + StringUtil.getDefaultValue(project.getSearch_option()));
		buffer.append("&");
		try {
			buffer.append("search_value=" + URLEncoder.encode(StringUtil.getDefaultValue(project.getSearch_value()), "UTF-8"));
		} catch(Exception e) {
			e.printStackTrace();
			buffer.append("search_value=");
		}
		buffer.append("&");
		buffer.append("start_date=" + StringUtil.getDefaultValue(project.getStart_date()));
		buffer.append("&");
		buffer.append("end_date=" + StringUtil.getDefaultValue(project.getEnd_date()));
		buffer.append("&");
		buffer.append("order_word=" + StringUtil.getDefaultValue(project.getOrder_word()));
		buffer.append("&");
		buffer.append("order_value=" + StringUtil.getDefaultValue(project.getOrder_value()));
		return buffer.toString();
	}
}
