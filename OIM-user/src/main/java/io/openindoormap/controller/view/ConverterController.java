package io.openindoormap.controller.view;

import io.openindoormap.domain.Key;
import io.openindoormap.domain.PageType;
import io.openindoormap.domain.cache.CacheManager;
import io.openindoormap.domain.common.Pagination;
import io.openindoormap.domain.converter.ConverterJob;
import io.openindoormap.domain.converter.ConverterJobFile;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.ConverterService;
import io.openindoormap.support.RoleSupport;
import io.openindoormap.support.SQLInjectSupport;
import io.openindoormap.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Converter
 * @author jeongdae
 *
 */
@Slf4j
@Controller
@RequestMapping("/converter")
public class ConverterController {
	
	@Autowired
	private ConverterService converterService;

	/**
	 * converter job 목록
	 * @param request
	 * @param converterJob
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/list")
	public String list(HttpServletRequest request, ConverterJob converterJob, @RequestParam(defaultValue="1") String pageNo, Model model) {
		converterJob.setSearchWord(SQLInjectSupport.replaceSqlInection(converterJob.getSearchWord()));
		converterJob.setOrderWord(SQLInjectSupport.replaceSqlInection(converterJob.getOrderWord()));
		
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		converterJob.setUserId(userSession.getUserId());		
		log.info("@@ converterJob = {}", converterJob);
		
		if(!StringUtils.isEmpty(converterJob.getStartDate())) {
			converterJob.setStartDate(converterJob.getStartDate().substring(0, 8) + DateUtils.START_TIME);
		}
		if(!StringUtils.isEmpty(converterJob.getEndDate())) {
			converterJob.setEndDate(converterJob.getEndDate().substring(0, 8) + DateUtils.END_TIME);
		}
		
		long totalCount = converterService.getConverterJobTotalCount(converterJob);
		
		Pagination pagination = new Pagination(	request.getRequestURI(),
												getSearchParameters(PageType.LIST, converterJob), 
												totalCount, 
												Long.parseLong(pageNo),
												converterJob.getListCounter());
		log.info("@@ pagination = {}", pagination);
		
		converterJob.setOffset(pagination.getOffset());
		converterJob.setLimit(pagination.getPageRows());
		List<ConverterJob> converterJobList = new ArrayList<>();
		if(totalCount > 0L) {
			converterJobList = converterService.getListConverterJob(converterJob);
		}
		
		model.addAttribute(pagination);
		model.addAttribute("converterJobList", converterJobList);
		
		return "/converter/list";
	}

	/**
	 *
	 * @param request
	 * @param pageNo
	 * @param converterJobFile
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/converter-job-file-list")
	public String converterJobFileList(HttpServletRequest request, @RequestParam(defaultValue="1") String pageNo, ConverterJobFile converterJobFile, Model model) {

		converterJobFile.setSearchWord(SQLInjectSupport.replaceSqlInection(converterJobFile.getSearchWord()));
		converterJobFile.setOrderWord(SQLInjectSupport.replaceSqlInection(converterJobFile.getOrderWord()));

		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
		converterJobFile.setUserId(userSession.getUserId());
		log.info("@@ converterJobFile = {}", converterJobFile);

		if(!StringUtils.isEmpty(converterJobFile.getStartDate())) {
			converterJobFile.setStartDate(converterJobFile.getStartDate().substring(0, 8) + DateUtils.START_TIME);
		}
		if(!StringUtils.isEmpty(converterJobFile.getEndDate())) {
			converterJobFile.setEndDate(converterJobFile.getEndDate().substring(0, 8) + DateUtils.END_TIME);
		}

		long totalCount = converterService.getConverterJobFileTotalCount(converterJobFile);
		StringBuffer buffer = new StringBuffer(converterJobFile.getParameters());
		Pagination pagination = new Pagination(request.getRequestURI(), buffer.toString(),
				totalCount, Long.parseLong(pageNo), converterJobFile.getListCounter());
		converterJobFile.setOffset(pagination.getOffset());
		converterJobFile.setLimit(pagination.getPageRows());

		List<ConverterJobFile> converterJobFileList = new ArrayList<>();
		if(totalCount > 0l) {
			converterJobFileList = converterService.getListConverterJobFile(converterJobFile);
		}

		model.addAttribute(pagination);
		model.addAttribute("converterJobFileList", converterJobFileList);

		return "/converter/converter-job-file-list";
	}

	/**
	 * 검색 조건
	 * @param pageType
	 * @param converterJob
	 * @return
	 */
	private String getSearchParameters(PageType pageType, ConverterJob converterJob) {
		StringBuilder builder = new StringBuilder(converterJob.getParameters());
		boolean isListPage = true;
		if(pageType == PageType.MODIFY || pageType == PageType.DETAIL) {
			isListPage = false;
		}
		
//		if(!isListPage) {
//			builder.append("pageNo=" + request.getParameter("pageNo"));
//			builder.append("&");
//			builder.append("list_count=" + uploadData.getList_counter());
//		}
		
		return builder.toString();
	}
	
	private String roleValidator(HttpServletRequest request, Integer userGroupId, String roleName) {
		List<String> userGroupRoleKeyList = CacheManager.getUserGroupRoleKeyList(userGroupId);
        if(!RoleSupport.isUserGroupRoleValid(userGroupRoleKeyList, roleName)) {
			log.info("---- Role 이 존재하지 않습니다. 확인 하세요. ");
        	request.setAttribute("httpStatusCode", 403);
			return "/error/error";
		}
		return null;
	}
}