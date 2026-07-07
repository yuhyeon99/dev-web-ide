package com.yuhyeon.devwebide.terminal.service;

import com.yuhyeon.devwebide.terminal.domain.TerminalLog;
import com.yuhyeon.devwebide.terminal.dto.TerminalLogListResponse;
import com.yuhyeon.devwebide.terminal.repository.TerminalLogRepository;
import com.yuhyeon.devwebide.workspace.repository.WorkspaceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 터미널 로그 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TerminalLogService {

    private final WorkspaceSessionRepository workspaceSessionRepository;
    private final TerminalLogRepository terminalLogRepository;

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
    public TerminalLogListResponse getTerminalLogs(
            Long workspaceSessionId,
            Long afterSequenceNo
    ) {
        validateAfterSequenceNo(afterSequenceNo);

        workspaceSessionRepository.findById(workspaceSessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "워크스페이스 세션을 찾을 수 없습니다."
                ));

        List<TerminalLog> terminalLogs =
                findTerminalLogs(workspaceSessionId, afterSequenceNo);

        Long lastSequenceNo = terminalLogRepository
                .findTopByWorkspaceSessionIdOrderBySequenceNoDesc(workspaceSessionId)
                .map(TerminalLog::getSequenceNo)
                .orElse(null);

        return TerminalLogListResponse.of(
                workspaceSessionId,
                terminalLogs,
                lastSequenceNo
        );
    }

    private void validateAfterSequenceNo(Long afterSequenceNo) {
        if (afterSequenceNo != null && afterSequenceNo < 0) {
            throw new IllegalArgumentException("afterSequenceNo는 0 이상이어야 합니다.");
        }
    }

    private List<TerminalLog> findTerminalLogs(
            Long workspaceSessionId,
            Long afterSequenceNo
    ) {
        if (afterSequenceNo == null) {
            return terminalLogRepository.findByWorkspaceSessionIdOrderBySequenceNoAsc(
                    workspaceSessionId
            );
        }

        return terminalLogRepository
                .findByWorkspaceSessionIdAndSequenceNoGreaterThanOrderBySequenceNoAsc(
                        workspaceSessionId,
                        afterSequenceNo
                );
    }
}
