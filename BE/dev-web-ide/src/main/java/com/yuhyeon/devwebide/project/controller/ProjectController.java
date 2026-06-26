package com.yuhyeon.devwebide.project.controller;

import com.yuhyeon.devwebide.project.dto.ProjectCreateRequest;
import com.yuhyeon.devwebide.project.dto.ProjectCreateResponse;
import com.yuhyeon.devwebide.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}