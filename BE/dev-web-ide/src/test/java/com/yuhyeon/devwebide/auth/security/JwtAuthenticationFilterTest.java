package com.yuhyeon.devwebide.auth.security;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AccessTokenAuthenticationService accessTokenAuthenticationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Authorization header가 없으면 filter chain을 진행한다")
    void doFilterWithoutAuthorizationHeader() throws Exception {
        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(accessTokenAuthenticationService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(filterChain.getRequest()).isEqualTo(request);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Bearer token 정상 인증 시 SecurityContext를 설정한다")
    void doFilterWithValidBearerToken() throws Exception {
        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(accessTokenAuthenticationService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                100L,
                "USER",
                1L,
                null,
                "USER"
        );

        given(accessTokenAuthenticationService.authenticate("access-token"))
                .willReturn(principal);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(filterChain.getRequest()).isEqualTo(request);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(principal);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("invalid token이면 401을 반환한다")
    void doFilterWithInvalidToken() throws Exception {
        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(accessTokenAuthenticationService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        given(accessTokenAuthenticationService.authenticate("invalid-token"))
                .willThrow(new IllegalArgumentException("유효하지 않은 Access Token입니다."));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Unauthorized");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Bearer prefix가 아니면 filter chain을 진행한다")
    void doFilterWithoutBearerPrefix() throws Exception {
        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(accessTokenAuthenticationService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(filterChain.getRequest()).isEqualTo(request);
        then(accessTokenAuthenticationService).shouldHaveNoInteractions();
    }
}
