package com.yuhyeon.devwebide.project.controller;

import com.yuhyeon.devwebide.project.dto.ProjectMemberInviteRequest;
import com.yuhyeon.devwebide.project.dto.ProjectMemberManageResponse;
import com.yuhyeon.devwebide.project.dto.ProjectMemberRoleUpdateRequest;
import com.yuhyeon.devwebide.project.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    public ResponseEntity<ProjectMemberManageResponse> inviteMember(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") Long requesterUserId,
            @Valid @RequestBody ProjectMemberInviteRequest request
    ) {
        ProjectMemberManageResponse response =
                projectMemberService.inviteMember(projectId, requesterUserId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<ProjectMemberManageResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestHeader("X-User-Id") Long requesterUserId,
            @Valid @RequestBody ProjectMemberRoleUpdateRequest request
    ) {
        ProjectMemberManageResponse response =
                projectMemberService.updateMemberRole(projectId, memberId, requesterUserId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ProjectMemberManageResponse> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestHeader("X-User-Id") Long requesterUserId
    ) {
        ProjectMemberManageResponse response =
                projectMemberService.removeMember(projectId, memberId, requesterUserId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{memberId}/accept")
    public ResponseEntity<ProjectMemberManageResponse> acceptInvitation(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestHeader("X-User-Id") Long requesterUserId
    ) {
        ProjectMemberManageResponse response =
                projectMemberService.acceptInvitation(projectId, memberId, requesterUserId);

        return ResponseEntity.ok(response);
    }
}
