package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.dto.oauth.OAuthAttributes;
import io.fundy.fundyserver.register.dto.oauth.SessionUser;
import io.fundy.fundyserver.register.entity.RoleType;
import io.fundy.fundyserver.register.entity.oauth.OAuthUser;
import io.fundy.fundyserver.register.repository.OAuthUserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

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
        } else {
            user = oAuthAttributes.toEntity();
            user.setRoleType(RoleType.USER); // ✅ 기본 권한 설정
        }

        OAuthUser savedUser = userRepository.save(user);
        session.setAttribute("user", new SessionUser(savedUser));

        return savedUser;
    }
}