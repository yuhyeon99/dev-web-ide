package com.yuhyeon.devwebide.realtime.dto;

import java.time.LocalDateTime;

/**
 * 파일 단위 CRDT 업데이트 메시지입니다.
 */
public record CrdtUpdateMessage(
        Long projectId,
        Long fileId,
        String roomId,
        String clientId,
        String updateBase64,
        LocalDateTime occurredAt
) {

    public static CrdtUpdateMessage of(
            Long projectId,
            Long fileId,
            String clientId,
            String updateBase64
    ) {
        return new CrdtUpdateMessage(
                projectId,
                fileId,
                "project:" + projectId + ":file:" + fileId,
                clientId,
                updateBase64,
                LocalDateTime.now()
        );
    }
}
