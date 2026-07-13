package com.yuhyeon.devwebide.realtime.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 팀 워크스페이스 채팅 메시지입니다.
 */
public record TeamChatMessage(
        String id,
        Long projectId,
        String clientId,
        String senderName,
        String message,
        LocalDateTime occurredAt
) {

    public static TeamChatMessage of(
            Long projectId,
            String clientId,
            String senderName,
            String message
    ) {
        return new TeamChatMessage(
                UUID.randomUUID().toString(),
                projectId,
                clientId,
                normalizeSenderName(senderName),
                message == null ? "" : message.trim(),
                LocalDateTime.now()
        );
    }

    private static String normalizeSenderName(String senderName) {
        if (senderName == null || senderName.isBlank()) {
            return "익명";
        }

        return senderName.trim();
    }
}
