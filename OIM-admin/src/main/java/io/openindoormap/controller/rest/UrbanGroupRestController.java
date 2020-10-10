package io.openindoormap.controller.rest;

import io.openindoormap.domain.Key;
import io.openindoormap.domain.urban.UrbanGroup;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.UrbanGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 도시 ajax 처리 관리
 */
@Slf4j
@RestController
@RequestMapping("/urban-groups")
public class UrbanGroupRestController {

	@Autowired
	private UrbanGroupService urbanGroupService;

	/**
	 * 그룹 Key 중복 체크
	 * @param request
	 * @param urbanGroup
	 * @return
	 */
	@GetMapping(value = "/duplication")
	public Map<String, Object> ajaxKeyDuplicationCheck(HttpServletRequest request, UrbanGroup urbanGroup) {
		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		// TODO @Valid 로 구현해야 함
		if(StringUtils.isEmpty(urbanGroup.getUrbanGroupKey())) {
			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
			result.put("errorCode", "urban.group.key.empty");
			result.put("message", message);

			return result;
		}

		Boolean duplication = urbanGroupService.isUrbanGroupKeyDuplication(urbanGroup);
		log.info("@@ duplication = {}", duplication);
		int statusCode = HttpStatus.OK.value();

		result.put("duplication", duplication);
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);

		return result;
	}

	/**
	 * 도시 그룹 등록
	 * @param request
	 * @param urbanGroup
	 * @param bindingResult
	 * @return
	 */
	@PostMapping
	public Map<String, Object> insert(HttpServletRequest request, @Valid @ModelAttribute UrbanGroup urbanGroup, BindingResult bindingResult) {
		log.info("@@@@@ insert urbanGroup = {}", urbanGroup);

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

		urbanGroup.setUserId(userSession.getUserId());

		urbanGroupService.insertUrbanGroup(urbanGroup);
		int statusCode = HttpStatus.OK.value();
			
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}

	/**
	 * 도시 그룹 수정
	 * @param request
	 * @param urbanGroup
	 * @param bindingResult
	 * @return
	 */
	@PutMapping("/{urbanGroupId:[0-9]+}")
	public Map<String, Object> update(HttpServletRequest request, @Valid UrbanGroup urbanGroup, BindingResult bindingResult) {
		log.info("@@ urbanGroup = {}", urbanGroup);
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

		urbanGroupService.updateUrbanGroup(urbanGroup);
		int statusCode = HttpStatus.OK.value();

		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}

	/**
	 * 도시 그룹 트리 순서 수정 (up/down)
	 * @param request
	 * @param urbanGroupId
	 * @param urbanGroup
	 * @return
	 */
	@PostMapping(value = "/view-order/{urbanGroupId:[0-9]+}")
	public Map<String, Object> moveUrbanGroup(HttpServletRequest request, @PathVariable Integer urbanGroupId, @ModelAttribute UrbanGroup urbanGroup) {
		log.info("@@ urbanGroup = {}", urbanGroup);

		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;
		
		urbanGroup.setUrbanGroupId(urbanGroupId);

		int updateCount = urbanGroupService.updateUrbanGroupViewOrder(urbanGroup);
		int statusCode = HttpStatus.OK.value();
		if(updateCount == 0) {
			statusCode = HttpStatus.BAD_REQUEST.value();
			errorCode = "layer.group.view-order.invalid";
		}
			
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}
}
