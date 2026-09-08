package com.yuhyeon.devwebide.realtime.dto;

/**
 * 클라이언트가 전송하는 저장 전 live file content 요청입니다.
 */
public record LiveFileContentRequest(
        String clientId,
        String content
) {
}
