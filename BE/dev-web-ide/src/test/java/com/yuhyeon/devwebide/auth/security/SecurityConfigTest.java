package com.yuhyeon.devwebide.auth.security;

import com.yuhyeon.devwebide.auth.dto.AuthTokenRefreshResponse;
import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.auth.service.AuthService;
import com.yuhyeon.devwebide.auth.service.GuestSessionService;
import com.yuhyeon.devwebide.auth.service.OAuthLoginService;
import com.yuhyeon.devwebide.runtime.service.RuntimeService;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.dto.UserMeResponse;
import com.yuhyeon.devwebide.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccessTokenAuthenticationService accessTokenAuthenticationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private OAuthLoginService oAuthLoginService;

    @MockitoBean
    private GuestSessionService guestSessionService;

    @MockitoBean
    private RuntimeService runtimeService;

    @Test
    @DisplayName("permitAll 경로는 인증 없이 접근할 수 있다")
    void permitAllPath() throws Exception {
        given(authService.refreshAccessToken(null))
                .willReturn(AuthTokenRefreshResponse.of(
                        "access-token",
                        1800L,
                        LocalDateTime.of(2026, 7, 9, 10, 30)
                ));

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/runtimes는 인증 없이 접근할 수 있다")
    void runtimesPermitAll() throws Exception {
        given(runtimeService.getActiveRuntimes())
                .willReturn(List.of());

        mockMvc.perform(get("/api/runtimes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users/me는 인증이 없으면 401을 반환한다")
    void usersMeWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 Bearer token이면 GET /api/users/me에 접근할 수 있다")
    void usersMeWithValidBearerToken() throws Exception {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                100L,
                "USER",
                1L,
                null,
                "USER"
        );

        given(accessTokenAuthenticationService.authenticate("access-token"))
                .willReturn(principal);
        given(userService.getCurrentUser(principal))
                .willReturn(new UserMeResponse(
                        1L,
                        "user@test.com",
                        "user",
                        UserRole.USER,
                        UserStatus.ACTIVE
                ));

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk());
    }
}
