package com.yuhyeon.devwebide.realtime.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.realtime.dto.CrdtUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Yjs CRDT 업데이트를 Redis Pub/Sub 또는 로컬 WebSocket으로 전달합니다.
 */
@Slf4j
@Service
public class CrdtUpdatePublisher {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${app.redis.crdt-updates-channel:dev-web-ide:crdt-updates}")
    private String crdtUpdatesChannel;

    public CrdtUpdatePublisher(
            StringRedisTemplate stringRedisTemplate,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.stringRedisTemplate = stringRedisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(CrdtUpdateMessage message) {
        if (message.updateBase64() == null || message.updateBase64().isBlank()) {
            return;
        }

        if (!redisEnabled) {
            publishToWebSocket(message);
            return;
        }

        try {
            stringRedisTemplate.convertAndSend(
                    crdtUpdatesChannel,
                    objectMapper.writeValueAsString(message)
            );
        } catch (JsonProcessingException exception) {
            log.warn("CRDT 업데이트 직렬화에 실패했습니다.", exception);
        } catch (RuntimeException exception) {
            log.warn("Redis CRDT 업데이트 발행에 실패해 로컬 WebSocket으로 대체합니다.", exception);
            publishToWebSocket(message);
        }
    }

    public void publishToWebSocket(CrdtUpdateMessage message) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + message.projectId()
                        + "/files/" + message.fileId() + "/crdt",
                message
        );
    }
}
