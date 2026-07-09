package com.yuhyeon.devwebide.user.controller;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.auth.security.AccessTokenAuthenticationService;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.dto.UserMeResponse;
import com.yuhyeon.devwebide.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AccessTokenAuthenticationService accessTokenAuthenticationService;

    @Test
    @DisplayName("Security Principal 기반으로 현재 사용자 조회 API 요청에 성공한다")
    void me() throws Exception {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                100L,
                "USER",
                1L,
                null,
                "USER"
        );
        UserMeResponse response = new UserMeResponse(
                1L,
                "user@test.com",
                "user",
                UserRole.USER,
                UserStatus.ACTIVE
        );

        given(accessTokenAuthenticationService.authenticate("access-token"))
                .willReturn(principal);
        given(userService.getCurrentUser(principal))
                .willReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.nickname").value("user"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        ArgumentCaptor<AuthenticatedPrincipal> principalCaptor =
                ArgumentCaptor.forClass(AuthenticatedPrincipal.class);
        then(userService).should()
                .getCurrentUser(principalCaptor.capture());
        assertThat(principalCaptor.getValue()).isEqualTo(principal);
    }

    @Test
    @DisplayName("인증이 없으면 현재 사용자 조회 API는 401을 반환한다")
    void meWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
