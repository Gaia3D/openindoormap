package io.openindoormap.controller.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.openindoormap.domain.Key;
import io.openindoormap.domain.extrusionmodel.DesignLayerGroup;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.DesignLayerGroupService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 디자인 레이어 그룹 관리
 */
@Slf4j
@RestController
@RequestMapping("/design-layer-groups")
public class DesignLayerGroupRestController {

	@Autowired
	private DesignLayerGroupService designLayerGroupService;

	/**
	 * 디자인 레이어 그룹 등록
	 * @param request
	 * @param designLayerGroup
	 * @param bindingResult
	 * @return
	 */
	@PostMapping
	public Map<String, Object> insert(HttpServletRequest request, @Valid @ModelAttribute DesignLayerGroup designLayerGroup, BindingResult bindingResult) {
		log.info("@@@@@ insert designLayerGroup = {}", designLayerGroup);

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

		designLayerGroup.setUserId(userSession.getUserId());

		designLayerGroupService.insertDesignLayerGroup(designLayerGroup);
		int statusCode = HttpStatus.OK.value();
			
		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}

	/**
	 * 디자인 레이어 그룹 수정
	 * @param request
	 * @param designLayerGroup
	 * @param bindingResult
	 * @return
	 */
	@PutMapping("/{designLayerGroupId:[0-9]+}")
	public Map<String, Object> update(HttpServletRequest request, @Valid DesignLayerGroup designLayerGroup, BindingResult bindingResult) {
		log.info("@@ designLayerGroup = {}", designLayerGroup);
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

		designLayerGroupService.updateDesignLayerGroup(designLayerGroup);
		int statusCode = HttpStatus.OK.value();

		result.put("statusCode", statusCode);
		result.put("errorCode", errorCode);
		result.put("message", message);
		return result;
	}

	/**
	 * 디자인 레이어 그룹 트리 순서 수정 (up/down)
	 * @param request
	 * @param designLayerGroupId
	 * @param designLayerGroup
	 * @return
	 */
	@PostMapping(value = "/view-order/{designLayerGroupId:[0-9]+}")
	public Map<String, Object> moveLayerGroup(HttpServletRequest request, @PathVariable Integer designLayerGroupId, @ModelAttribute DesignLayerGroup designLayerGroup) {
		log.info("@@ designLayerGroup = {}", designLayerGroup);

		Map<String, Object> result = new HashMap<>();
		String errorCode = null;
		String message = null;

		int updateCount = designLayerGroupService.updateDesignLayerGroupViewOrder(designLayerGroup);
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
