package com.yuhyeon.devwebide.realtime.service;

import com.yuhyeon.devwebide.realtime.dto.WorkspacePresenceMessage;
import com.yuhyeon.devwebide.realtime.dto.WorkspacePresenceRequest;
import com.yuhyeon.devwebide.realtime.dto.WorkspacePresenceUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 프로젝트별 워크스페이스 접속자 목록을 메모리에 유지하고 WebSocket으로 공유합니다.
 */
@Service
public class WorkspacePresenceService {

    private static final Duration PRESENCE_TTL = Duration.ofSeconds(45);

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, Map<String, WorkspacePresenceUser>> presenceByProject =
            new ConcurrentHashMap<>();

    public WorkspacePresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void join(Long projectId, WorkspacePresenceRequest request) {
        Map<String, WorkspacePresenceUser> projectPresence =
                presenceByProject.computeIfAbsent(projectId, ignored -> new ConcurrentHashMap<>());
        LocalDateTime now = LocalDateTime.now();
        WorkspacePresenceUser existingUser = projectPresence.get(request.clientId());

        projectPresence.put(
                request.clientId(),
                new WorkspacePresenceUser(
                        request.clientId(),
                        sanitize(request.displayName(), "Guest"),
                        sanitize(request.role(), "Editor"),
                        sanitize(request.currentFile(), "파일 선택 안 됨"),
                        sanitize(request.status(), "online"),
                        existingUser == null ? now : existingUser.joinedAt(),
                        now
                )
        );
        publish(projectId);
    }

    public void leave(Long projectId, String clientId) {
        Map<String, WorkspacePresenceUser> projectPresence = presenceByProject.get(projectId);

        if (projectPresence == null) {
            return;
        }

        projectPresence.remove(clientId);
        publish(projectId);
    }

    public void publish(Long projectId) {
        Map<String, WorkspacePresenceUser> projectPresence = presenceByProject.get(projectId);

        if (projectPresence == null) {
            publishToWebSocket(projectId, List.of());
            return;
        }

        pruneExpiredUsers(projectPresence);
        List<WorkspacePresenceUser> users = projectPresence.values().stream()
                .sorted(Comparator.comparing(WorkspacePresenceUser::joinedAt))
                .toList();

        publishToWebSocket(projectId, users);
    }

    private void pruneExpiredUsers(Map<String, WorkspacePresenceUser> projectPresence) {
        LocalDateTime expiresBefore = LocalDateTime.now().minus(PRESENCE_TTL);

        projectPresence.entrySet().removeIf(entry ->
                entry.getValue().lastSeenAt().isBefore(expiresBefore)
        );
    }

    private void publishToWebSocket(Long projectId, List<WorkspacePresenceUser> users) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/presence",
                new WorkspacePresenceMessage(projectId, users, LocalDateTime.now())
        );
    }

    private String sanitize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim();
    }
}
