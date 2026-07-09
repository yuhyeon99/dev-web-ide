package com.yuhyeon.devwebide.auth.security;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.auth.service.TokenService;
import com.yuhyeon.devwebide.user.domain.AuthSession;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessTokenAuthenticationService {

    private final TokenService tokenService;
    private final AuthSessionRepository authSessionRepository;

    public AuthenticatedPrincipal authenticate(String accessToken) {
        AuthenticatedPrincipal principal = tokenService.validateAccessToken(accessToken);
        LocalDateTime now = LocalDateTime.now();

        AuthSession authSession = authSessionRepository.findById(principal.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("인증 세션을 찾을 수 없습니다."));

        validateActiveSession(authSession, now);

        if (principal.isUserSession()) {
            validateUserSession(principal, authSession);
            return principal;
        }

        if (principal.isGuestSession()) {
            validateGuestSession(principal, authSession, now);
            return principal;
        }

        throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
    }

    private void validateActiveSession(AuthSession authSession, LocalDateTime now) {
        if (authSession.isRevoked()) {
            throw new IllegalArgumentException("폐기된 인증 세션입니다.");
        }

        if (authSession.isExpired(now)) {
            throw new IllegalArgumentException("만료된 인증 세션입니다.");
        }
    }

    private void validateUserSession(
            AuthenticatedPrincipal principal,
            AuthSession authSession
    ) {
        if (!authSession.isUserSession()) {
            throw new IllegalArgumentException("회원 인증 세션이 아닙니다.");
        }

        if (!authSession.getUser().getId().equals(principal.userId())) {
            throw new IllegalArgumentException("Access Token의 사용자 정보가 일치하지 않습니다.");
        }

        if (authSession.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 사용자가 아닙니다.");
        }
    }

    private void validateGuestSession(
            AuthenticatedPrincipal principal,
            AuthSession authSession,
            LocalDateTime now
    ) {
        if (!authSession.isGuestSession()) {
            throw new IllegalArgumentException("게스트 인증 세션이 아닙니다.");
        }

        if (!authSession.getGuestSession().getId().equals(principal.guestSessionId())) {
            throw new IllegalArgumentException("Access Token의 게스트 세션 정보가 일치하지 않습니다.");
        }

        if (authSession.getGuestSession().isExpired(now)) {
            throw new IllegalArgumentException("만료된 게스트 세션입니다.");
        }
    }
}
