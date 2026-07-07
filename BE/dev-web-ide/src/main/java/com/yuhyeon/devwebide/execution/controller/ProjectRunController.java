package com.yuhyeon.devwebide.execution.controller;

import com.yuhyeon.devwebide.execution.dto.ProjectRunRequest;
import com.yuhyeon.devwebide.execution.dto.ProjectRunResponse;
import com.yuhyeon.devwebide.execution.service.ProjectRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 프로젝트 실행 컨트롤러
 *
 * 웹 IDE 프로젝트 실행 API를 처리합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectRunController {

    private final ProjectRunService projectRunService;

    /**
     * 프로젝트 실행
     *
     * 인증 연동 전까지는 userId 또는 guestSessionId를 요청 본문으로 받습니다.
     *
     * @param projectId 실행할 프로젝트 ID
     * @param request 프로젝트 실행 요청
     * @return 프로젝트 실행 응답
     */
    @PostMapping("/{projectId}/run")
    public ResponseEntity<ProjectRunResponse> runProject(
            @PathVariable Long projectId,
            @RequestBody ProjectRunRequest request
    ) {
        ProjectRunResponse response =
                projectRunService.runProject(projectId, request);

        return ResponseEntity.ok(response);
    }
}
