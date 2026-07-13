package com.yuhyeon.devwebide.user.controller;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.user.dto.UserMeResponse;
import com.yuhyeon.devwebide.user.dto.UserProfileUpdateRequest;
import com.yuhyeon.devwebide.user.dto.UserSearchResponse;
import com.yuhyeon.devwebide.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        UserMeResponse response = userService.getCurrentUser(principal);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> search(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(name = "query", defaultValue = "") String query
    ) {
        List<UserSearchResponse> response =
                userService.searchInvitableUsers(principal, query);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<UserMeResponse> updateProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserMeResponse response = userService.updateCurrentUserProfile(principal, request);

        return ResponseEntity.ok(response);
    }
}
