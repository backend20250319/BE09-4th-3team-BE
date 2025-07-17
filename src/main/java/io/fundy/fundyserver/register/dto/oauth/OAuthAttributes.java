package io.fundy.fundyserver.register.dto.oauth;

import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.entity.RoleType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;

@Getter
@Slf4j
public class OAuthAttributes {

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String name;
    private final String email;
    private final String picture;
    private final String registrationId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String name, String email, String picture, String registrationId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.registrationId = registrationId;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        log.debug("[OAuthAttributes.of] registrationId = {}, userNameAttrKey = {}", registrationId, userNameAttributeName);
        log.debug("[OAuthAttributes.of] raw attributes = {}", attributes);

        if ("kakao".equalsIgnoreCase(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        log.debug("[OAuthAttributes.ofGoogle] attributes = {}", attributes);

        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .registrationId("google")
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        log.debug("[OAuthAttributes.ofKakao] 호출됨");
        log.debug("[OAuthAttributes.ofKakao] attributes = {}", attributes);

        if (!attributes.containsKey("kakao_account")) {
            log.error("[OAuthAttributes.ofKakao] kakao_account 키가 없음. attributes = {}", attributes);
            throw new IllegalArgumentException("kakao_account가 null 또는 누락됨");
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        if (!kakaoAccount.containsKey("profile")) {
            log.error("[OAuthAttributes.ofKakao] profile 키가 없음. kakaoAccount = {}", kakaoAccount);
            throw new IllegalArgumentException("profile이 null 또는 누락됨");
        }

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        log.debug("[OAuthAttributes.ofKakao] kakaoAccount = {}", kakaoAccount);
        log.debug("[OAuthAttributes.ofKakao] profile = {}", profile);

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .picture((String) profile.get("profile_image_url"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .registrationId("kakao")
                .build();
    }

    public OAuthUser toEntity() {
        return OAuthUser.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .roleType(RoleType.USER)
                .registrationId(registrationId)
                .build();
    }

    public OAuth2User toOAuth2User() {
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(RoleType.USER.getKey())),
                attributes,
                nameAttributeKey
        );
    }
}
