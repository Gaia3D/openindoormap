package io.openindoormap.controller.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.PageType;
import io.openindoormap.domain.common.Pagination;
import io.openindoormap.domain.common.Search;
import io.openindoormap.domain.extrusionmodel.DataLibraryConverterJob;
import io.openindoormap.service.DataLibraryConverterService;
import io.openindoormap.service.DataLibraryGroupService;
import io.openindoormap.service.PolicyService;
import io.openindoormap.support.SQLInjectSupport;
import io.openindoormap.utils.DateUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 데이터 라이브러리
 */
@Slf4j
@Controller
@RequestMapping("/data-library-converter")
public class DataLibraryConverterController {

    // 파일 copy 시 버퍼 사이즈
    public static final int BUFFER_SIZE = 8192;

	@Autowired
	private DataLibraryGroupService dataLibraryGroupService;
	@Autowired
	private DataLibraryConverterService dataLibraryConverterService;
	@Autowired
	private PolicyService policyService;
	@Autowired
	private PropertiesConfig propertiesConfig;

	/**
	 * 변환 job 목록
	 * @param request
	 * @param dataLibraryConverterJob
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/list")
	public String list(HttpServletRequest request, DataLibraryConverterJob dataLibraryConverterJob, @RequestParam(defaultValue="1") String pageNo, Model model) {
		dataLibraryConverterJob.setSearchWord(SQLInjectSupport.replaceSqlInection(dataLibraryConverterJob.getSearchWord()));
		dataLibraryConverterJob.setOrderWord(SQLInjectSupport.replaceSqlInection(dataLibraryConverterJob.getOrderWord()));

//		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
//		dataLibraryConverterJob.setUserId(userSession.getUserId());
		log.info("@@ dataLibraryConverterJob = {}", dataLibraryConverterJob);

		if(!StringUtils.isEmpty(dataLibraryConverterJob.getStartDate())) {
			dataLibraryConverterJob.setStartDate(dataLibraryConverterJob.getStartDate().substring(0, 8) + DateUtils.START_TIME);
		}
		if(!StringUtils.isEmpty(dataLibraryConverterJob.getEndDate())) {
			dataLibraryConverterJob.setEndDate(dataLibraryConverterJob.getEndDate().substring(0, 8) + DateUtils.END_TIME);
		}

		long totalCount = dataLibraryConverterService.getDataLibraryConverterJobTotalCount(dataLibraryConverterJob);
		Pagination pagination = new Pagination(request.getRequestURI(), getSearchParameters(PageType.LIST, dataLibraryConverterJob),
				totalCount, Long.parseLong(pageNo), dataLibraryConverterJob.getListCounter());
		dataLibraryConverterJob.setOffset(pagination.getOffset());
		dataLibraryConverterJob.setLimit(pagination.getPageRows());

		List<DataLibraryConverterJob> dataLibraryConverterJobList = new ArrayList<>();
		if(totalCount > 0l) {
			dataLibraryConverterJobList = dataLibraryConverterService.getListDataLibraryConverterJob(dataLibraryConverterJob);
		}

		model.addAttribute(pagination);
		model.addAttribute("dataLibraryConverterJobList", dataLibraryConverterJobList);
		return "/data-library-converter/list";
	}

	/**
     * 검색 조건
	 * @param pageType
     * @param search
     * @return
     */
	private String getSearchParameters(PageType pageType, Search search) {
		StringBuffer buffer = new StringBuffer(search.getParameters());
		boolean isListPage = true;
		if(pageType == PageType.MODIFY || pageType == PageType.DETAIL) {
			isListPage = false;
		}

//		if(!isListPage) {
//			buffer.append("pageNo=" + request.getParameter("pageNo"));
//			buffer.append("&");
//			buffer.append("list_count=" + dataLibraryUpload.getList_counter());
//		}

		return buffer.toString();
	}
}
