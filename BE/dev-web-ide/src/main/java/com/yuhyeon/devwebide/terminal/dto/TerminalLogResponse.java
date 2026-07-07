package com.yuhyeon.devwebide.terminal.dto;

import com.yuhyeon.devwebide.terminal.domain.TerminalLog;
import com.yuhyeon.devwebide.terminal.domain.TerminalStreamType;

import java.time.LocalDateTime;

/**
 * 터미널 로그 응답 DTO
 */
public record TerminalLogResponse(
        Long id,
        Long sequenceNo,
        TerminalStreamType streamType,
        String content,
        LocalDateTime createdAt
) {

    /**
     * TerminalLog 엔티티를 응답 DTO로 변환합니다.
     *
     * @param terminalLog 터미널 로그 엔티티
     * @return 터미널 로그 응답
     */
    public static TerminalLogResponse from(TerminalLog terminalLog) {
        return new TerminalLogResponse(
                terminalLog.getId(),
                terminalLog.getSequenceNo(),
                terminalLog.getStreamType(),
                terminalLog.getContent(),
                terminalLog.getCreatedAt()
        );
    }
}
