package io.openindoormap.persistence;

import org.springframework.stereotype.Repository;

import io.openindoormap.domain.user.UserInfo;
import io.openindoormap.domain.user.UserSession;

/**
 * 사용자
 * @author jeongdae
 *
 */
@Repository
public interface UserMapper {

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
     * 사용자 등록
     * @param userInfo
     * @return
     */
    int insertUser(UserInfo userInfo);

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
     * 사용자 정보 취득
     * @param userId
     * @return
     */
    int deleteUser(String userId);
}
