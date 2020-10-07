package io.openindoormap.controller.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openindoormap.config.CacheConfig;
import io.openindoormap.controller.AuthorizationController;
import io.openindoormap.domain.cache.CacheName;
import io.openindoormap.domain.cache.CacheParams;
import io.openindoormap.domain.cache.CacheType;
import io.openindoormap.domain.menu.Menu;
import io.openindoormap.domain.menu.MenuTarget;
import io.openindoormap.domain.menu.MenuType;
import io.openindoormap.domain.policy.Policy;
import io.openindoormap.domain.role.Role;
import io.openindoormap.domain.user.UserGroup;
import io.openindoormap.domain.user.UserGroupMenu;
import io.openindoormap.domain.user.UserGroupRole;
import io.openindoormap.service.MenuService;
import io.openindoormap.service.PolicyService;
import io.openindoormap.service.RoleService;
import io.openindoormap.service.UserGroupService;
import io.openindoormap.support.LogMessageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/user-group")
public class UserGroupController implements AuthorizationController {

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private MenuService menuService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private PolicyService policyService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CacheConfig cacheConfig;

	/**
	 * 사용자 그룹 목록
	 * @param request
	 * @param userGroup
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/list")
	public String list(HttpServletRequest request, @ModelAttribute UserGroup userGroup, Model model) {
		List<UserGroup> userGroupList = userGroupService.getListUserGroup();

		model.addAttribute("userGroupList", userGroupList);

		return "/user-group/list";
	}

	/**
	 * 사용자 그룹 등록 페이지 이동
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/input")
	public String input(Model model) {
		Policy policy = policyService.getPolicy();

		List<UserGroup> userGroupList = userGroupService.getListUserGroup();

		UserGroup userGroup = new UserGroup();
		userGroup.setParentName(policy.getContentUserGroupRoot());
		userGroup.setParent(0);

		model.addAttribute("policy", policy);
		model.addAttribute("userGroup", userGroup);
		model.addAttribute("userGroupList", userGroupList);

		return "/user-group/input";
	}

	/**
	 * 사용자 그룹 수정 페이지 이동
	 * @param request
	 * @param userGroupId
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/modify")
	public String modify(HttpServletRequest request, @RequestParam Integer userGroupId, Model model) {
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(userGroupId);
		userGroup = userGroupService.getUserGroup(userGroup);
		Policy policy = policyService.getPolicy();

		if(userGroup.getParent() == 0) {
			userGroup.setParentName(policy.getContentUserGroupRoot());
		}

		model.addAttribute("policy", policy);
		model.addAttribute("userGroup", userGroup);

		return "/user-group/modify";
	}

    /**
	 * 사용자 그룹 삭제
	 * @param userGroupId
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/delete")
	public String delete(@RequestParam("userGroupId") Integer userGroupId, Model model) {

		// TODO validation 체크 해야 함
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(userGroupId);

		userGroupService.deleteUserGroup(userGroup);

		CacheParams cacheParams = new CacheParams();
		cacheParams.setCacheType(CacheType.BROADCAST);
		cacheParams.setCacheName(CacheName.USER_GROUP);
		cacheConfig.loadCache(cacheParams);

		return "redirect:/user-group/list";
	}

	/**
	 * 사용자 그룹 메뉴 할당 페이지 이동
	 * @param request
	 * @param userGroupId
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/menu")
	public String menu(HttpServletRequest request, @RequestParam Integer userGroupId, Model model) {
		Menu menu = new Menu();
		menu.setMenuTarget(MenuTarget.ADMIN.getValue());
		menu.setMenuType(MenuType.URL.getValue());
		List<Menu> menuList = menuService.getListMenu(menu);
		menu.setMenuTarget(MenuTarget.USER.getValue());
		menu.setMenuType(MenuType.HTMLID.getValue());
		menuList.addAll(menuService.getListMenu(menu));

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(userGroupId);
		userGroup = userGroupService.getUserGroup(userGroup);

		String userGroupMenuJson = "";

		try {
			UserGroupMenu userGroupMenu = new UserGroupMenu();
			userGroupMenu.setUserGroupId(userGroupId);
			List<UserGroupMenu> userGroupMenuList = userGroupService.getListUserGroupMenu(userGroupMenu);
			userGroupMenuJson = objectMapper.writeValueAsString(userGroupMenuList);
		} catch(JsonProcessingException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ jsonProcessing exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		} catch(RuntimeException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ runtime exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		} catch(Exception e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		}

		model.addAttribute("userGroup", userGroup);
		model.addAttribute("menuList", menuList);
		model.addAttribute("userGroupMenuJson", userGroupMenuJson);

		return "/user-group/menu";
	}

	/**
	 * 사용자 그룹 Role 할당 페이지 이동
	 * @param request
	 * @param userGroupId
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/role")
	public String role(HttpServletRequest request, @RequestParam Integer userGroupId, Model model) {
		Role role = new Role();
		role.setOffset(0l);
		role.setLimit(1000l);
		role.setOrderWord("role_id");
		role.setOrderValue("ASC");
		List<Role> roleList = roleService.getListRole(role);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(userGroupId);
		userGroup = userGroupService.getUserGroup(userGroup);

		String userGroupRoleJson = "";

		try {
			UserGroupRole userGroupRole = new UserGroupRole();
			userGroupRole.setUserGroupId(userGroupId);
			List<UserGroupRole> userGroupRoleList = userGroupService.getListUserGroupRole(userGroupRole);
			userGroupRoleJson = objectMapper.writeValueAsString(userGroupRoleList);
		} catch(JsonProcessingException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ jsonProcessing exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		} catch(RuntimeException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ runtime exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		} catch(Exception e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		}

		model.addAttribute("userGroup", userGroup);
		model.addAttribute("roleList", roleList);
		model.addAttribute("userGroupRoleJson", userGroupRoleJson);

		return "/user-group/role";
	}
}
