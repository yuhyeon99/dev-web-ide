package com.yuhyeon.devwebide.terminal.dto;

import com.yuhyeon.devwebide.terminal.domain.TerminalLog;

import java.util.List;

/**
 * 터미널 로그 목록 응답 DTO
 */
public record TerminalLogListResponse(
        Long workspaceSessionId,
        List<TerminalLogResponse> logs,
        Long lastSequenceNo
) {

    /**
     * 터미널 로그 목록 응답을 생성합니다.
     *
     * @param workspaceSessionId 워크스페이스 세션 ID
     * @param terminalLogs 터미널 로그 목록
     * @param lastSequenceNo 마지막 로그 순번
     * @return 터미널 로그 목록 응답
     */
    public static TerminalLogListResponse of(
            Long workspaceSessionId,
            List<TerminalLog> terminalLogs,
            Long lastSequenceNo
    ) {
        List<TerminalLogResponse> logs = terminalLogs.stream()
                .map(TerminalLogResponse::from)
                .toList();

        return new TerminalLogListResponse(
                workspaceSessionId,
                logs,
                lastSequenceNo
        );
    }
}
