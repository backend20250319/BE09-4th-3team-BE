package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.dto.oauth.OAuthAttributes;
import io.fundy.fundyserver.register.dto.oauth.SessionUser;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.repository.OAuthUserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Service("customOAuth2UserService")
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuthUserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("===== [CustomOAuth2UserService] loadUser() 진입 =====");

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        log.debug("[CustomOAuth2UserService] registrationId = {}", registrationId);
        log.debug("[CustomOAuth2UserService] userNameAttributeName = {}", userNameAttributeName);
        log.debug("[CustomOAuth2UserService] attributes = {}", oAuth2User.getAttributes());

        OAuthAttributes attributes;
        try {
            attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
            log.debug("[CustomOAuth2UserService] OAuthAttributes 생성 완료");
        } catch (Exception e) {
            log.error("[CustomOAuth2UserService] OAuthAttributes.of() 예외 발생", e);
            throw e;
        }

        OAuthUser user;
        try {
            user = saveOrUpdate(attributes);
            log.debug("[CustomOAuth2UserService] 저장/업데이트된 사용자 = {}", user);
        } catch (Exception e) {
            log.error("[CustomOAuth2UserService] saveOrUpdate() 예외 발생", e);
            throw e;
        }

        try {
            httpSession.setAttribute("user", new SessionUser(user));
            log.debug("[CustomOAuth2UserService] 세션 저장 완료 - user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("[CustomOAuth2UserService] 세션 저장 실패", e);
            throw e;
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getUserRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private OAuthUser saveOrUpdate(OAuthAttributes attributes) {
        return userRepository.findByEmail(attributes.getEmail())
                .map(entity -> {
                    entity.update(attributes.getName(), attributes.getPicture());
                    entity.setRegistrationId(attributes.getRegistrationId());
                    return userRepository.save(entity);
                })
                .orElseGet(() -> userRepository.save(attributes.toEntity()));
    }
}
