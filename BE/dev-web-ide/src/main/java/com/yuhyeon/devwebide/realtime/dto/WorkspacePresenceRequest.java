package com.yuhyeon.devwebide.realtime.dto;

/**
 * 클라이언트가 워크스페이스 접속 상태를 알릴 때 사용합니다.
 */
public record WorkspacePresenceRequest(
        String clientId,
        String displayName,
        String role,
        String currentFile,
        String status
) {
}
