package com.yuhyeon.devwebide.auth.controller;

import com.yuhyeon.devwebide.auth.dto.GuestSessionCreateResponse;
import com.yuhyeon.devwebide.auth.service.GuestSessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuestSessionController.class)
class GuestSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestSessionService guestSessionService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("게스트 세션을 생성한다")
    void createGuestSession() throws Exception {
        String clientIp = "127.0.0.1";
        LocalDateTime now = LocalDateTime.of(2026, 7, 8, 10, 0);
        GuestSessionCreateResponse response = new GuestSessionCreateResponse(
                1L,
                "guest-token",
                "access-token",
                "Bearer",
                3600L,
                now.plusHours(1),
                now.plusDays(7),
                now
        );

        given(guestSessionService.createGuestSession(clientIp))
                .willReturn(response);

        mockMvc.perform(post("/api/guest-sessions")
                        .with(request -> {
                            request.setRemoteAddr(clientIp);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestSessionId").value(1L))
                .andExpect(jsonPath("$.guestToken").value("guest-token"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600L))
                .andExpect(jsonPath("$.accessTokenExpiresAt").exists())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        then(guestSessionService).should()
                .createGuestSession(clientIp);
    }

    @Test
    @DisplayName("X-Forwarded-For가 있으면 첫 번째 IP를 사용한다")
    void createGuestSession_forwardedFor() throws Exception {
        String clientIp = "1.1.1.1";
        LocalDateTime now = LocalDateTime.of(2026, 7, 8, 10, 0);
        GuestSessionCreateResponse response = new GuestSessionCreateResponse(
                1L,
                "guest-token",
                "access-token",
                "Bearer",
                3600L,
                now.plusHours(1),
                now.plusDays(7),
                now
        );

        given(guestSessionService.createGuestSession(clientIp))
                .willReturn(response);

        mockMvc.perform(post("/api/guest-sessions")
                        .header("X-Forwarded-For", "1.1.1.1, 2.2.2.2")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestSessionId").value(1L))
                .andExpect(jsonPath("$.guestToken").value("guest-token"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600L))
                .andExpect(jsonPath("$.accessTokenExpiresAt").exists())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        then(guestSessionService).should()
                .createGuestSession(clientIp);
    }
}
