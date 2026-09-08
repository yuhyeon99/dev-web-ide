package com.yuhyeon.devwebide.realtime.service;

import com.yuhyeon.devwebide.realtime.dto.LiveFileContentMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 저장하지 않은 편집 내용을 현재 접속 중인 워크스페이스 탭에만 공유합니다.
 */
@Service
public class LiveFileContentPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, LiveFileContentMessage> liveFileContents =
            new ConcurrentHashMap<>();

    public LiveFileContentPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(LiveFileContentMessage message) {
        liveFileContents.put(getLiveFileContentKey(message.projectId(), message.fileId()), message);
        publishToWebSocket(message);
    }

    public void publishSnapshot(Long projectId, Long fileId) {
        LiveFileContentMessage message =
                liveFileContents.get(getLiveFileContentKey(projectId, fileId));

        if (message == null) {
            return;
        }

        publishToWebSocket(message);
    }

    private void publishToWebSocket(LiveFileContentMessage message) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + message.projectId()
                        + "/files/" + message.fileId() + "/live-content",
                message
        );
    }

    private String getLiveFileContentKey(Long projectId, Long fileId) {
        return projectId + ":" + fileId;
    }
}
