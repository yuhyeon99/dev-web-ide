package com.yuhyeon.devwebide.terminal.repository;

import com.yuhyeon.devwebide.terminal.domain.TerminalLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 터미널 로그 Repository
 */
public interface TerminalLogRepository extends JpaRepository<TerminalLog, Long> {

    /**
     * 워크스페이스 세션 ID 기준 터미널 로그 목록 조회
     *
     * @param workspaceSessionId 워크스페이스 세션 ID
     * @return sequenceNo 오름차순 터미널 로그 목록
     */
    List<TerminalLog> findByWorkspaceSessionIdOrderBySequenceNoAsc(
            Long workspaceSessionId
    );

    /**
     * 특정 순번 이후 터미널 로그 목록 조회
     *
     * @param workspaceSessionId 워크스페이스 세션 ID
     * @param sequenceNo 기준 로그 순번
     * @return 기준 순번보다 큰 터미널 로그 목록
     */
    List<TerminalLog> findByWorkspaceSessionIdAndSequenceNoGreaterThanOrderBySequenceNoAsc(
            Long workspaceSessionId,
            Long sequenceNo
    );

    /**
     * 워크스페이스 세션의 마지막 터미널 로그 조회
     *
     * @param workspaceSessionId 워크스페이스 세션 ID
     * @return 가장 큰 sequenceNo를 가진 터미널 로그
     */
    Optional<TerminalLog> findTopByWorkspaceSessionIdOrderBySequenceNoDesc(
            Long workspaceSessionId
    );
}
