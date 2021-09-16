package io.openindoormap.service.impl;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.openindoormap.domain.cache.CacheManager;
import io.openindoormap.domain.policy.Policy;
import io.openindoormap.domain.social.SocialLoginType;
import io.openindoormap.domain.user.UserInfo;
import io.openindoormap.domain.user.UserSession;
import io.openindoormap.persistence.UserMapper;
import io.openindoormap.service.SocialOauth;
import io.openindoormap.service.UserService;
import lombok.RequiredArgsConstructor;

/**
 * 사용자
 * @author jeongdae
 *
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ApplicationContext context;
    private final UserMapper userMapper;

    /**
     * 사용자 정보 취득
     * @param userId
     * @return
     */
    @Transactional(readOnly=true)
    public UserInfo getUser(String userId) {
        return userMapper.getUser(userId);
    }

    /**
     * 사용자 비밀번호 수정
     * @param userInfo
     * @return
     */
    @Transactional
    public int updatePassword(UserInfo userInfo) {
        return userMapper.updatePassword(userInfo);
    }

    /**
     * 회원 세션 정보를 취득
     * @param userInfo
     * @return
     */
    @Transactional(readOnly=true)
    public UserSession getUserSession(UserInfo userInfo) {
        return userMapper.getUserSession(userInfo);
    }

    /**
     * Sign in 성공 후 회원 정보를 갱신
     * @param userSession
     * @return
     */
    @Transactional
    public int updateSigninUserSession(UserSession userSession) {
        return userMapper.updateSigninUserSession(userSession);
    }

    public String requestUrl(SocialLoginType socialLoginType) {
        SocialOauth socialOauth = (SocialOauth) context.getBean(socialLoginType.getName());
        return socialOauth.getOauthRedirectURL();
    }

    public UserSession requestUserSession(SocialLoginType socialLoginType, String code) {
        Policy policy = CacheManager.getPolicy();

        SocialOauth socialOauth = (SocialOauth) context.getBean(socialLoginType.getName());
        UserInfo userInfo = socialOauth.getUserInfo(code);
        String userName = userInfo.getUserName();

        userInfo.setPasswordChangeTerm(policy.getPasswordChangeTerm());
        userInfo.setUserLastSigninLock(policy.getUserLastSigninLock());
        UserSession userSession = userMapper.getUserSession(userInfo);

        if (userSession == null) {
            userInfo.setUserGroupId(2);
            userInfo.setPassword(code);
            userInfo.setUserName("");

            userMapper.insertUser(userInfo);
            userSession = userMapper.getUserSession(userInfo);
        } else {
            userMapper.updateSigninUserSession(UserSession.builder().userId(userInfo.getUserId()).status("0").build());
        }
        userSession.setUserName(userName);
        return userSession;
    }

    @Transactional
    @Override
    public void withdrawUser(String userId) {

        userMapper.deleteUser(userId);
    }

}
