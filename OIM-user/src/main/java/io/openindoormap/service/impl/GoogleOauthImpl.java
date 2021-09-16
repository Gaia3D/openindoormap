package io.openindoormap.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import io.openindoormap.domain.user.UserInfo;
import io.openindoormap.service.SocialOauth;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GoogleOauthImpl implements SocialOauth {

    @Value("${sns.google.url}")
    private String GOOGLE_SNS_BASE_URL;
    @Value("${sns.google.client.id}")
    private String GOOGLE_SNS_CLIENT_ID;
    @Value("${sns.google.callback.url}")
    private String GOOGLE_SNS_CALLBACK_URL;
    @Value("${sns.google.client.secret}")
    private String GOOGLE_SNS_CLIENT_SECRET;
    @Value("${sns.google.token.url}")
    private String GOOGLE_SNS_TOKEN_BASE_URL;
    @Value("${sns.google.userinfo.url}")
    private String GOOGLE_SNS_USERINFO_URL;


    private ObjectMapper mapper;

    public GoogleOauthImpl() {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        params.put("scope", "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile openid");
        params.put("response_type", "code");
        params.put("client_id", GOOGLE_SNS_CLIENT_ID);
        params.put("redirect_uri", GOOGLE_SNS_CALLBACK_URL);

        String parameterString = params.entrySet().stream()
                                .map(x -> x.getKey() + "=" + x.getValue())
                                .collect(Collectors.joining("&"));
        return GOOGLE_SNS_BASE_URL + "?" + parameterString;
    }

    @Override
    public String requestAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", GOOGLE_SNS_CALLBACK_URL);
        params.put("client_id", GOOGLE_SNS_CLIENT_ID);
        params.put("client_secret", GOOGLE_SNS_CLIENT_SECRET);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(GOOGLE_SNS_TOKEN_BASE_URL, params, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            try {
                GoogleToken token = mapper.readValue(responseEntity.getBody(), GoogleToken.class);
                return token.getAccessToken();
            } catch (JsonProcessingException e) {
                log.error(" JsonProcessingException =>  ", e);
                throw new RuntimeException("구글 로그인 요청 처리 실패");
            }
        } else {
            throw new RuntimeException("구글 로그인 요청 처리 실패");
        }
    }

    @Override
    public UserInfo getUserInfo(String code) {
        String accessToken = requestAccessToken(code);

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            // headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> request = new HttpEntity<String>(headers);
            log.debug("========================>[" + accessToken + "]");
            ResponseEntity<String> userInfo = restTemplate.exchange(GOOGLE_SNS_USERINFO_URL, HttpMethod.GET, request, String.class);
            log.debug("================> " + userInfo.getBody());
            // UserInfo.builder().email(accessToken).userName(accessToken).build()
            GoogleUserInfo g = mapper.readValue(userInfo.getBody(), GoogleUserInfo.class);
            UserInfo u = UserInfo.builder().userId(g.getId()).email(g.getEmail()).userName(g.getName()).build();
            return u;
        } catch (JsonProcessingException e) {
            log.error(" JsonProcessingException =>  ", e);
            throw new RuntimeException("구글 로그인 요청 처리 실패");
        }
    }

    @Getter
    @Setter
    private static class GoogleToken {
        private String accessToken;
        private int expiresIn;
        private String scope;
        private String tokenType;
        private String idToken;
    }

    @Getter
    @Setter
    private static class GoogleUserInfo {
        private String id;
        private String email;
        private boolean verifiedEmail;
        private String name;
        private String givenName;
        private String familyName;
        private String picture;
        private String locale;
    }
}
