package com.yuhyeon.devwebide.realtime.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.realtime.dto.ProjectRealtimeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis Pub/Sub으로 수신한 프로젝트 이벤트를 WebSocket으로 전달합니다.
 */
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "app.redis",
        name = "enabled",
        havingValue = "true"
)
public class ProjectRealtimeRedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final ProjectRealtimeEventPublisher eventPublisher;

    public ProjectRealtimeRedisSubscriber(
            ProjectRealtimeEventPublisher eventPublisher
    ) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(
                    message.getBody(),
                    StandardCharsets.UTF_8
            );
            ProjectRealtimeEvent event =
                    objectMapper.readValue(payload, ProjectRealtimeEvent.class);

            eventPublisher.publishToWebSocket(event);
        } catch (RuntimeException exception) {
            log.warn("Redis 프로젝트 이벤트 처리에 실패했습니다.", exception);
        } catch (Exception exception) {
            log.warn("Redis 프로젝트 이벤트 역직렬화에 실패했습니다.", exception);
        }
    }
}
