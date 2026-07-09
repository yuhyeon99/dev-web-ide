package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.OAuthLoginRequest;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResult;
import com.yuhyeon.devwebide.user.domain.*;
import com.yuhyeon.devwebide.user.repository.AuthSessionRepository;
import com.yuhyeon.devwebide.user.repository.OAuthAccountRepository;
import com.yuhyeon.devwebide.user.repository.TermsAgreementRepository;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OAuthLoginServiceTest {

    @Mock
    private OAuthAccountRepository oAuthAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TermsAgreementRepository termsAgreementRepository;

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private OAuthLoginService oAuthLoginService;

    @Test
    @DisplayName("기존 OAuthAccount로 로그인한다")
    void loginWithExistingOAuthAccount() {
        OAuthLoginRequest request = createRequest();
        User user = createUser(1L, UserStatus.ACTIVE);
        OAuthAccount oAuthAccount = createOAuthAccount(user, request);
        AuthSession savedAuthSession = createAuthSession(100L, user, "refresh-token-hash");

        given(oAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GITHUB,
                "github-user-id"
        )).willReturn(Optional.of(oAuthAccount));
        given(tokenService.createRefreshToken())
                .willReturn("refresh-token");
        given(tokenService.hashRefreshToken("refresh-token"))
                .willReturn("refresh-token-hash");
        given(tokenService.calculateRefreshTokenExpiresAt(any(LocalDateTime.class)))
                .willAnswer(invocation -> invocation.<LocalDateTime>getArgument(0).plusDays(14));
        given(authSessionRepository.save(any(AuthSession.class)))
                .willReturn(savedAuthSession);
        given(tokenService.createAccessToken(any(AuthSession.class), any(LocalDateTime.class)))
                .willReturn("access-token");
        given(tokenService.getAccessTokenExpiresInSeconds())
                .willReturn(1800L);
        given(tokenService.calculateAccessTokenExpiresAt(any(LocalDateTime.class)))
                .willAnswer(invocation -> invocation.<LocalDateTime>getArgument(0).plusMinutes(30));
        given(tokenService.getRefreshTokenExpiresInSeconds())
                .willReturn(1_209_600L);

        OAuthLoginResult result = oAuthLoginService.login(
                request,
                "127.0.0.1",
                "Chrome"
        );

        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.refreshTokenExpiresInSeconds()).isEqualTo(1_209_600L);
        assertThat(result.response().accessToken()).isEqualTo("access-token");
        assertThat(result.response().tokenType()).isEqualTo("Bearer");
        assertThat(result.response().expiresIn()).isEqualTo(1800L);
        assertThat(result.response().accessTokenExpiresAt()).isBetween(
                LocalDateTime.now().plusMinutes(30).minusSeconds(1),
                LocalDateTime.now().plusMinutes(30).plusSeconds(1)
        );
        assertThat(result.response().userId()).isEqualTo(1L);
        assertThat(result.response().email()).isEqualTo("user@test.com");
        assertThat(result.response().nickname()).isEqualTo("user");
        assertThat(result.response().role()).isEqualTo(UserRole.USER);
        assertThat(result.response().status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.response().newUser()).isFalse();

        ArgumentCaptor<AuthSession> authSessionCaptor =
                ArgumentCaptor.forClass(AuthSession.class);
        then(authSessionRepository).should()
                .save(authSessionCaptor.capture());

        AuthSession authSession = authSessionCaptor.getValue();
        assertThat(authSession.getUser()).isEqualTo(user);
        assertThat(authSession.isUserSession()).isTrue();
        assertThat(authSession.getGuestSession()).isNull();
        assertThat(authSession.getRefreshTokenHash()).isEqualTo("refresh-token-hash");
        assertThat(authSession.getClientIp()).isEqualTo("127.0.0.1");
        assertThat(authSession.getUserAgent()).isEqualTo("Chrome");

        then(userRepository).should(never())
                .save(any(User.class));
        then(termsAgreementRepository).should(never())
                .save(any(TermsAgreement.class));
    }

    @Test
    @DisplayName("신규 User, OAuthAccount, TermsAgreement, AuthSession을 생성한다")
    void loginWithNewUser() {
        OAuthLoginRequest request = createRequest();
        User savedUser = createUser(1L, UserStatus.ACTIVE);
        AuthSession savedAuthSession = createAuthSession(100L, savedUser, "refresh-token-hash");

        given(oAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GITHUB,
                "github-user-id"
        )).willReturn(Optional.empty());
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.empty());
        given(userRepository.save(any(User.class)))
                .willReturn(savedUser);
        given(tokenService.createRefreshToken())
                .willReturn("refresh-token");
        given(tokenService.hashRefreshToken("refresh-token"))
                .willReturn("refresh-token-hash");
        given(tokenService.calculateRefreshTokenExpiresAt(any(LocalDateTime.class)))
                .willAnswer(invocation -> invocation.<LocalDateTime>getArgument(0).plusDays(14));
        given(authSessionRepository.save(any(AuthSession.class)))
                .willReturn(savedAuthSession);
        given(tokenService.createAccessToken(any(AuthSession.class), any(LocalDateTime.class)))
                .willReturn("access-token");
        given(tokenService.getAccessTokenExpiresInSeconds())
                .willReturn(1800L);
        given(tokenService.calculateAccessTokenExpiresAt(any(LocalDateTime.class)))
                .willAnswer(invocation -> invocation.<LocalDateTime>getArgument(0).plusMinutes(30));
        given(tokenService.getRefreshTokenExpiresInSeconds())
                .willReturn(1_209_600L);

        OAuthLoginResult result = oAuthLoginService.login(
                request,
                "127.0.0.1",
                "Chrome"
        );

        assertThat(result.response().newUser()).isTrue();
        assertThat(result.response().userId()).isEqualTo(1L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should()
                .save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("user@test.com");
        assertThat(userCaptor.getValue().getNickname()).isEqualTo("user");
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.USER);
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<OAuthAccount> oAuthAccountCaptor =
                ArgumentCaptor.forClass(OAuthAccount.class);
        then(oAuthAccountRepository).should()
                .save(oAuthAccountCaptor.capture());
        assertThat(oAuthAccountCaptor.getValue().getUser()).isEqualTo(savedUser);
        assertThat(oAuthAccountCaptor.getValue().getProvider()).isEqualTo(OAuthProvider.GITHUB);
        assertThat(oAuthAccountCaptor.getValue().getProviderUserId()).isEqualTo("github-user-id");
        assertThat(oAuthAccountCaptor.getValue().getProviderEmail()).isEqualTo("user@test.com");

        ArgumentCaptor<TermsAgreement> termsAgreementCaptor =
                ArgumentCaptor.forClass(TermsAgreement.class);
        then(termsAgreementRepository).should()
                .save(termsAgreementCaptor.capture());
        assertThat(termsAgreementCaptor.getValue().getUser()).isEqualTo(savedUser);
        assertThat(termsAgreementCaptor.getValue().isTermsAgreed()).isTrue();
        assertThat(termsAgreementCaptor.getValue().isPrivacyAgreed()).isTrue();

        ArgumentCaptor<AuthSession> authSessionCaptor =
                ArgumentCaptor.forClass(AuthSession.class);
        then(authSessionRepository).should()
                .save(authSessionCaptor.capture());
        assertThat(authSessionCaptor.getValue().getRefreshTokenHash())
                .isEqualTo("refresh-token-hash");
    }

    @Test
    @DisplayName("신규 가입 시 이용약관 미동의면 예외가 발생한다")
    void loginWithTermsNotAgreed() {
        OAuthLoginRequest request = new OAuthLoginRequest(
                OAuthProvider.GITHUB,
                "github-user-id",
                "user@test.com",
                "user",
                false,
                true
        );

        given(oAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GITHUB,
                "github-user-id"
        )).willReturn(Optional.empty());
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이용약관 동의가 필요합니다.");
    }

    @Test
    @DisplayName("신규 가입 시 개인정보 처리방침 미동의면 예외가 발생한다")
    void loginWithPrivacyNotAgreed() {
        OAuthLoginRequest request = new OAuthLoginRequest(
                OAuthProvider.GITHUB,
                "github-user-id",
                "user@test.com",
                "user",
                true,
                false
        );

        given(oAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GITHUB,
                "github-user-id"
        )).willReturn(Optional.empty());
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("개인정보 처리방침 동의가 필요합니다.");
    }

    @Test
    @DisplayName("기존 User가 INACTIVE이면 예외가 발생한다")
    void loginWithInactiveUser() {
        assertInactiveUserStatus(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("기존 User가 DELETED이면 예외가 발생한다")
    void loginWithDeletedUser() {
        assertInactiveUserStatus(UserStatus.DELETED);
    }

    @Test
    @DisplayName("provider가 null이면 예외가 발생한다")
    void loginWithNullProvider() {
        OAuthLoginRequest request = new OAuthLoginRequest(
                null,
                "github-user-id",
                "user@test.com",
                "user",
                true,
                true
        );

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth provider는 필수입니다.");
    }

    @Test
    @DisplayName("providerUserId가 null이면 예외가 발생한다")
    void loginWithNullProviderUserId() {
        OAuthLoginRequest request = new OAuthLoginRequest(
                OAuthProvider.GITHUB,
                null,
                "user@test.com",
                "user",
                true,
                true
        );

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth provider 사용자 ID는 필수입니다.");
    }

    @Test
    @DisplayName("providerUserId가 blank이면 예외가 발생한다")
    void loginWithBlankProviderUserId() {
        OAuthLoginRequest request = new OAuthLoginRequest(
                OAuthProvider.GITHUB,
                " ",
                "user@test.com",
                "user",
                true,
                true
        );

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth provider 사용자 ID는 필수입니다.");
    }

    @Test
    @DisplayName("providerEmail이 null이면 예외가 발생한다")
    void loginWithNullProviderEmail() {
        OAuthLoginRequest request = new OAuthLoginRequest(
                OAuthProvider.GITHUB,
                "github-user-id",
                null,
                "user",
                true,
                true
        );

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth provider 이메일은 필수입니다.");
    }

    @Test
    @DisplayName("providerEmail이 blank이면 예외가 발생한다")
    void loginWithBlankProviderEmail() {
        OAuthLoginRequest request = new OAuthLoginRequest(
                OAuthProvider.GITHUB,
                "github-user-id",
                " ",
                "user",
                true,
                true
        );

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth provider 이메일은 필수입니다.");
    }

    @Test
    @DisplayName("nickname이 null이면 예외가 발생한다")
    void loginWithNullNickname() {
        OAuthLoginRequest request = new OAuthLoginRequest(
                OAuthProvider.GITHUB,
                "github-user-id",
                "user@test.com",
                null,
                true,
                true
        );

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 필수입니다.");
    }

    @Test
    @DisplayName("nickname이 blank이면 예외가 발생한다")
    void loginWithBlankNickname() {
        OAuthLoginRequest request = new OAuthLoginRequest(
                OAuthProvider.GITHUB,
                "github-user-id",
                "user@test.com",
                " ",
                true,
                true
        );

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 필수입니다.");
    }

    @Test
    @DisplayName("email 중복이지만 OAuthAccount가 없으면 예외가 발생한다")
    void loginWithDuplicatedEmailWithoutOAuthAccount() {
        OAuthLoginRequest request = createRequest();

        given(oAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GITHUB,
                "github-user-id"
        )).willReturn(Optional.empty());
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(createUser(1L, UserStatus.ACTIVE)));

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    private void assertInactiveUserStatus(UserStatus userStatus) {
        OAuthLoginRequest request = createRequest();
        User user = createUser(1L, userStatus);
        OAuthAccount oAuthAccount = createOAuthAccount(user, request);

        given(oAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GITHUB,
                "github-user-id"
        )).willReturn(Optional.of(oAuthAccount));

        assertThatThrownBy(() -> oAuthLoginService.login(request, "127.0.0.1", "Chrome"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활성 상태의 사용자가 아닙니다.");
    }

    private OAuthLoginRequest createRequest() {
        return new OAuthLoginRequest(
                OAuthProvider.GITHUB,
                "github-user-id",
                "user@test.com",
                "user",
                true,
                true
        );
    }

    private User createUser(Long id, UserStatus status) {
        User user = User.builder()
                .email("user@test.com")
                .nickname("user")
                .role(UserRole.USER)
                .status(status)
                .build();
        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }

    private OAuthAccount createOAuthAccount(User user, OAuthLoginRequest request) {
        return OAuthAccount.builder()
                .user(user)
                .provider(request.provider())
                .providerUserId(request.providerUserId())
                .providerEmail(request.providerEmail())
                .build();
    }

    private AuthSession createAuthSession(
            Long id,
            User user,
            String refreshTokenHash
    ) {
        AuthSession authSession = AuthSession.builder()
                .user(user)
                .refreshTokenHash(refreshTokenHash)
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build();
        ReflectionTestUtils.setField(authSession, "id", id);

        return authSession;
    }
}
