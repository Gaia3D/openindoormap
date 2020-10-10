//package io.openindoormap.controller.view;
//
//import io.openindoormap.controller.AuthorizationController;
//import io.openindoormap.domain.Key;
//import io.openindoormap.domain.PageType;
//import io.openindoormap.domain.common.Pagination;
//import io.openindoormap.domain.urban.Urban;
//import io.openindoormap.domain.urban.UrbanGroup;
//import io.openindoormap.domain.policy.Policy;
//import io.openindoormap.domain.role.RoleKey;
//import io.openindoormap.domain.user.UserSession;
//import io.openindoormap.service.PolicyService;
//import io.openindoormap.service.NewTownGroupService;
//import io.openindoormap.service.NewTownService;
//import io.openindoormap.support.SQLInjectSupport;
//import io.openindoormap.utils.DateUtils;
//import io.openindoormap.utils.FormatUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 뉴타운
// * @author kimhj
// *
// */
//@Slf4j
//@Controller
//@RequestMapping("/new-town")
//public class UrbanController implements AuthorizationController {
//
//	@Autowired
//	private NewTownService newTownService;
//
//	@Autowired
//	private NewTownGroupService newTownGroupService;
//
//	@Autowired
//	private PolicyService policyService;
//
//	/**
//	 * 뉴타운 목록
//	 * @param request
//	 * @param newTown
//	 * @param pageNo
//	 * @param model
//	 * @return
//	 */
//	@GetMapping(value = "/list")
//	public String list(HttpServletRequest request, @RequestParam(defaultValue="1") String pageNo, Urban newTown, Model model) {
//		newTown.setSearchWord(SQLInjectSupport.replaceSqlInection(newTown.getSearchWord()));
//		newTown.setOrderWord(SQLInjectSupport.replaceSqlInection(newTown.getOrderWord()));
//
////		String roleCheckResult = roleValidate(request);
////    	if(roleValidate(request) != null) return roleCheckResult;
//
//    	String today = DateUtils.getToday(FormatUtils.YEAR_MONTH_DAY);
//		if(StringUtils.isEmpty(newTown.getStartDate())) {
//			newTown.setStartDate(today.substring(0,4) + DateUtils.START_DAY_TIME);
//		} else {
//			newTown.setStartDate(newTown.getStartDate().substring(0, 8) + DateUtils.START_TIME);
//			newTown.setStartDate(newTown.getStartDate().substring(0, 8) + DateUtils.START_TIME);
//		}
//		if(StringUtils.isEmpty(newTown.getEndDate())) {
//			newTown.setEndDate(today + DateUtils.END_TIME);
//		} else {
//			newTown.setEndDate(newTown.getEndDate().substring(0, 8) + DateUtils.END_TIME);
//		}
//
//    	long totalCount = newTownService.getNewTownTotalCount(newTown);
//    	Pagination pagination = new Pagination(request.getRequestURI(), getSearchParameters(PageType.LIST, newTown), totalCount, Long.parseLong(pageNo), newTown.getListCounter());
//    	newTown.setOffset(pagination.getOffset());
//    	newTown.setLimit(pagination.getPageRows());
//
//		List<Urban> newTownList = new ArrayList<>();
//		if(totalCount > 0l) {
//			newTownList = newTownService.getListNewTown(newTown);
//		}
//
//		model.addAttribute(pagination);
//		model.addAttribute("newTownList", newTownList);
//		return "/new-town/list";
//	}
//
//	/**
//	 * 뉴타운 등록  페이지 이동
//	 */
//	@GetMapping(value = "/input")
//	public String input(Model model) {
//		Policy policy = policyService.getPolicy();
//		List<UrbanGroup> newTownGroupList = newTownGroupService.getListNewTownGroup();
//
//		model.addAttribute("policy", policy);
//		model.addAttribute("newTownGroupList", newTownGroupList);
//		model.addAttribute("newTown", new Urban());
//		return "/user/input";
//	}
//
//	/**
//	 * 뉴타운 수정  페이지 이동
//	 * @param request
//	 * @param newTownId
//	 * @param model
//	 * @return
//	 */
//	@GetMapping(value = "/modify")
//	public String modify(HttpServletRequest request, @RequestParam Integer newTownId, Model model) {
//		String roleCheckResult = roleValidate(request);
//    	if(roleValidate(request) != null) return roleCheckResult;
//
//        Policy policy = policyService.getPolicy();
//        Urban newTown = newTownService.getNewTown(newTownId);
//		List<UrbanGroup> newTownGroupList = newTownGroupService.getListNewTownGroup();
//
//        model.addAttribute("policy", policy);
//        model.addAttribute("newTown", newTown);
//        model.addAttribute("newTownGroupList", newTownGroupList);
//
//        return "/user/modify";
//	}
//
//	/**
//	 * 검색 조건
//	 * @param newTown
//	 * @return
//	 */
//	private String getSearchParameters(PageType pageType, Urban newTown) {
//		StringBuffer buffer = new StringBuffer(newTown.getParameters());
//		boolean isListPage = true;
//		if(pageType == PageType.MODIFY || pageType == PageType.DETAIL) {
//			isListPage = false;
//		}
//
////		if(!isListPage) {
////			buffer.append("pageNo=" + request.getParameter("pageNo"));
////			buffer.append("&");
////			buffer.append("list_count=" + uploadData.getList_counter());
////		}
//
//		return buffer.toString();
//	}
//
//	private String roleValidate(HttpServletRequest request) {
//    	UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
//		int httpStatusCode = getRoleStatusCode(userSession.getUserGroupId(), RoleKey.ADMIN_EXTRUSION_MODEL_MANAGE.name());
//		if(httpStatusCode > 200) {
//			log.info("@@ httpStatusCode = {}", httpStatusCode);
//			request.setAttribute("httpStatusCode", httpStatusCode);
//			return "/error/error";
//		}
//
//		return null;
//    }
//}
