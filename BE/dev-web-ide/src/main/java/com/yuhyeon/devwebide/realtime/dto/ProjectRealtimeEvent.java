package com.yuhyeon.devwebide.realtime.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 프로젝트 실시간 동기화 이벤트입니다.
 */
public record ProjectRealtimeEvent(
        ProjectRealtimeEventType type,
        Long projectId,
        Long fileId,
        List<Long> fileIds,
        String path,
        String actorType,
        Long actorId,
        LocalDateTime occurredAt
) {

    public static ProjectRealtimeEvent of(
            ProjectRealtimeEventType type,
            Long projectId,
            Long fileId,
            List<Long> fileIds,
            String path,
            Long userId,
            Long guestSessionId
    ) {
        return new ProjectRealtimeEvent(
                type,
                projectId,
                fileId,
                fileIds == null ? List.of() : fileIds,
                path,
                resolveActorType(userId, guestSessionId),
                userId != null ? userId : guestSessionId,
                LocalDateTime.now()
        );
    }

    private static String resolveActorType(Long userId, Long guestSessionId) {
        if (userId != null) {
            return "USER";
        }

        if (guestSessionId != null) {
            return "GUEST";
        }

        return "SYSTEM";
    }
}
