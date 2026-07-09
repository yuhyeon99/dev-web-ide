package com.yuhyeon.devwebide.user.controller;

import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.dto.UserMeResponse;
import com.yuhyeon.devwebide.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("현재 사용자 조회 API 요청에 성공한다")
    void me() throws Exception {
        String authorizationHeader = "Bearer access-token";
        UserMeResponse response = new UserMeResponse(
                1L,
                "user@test.com",
                "user",
                UserRole.USER,
                UserStatus.ACTIVE
        );

        given(userService.getCurrentUser(authorizationHeader))
                .willReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.nickname").value("user"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        then(userService).should()
                .getCurrentUser(authorizationHeader);
    }

    @Test
    @DisplayName("Authorization header가 없으면 Service 예외 흐름으로 연결된다")
    void meWithoutAuthorizationHeader() {
        given(userService.getCurrentUser(null))
                .willThrow(new IllegalArgumentException("Authorization header가 필요합니다."));

        assertThatThrownBy(() -> mockMvc.perform(get("/api/users/me")))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        then(userService).should()
                .getCurrentUser(null);
    }
}
