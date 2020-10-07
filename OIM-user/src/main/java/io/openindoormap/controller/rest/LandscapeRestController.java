/**
 * 
 */
package io.openindoormap.controller.rest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.openindoormap.domain.landscape.LandScapeDiffParam;

/**
 * @author gravity
 * @since 2020. 9. 14.
 *
 */
@RestController
public class LandscapeRestController {
	
	@Value("${openindoormap.data-upload-dir}")
	private String dataUploadDir;
	
	@Value("${app.ls.diff.uri}")
	private String lsDiffUri;
	
	
	/**
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/ls-diff-rest/group")
	public ResponseEntity<Map<String,Object>> getGroups() throws IOException{
		
		String responseString = CmmnUtils.httpGet(lsDiffUri + "/ls-diff-rest/group");
		
		//
		Map<String,Object> map = new HashMap<>();
		map.putAll( new ObjectMapper().readValue(responseString, Map.class));
		
		//
		return new ResponseEntity<Map<String,Object>>(map, HttpStatus.OK);
	}
	
	
	/**
	 * @param domain
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/ls-diff-rest")
	public ResponseEntity<Map<String,Object>> save(LandScapeDiffParam domain) throws IOException{
		String filename = CmmnUtils.createShortUid("f");
		domain.getImage().transferTo(Paths.get(dataUploadDir, filename));
		
		//
		HttpEntity entity = MultipartEntityBuilder.create()
				.addPart("captureCameraState", new StringBody(domain.getCaptureCameraState(), ContentType.TEXT_PLAIN))
				.addPart("landScapeDiffGroupId", new StringBody(domain.getLandScapeDiffGroupId()+"", ContentType.TEXT_PLAIN))
				.addPart("landscapeName", new StringBody(domain.getLandscapeName(), ContentType.TEXT_PLAIN))
				.addPart("image", new FileBody(Paths.get(dataUploadDir, filename).toFile()))
				.build();
		
		//
		CmmnUtils.httpPost(lsDiffUri, entity);
		
		//
		Paths.get(dataUploadDir, filename).toFile().delete();
		
		//
		Map<String,Object> map = new HashMap<>();
		return new ResponseEntity<Map<String,Object>>(map, HttpStatus.OK);
	}
	
	
	
	@SuppressWarnings("unchecked")
	@GetMapping("/ls-diff-rest/{groupId}?lsDiffPage={pageNum}")
	public ResponseEntity<Map<String,Object>> gets(@PathVariable("groupId") String groupId, @PathVariable("pageNum") String pageNum) throws IOException{
		Map<String,Object> param = new HashMap<>();
		param.put("pageNum", pageNum);
		
		//
		String responseString = CmmnUtils.httpGet(lsDiffUri + "/"+groupId, param);
		//
		Map<String,Object> map = new HashMap<>();
		map.putAll(new ObjectMapper().readValue(responseString, Map.class));
		//
		return new ResponseEntity<Map<String,Object>>(map, HttpStatus.OK);
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	@GetMapping("/ls-diff-rest/scene/{id}")
	public ResponseEntity<Map<String,Object>> getScene(@PathVariable("id") String id) throws IOException{
		String responseString = CmmnUtils.httpGet(lsDiffUri + "/scene/" + id);
		
		//
		Map<String,Object> map = new HashMap<>();
		map.putAll(new ObjectMapper().readValue(responseString, Map.class));
		//
		return new ResponseEntity<Map<String,Object>>(map, HttpStatus.OK);
	}
	
	
	//////////////////////////////////////////////////
}


class CmmnUtils extends PpUtil{

	public static String httpGet(String uri) throws IOException {
		return httpGet(uri, null);
	}
	
	


	public static String httpPost(String uri, HttpEntity reqEntity) throws ClientProtocolException, IOException {
		try(CloseableHttpClient httpClient = HttpClients.createDefault()){
			
			//
//			if(PpUtil.isNotEmpty(param)) {
//				uri += "?_=" + PpUtil.createShortUid("");
//				
//				//
//				Iterator<String> iter = param.keySet().iterator();
//				while(iter.hasNext()) {
//					String k = "";
//					uri +=  "&" + k + "=" + param.get(k);
//				}
//			}
			
			//
			
			//	
			HttpPost hp = new HttpPost(uri);
			hp.setEntity(reqEntity);
			
			//
			try(CloseableHttpResponse response = httpClient.execute(hp)){
				HttpEntity entity = response.getEntity();
				if(null == entity) {
					return null;
				}
				
				//
				return EntityUtils.toString(entity);
			}
		}
		//
	}
	


	/**
	 * 
	 * @param uri
	 * @param param
	 * @return
	 * @throws IOException
	 */
	public static String httpGet(String uri, Map<String,Object> param) throws IOException {
		try(CloseableHttpClient httpClient = HttpClients.createDefault()){
			
			//
			if(PpUtil.isNotEmpty(param)) {
				uri += "?_=" + PpUtil.createShortUid("");
				
				//
				Iterator<String> iter = param.keySet().iterator();
				while(iter.hasNext()) {
					String k = "";
					uri +=  "&" + k + "=" + param.get(k);
				}
			}
			
			//	
			HttpGet g = new HttpGet(uri);
			//
			try(CloseableHttpResponse response = httpClient.execute(g)){
				HttpEntity entity = response.getEntity();
				if(null == entity) {
					return null;
				}
				
				//
				return EntityUtils.toString(entity);
			}
		}
		//
	}
	
}


class PpUtil{



	/**
	 * 널여부 검사
	 * @param obj 오브젝트
	 * @return 널이면 true
	 */
	public static boolean isNull(Object obj){
		return (null == obj);
	}

	/**
	 * 공백 여부
	 * @param obj 오브젝트. String|Collection|Map|Set|List|배열
	 * @return 공백이면 true
	 * @since
	 * 	20180322	배열, 리스트 처리 추가
	 * 	20200221	Map관련 추가
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj) {
			if(isNull(obj)){
				return true;
			}
			
			//문자열
			if(String.class == obj.getClass() ) {
				return (0 == obj.toString().trim().length());
			}
			
			//
			if(obj instanceof Collection) {
				return (0 ==((Collection)obj).size());
			}
			
			//
			if(obj instanceof Map) {
				return (0 == ((Map)obj).size());
			}
			
			//
			if(Set.class == obj.getClass()) {
				return (0 == ((Set)obj).size());
			}
			
			//리스트
			if(List.class == obj.getClass() || (ArrayList.class == obj.getClass())) {
				return (0 == ((List)obj).size());
			}
			
			
			//배열
	//		if(obj.getClass().toString().contains("[L")) {
	//			return (0 == Array.getLength(obj));
	//		}
			
			//
			return (0 == obj.toString().length());
		}

	/**
	 * nanotime 으로 유니크한 문자열 생성
	 * @param prefix 리턴값 앞에 붙일 접두어
	 * @return 유니크한 문자열
	 * @since
	 * 	20180215	prefix 추가
	 */
	public static String createShortUid(String prefix) {		
			return (isEmpty(prefix) ? "UID" : prefix) + System.nanoTime(); 
	//		return (isEmpty(prefix) ? "UID" : prefix) 
	//				+ (new SimpleDateFormat("yyyyMMddHHmmssSSS")).format(new Date())
	//				+ (new Random()).nextInt(10);
		}

	
	

	/**
	 * isEmpty의 반대
	 * @param obj 문자열
	 * @return true / false
	 * 	true 조건
	 * 		문자열인 경우 공백이 아니면
	 * 		collection(Set, List,...)인 경우 0 &lt; size
	 * 		배열인 경우 0 &lt; length
	 * 		Map인 경우 0 &lt; size
	 */
	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}
	
	
}