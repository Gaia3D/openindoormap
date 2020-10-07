package io.openindoormap.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import io.openindoormap.OIMUserApplication;
import io.openindoormap.service.AccessLogService;

import java.nio.charset.StandardCharsets;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ContextConfiguration(classes = OIMUserApplication.class)
//@SpringBootTest
public class BaseControllerTest {

	protected MockMvc mockMvc;
	@MockBean
	protected AccessLogService accessLogService;
//	@Autowired
//	protected MockHttpSession session;
//	@Autowired
//	protected ObjectMapper objectMapper;
//	@Autowired
//	protected ModelMapper modelMapper;

	@BeforeEach
	public void setup(WebApplicationContext ctx, RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
				.addFilter(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
				.apply(
						documentationConfiguration(restDocumentation)
						.operationPreprocessors()
						.withRequestDefaults(prettyPrint())
						.withResponseDefaults(prettyPrint())
				)
				.build();
	}
}
