package com.yuhyeon.devwebide.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yuhyeon.devwebide.auth.dto.GoogleOAuthSignupRequest;
import com.yuhyeon.devwebide.auth.dto.GoogleOAuthUserInfo;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginRequest;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResult;
import com.yuhyeon.devwebide.user.domain.OAuthProvider;
import com.yuhyeon.devwebide.user.repository.OAuthAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private static final String SCOPE = "openid email profile";

    private final OAuthLoginService oAuthLoginService;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final RestClient restClient = RestClient.create();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.oauth.google.client-id}")
    private String clientId;

    @Value("${app.oauth.google.client-secret}")
    private String clientSecret;

    @Value("${app.oauth.google.redirect-uri}")
    private String redirectUri;

    @Value("${app.oauth.google.authorization-uri}")
    private String authorizationUri;

    @Value("${app.oauth.google.token-uri}")
    private String tokenUri;

    @Value("${app.oauth.google.user-info-uri}")
    private String userInfoUri;

    @Value("${app.oauth.google.frontend-redirect-uri}")
    private String frontendRedirectUri;

    public String createState() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public String buildAuthorizationUri(String state) {
        validateGoogleOAuthConfiguration();

        return UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "select_account")
                .build()
                .toUriString();
    }

    public GoogleOAuthUserInfo fetchUserInfoWithAuthorizationCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Google OAuth code가 필요합니다.");
        }

        GoogleTokenResponse tokenResponse = exchangeAuthorizationCode(code);

        return fetchUserInfo(tokenResponse.accessToken());
    }

    public boolean isRegisteredGoogleUser(GoogleOAuthUserInfo userInfo) {
        return oAuthAccountRepository
                .findByProviderAndProviderUserId(OAuthProvider.GOOGLE, userInfo.subject())
                .isPresent();
    }

    public OAuthLoginResult loginExistingGoogleUser(
            GoogleOAuthUserInfo userInfo,
            String clientIp,
            String userAgent
    ) {
        return loginWithUserInfo(
                userInfo,
                new GoogleOAuthSignupRequest(
                        resolveNickname(null, userInfo),
                        true,
                        true
                ),
                clientIp,
                userAgent
        );
    }

    public OAuthLoginResult loginWithUserInfo(
            GoogleOAuthUserInfo userInfo,
            GoogleOAuthSignupRequest signupRequest,
            String clientIp,
            String userAgent
    ) {
        OAuthLoginRequest loginRequest = new OAuthLoginRequest(
                OAuthProvider.GOOGLE,
                userInfo.subject(),
                userInfo.email(),
                resolveNickname(signupRequest, userInfo),
                signupRequest.termsAgreed(),
                signupRequest.privacyAgreed()
        );

        return oAuthLoginService.login(loginRequest, clientIp, userAgent);
    }

    public OAuthLoginResult loginWithAuthorizationCode(
            String code,
            GoogleOAuthSignupRequest signupRequest,
            String clientIp,
            String userAgent
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Google OAuth code가 필요합니다.");
        }

        GoogleTokenResponse tokenResponse = exchangeAuthorizationCode(code);
        GoogleOAuthUserInfo userInfo = fetchUserInfo(tokenResponse.accessToken());

        return loginWithUserInfo(userInfo, signupRequest, clientIp, userAgent);
    }

    public String buildFrontendSuccessRedirectUri(OAuthLoginResult result) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("oauth", "success")
                .queryParam("accessToken", result.response().accessToken())
                .queryParam("tokenType", result.response().tokenType())
                .queryParam("expiresIn", result.response().expiresIn())
                .queryParam("accessTokenExpiresAt", result.response().accessTokenExpiresAt())
                .queryParam("userId", result.response().userId())
                .queryParam("email", result.response().email())
                .queryParam("nickname", result.response().nickname())
                .queryParam("role", result.response().role())
                .queryParam("status", result.response().status())
                .queryParam("newUser", result.response().newUser())
                .build()
                .toUriString();
    }

    public String buildFrontendFailureRedirectUri(String reason) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("oauth", "error")
                .queryParam("reason", reason)
                .build()
                .toUriString();
    }

    public String buildFrontendSignupRedirectUri(GoogleOAuthUserInfo userInfo) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("oauth", "signup_required")
                .queryParam("provider", "google")
                .queryParam("email", userInfo.email())
                .queryParam("name", resolveNickname(null, userInfo))
                .build()
                .toUriString();
    }

    public String encodeCookieValue(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public String decodeCookieValue(String value) {
        return new String(
                Base64.getUrlDecoder().decode(value),
                StandardCharsets.UTF_8
        );
    }

    private GoogleTokenResponse exchangeAuthorizationCode(String code) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("code", code);
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("redirect_uri", redirectUri);
        requestBody.add("grant_type", "authorization_code");

        return restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    private GoogleOAuthUserInfo fetchUserInfo(String accessToken) {
        GoogleUserInfoResponse response = restClient.get()
                .uri(userInfoUri)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(GoogleUserInfoResponse.class);

        if (response == null || response.sub() == null || response.email() == null) {
            throw new IllegalArgumentException("Google 사용자 정보를 확인할 수 없습니다.");
        }

        if (!response.isEmailVerified()) {
            throw new IllegalArgumentException("Google 이메일 인증이 필요합니다.");
        }

        return new GoogleOAuthUserInfo(
                response.sub(),
                response.email(),
                response.name()
        );
    }

    private String resolveNickname(
            GoogleOAuthSignupRequest signupRequest,
            GoogleOAuthUserInfo userInfo
    ) {
        if (signupRequest != null
                && signupRequest.nickname() != null
                && !signupRequest.nickname().isBlank()) {
            return signupRequest.nickname();
        }

        if (userInfo.name() != null && !userInfo.name().isBlank()) {
            return userInfo.name();
        }

        return userInfo.email();
    }

    private void validateGoogleOAuthConfiguration() {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Google OAuth client id가 설정되지 않았습니다.");
        }

        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("Google OAuth client secret이 설정되지 않았습니다.");
        }

        if (redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalStateException("Google OAuth redirect uri가 설정되지 않았습니다.");
        }
    }

    private record GoogleTokenResponse(
            @JsonProperty("access_token")
            String accessToken,
            @JsonProperty("token_type")
            String tokenType,
            @JsonProperty("expires_in")
            Long expiresIn,
            String scope,
            @JsonProperty("id_token")
            String idToken
    ) {
    }

    private record GoogleUserInfoResponse(
            String sub,
            String email,
            @JsonProperty("email_verified")
            Boolean emailVerified,
            String name,
            String picture
    ) {

        private boolean isEmailVerified() {
            return Boolean.TRUE.equals(emailVerified);
        }
    }
}
