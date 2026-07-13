package com.yuhyeon.devwebide.realtime.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.realtime.dto.TeamChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis에서 수신한 팀 채팅 메시지를 WebSocket topic으로 전달합니다.
 */
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "app.redis",
        name = "enabled",
        havingValue = "true"
)
public class TeamChatRedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final TeamChatPublisher publisher;

    public TeamChatRedisSubscriber(TeamChatPublisher publisher) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.publisher = publisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(
                    message.getBody(),
                    StandardCharsets.UTF_8
            );
            TeamChatMessage chatMessage =
                    objectMapper.readValue(payload, TeamChatMessage.class);

            publisher.publishToWebSocket(chatMessage);
        } catch (Exception exception) {
            log.warn("Redis 채팅 메시지 처리에 실패했습니다.", exception);
        }
    }
}
