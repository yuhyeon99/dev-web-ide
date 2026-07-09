package com.yuhyeon.devwebide.project.controller;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.project.dto.ProjectMemberInviteRequest;
import com.yuhyeon.devwebide.project.dto.ProjectMemberManageResponse;
import com.yuhyeon.devwebide.project.dto.ProjectMemberRoleUpdateRequest;
import com.yuhyeon.devwebide.project.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    public ResponseEntity<ProjectMemberManageResponse> inviteMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberInviteRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        ProjectMemberManageResponse response =
                projectMemberService.inviteMember(projectId, request, principal);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<ProjectMemberManageResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody ProjectMemberRoleUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        ProjectMemberManageResponse response =
                projectMemberService.updateMemberRole(projectId, memberId, request, principal);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ProjectMemberManageResponse> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        ProjectMemberManageResponse response =
                projectMemberService.removeMember(projectId, memberId, principal);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{memberId}/accept")
    public ResponseEntity<ProjectMemberManageResponse> acceptInvitation(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        ProjectMemberManageResponse response =
                projectMemberService.acceptInvitation(projectId, memberId, principal);

        return ResponseEntity.ok(response);
    }
}
