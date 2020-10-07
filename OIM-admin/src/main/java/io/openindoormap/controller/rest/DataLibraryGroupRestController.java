package io.openindoormap.controller.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.openindoormap.domain.Key;
import io.openindoormap.domain.extrusionmodel.DataLibraryGroup;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.DataLibraryGroupService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 데이터 라이브러리 그룹 Ajax 통신 관리
 */
@Slf4j
@RestController
@RequestMapping("/data-library-groups")
public class DataLibraryGroupRestController {

	@Autowired
	private DataLibraryGroupService dataLibraryGroupService;

	/**
	 * 데이터 라이브러리 그룹 Key 중복 체크
	 * @param request
	 * @param dataLibraryGroup
	 * @return
	 */
	@GetMapping(value = "/duplication")
	public Map<String, Object> keyDuplicationCheck(HttpServletRequest request, DataLibraryGroup dataLibraryGroup) {
		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		// TODO @Valid 로 구현해야 함
		if(StringUtils.isEmpty(dataLibraryGroup.getDataLibraryGroupKey())) {
			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
			result.put("errorCode", "data.library.group.key.empty");
			result.put("message", message);

			return result;
		}

		Boolean duplication = dataLibraryGroupService.isDataLibraryGroupKeyDuplication(dataLibraryGroup);
		log.info("@@ duplication = {}", duplication);
		int statusCode = HttpStatus.OK.value();

		result.put("duplication", duplication);
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);

		return result;
	}

	/**
	 * 데이터 라이브러리 그룹 등록
	 * @param request
	 * @param dataLibraryGroup
	 * @param bindingResult
	 * @return
	 */
	@PostMapping
	public Map<String, Object> insert(HttpServletRequest request, @Valid @ModelAttribute DataLibraryGroup dataLibraryGroup, BindingResult bindingResult) {
		log.info("@@@@@ insert dataLibraryGroup = {}", dataLibraryGroup);

		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());

		if(bindingResult.hasErrors()) {
			message = bindingResult.getAllErrors().get(0).getDefaultMessage();
			log.info("@@@@@ message = {}", message);
			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
			result.put("errorCode", errorCode);
			result.put("message", message);
            return result;
		}

		dataLibraryGroup.setUserId(userSession.getUserId());

		dataLibraryGroupService.insertDataLibraryGroup(dataLibraryGroup);
		int statusCode = HttpStatus.OK.value();
			
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}

	/**
	 * 데이터 라이브러리 그룹 수정
	 * @param request
	 * @param dataLibraryGroup
	 * @param bindingResult
	 * @return
	 */
	@PutMapping("/{dataLibraryGroupId:[0-9]+}")
	public Map<String, Object> update(HttpServletRequest request, @Valid DataLibraryGroup dataLibraryGroup, BindingResult bindingResult) {
		log.info("@@ dataLibraryGroup = {}", dataLibraryGroup);
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

		dataLibraryGroupService.updateDataLibraryGroup(dataLibraryGroup);
		int statusCode = HttpStatus.OK.value();

		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}

	/**
	 * 데이터 라이브러리 그룹 트리 순서 수정 (up/down)
	 * @param request
	 * @param dataLibraryGroupId
	 * @param dataLibraryGroup
	 * @return
	 */
	@PostMapping(value = "/view-order/{dataLibraryGroupId:[0-9]+}")
	public Map<String, Object> moveLayerGroup(HttpServletRequest request, @PathVariable Integer dataLibraryGroupId, @ModelAttribute DataLibraryGroup dataLibraryGroup) {
		log.info("@@ dataLibraryGroup = {}", dataLibraryGroup);

		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		int updateCount = dataLibraryGroupService.updateDataLibraryGroupViewOrder(dataLibraryGroup);
		int statusCode = HttpStatus.OK.value();
		if(updateCount == 0) {
			statusCode = HttpStatus.BAD_REQUEST.value();
			errorCode = "design.layer.group.view-order.invalid";
		}
			
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}
}
