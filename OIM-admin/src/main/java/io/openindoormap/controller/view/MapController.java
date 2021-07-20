package io.openindoormap.controller.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import io.openindoormap.domain.*;
import io.openindoormap.domain.data.DataInfo;
import io.openindoormap.domain.policy.GeoPolicy;
import io.openindoormap.domain.user.UserPolicy;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.service.DataService;
import io.openindoormap.service.GeoPolicyService;
import io.openindoormap.service.UserPolicyService;
import io.openindoormap.support.LogMessageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 지도에서 위치 찾기, 보기 등을 위한 공통 클래스
 * @author Jeongdae
 *
 */
@Slf4j
@Controller
@RequestMapping("/map")
public class MapController {
	
	@Autowired
	private DataService dataService;
	@Autowired
	private GeoPolicyService geoPolicyService;
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private UserPolicyService userPolicyService;
	
	/**
	 * 위치(경도, 위도) 찾기
     * @param request
     * @param dataId
     * @param model
     * @return
     */
    @GetMapping(value = "/find-data-point")
    public String findDataPoint(HttpServletRequest request, DataInfo dataInfo, Model model) {
    	log.info("@@@@@@ dataInfo = {}, referrer = ", dataInfo.getReferrer());
    	
    	// list, modify 에서 온것 구분하기 위함
    	String referrer = dataInfo.getReferrer();
    	dataInfo = dataService.getData(dataInfo);
		
		String dataInfoJson = "";
		try {
			dataInfoJson = objectMapper.writeValueAsString(dataInfo);
		} catch(JsonProcessingException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ jsonProcessing exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		}
		
		model.addAttribute("referrer", referrer);
		model.addAttribute("dataInfo", dataInfo);
		model.addAttribute("dataInfoJson", dataInfoJson);
        
        return "/map/find-data-point";
    }
    
    /**
	 * 위치(경도, 위도) 찾기
     * @param request
     * @param model
     * @return
     */
    @GetMapping(value = "/find-point")
    public String findPoint(HttpServletRequest request, Model model) {
        return "/map/find-point";
    }

    /**
	 * 위치(경도, 위도) 보기
     * @param request
     * @param model
     * @return
     */
    @GetMapping(value = "/fly-to-point")
    public String flyToPoint(HttpServletRequest request, Model model, @RequestParam(value="readOnly",required=false) Boolean readOnly,
    		@RequestParam(value="longitude",required=false) String longitude, @RequestParam(value="latitude",required=false) String latitude) {

        UserSession userSession = (UserSession)request.getSession().getAttribute(Key.USER_SESSION.name());
        UserPolicy userPolicy = userPolicyService.getUserPolicy(userSession.getUserId());
        GeoPolicy geoPolicy = geoPolicyService.getGeoPolicy();
        try {
        	if(!StringUtils.isEmpty(longitude)) {
            	geoPolicy.setInitLongitude(longitude);
        	}
        	if(!StringUtils.isEmpty(latitude)) {
            	geoPolicy.setInitLatitude(latitude);
        	}
        	if(readOnly == null) {
        		readOnly = true;
        	}
            model.addAttribute("geoPolicyJson", objectMapper.writeValueAsString(geoPolicy));
        } catch(JsonProcessingException e) {
        	LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ jsonProcessing exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch(DataAccessException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ dataAccess exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		} catch(RuntimeException e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ runtime exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		} catch(Exception e) {
			LogMessageSupport.printMessage(e, "@@@@@@@@@@@@ exception. message = {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		}

        model.addAttribute("readOnly", readOnly);
        model.addAttribute("baseLayers", userPolicy.getBaseLayers());
        return "/map/fly-to-point";
    }
}
