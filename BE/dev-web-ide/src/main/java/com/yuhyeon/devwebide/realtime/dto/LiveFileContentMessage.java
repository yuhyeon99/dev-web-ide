package com.yuhyeon.devwebide.realtime.dto;

import java.time.LocalDateTime;

/**
 * 저장 전 편집 중인 파일 내용을 탭 간 실시간으로 동기화하는 메시지입니다.
 */
public record LiveFileContentMessage(
        Long projectId,
        Long fileId,
        String roomId,
        String clientId,
        String content,
        LocalDateTime occurredAt
) {

    public static LiveFileContentMessage of(
            Long projectId,
            Long fileId,
            String clientId,
            String content
    ) {
        return new LiveFileContentMessage(
                projectId,
                fileId,
                "project:" + projectId + ":file:" + fileId,
                clientId,
                content,
                LocalDateTime.now()
        );
    }
}
