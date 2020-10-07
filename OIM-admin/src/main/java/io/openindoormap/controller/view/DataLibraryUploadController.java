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
import io.openindoormap.domain.Key;
import io.openindoormap.domain.PageType;
import io.openindoormap.domain.common.Pagination;
import io.openindoormap.domain.common.Search;
import io.openindoormap.domain.converter.ConverterJob;
import io.openindoormap.domain.extrusionmodel.DataLibrary;
import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;
import io.openindoormap.domain.extrusionmodel.DataLibraryUpload;
import io.openindoormap.domain.extrusionmodel.DataLibraryUploadFile;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.DataLibraryGroupService;
import io.openindoormap.service.DataLibraryService;
import io.openindoormap.service.DataLibraryUploadService;
import io.openindoormap.service.PolicyService;
import io.openindoormap.support.SQLInjectSupport;
import io.openindoormap.utils.DateUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 데이터 라이브러리
 */
@Slf4j
@Controller
@RequestMapping("/data-library-upload")
public class DataLibraryUploadController {

    // 파일 copy 시 버퍼 사이즈
    public static final int BUFFER_SIZE = 8192;

	@Autowired
	private DataLibraryGroupService dataLibraryGroupService;
	@Autowired
	private DataLibraryUploadService dataLibraryUploadService;
	@Autowired
	private PolicyService policyService;
	@Autowired
	private PropertiesConfig propertiesConfig;

	/**
	 * 데이터 라이브러리 upload 화면
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/input")
	public String input(HttpServletRequest request, Model model) {
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());

		DataLibraryGroup dataLibraryGroup = new DataLibraryGroup();
		dataLibraryGroup.setUserId(userSession.getUserId());
		List<DataLibraryGroup> dataLibraryGroupList = dataLibraryGroupService.getListDataLibraryGroup(dataLibraryGroup);
		DataLibraryGroup basicDataLibraryGroup = dataLibraryGroupService.getBasicDataLibraryGroup();

		// basic 디렉토리를 실수로 지웠거나 만들지 않았는지 확인
		File basicDirectory = new File(propertiesConfig.getAdminDataLibraryServiceDir() + "basic");
		if(!basicDirectory.exists()) {
			basicDirectory.mkdir();
		}

		DataLibraryUpload dataLibraryUpload = DataLibraryUpload.builder().
				dataLibraryGroupId(basicDataLibraryGroup.getDataLibraryGroupId()).
				dataLibraryGroupName(basicDataLibraryGroup.getDataLibraryGroupName()).build();

		String acceptedFiles = policyService.getUserUploadType();

		model.addAttribute("dataLibraryUpload", dataLibraryUpload);
		model.addAttribute("dataLibraryGroupList", dataLibraryGroupList);
		model.addAttribute("acceptedFiles", acceptedFiles);

		return "/data-library-upload/input";
	}

	/**
	 * 데이터 라이브러리 업로드 파일 목록
	 * @param request
	 * @param dataLibraryUpload
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/list")
	public String list(HttpServletRequest request, DataLibraryUpload dataLibraryUpload, @RequestParam(defaultValue="1") String pageNo, Model model) {
		dataLibraryUpload.setSearchWord(SQLInjectSupport.replaceSqlInection(dataLibraryUpload.getSearchWord()));
		dataLibraryUpload.setOrderWord(SQLInjectSupport.replaceSqlInection(dataLibraryUpload.getOrderWord()));

		log.info("@@ dataLibraryUpload = {}", dataLibraryUpload);

//		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
//		dataLibraryUpload.setUserId(userSession.getUserId());

		if(!StringUtils.isEmpty(dataLibraryUpload.getStartDate())) {
			dataLibraryUpload.setStartDate(dataLibraryUpload.getStartDate().substring(0, 8) + DateUtils.START_TIME);
		}
		if(!StringUtils.isEmpty(dataLibraryUpload.getEndDate())) {
			dataLibraryUpload.setEndDate(dataLibraryUpload.getEndDate().substring(0, 8) + DateUtils.END_TIME);
		}

		long totalCount = dataLibraryUploadService.getDataLibraryUploadTotalCount(dataLibraryUpload);
		Pagination pagination = new Pagination(request.getRequestURI(), getSearchParameters(PageType.LIST, dataLibraryUpload),
				totalCount, Long.parseLong(pageNo), dataLibraryUpload.getListCounter());
		dataLibraryUpload.setOffset(pagination.getOffset());
		dataLibraryUpload.setLimit(pagination.getPageRows());

		List<DataLibraryUpload> dataLibraryUploadList = new ArrayList<>();
		if(totalCount > 0l) {
			dataLibraryUploadList = dataLibraryUploadService.getListDataLibraryUpload(dataLibraryUpload);
		}

		model.addAttribute(pagination);
		model.addAttribute("dataLibraryUpload", dataLibraryUpload);
		model.addAttribute("converterJobForm", new ConverterJob());
		model.addAttribute("dataLibraryUploadList", dataLibraryUploadList);

		return "/data-library-upload/list";
	}

	/**
	 * 데이터 라이브러리 업로드 수정
	 * @param model
	 * @return
	 */
	@GetMapping(value = "/modify")
	public String modify(HttpServletRequest request, DataLibraryUpload dataLibraryUpload, Model model) {
		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
//		dataLibraryUpload.setUserId(userSession.getUserId());

		dataLibraryUpload = dataLibraryUploadService.getDataLibraryUpload(dataLibraryUpload);
		List<DataLibraryUploadFile> dataLibraryUploadFileList = dataLibraryUploadService.getListDataLibraryUploadFile(dataLibraryUpload);

		DataLibraryGroup dataLibraryGroup = new DataLibraryGroup();
		dataLibraryGroup.setUserId(userSession.getUserId());
		List<DataLibraryGroup> dataLibraryGroupList = dataLibraryGroupService.getListDataLibraryGroup(dataLibraryGroup);

		model.addAttribute("dataLibraryUpload", dataLibraryUpload);
		model.addAttribute("dataLibraryUploadFileList", dataLibraryUploadFileList);
		model.addAttribute("dataLibraryGroupList", dataLibraryGroupList);

		return "/data-library-upload/modify";
	}

//	/**
//	 * 데이터 라이브러리 정보
//	 * @param dataLibrary
//	 * @param model
//	 * @return
//	 */
//	@GetMapping(value = "/detail")
//	public String detail(HttpServletRequest request, DataLibrary dataLibrary, Model model) {
//		log.info("@@@ detail-info dataLibrary = {}", dataLibrary);
//
//		String listParameters = getSearchParameters(PageType.DETAIL, dataLibrary);
//
//		dataLibrary =  dataLibraryService.getDataLibrary(dataLibrary);
//		Policy policy = policyService.getPolicy();
//
//		model.addAttribute("policy", policy);
//		model.addAttribute("listParameters", listParameters);
//		model.addAttribute("dataLibrary", dataLibrary);
//
//		return "/data-library/detail-data";
//	}
//
//	/**
//	 * 데이터 라이브러리 수정 화면
//	 * @param request
//	 * @param dataLibraryId
//	 * @param model
//	 * @return
//	 */
//	@GetMapping(value = "/modify")
//	public String modify(HttpServletRequest request, @RequestParam("dataLibraryId") Long dataId, Model model) {
//		//UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
//
//		DataLibrary dataLibrary = new DataLibrary();
//		//dataLibrary.setUserId(userSession.getUserId());
//		dataLibrary.setDataLibraryId(dataLibraryId);
//
//		dataLibrary = dataLibraryService.getDataLibrary(dataLibrary);
//
//		model.addAttribute("dataLibrary", dataLibrary);
//
//		return "/data-library/modify";
//	}

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
