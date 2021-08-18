package io.openindoormap.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.openindoormap.service.SocialOauth;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class GoogleOauthImplTest {

    @Autowired
    SocialOauth oauth;

    @Test
    void testGetOauthRedirectURL() {
        log.debug( oauth.getOauthRedirectURL());
    }

    @Test
    @Ignore
    void testRequestAccessToken() {
        oauth.getOauthRedirectURL();
        oauth.requestAccessToken(null);
    }

    @Test
    @Ignore
    void testGetUserInfo() {
        fail("Not yet implemented");
    }

}
