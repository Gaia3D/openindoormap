/**
 * 
 */
package io.openindoormap.controller.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 지구 계획
 * @author gravity
 * @since 2020. 8. 20.
 *
 */
@Controller
@RequestMapping("/cityplan/")
public class CityPlanController {
	private static final String P = "/cityplan/";
	
	/**
	 * 지구 단위 계획 확인
	 * @return
	 */
	@RequestMapping("city-unit-plan-confm")
	public String cityUnitPlanConfm() {
		
		return P + "city-unit-plan-confm"; 
	}
	
	/**
	 * 평균 높이 확인
	 * @return
	 */
	@RequestMapping("avrg-hg-confm")
	public String avrgHgConfm() {
		
		return P + "avrg-hg-confm"; 
	}
	
	/**
	 * 지구 계획 실행
	 * @return
	 */
	@RequestMapping("city-plan-exc")
	public String cityPlanExc() {
		
		return P + "city-plan-exc"; 
	}
	
	
	/**
	 * TODO 지구 목록
	 * @return
	 * @throws JsonProcessingException 
	 */
	@GetMapping("/lot-view/city-datas")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> cityDatas() throws JsonProcessingException{		
		
		//
		Map<String,Object> map = new HashMap<>();
		
		List<Map<String,Object>> dummyLists = new ArrayList<>();
		//
		Map<String,Object> m1 = new HashMap<>();
		dummyLists.add(m1);
		m1.put("cityNo", "c1");
		m1.put("cityName", "하남 교산");
		
		//
		Map<String,Object> m2 = new HashMap<>();
		dummyLists.add(m2);
		m2.put("cityNo", "c2");
		m2.put("cityName", "과천 과천");
		
		//		
		map.put("datas", dummyLists);
		
		//
		return new ResponseEntity<Map<String,Object>>(map, HttpStatus.OK);
	}
	
	
	
	/**
	 * TODO 지구의 레이어 목록
	 * @return
	 * @throws JsonProcessingException 
	 */
	@ResponseBody
	@GetMapping("/lot-view/city-datas/{cityNo}/layer-datas")
	public ResponseEntity<Map<String,Object>> layerDatas(@PathVariable("cityNo") String cityNo) throws JsonProcessingException{		
		
		//
		Map<String,Object> map = new HashMap<>();
		
		List<Map<String,Object>> dummyLists = new ArrayList<>();
		//
		Map<String,Object> m1 = new HashMap<>();
		dummyLists.add(m1);
		m1.put("layerKey", "layerkey");
		m1.put("layerName", cityNo + "'s layer명");
		
		//
		Map<String,Object> m2 = new HashMap<>();
		dummyLists.add(m2);
		m2.put("layerKey", "sk_sdo");
		m2.put("layerName", cityNo + "'s sk_sdo");
		
		//		
		map.put("datas", dummyLists);
		
		//
		return new ResponseEntity<Map<String,Object>>(map, HttpStatus.OK);
	}
	
}
