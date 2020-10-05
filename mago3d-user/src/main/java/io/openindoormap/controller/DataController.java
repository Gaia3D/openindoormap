package io.openindoormap.controller;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.openindoormap.domain.DataInfo;
import io.openindoormap.domain.DataSharingType;
import io.openindoormap.domain.PageType;
import io.openindoormap.domain.Pagination;
import io.openindoormap.domain.Project;
import io.openindoormap.domain.UserSession;
import io.openindoormap.service.DataService;
import io.openindoormap.service.ProjectService;
import io.openindoormap.util.DateUtil;
import io.openindoormap.util.FormatUtil;
import io.openindoormap.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Data
 *
 */
@Slf4j
@Controller
@RequestMapping("/data")
public class DataController {
	
	@Autowired
	private DataService dataService;
	
	@Autowired
	private ProjectService projectService;
	
	/**
	 * Data 목록
	 * @param request
	 * @param dataInfo
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "list-data")
	public String listData(HttpServletRequest request, DataInfo dataInfo, @RequestParam(defaultValue="1") String pageNo, Model model) {
		log.info("@@ dataInfo = {}", dataInfo);
			
//		Project project = new Project();
//		project.setUser_id(userId);
//		project.setUse_yn(Project.IN_USE);
//		List<Project> projectList = projectService.getListProject(project);

		// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
		// String id = userSession.getUser_id();
		String id = "guest";
		dataInfo.setUser_id(id);
		//dataInfo.setUser_id(userId);
		//dataInfo.setSharing_type(DataSharingType.PUBLIC.getValue());

		if(StringUtil.isNotEmpty(dataInfo.getStart_date())) {
			dataInfo.setStart_date(dataInfo.getStart_date().substring(0, 8) + DateUtil.START_TIME);
		}
		if(StringUtil.isNotEmpty(dataInfo.getEnd_date())) {
			dataInfo.setEnd_date(dataInfo.getEnd_date().substring(0, 8) + DateUtil.END_TIME);
		}

		long totalCount = dataService.getDataTotalCount(dataInfo);
		Pagination pagination = new Pagination(request.getRequestURI(), getSearchParameters(PageType.LIST, request, dataInfo), totalCount, Long.valueOf(pageNo).longValue(), dataInfo.getList_counter());
		log.info("@@ pagination = {}", pagination);
		
		dataInfo.setOffset(pagination.getOffset());
		dataInfo.setLimit(pagination.getPageRows());
		List<DataInfo> dataList = new ArrayList<>();
		if(totalCount > 0l) {
			dataList = dataService.getListData(dataInfo);
		}
		
		// TODO 다국어 처리를 여기서 해야 할거 같은데....
//		Map<String, String> statusMap = new HashMap<>();
//		String welcome = messageSource.getMessage("xxx.xxxx", new Object[]{}, locale);
		
		model.addAttribute(pagination);
//		model.addAttribute("projectList", projectList);
		model.addAttribute("dataList", dataList);
		return "data/list-data";
	}
	
	/**
	 * Data 정보
	 * @param data_id
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "detail-data")
	public String detailData(@RequestParam("data_id") String data_id, HttpServletRequest request, Model model) {
		
		String listParameters = getSearchParameters(PageType.DETAIL, request, null);
		DataInfo dataInfo = new DataInfo();
		dataInfo.setData_id(Long.valueOf(data_id));
		dataInfo = dataService.getData(dataInfo);
		
		// Policy policy = CacheManager.getPolicy();
		
		// model.addAttribute("policy", policy);
		model.addAttribute("listParameters", listParameters);
		model.addAttribute("dataInfo", dataInfo);
		
		return "data/detail-data";
	}
	
	/**
	 * Data 정보 수정 화면
	 * @param data_id
	 * @param model
	 * @return
	 */
	@GetMapping(value = "modify-data")
	public String modifyData(HttpServletRequest request, @RequestParam("data_id") Long data_id, Model model) {
		
		UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
		String user_id = "guest";

		Project project = new Project();
		project.setUse_yn(Project.IN_USE);
		project.setSharing_type(DataSharingType.PUBLIC.getValue());
		// project.setUser_id(userSession.getUser_id());
		project.setUser_id(user_id);
		List<Project> projectList = projectService.getListProject(project);
		
		DataInfo dataInfo = new DataInfo();
		dataInfo.setData_id(data_id);
		dataInfo =  dataService.getData(dataInfo);
		
		log.info("@@@@@@@@ dataInfo = {}", dataInfo);
		// Policy policy = CacheManager.getPolicy();
		
		String listParameters = getSearchParameters(PageType.MODIFY, request, null);
		
		model.addAttribute("listParameters", listParameters);
		// model.addAttribute("policy", policy);
		model.addAttribute("projectList", projectList);
		model.addAttribute(dataInfo);
		
		return "data/modify-data";
	}
	
	/**
	 * Data 정보 수정
	 * @param request
	 * @param dataInfo
	 * @return
	 */
	@PostMapping(value = "ajax-update-data-info")
	@ResponseBody
	public Map<String, Object> ajaxUpdateDataInfo(HttpServletRequest request, DataInfo dataInfo) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		
		log.info("@@ dataInfo = {}", dataInfo);
		try {
			dataInfo.setMethod_mode("update");
			String errorcode = dataValidate(dataInfo);
			if(errorcode != null) {
				result = errorcode;
				map.put("result", result);
				return map;
			}
			
			if(dataInfo.getLatitude() != null && dataInfo.getLatitude().floatValue() != 0f &&
					dataInfo.getLongitude() != null && dataInfo.getLongitude().floatValue() != 0f) {
				dataInfo.setLocation("POINT(" + dataInfo.getLongitude() + " " + dataInfo.getLatitude() + ")");
			}
			log.info("@@@@@@@@ dataInfo = {}", dataInfo);
			
			dataService.updateData(dataInfo);
			
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
	
		map.put("result", result);
		return map;
	}
	
	/**
	 * Data 삭제
	 * @param data_id
	 * @param model
	 * @return
	 */
	@PostMapping(value = "ajax-delete-data")
	@ResponseBody
	public Map<String, Object> deleteData(HttpServletRequest request, DataInfo dataInfo) {
		log.info("@@@@@@@ dataInfo = {}", dataInfo);
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		try {
			if(dataInfo.getData_id() == null || dataInfo.getData_id().intValue() <=0) {
				map.put("result", "dataInfo.data_id.empty");
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
			dataService.deleteData(dataInfo);
		} catch(Exception e) {
			e.printStackTrace();
			map.put("result", "db.exception");
			return map;
		}
		
		map.put("result", result);
		return map;

		// public String deleteData(@RequestParam("data_id") String data_id, Model model) {
		// UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
		// DataInfo dataInfo = new DataInfo();
		// dataInfo.setData_id(Long.valueOf(data_id));
		// dataInfo.setUser_id(userSession.getUser_id());

		// validation 체크 해야 함
		// dataService.deleteData(dataInfo);

		// dataService.deleteData(Long.valueOf(data_id));
		// CacheParams cacheParams = new CacheParams();
		// cacheParams.setCacheName(CacheName.DATA_INFO);
		// cacheParams.setCacheType(CacheType.BROADCAST);
		// cacheConfig.loadCache(cacheParams);
		// return "redirect:/data/list-data";
	}
	
	/**
	 * 선택 Data 삭제
	 * @param request
	 * @param data_select_id
	 * @param model
	 * @return
	 */
	@PostMapping(value = "ajax-delete-datas.do")
	@ResponseBody
	public Map<String, Object> ajaxDeleteDatas(HttpServletRequest request, @RequestParam("check_ids") String check_ids) {
		
		log.info("@@@@@@@ check_ids = {}", check_ids);
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		try {
			if(check_ids.length() <= 0) {
				map.put("result", "check.value.required");
				return map;
			}
			
			// dataService.deleteDataList(check_ids);
			
			// CacheParams cacheParams = new CacheParams();
			// cacheParams.setCacheName(CacheName.DATA_INFO);
			// cacheParams.setCacheType(CacheType.BROADCAST);
			// cacheConfig.loadCache(cacheParams);
		} catch(Exception e) {
			e.printStackTrace();
			map.put("result", "db.exception");
		}
		
		map.put("result", result	);
		return map;
	}

	/**
	 * 프로젝트에 등록된 Data 목록
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "ajax-list-data-by-project-id.do")
	@ResponseBody
	public Map<String, Object> ajaxListDataByProjectId(HttpServletRequest request, @RequestParam("project_id") Integer project_id) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		List<DataInfo> dataList = new ArrayList<>();
		try {		
			UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
			
			DataInfo dataInfo = new DataInfo();
			dataInfo.setUser_id(userSession.getUser_id());
			dataInfo.setProject_id(project_id);
			dataList = dataService.getListDataByProjectId(dataInfo);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
		
		map.put("result", result);
		map.put("dataList", dataList);
		
		return map;
	}
	
	/**
	 * ajax 용 Data validation 체크
	 * @param dataInfo
	 * @return
	 */
	private String dataValidate(DataInfo dataInfo) {
		if(dataInfo.getData_key() == null || "".equals(dataInfo.getData_key())) {
			return "data.input.invalid";
		}
			
		if(dataInfo.getProject_id() == null || dataInfo.getProject_id().longValue() <= 0
				|| dataInfo.getData_name() == null || "".equals(dataInfo.getData_name())) {
			return "data.project.id.invalid";
		}
		
		return null;
	}
	
	/**
	 * Data key 중복 체크
	 * @param model
	 * @return
	 */
	@PostMapping(value = "ajax-data-key-duplication-check")
	@ResponseBody
	public Map<String, Object> ajaxDataKeyDuplicationCheck(HttpServletRequest request, DataInfo dataInfo) {
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		String duplication_value = "";
		try {
			if(dataInfo.getProject_id() == null || dataInfo.getProject_id().longValue() < 0) {
				result = "project.id.empty";
				map.put("result", result);
				return map;
			}
			else if(dataInfo.getData_key() == null || "".equals(dataInfo.getData_key())) {
				result = "data.key.empty";
				map.put("result", result);
				return map;
			} else if(dataInfo.getOld_data_key() != null && !"".equals(dataInfo.getOld_data_key())) {
				if(dataInfo.getData_key().equals(dataInfo.getOld_data_key())) {
					result = "data.key.same";
					map.put("result", result);
					return map;
				}
			}
			
			int count = dataService.getDuplicationKeyCount(dataInfo);
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
	 * 최근 data_info
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "ajax-data-info-widget.do")
	@ResponseBody
	public Map<String, Object> dataInfoWidget(HttpServletRequest request) {
		
		log.info(" >>>>>>>>>>>>>>>>>>>>>>>>>>>> dataInfoWidget");
		Map<String, Object> map = new HashMap<>();
		String result = "success";
		try {
			UserSession userSession = (UserSession)request.getSession().getAttribute(UserSession.KEY);
			String today = DateUtil.getToday(FormatUtil.YEAR_MONTH_DAY);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -30);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
			String searchDay = simpleDateFormat.format(calendar.getTime());
			String startDate = searchDay + DateUtil.START_TIME;
			String endDate = today + DateUtil.END_TIME;
			
			DataInfo datInfo = new DataInfo();
			datInfo.setUser_id(userSession.getUser_id());
			datInfo.setStart_date(startDate);
			datInfo.setEnd_date(endDate);
			datInfo.setOffset(0l);
			datInfo.setLimit(7l);
			List<DataInfo> dataInfoList = dataService.getListData(datInfo);
			
			map.put("dataInfoList", dataInfoList);
		} catch(Exception e) {
			e.printStackTrace();
			result = "db.exception";
		}
		
		map.put("result", result);
		return map;
	}
	
	/**
	 * 검색 조건
	 * @param dataInfo
	 * @return
	 */
	private String getSearchParameters(PageType pageType, HttpServletRequest request, DataInfo dataInfo) {
		StringBuffer buffer = new StringBuffer();
		boolean isListPage = true;
		if(pageType.equals(PageType.MODIFY) || pageType.equals(PageType.DETAIL)) {
			isListPage = false;
		}
		
		if(!isListPage) {
			buffer.append("pageNo=" + request.getParameter("pageNo"));
		}
		buffer.append("&");
		buffer.append("search_word=" + StringUtil.getDefaultValue(isListPage ? dataInfo.getSearch_word() : request.getParameter("search_word")));
		buffer.append("&");
		buffer.append("search_option=" + StringUtil.getDefaultValue(isListPage ? dataInfo.getSearch_option() : request.getParameter("search_option")));
		buffer.append("&");
		try {
			buffer.append("search_value=" + URLEncoder.encode(StringUtil.getDefaultValue(
					isListPage ? dataInfo.getSearch_value() : request.getParameter("search_value")), "UTF-8"));
		} catch(Exception e) {
			e.printStackTrace();
			buffer.append("search_value=");
		}
		buffer.append("&");
		buffer.append("start_date=" + StringUtil.getDefaultValue(isListPage ? dataInfo.getStart_date() : request.getParameter("start_date")));
		buffer.append("&");
		buffer.append("end_date=" + StringUtil.getDefaultValue(isListPage ? dataInfo.getEnd_date() : request.getParameter("end_date")));
		buffer.append("&");
		buffer.append("order_word=" + StringUtil.getDefaultValue(isListPage ? dataInfo.getOrder_word() : request.getParameter("order_word")));
		buffer.append("&");
		buffer.append("order_value=" + StringUtil.getDefaultValue(isListPage ? dataInfo.getOrder_value() : request.getParameter("order_value")));
		if(!isListPage) {
			buffer.append("&");
			buffer.append("list_count=" + request.getParameter("list_count"));
		}
		return buffer.toString();
	}
}