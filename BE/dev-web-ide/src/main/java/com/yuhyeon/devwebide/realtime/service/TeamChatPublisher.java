package com.yuhyeon.devwebide.realtime.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.realtime.dto.TeamChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 팀 워크스페이스 채팅 메시지를 Redis Pub/Sub 또는 로컬 WebSocket으로 전달합니다.
 */
@Slf4j
@Service
public class TeamChatPublisher {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${app.redis.chat-messages-channel:dev-web-ide:chat-messages}")
    private String chatMessagesChannel;

    public TeamChatPublisher(
            StringRedisTemplate stringRedisTemplate,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.stringRedisTemplate = stringRedisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(TeamChatMessage message) {
        if (message.message().isBlank()) {
            return;
        }

        if (!redisEnabled) {
            publishToWebSocket(message);
            return;
        }

        try {
            stringRedisTemplate.convertAndSend(
                    chatMessagesChannel,
                    objectMapper.writeValueAsString(message)
            );
        } catch (JsonProcessingException exception) {
            log.warn("채팅 메시지 직렬화에 실패했습니다.", exception);
        } catch (RuntimeException exception) {
            log.warn("Redis 채팅 메시지 발행에 실패해 로컬 WebSocket으로 대체합니다.", exception);
            publishToWebSocket(message);
        }
    }

    public void publishToWebSocket(TeamChatMessage message) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + message.projectId() + "/chat",
                message
        );
    }
}
