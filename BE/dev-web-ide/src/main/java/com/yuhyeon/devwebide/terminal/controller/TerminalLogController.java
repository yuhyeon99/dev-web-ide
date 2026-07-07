package com.yuhyeon.devwebide.terminal.controller;

import com.yuhyeon.devwebide.terminal.dto.TerminalLogListResponse;
import com.yuhyeon.devwebide.terminal.service.TerminalLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 터미널 로그 컨트롤러
 *
 * 워크스페이스 실행 세션의 터미널 로그 조회 API를 처리합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workspace-sessions")
public class TerminalLogController {

    private final TerminalLogService terminalLogService;

    /**
     * 터미널 로그 목록 조회
     *
     * afterSequenceNo가 없으면 전체 로그를 조회하고,
     * 값이 있으면 해당 순번 이후 로그만 조회합니다.
     *
     * @param workspaceSessionId 워크스페이스 세션 ID
     * @param afterSequenceNo 기준 로그 순번
     * @return 터미널 로그 목록 응답
     */
    @GetMapping("/{workspaceSessionId}/terminal/logs")
    public ResponseEntity<TerminalLogListResponse> getTerminalLogs(
            @PathVariable Long workspaceSessionId,
            @RequestParam(required = false) Long afterSequenceNo
    ) {
        TerminalLogListResponse response =
                terminalLogService.getTerminalLogs(
                        workspaceSessionId,
                        afterSequenceNo
                );

        return ResponseEntity.ok(response);
    }
}
