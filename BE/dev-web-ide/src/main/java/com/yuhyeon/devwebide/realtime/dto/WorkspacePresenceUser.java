package com.yuhyeon.devwebide.realtime.dto;

import java.time.LocalDateTime;

/**
 * 워크스페이스에 접속 중인 단일 사용자 정보입니다.
 */
public record WorkspacePresenceUser(
        String clientId,
        String displayName,
        String role,
        String currentFile,
        String status,
        LocalDateTime joinedAt,
        LocalDateTime lastSeenAt
) {
}
