package com.yuhyeon.devwebide.realtime.dto;

/**
 * 팀 워크스페이스 채팅 요청입니다.
 */
public record TeamChatMessageRequest(
        String clientId,
        String senderName,
        String message
) {
}
