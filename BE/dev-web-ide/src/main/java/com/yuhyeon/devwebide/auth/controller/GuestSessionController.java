package com.yuhyeon.devwebide.auth.controller;

import com.yuhyeon.devwebide.auth.dto.GuestSessionCreateResponse;
import com.yuhyeon.devwebide.auth.service.GuestSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guest-sessions")
public class GuestSessionController {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final GuestSessionService guestSessionService;

    @PostMapping
    public ResponseEntity<GuestSessionCreateResponse> createGuestSession(
            HttpServletRequest request
    ) {
        String clientIp = resolveClientIp(request);
        GuestSessionCreateResponse response =
                guestSessionService.createGuestSession(clientIp);

        return ResponseEntity.ok(response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
