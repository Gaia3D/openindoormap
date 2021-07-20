package io.openindoormap.controller.rest;

import io.openindoormap.domain.Key;
import io.openindoormap.domain.LocationUdateType;
import io.openindoormap.domain.PageType;
import io.openindoormap.domain.common.Pagination;
import io.openindoormap.domain.data.DataGroup;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.DataGroupService;
import io.openindoormap.support.SQLInjectSupport;
import io.openindoormap.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 데이터 그룹 관리
 * @author Jeongdae
 *
 */
@Slf4j
@RestController
@RequestMapping("/data-groups")
public class DataGroupRestController {

	private static final long PAGE_ROWS = 4L;
	private static final long PAGE_LIST_COUNT = 5L;
	
	@Autowired
	private DataGroupService dataGroupService;

	/**
	 * 데이터 그룹 전체 목록
	 * @param request
	 * @param dataGroup
	 * @return
	 */
	@GetMapping(value = "/all")
	public Map<String, Object> allList(HttpServletRequest request, DataGroup dataGroup) {

		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		dataGroup.setSearchWord(SQLInjectSupport.replaceSqlInection(dataGroup.getSearchWord()));
		dataGroup.setOrderWord(SQLInjectSupport.replaceSqlInection(dataGroup.getOrderWord()));
		
		log.info("@@@@@ list dataGroup = {}", dataGroup);
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		String userId = userSession == null ? "" : userSession.getUserId();
		int userGroupId = userSession == null ? 0 : userSession.getUserGroupId();

		dataGroup.setUserGroupId(userGroupId);
		dataGroup.setUserId(userId);
		List<DataGroup> dataGroupList = dataGroupService.getAllListDataGroup(dataGroup);
		int statusCode = HttpStatus.OK.value();
		
		result.put("dataGroupList", dataGroupList);
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}

	/**
	 * 데이터 그룹 정보
	 * @param request
	 * @param dataGroup
	 * @param pageNo
	 * @return
	 */
	@GetMapping
	public Map<String, Object> list(HttpServletRequest request, DataGroup dataGroup, @RequestParam(defaultValue="1") String pageNo) {
		log.info("@@@@@ list dataGroup = {}, pageNo = {}", dataGroup, pageNo);
		
		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;
		
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		String userId = userSession == null ? "" : userSession.getUserId();
		int userGroupId = userSession == null ? 0 : userSession.getUserGroupId();

		dataGroup.setUserGroupId(userGroupId);
		dataGroup.setUserId(userId);

		if(!StringUtils.isEmpty(dataGroup.getStartDate())) {
			dataGroup.setStartDate(dataGroup.getStartDate().substring(0, 8) + DateUtils.START_TIME);
		}
		if(!StringUtils.isEmpty(dataGroup.getEndDate())) {
			dataGroup.setEndDate(dataGroup.getEndDate().substring(0, 8) + DateUtils.END_TIME);
		}
		long totalCount = dataGroupService.getDataGroupTotalCount(dataGroup);
		
		Pagination pagination = new Pagination(	request.getRequestURI(),
												getSearchParameters(PageType.LIST, dataGroup), 
												totalCount, 
												Long.parseLong(pageNo),
												PAGE_ROWS,
												PAGE_LIST_COUNT);
		log.info("@@ pagination = {}", pagination);
		
		dataGroup.setOffset(pagination.getOffset());
		dataGroup.setLimit(pagination.getPageRows());
		List<DataGroup> dataGroupList = new ArrayList<>();
		if(totalCount > 0L) {
			dataGroupList = dataGroupService.getListDataGroup(dataGroup);
		}
		
		int statusCode = HttpStatus.OK.value();
		
		result.put("pagination", pagination);
		result.put("owner", userId);
		result.put("dataGroupList", dataGroupList);
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}
	
	/**
	 * 사용자 데이터 그룹 정보
	 * @param dataGroup
	 * @return
	 */
	@GetMapping(value = "/duplication")
	public Map<String, Object> dataGroupKeyDuplicationCheck(	HttpServletRequest request, DataGroup dataGroup ) {
		log.info("@@@@@ detail dataGroup = {}", dataGroup);
		
		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		// TODO @Valid 로 구현해야 함
		if(StringUtils.isEmpty(dataGroup.getDataGroupKey())) {
			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
			result.put("errorCode", "data.group.key.empty");
			result.put("message", message);
			return result;
		}
		
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		dataGroup.setUserId(userSession.getUserId());
		Boolean duplication = dataGroupService.isDataGroupKeyDuplication(dataGroup);
		log.info("@@ duplication = {}", duplication);
		int statusCode = HttpStatus.OK.value();
		
		result.put("duplication", duplication);
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}
	
	/**
	 * 사용자 데이터 그룹 정보
	 * @param dataGroup
	 * @return
	 */
	@GetMapping("/{dataGroupId:[0-9]+}")
	public Map<String, Object> detail(	HttpServletRequest request, @PathVariable Integer dataGroupId, DataGroup dataGroup ) {
		log.info("@@@@@ detail dataGroup = {}, dataGroupId = {}", dataGroup, dataGroupId);
		
		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;
		
//			dataGroup.setUserId(userSession.getUserId());
//			dataGroup.setDataGroupId(dataGroupId);
		dataGroup = dataGroupService.getDataGroup(dataGroup);
		int statusCode = HttpStatus.OK.value();
		
		result.put("dataGroup", dataGroup);
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}
	
	/**
	 * 사용자 데이터 그룹 등록
	 * @param request
	 * @param dataGroup
	 * @param bindingResult
	 * @return
	 */
	@PostMapping
	public Map<String, Object> insert(HttpServletRequest request, @Valid @ModelAttribute DataGroup dataGroup, BindingResult bindingResult) {
		log.info("@@@@@ insert dataGroup = {}", dataGroup);
		
		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;
		
		if(bindingResult.hasErrors()) {
			message = bindingResult.getAllErrors().get(0).getDefaultMessage();
			log.info("@@@@@ message = {}", message);
			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
			result.put("errorCode", errorCode);
			result.put("message", message);
            return result;
		}
		
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		dataGroup.setUserId(userSession.getUserId());
		if(dataGroup.getLongitude() != null && dataGroup.getLatitude() != null) {
			dataGroup.setLocationUpdateType(LocationUdateType.USER.name().toLowerCase());
			dataGroup.setLocation("POINT(" + dataGroup.getLongitude() + " " + dataGroup.getLatitude() + ")");
		}
		
		Boolean duplication = dataGroupService.isDataGroupKeyDuplication(dataGroup);
		if(duplication) {
			log.info("@@@@@ key duplication duplication = {}", duplication);
			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
			result.put("errorCode", "data.group.key.duplication");
			result.put("message", message);
            return result;
		}
		
		dataGroupService.insertDataGroup(dataGroup);
		int statusCode = HttpStatus.OK.value();
		
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}
	
	/**
	 * 사용자 데이터 그룹 수정
	 * @param request
	 * @param dataGroup
	 * @param bindingResult
	 * @return
	 */
	@PostMapping("/{dataGroupId:[0-9]+}")
	public Map<String, Object> update(HttpServletRequest request, @PathVariable Integer dataGroupId, @Valid @ModelAttribute DataGroup dataGroup, BindingResult bindingResult) {
		log.info("@@@@@ update dataGroup = {}", dataGroup);
		
		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;
		
		// @Valid 로 dataGroupKey를 걸어 뒀더니 수정화면에서는 수정 불가라서 hidden으로는 보내고 싶지 않고~
		if(bindingResult.hasErrors()) {
			message = bindingResult.getAllErrors().get(0).getDefaultMessage();
			log.info("@@@@@ message = {}", message);
			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
			result.put("errorCode", errorCode);
			result.put("message", message);
            return result;
		}
		
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		dataGroup.setUserId(userSession.getUserId());
		if(dataGroup.getLongitude() != null && dataGroup.getLatitude() != null) {
			dataGroup.setLocationUpdateType(LocationUdateType.USER.name().toLowerCase());
			dataGroup.setLocation("POINT(" + dataGroup.getLongitude() + " " + dataGroup.getLatitude() + ")");
		}
		
		dataGroupService.updateDataGroup(dataGroup);
		int statusCode = HttpStatus.OK.value();
		
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}
	
	/**
	 * 사용자 데이터 그룹 순서 수정
	 * @param request
	 * @param dataGroupId
	 * @param dataGroup
	 * @return
	 */
	@PostMapping(value = "/view-order/{dataGroupId:[0-9]+}")
	public Map<String, Object> moveUserGroup(HttpServletRequest request, @PathVariable Integer dataGroupId, @ModelAttribute DataGroup dataGroup) {
		log.info("@@ dataGroupId = {}, dataGroup = {}", dataGroupId, dataGroup);
		
		Map<String, Object> result = new HashMap<>();
		int statusCode = 0;
		String errorCode = null;
		String message = null;
		
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		dataGroup.setUserId(userSession.getUserId());
		dataGroup.setDataGroupId(dataGroupId);
		
		int updateCount = dataGroupService.updateDataGroupViewOrder(dataGroup);
		if(updateCount == 0) {
			statusCode = HttpStatus.BAD_REQUEST.value();
			errorCode = "data.group.view-order.invalid";
		} else {
			statusCode = HttpStatus.OK.value();
		}
		
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}
	
	/**
	 * 검색 조건
	 * @param pageType
	 * @param dataGroup
	 * @return
	 */
	private String getSearchParameters(PageType pageType, DataGroup dataGroup) {
		StringBuilder builder = new StringBuilder(dataGroup.getParameters());
//		buffer.append("&");
//		try {
//			builder.append("dataName=" + URLEncoder.encode(getDefaultValue(dataInfo.getDataName()), "UTF-8"));
//		} catch(Exception e) {
//			builder.append("dataName=");
//		}
		return builder.toString();
	}
	
	private String getDefaultValue(String value) {
		if(value == null || "".equals(value.trim())) {
			return "";
		}
		
		return value;
	}
}
