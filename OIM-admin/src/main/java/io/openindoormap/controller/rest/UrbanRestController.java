package io.openindoormap.controller.rest;
//package lhdt.controller.rest;
//
//import lhdt.controller.AuthorizationController;
//import lhdt.domain.urban.Urban;
//import lhdt.service.NewTownService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.validation.Valid;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 뉴타운
// * @author kimhj
// *
// */
//@Slf4j
//@RestController
//@RequestMapping("/new-towns")
//public class UrbanRestController implements AuthorizationController {
//
//	@Autowired
//	private NewTownService newTownService;
//
//	/**
//	 * 뉴타운 등록
//	 * @param request
//	 * @param newTown
//	 * @param bindingResult
//	 * @return
//	 */
//	@PostMapping
//	public Map<String, Object> insert(HttpServletRequest request, @Valid @ModelAttribute Urban newTown, BindingResult bindingResult) {
//		log.info("@@@@@ insert newTown = {}", newTown);
//
//		Map<String, Object> result = new HashMap<>();
//		String errorCode = null;
//		String message = null;
//
//		if(bindingResult.hasErrors()) {
//			message = bindingResult.getAllErrors().get(0).getDefaultMessage();
//			log.info("@@@@@ message = {}", message);
//			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
//			result.put("errorCode", errorCode);
//			result.put("message", message);
//            return result;
//		}
//
//		newTownService.insertNewTown(newTown);
//		int statusCode = HttpStatus.OK.value();
//
//		result.put("statusCode", statusCode);
//		result.put("errorCode", errorCode);
//		result.put("message", message);
//		return result;
//	}
//
//	/**
//	 * 뉴타운 수정
//	 * @param request
//	 * @param newTown
//	 * @param bindingResult
//	 * @return
//	 */
//	@PutMapping(value = "/{newTownId:[0-9]+}")
//	public Map<String, Object> update(HttpServletRequest request, @Valid Urban newTown, BindingResult bindingResult) {
//		log.info("@@ newTown = {}", newTown);
//
//		Map<String, Object> result = new HashMap<>();
//		String errorCode = null;
//		String message = null;
//
//		if(bindingResult.hasErrors()) {
//			message = bindingResult.getAllErrors().get(0).getDefaultMessage();
//			log.info("@@@@@ message = {}", message);
//			result.put("statusCode", HttpStatus.BAD_REQUEST.value());
//			result.put("errorCode", errorCode);
//			result.put("message", message);
//            return result;
//		}
//
//		newTownService.updateNewTown(newTown);
//		int statusCode = HttpStatus.OK.value();
//
//		result.put("statusCode", statusCode);
//		result.put("errorCode", errorCode);
//		result.put("message", message);
//		return result;
//	}
//
//	/**
//	 * 뉴타운 삭제
//	 * @param newTownId
//	 * @return
//	 */
//	@DeleteMapping(value = "/{newTownId:[0-9]+}")
//	public Map<String, Object> delete(@RequestParam Integer newTownId) {
//		Map<String, Object> result = new HashMap<>();
//		String errorCode = null;
//		String message = null;
//
//		newTownService.deleteNewTown(newTownId);
//		int statusCode = HttpStatus.OK.value();
//
//		result.put("statusCode", statusCode);
//		result.put("errorCode", errorCode);
//		result.put("message", message);
//		return result;
//	}
//}
