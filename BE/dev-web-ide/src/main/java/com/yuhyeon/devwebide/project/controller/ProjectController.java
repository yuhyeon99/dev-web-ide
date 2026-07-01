package com.yuhyeon.devwebide.project.controller;

import com.yuhyeon.devwebide.project.dto.*;
import com.yuhyeon.devwebide.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * 현재는 인증 기능 구현 전이므로
     * 임시로 Header에서 사용자 ID와 게스트 세션 ID를 전달받습니다.
     *
     * 추후 Spring Security 적용 시
     * X-User-Id, X-Guest-Session-Id 대신 인증 객체에서 꺼내도록 변경합니다.
     *
     * @param request 프로젝트 생성 요청
     * @param ownerUserId 현재 로그인한 회원 사용자 ID
     * @param guestSessionId 현재 게스트 세션 ID
     * @return 생성된 프로젝트 정보
     */
    @PostMapping
    public ResponseEntity<ProjectCreateResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long ownerUserId,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) Long guestSessionId
    ) {
        ProjectCreateResponse response = projectService.createProject(
                request,
                ownerUserId,
                guestSessionId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 내 프로젝트 목록 조회
     *
     * 로그인한 사용자가 생성한 프로젝트 목록을 조회합니다.
     * 인증 연동 전까지는 ownerUserId를 RequestParam으로 받습니다.
     *
     * @param ownerUserId 프로젝트 소유자 ID
     * @return 내 프로젝트 목록
     */
    @GetMapping("/my")
    public ResponseEntity<List<ProjectSummaryResponse>> getMyProjects(
            @RequestParam Long ownerUserId
    ) {
        List<ProjectSummaryResponse> response =
                projectService.getMyProjects(ownerUserId);

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
            @PathVariable Long projectId
    ) {
        ProjectDetailResponse response =
                projectService.getProjectDetail(projectId);

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