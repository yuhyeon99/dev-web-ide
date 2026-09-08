package com.yuhyeon.devwebide.realtime.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 프로젝트 워크스페이스에 현재 접속 중인 사용자 목록입니다.
 */
public record WorkspacePresenceMessage(
        Long projectId,
        List<WorkspacePresenceUser> users,
        LocalDateTime occurredAt
) {
}
