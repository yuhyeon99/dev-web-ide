package com.yuhyeon.devwebide.user.controller;

import com.yuhyeon.devwebide.user.dto.UserMeResponse;
import com.yuhyeon.devwebide.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
            String authorizationHeader
    ) {
        UserMeResponse response = userService.getCurrentUser(authorizationHeader);

        return ResponseEntity.ok(response);
    }
}
