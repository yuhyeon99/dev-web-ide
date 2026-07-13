package com.yuhyeon.devwebide.realtime.dto;

/**
 * 클라이언트가 전송하는 CRDT 업데이트 요청입니다.
 */
public record CrdtUpdateRequest(
        String clientId,
        String updateBase64
) {
}
