package io.openindoormap.controller.view;
//package lhdt.controller.view;
//
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.servlet.error.ErrorAttributes;
//import org.springframework.boot.web.servlet.error.ErrorController;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Controller
//public class CustomErrorController implements ErrorController {
//	@Autowired
//	private ErrorAttributes errorAttributes;
//
//	@Override
//	public String getErrorPath() {
//		return "/error";
//	}
//
//	@RequestMapping("/error")
//	public String error(HttpServletRequest servletRequest, Model model) {
//		Map<String, Object> attrs = errorAttributes.getErrorAttributes(new ServletRequestAttributes(servletRequest), false);
//		model.addAttribute("attrs", attrs);
//		log.info("@@@@@@@@@@@@@@@@@@@@@@@ CustomErrorController attrs = {}", attrs.toString());
//		return "/error/error";
//	}
//}