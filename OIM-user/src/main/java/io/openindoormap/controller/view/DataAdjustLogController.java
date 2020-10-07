package io.openindoormap.controller.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.openindoormap.domain.Key;
import io.openindoormap.domain.PageType;
import io.openindoormap.domain.common.Pagination;
import io.openindoormap.domain.data.DataAdjustLog;
import io.openindoormap.domain.data.DataGroup;
import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.DataAdjustLogService;
import io.openindoormap.service.DataGroupService;
import io.openindoormap.service.DataService;
import io.openindoormap.support.SQLInjectSupport;
import io.openindoormap.utils.DateUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 데이터 geometry 변경 이력
 * @author jeongdae
 *
 */
@Slf4j
@Controller
@RequestMapping("/data-adjust-log")
public class DataAdjustLogController {
	
	@Autowired
	private DataService dataService;
	@Autowired
	private DataGroupService dataGroupService;
	@Autowired
	private DataAdjustLogService dataAdjustLogService;
	
	/**
	 * TODO 현재 사용하지 않음
	 * 데이터 geometry 변경 이력 수정 화면
	 * @param request
	 * @param dataId
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/modify")
	public String modify(HttpServletRequest request, @RequestParam("dataId") Long dataId, Model model) {
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		
		DataInfo dataInfo = new DataInfo();
		//dataInfo.setUserId(userSession.getUserId());
		dataInfo.setDataId(dataId);
		
		dataInfo = dataService.getData(dataInfo);
		
		model.addAttribute("dataInfo", dataInfo);
		
		return "/data-adjust-log/modify";
	}

	
	/**
	 * 데이터 geometry 변경 이력 목록
	 * @param locale
	 * @param request
	 * @param dataAdjustLog
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/list")
	public String list(Locale locale, HttpServletRequest request, DataAdjustLog dataAdjustLog, @RequestParam(defaultValue="1") String pageNo, Model model) {
		dataAdjustLog.setSearchWord(SQLInjectSupport.replaceSqlInection(dataAdjustLog.getSearchWord()));
		dataAdjustLog.setOrderWord(SQLInjectSupport.replaceSqlInection(dataAdjustLog.getOrderWord()));
		
		log.info("@@ dataAdjustLog = {}", dataAdjustLog);
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		
		DataGroup dataGroup = new DataGroup();
		dataGroup.setUserId(userSession.getUserId());
		List<DataGroup> dataGroupList = dataGroupService.getListDataGroup(dataGroup);
		
		if(!StringUtils.isEmpty(dataAdjustLog.getStartDate())) {
			dataAdjustLog.setStartDate(dataAdjustLog.getStartDate().substring(0, 8) + DateUtils.START_TIME);
		}
		if(!StringUtils.isEmpty(dataAdjustLog.getEndDate())) {
			dataAdjustLog.setEndDate(dataAdjustLog.getEndDate().substring(0, 8) + DateUtils.END_TIME);
		}

		long totalCount = dataAdjustLogService.getDataAdjustLogTotalCount(dataAdjustLog);
		Pagination pagination = new Pagination(	request.getRequestURI(),
												getSearchParameters(PageType.LIST, dataAdjustLog), 
												totalCount, 
												Long.parseLong(pageNo), 
												dataAdjustLog.getListCounter());
		log.info("@@ pagination = {}", pagination);
		
		dataAdjustLog.setOffset(pagination.getOffset());
		dataAdjustLog.setLimit(pagination.getPageRows());
		List<DataAdjustLog> dataAdjustLogList = new ArrayList<>();
		if(totalCount > 0L) {
			dataAdjustLogList = dataAdjustLogService.getListDataAdjustLog(dataAdjustLog);
		}
		
		model.addAttribute(pagination);
		model.addAttribute("dataGroupList", dataGroupList);
		model.addAttribute("dataAdjustLogList", dataAdjustLogList);
		
		return "/data-adjust-log/list";
	}
	
	/**
	 * 검색 조건
	 * @param pageType
	 * @param dataInfoAdjustLog
	 * @return
	 */
	private String getSearchParameters(PageType pageType, DataAdjustLog dataInfoAdjustLog) {
		return dataInfoAdjustLog.getParameters();
	}
}