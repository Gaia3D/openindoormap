package io.openindoormap.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openindoormap.domain.user.UserInfo;
import io.openindoormap.service.SocialOauth;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KakaoOauthImpl implements SocialOauth {

    @Value("${sns.kakao.url}")
    private String KAKAO_SNS_BASE_URL;
    @Value("${sns.kakao.client.id}")
    private String KAKAO_SNS_CLIENT_ID;
    @Value("${sns.kakao.callback.url}")
    private String KAKAO_SNS_CALLBACK_URL;
    @Value("${sns.kakao.client.secret}")
    private String KAKAO_SNS_CLIENT_SECRET;
    @Value("${sns.kakao.token.url}")
    private String KAKAO_SNS_TOKEN_BASE_URL;
    @Value("${sns.kakao.userinfo.url}")
    private String KAKAO_SNS_USERINFO_URL;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        // params.put("scope", "https://www.kakaoapis.com/auth/userinfo.email https://www.kakaoapis.com/auth/userinfo.profile openid");
        params.put("response_type", "code");
        params.put("client_id", KAKAO_SNS_CLIENT_ID);
        params.put("redirect_uri", KAKAO_SNS_CALLBACK_URL);
        // params.put("scope", "profile");

        String parameterString = params.entrySet().stream()
                                .map(x -> x.getKey() + "=" + x.getValue())
                                .collect(Collectors.joining("&"));
        return KAKAO_SNS_BASE_URL + "?" + parameterString;
    }

    @Override
    public String requestAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", KAKAO_SNS_CLIENT_ID);
        params.put("redirect_uri", KAKAO_SNS_CALLBACK_URL);
        params.put("code", code);
        params.put("client_secret", KAKAO_SNS_CLIENT_SECRET);

        // Content-Type: application/x-www-form-urlencoded
        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<String>(parameterString, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(KAKAO_SNS_TOKEN_BASE_URL, request, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            try {
                KakaoToken token = objectMapper.readValue(responseEntity.getBody(), KakaoToken.class);
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
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<String>("property_key=[\"properties.nickname\"]",  headers);
            log.debug("================> " + accessToken);

            ResponseEntity<String> userInfo = restTemplate.exchange(KAKAO_SNS_USERINFO_URL, HttpMethod.GET, request, String.class);
            log.debug("================> " + userInfo.getBody());
            // UserInfo.builder().email(accessToken).userName(accessToken).build()
            KakaoUserInfo g = objectMapper.readValue(userInfo.getBody(), KakaoUserInfo.class);
            UserInfo u = UserInfo.builder().userId(g.getId()).userName(g.name).build();
            return u;
        } catch (JsonProcessingException e) {
            log.error(" JsonProcessingException =>  ", e);
            throw new RuntimeException("구글 로그인 요청 처리 실패");
        }
    }

    @Getter
    @Setter
    private static class KakaoToken {
        private String tokenType;
        private String accessToken;
        private int expiresIn;
        private String refreshToken;
        private int refreshTokenExpiresIn;
        private String scope;
        private String idToken;
    }

    @Getter
    @Setter
    private static class KakaoUserInfo {
        private String id;
        private String connectedAt;
        private String name;
    }
}
