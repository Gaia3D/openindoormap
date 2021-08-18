package io.openindoormap.service;

import io.openindoormap.domain.social.SocialLoginType;
import io.openindoormap.domain.user.UserInfo;
import io.openindoormap.domain.user.UserSession;

/**
 * 사용자
 * @author jeongdae
 *
 */
public interface UserService {

    /**
     * 사용자 정보 취득
     * @param userId
     * @return
     */
    UserInfo getUser(String userId);

    /**
     * 사용자 비밀번호 수정
     * @param userInfo
     * @return
     */
    int updatePassword(UserInfo userInfo);


    /**
     * 회원 세션 정보를 취득
     * @param userInfo
     * @return
     */
    UserSession getUserSession(UserInfo userInfo);

    /**
     * Sign in 성공 후 회원 정보를 갱신
     * @param userSession
     * @return
     */
    int updateSigninUserSession(UserSession userSession);

    /**
     * 소셜 로그인 url 요청
     * @param socialLoginType
     * @return
     */
    String requestUrl(SocialLoginType socialLoginType);

    /**
     * 소설 로그인 후 사용자 셔션 정보 반환
     * @param socialLoginType
     * @param code
     * @return
     */
    UserSession requestUserSession(SocialLoginType socialLoginType, String code);
}
