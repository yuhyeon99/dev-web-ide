package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.AuthTokenRefreshResponse;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginRequest;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResponse;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResult;
import com.yuhyeon.devwebide.user.domain.*;
import com.yuhyeon.devwebide.user.repository.AuthSessionRepository;
import com.yuhyeon.devwebide.user.repository.OAuthAccountRepository;
import com.yuhyeon.devwebide.user.repository.TermsAgreementRepository;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthLoginService {

    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final AuthSessionRepository authSessionRepository;
    private final TokenService tokenService;

    @Transactional
    public OAuthLoginResult login(
            OAuthLoginRequest request,
            String clientIp,
            String userAgent
    ) {
        validateRequest(request);

        OAuthAccount oAuthAccount = oAuthAccountRepository
                .findByProviderAndProviderUserId(
                        request.provider(),
                        request.providerUserId()
                )
                .orElse(null);

        boolean newUser = oAuthAccount == null;
        User user = newUser
                ? createNewUser(request)
                : oAuthAccount.getUser();

        validateActiveUser(user);

        if (newUser) {
            createOAuthAccount(user, request);
            createTermsAgreement(user, request);
        }

        return createLoginResult(user, newUser, clientIp, userAgent);
    }

    private void validateRequest(OAuthLoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("OAuth 로그인 요청이 필요합니다.");
        }

        if (request.provider() == null) {
            throw new IllegalArgumentException("OAuth provider는 필수입니다.");
        }

        if (request.providerUserId() == null || request.providerUserId().isBlank()) {
            throw new IllegalArgumentException("OAuth provider 사용자 ID는 필수입니다.");
        }

        if (request.providerEmail() == null || request.providerEmail().isBlank()) {
            throw new IllegalArgumentException("OAuth provider 이메일은 필수입니다.");
        }

        if (request.nickname() == null || request.nickname().isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
    }

    private User createNewUser(OAuthLoginRequest request) {
        userRepository.findByEmail(request.providerEmail())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
                });

        if (!request.termsAgreed()) {
            throw new IllegalArgumentException("이용약관 동의가 필요합니다.");
        }

        if (!request.privacyAgreed()) {
            throw new IllegalArgumentException("개인정보 처리방침 동의가 필요합니다.");
        }

        User user = User.builder()
                .email(request.providerEmail())
                .nickname(request.nickname())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    private void validateActiveUser(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 사용자가 아닙니다.");
        }
    }

    private void createOAuthAccount(User user, OAuthLoginRequest request) {
        OAuthAccount oAuthAccount = OAuthAccount.builder()
                .user(user)
                .provider(request.provider())
                .providerUserId(request.providerUserId())
                .providerEmail(request.providerEmail())
                .build();

        oAuthAccountRepository.save(oAuthAccount);
    }

    private void createTermsAgreement(User user, OAuthLoginRequest request) {
        TermsAgreement termsAgreement = TermsAgreement.builder()
                .user(user)
                .termsAgreed(request.termsAgreed())
                .privacyAgreed(request.privacyAgreed())
                .build();

        termsAgreementRepository.save(termsAgreement);
    }

    private OAuthLoginResult createLoginResult(
            User user,
            boolean newUser,
            String clientIp,
            String userAgent
    ) {
        LocalDateTime now = LocalDateTime.now();
        String refreshToken = tokenService.createRefreshToken();
        String refreshTokenHash = tokenService.hashRefreshToken(refreshToken);

        AuthSession authSession = AuthSession.builder()
                .user(user)
                .refreshTokenHash(refreshTokenHash)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .expiresAt(tokenService.calculateRefreshTokenExpiresAt(now))
                .build();
        AuthSession savedAuthSession = authSessionRepository.save(authSession);

        String accessToken = tokenService.createAccessToken(savedAuthSession, now);
        AuthTokenRefreshResponse tokenResponse = AuthTokenRefreshResponse.of(
                accessToken,
                tokenService.getAccessTokenExpiresInSeconds(),
                tokenService.calculateAccessTokenExpiresAt(now)
        );

        OAuthLoginResponse response = OAuthLoginResponse.of(
                tokenResponse,
                user,
                newUser
        );

        return new OAuthLoginResult(
                response,
                refreshToken,
                tokenService.getRefreshTokenExpiresInSeconds()
        );
    }
}
