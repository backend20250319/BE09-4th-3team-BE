package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.dto.oauth.OAuthAttributes;
import io.fundy.fundyserver.register.dto.oauth.SessionUser;
import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.repository.OAuthUserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthUserService {

    private final OAuthUserRepository userRepository;
    private final HttpSession session;

    public OAuthUser saveOrUpdate(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, userNameAttributeName, attributes);

        Optional<OAuthUser> userOptional = userRepository.findByEmail(oAuthAttributes.getEmail());

        OAuthUser user;
        if (userOptional.isPresent()) {
            user = userOptional.get().update(oAuthAttributes.getName(), oAuthAttributes.getPicture());
            // 기존 사용자의 경우 닉네임이 없으면 OAuth 이름으로 설정
            if (user.getNickname() == null || user.getNickname().isEmpty()) {
                String oauthName = oAuthAttributes.getName();
                if (oauthName != null && !oauthName.isEmpty()) {
                    user.setNickname(oauthName);
                    log.info("[OAuth2 기존 사용자 닉네임 설정] email={}, nickname={}", user.getEmail(), user.getNickname());
                }
            }
        } else {
            user = oAuthAttributes.toEntity();
            user.setRoleType(RoleType.USER); // 기본 권한 설정
            // 신규 사용자의 경우 OAuth 이름을 닉네임으로 설정
            String oauthName = oAuthAttributes.getName();
            if (oauthName != null && !oauthName.isEmpty()) {
                user.setNickname(oauthName);
                log.info("[OAuth2 신규 사용자 닉네임 설정] email={}, nickname={}", user.getEmail(), user.getNickname());
            }
        }

        OAuthUser savedUser = userRepository.save(user);
        session.setAttribute("user", new SessionUser(savedUser));

        log.info("[OAuth2 사용자 저장/업데이트 완료] email={}, nickname={}, provider={}",
                savedUser.getEmail(), savedUser.getNickname(), registrationId);

        return savedUser;
    }
}