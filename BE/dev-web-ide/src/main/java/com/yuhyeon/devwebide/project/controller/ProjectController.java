package com.yuhyeon.devwebide.project.controller;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.project.dto.*;
import com.yuhyeon.devwebide.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 프로젝트 API 컨트롤러
 *
 * 프로젝트 생성, 목록 조회, 상세 조회, 열기 등의 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 프로젝트 생성
     *
     * 개인 프로젝트, 팀 프로젝트, 게스트 프로젝트를 생성합니다.
     *
     * @param request 프로젝트 생성 요청
     * @param principal 인증 사용자 정보
     * @return 생성된 프로젝트 정보
     */
    @PostMapping
    public ResponseEntity<ProjectCreateResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        ProjectCreateResponse response = projectService.createProject(
                request,
                principal
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 내 프로젝트 목록 조회
     *
     * 로그인한 사용자가 생성한 프로젝트 목록을 조회합니다.
     *
     * @param principal 인증 사용자 정보
     * @return 내 프로젝트 목록
     */
    @GetMapping("/my")
    public ResponseEntity<List<ProjectSummaryResponse>> getMyProjects(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        List<ProjectSummaryResponse> response =
                projectService.getMyProjects(principal);

        return ResponseEntity.ok(response);
    }

    /**
     * 프로젝트 상세 조회
     *
     * 프로젝트 목록에서 선택한 프로젝트의 상세 정보를 조회합니다.
     * 프로젝트 기본 정보, 런타임 정보, 설정 정보, 멤버 목록을 함께 반환합니다.
     *
     * @param projectId 조회할 프로젝트 ID
     * @return 프로젝트 상세 정보
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponse> getProjectDetail(
            @PathVariable Long projectId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        ProjectDetailResponse response =
                projectService.getProjectDetail(projectId, principal);

        return ResponseEntity.ok(response);
    }

    /**
     * 프로젝트 열기
     *
     * 프로젝트를 열고 접근 기록을 저장합니다.
     * 인증 연동 전까지는 userId 또는 guestSessionId를 RequestParam으로 받습니다.
     *
     * 회원 사용자는 userId를 전달하고,
     * 게스트 사용자는 guestSessionId를 전달합니다.
     *
     * @param projectId 열 프로젝트 ID
     * @param userId 회원 사용자 ID
     * @param guestSessionId 게스트 세션 ID
     * @return 프로젝트 열기 응답
     */
    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        ProjectDetailResponse response =
                projectService.updateProject(projectId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ProjectDeleteResponse> deleteProject(
            @PathVariable Long projectId
    ) {
        ProjectDeleteResponse response =
                projectService.deleteProject(projectId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{projectId}/open")
    public ResponseEntity<ProjectOpenResponse> openProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long guestSessionId
    ) {
        ProjectOpenResponse response = projectService.openProject(
                projectId,
                userId,
                guestSessionId
        );

        return ResponseEntity.ok(response);
    }
}
