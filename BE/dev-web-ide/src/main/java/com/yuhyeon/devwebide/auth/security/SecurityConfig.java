package com.yuhyeon.devwebide.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AccessTokenAuthenticationService accessTokenAuthenticationService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(accessTokenAuthenticationService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(401)
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(401)
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/guest-sessions").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/oauth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/runtimes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/projects").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/projects/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/projects/*").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/projects/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/projects/*/open").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/projects/*/members").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/projects/*/members/*/accept").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/projects/*/members/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/*/members/*").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}
