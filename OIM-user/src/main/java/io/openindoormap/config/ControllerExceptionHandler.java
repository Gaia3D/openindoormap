package io.openindoormap.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import io.openindoormap.support.LogMessageSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller Exception 처리
 * @author Cheon JeongDae
 *
 */
@Slf4j
@ControllerAdvice(basePackages = {"io.openindoormap.controller.view"})
public class ControllerExceptionHandler {
	
	@ExceptionHandler(Exception.class)
	public ModelAndView error(Exception exception) {
		log.error("**********************************************************");
		log.error("**************** GlobalExceptionHandler ******************");
		log.error("**********************************************************");
		
		//log.info("@@@ message = {}", exception.getMessage());
		LogMessageSupport.printMessage(exception);
		
		ModelAndView mav = new ModelAndView();
	    mav.addObject("exception", exception);
	    mav.setViewName("/error/error");
	    return mav;
	}

//	@ExceptionHandler(BusinessLogicException.class)
//	public String notFound(Exception exception) {
//		//System.out.println("----Caught KeywordNotFoundException----");
//		return "404";
//	}
}
