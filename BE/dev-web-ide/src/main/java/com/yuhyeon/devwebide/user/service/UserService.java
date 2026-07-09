package com.yuhyeon.devwebide.user.service;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.auth.service.TokenService;
import com.yuhyeon.devwebide.user.domain.AuthSession;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.dto.UserMeResponse;
import com.yuhyeon.devwebide.user.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final AuthSessionRepository authSessionRepository;

    public UserMeResponse getCurrentUser(String authorizationHeader) {
        String accessToken = extractAccessToken(authorizationHeader);
        AuthenticatedPrincipal principal = tokenService.validateAccessToken(accessToken);

        if (!principal.isUserSession()) {
            throw new IllegalArgumentException("회원 Access Token이 필요합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        AuthSession authSession = authSessionRepository.findById(principal.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("인증 세션을 찾을 수 없습니다."));

        validateAuthSession(authSession, principal, now);

        return UserMeResponse.from(authSession.getUser());
    }

    private String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("Authorization header가 필요합니다.");
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Bearer Access Token이 필요합니다.");
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());
        if (accessToken.isBlank()) {
            throw new IllegalArgumentException("Access Token이 필요합니다.");
        }

        return accessToken;
    }

    private void validateAuthSession(
            AuthSession authSession,
            AuthenticatedPrincipal principal,
            LocalDateTime now
    ) {
        if (authSession.isRevoked()) {
            throw new IllegalArgumentException("폐기된 인증 세션입니다.");
        }

        if (authSession.isExpired(now)) {
            throw new IllegalArgumentException("만료된 인증 세션입니다.");
        }

        if (!authSession.isUserSession()) {
            throw new IllegalArgumentException("회원 인증 세션이 아닙니다.");
        }

        User user = authSession.getUser();
        if (!user.getId().equals(principal.userId())) {
            throw new IllegalArgumentException("Access Token의 사용자 정보가 일치하지 않습니다.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 사용자가 아닙니다.");
        }
    }
}
