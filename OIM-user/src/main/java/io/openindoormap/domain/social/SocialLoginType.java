package io.openindoormap.domain.social;

import lombok.Getter;

public enum SocialLoginType {
    GOOGLE("googleOauthImpl"),
    FACEBOOK(""),
    KAKAO(""),
    NAVER("");

    @Getter
    private final String name;

    private SocialLoginType(String name) {
        this.name = name;
    }
}
