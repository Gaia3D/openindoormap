package io.openindoormap.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openindoormap.common.BaseControllerTest;
import io.openindoormap.domain.Key;
import io.openindoormap.domain.issue.Issue;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.persistence.IssueMapper;
import io.openindoormap.service.IssueService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@TestMethodOrder(OrderAnnotation.class)
public class IssueTests extends BaseControllerTest {

	@Autowired
	MockHttpSession session;
	@Autowired
	IssueService issueService;
	@Autowired
	IssueMapper issueMapper;
	@Autowired
	ModelMapper modelMapper;
	@Autowired
	ObjectMapper objectMapper;
	
	@BeforeEach
	public void initSession() throws Exception {
		UserSession userSession = UserSession.builder()
									.userId("admin")
									.userGroupId(1)
									.build();
		this.session.setAttribute(Key.USER_SESSION.name(), userSession);
	}
	
	public void 목록_조회() throws Exception {
		// 이슈 목록 조회
		MvcResult result = mockMvc.perform(get("/issues")
				.session(session)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andReturn();
		@SuppressWarnings("unchecked")
		Map<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
		log.info("map ========================== {} " , map);
		List<Issue> issueList = objectMapper.convertValue(map.get("issueList"),  new TypeReference<List<Issue>>(){});

		assertTrue(Integer.parseInt(map.get("totalCount").toString())  == issueList.size());
	}
	
	public void 반경_목록_조회() throws Exception {
		// 특정 위치를 반경으로 하여 목록 조회 가능한지 테스트
		MvcResult result = mockMvc.perform(get("/issues")
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.param("location", "POINT(127.89204364424 38.5682800011791)"))
			.andDo(print())
			.andReturn();
		@SuppressWarnings("unchecked")
		Map<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
		log.info("map ========================== {} " , map);
		List<Issue> issueList = objectMapper.convertValue(map.get("issueList"),  new TypeReference<List<Issue>>(){});

		assertTrue(Integer.parseInt(map.get("totalCount").toString())  == issueList.size());
	}
	
	public void 이슈_디테일() throws Exception {
		// 이슈상세정보 읽어오기
		Long testIssueId = 5l;
		MvcResult result = mockMvc.perform(get("/issues/{issueId}",testIssueId)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andReturn();
		@SuppressWarnings("unchecked")
		Map<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
		log.info("map ========================== {} " , map);
		Issue issue = objectMapper.convertValue(map.get("issue"),  new TypeReference<Issue>(){});

		assertTrue(testIssueId.longValue() == issue.getIssueId().longValue());
	}
}
