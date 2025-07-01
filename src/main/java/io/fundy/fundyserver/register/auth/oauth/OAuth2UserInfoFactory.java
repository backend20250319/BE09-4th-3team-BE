package io.fundy.fundyserver.register.auth.oauth;

import io.fundy.fundyserver.register.auth.oauth.info.GoogleOAuth2UserInfo;
import io.fundy.fundyserver.register.auth.oauth.info.KakaoOAuth2UserInfo;
import io.fundy.fundyserver.register.auth.oauth.info.OAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if ("kakao".equalsIgnoreCase(provider)) {
            return new KakaoOAuth2UserInfo(attributes);
        } else {
            throw new IllegalArgumentException("지원하지 않는 OAuth2 provider: " + provider);
        }
    }
}
