package io.fundy.fundyserver.register.service;

import io.fundy.fundyserver.register.entity.RefreshToken;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.exception.ApiException;
import io.fundy.fundyserver.register.exception.ErrorCode;
import io.fundy.fundyserver.register.repository.RefreshTokenRepository;
import io.fundy.fundyserver.register.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtProvider;

    @Transactional
    public RefreshToken createOrUpdateRefreshToken(User user) {
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByUser(user);
        RefreshToken tokenEntity;

        if (optionalToken.isPresent()) {
            tokenEntity = optionalToken.get();
            tokenEntity.setToken(jwtProvider.createRefreshToken(user.getUserId()));
            tokenEntity.setExpiryDate(Instant.now().plusMillis(jwtProvider.getRefreshTokenExpiryMs()));
        } else {
            tokenEntity = new RefreshToken();
            tokenEntity.setUser(user);
            tokenEntity.setToken(jwtProvider.createRefreshToken(user.getUserId()));
            tokenEntity.setExpiryDate(Instant.now().plusMillis(jwtProvider.getRefreshTokenExpiryMs()));
        }

        return refreshTokenRepository.save(tokenEntity);
    }

    @Transactional(readOnly = true)
    public RefreshToken getValidRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        }

        return token;
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
