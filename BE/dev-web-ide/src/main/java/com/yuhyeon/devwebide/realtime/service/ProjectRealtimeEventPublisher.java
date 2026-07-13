package com.yuhyeon.devwebide.realtime.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.realtime.dto.ProjectRealtimeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 프로젝트 이벤트를 Redis Pub/Sub 또는 로컬 WebSocket topic으로 발행합니다.
 */
@Slf4j
@Service
public class ProjectRealtimeEventPublisher {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public ProjectRealtimeEventPublisher(
            StringRedisTemplate stringRedisTemplate,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.stringRedisTemplate = stringRedisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Value("${app.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${app.redis.project-events-channel:dev-web-ide:project-events}")
    private String projectEventsChannel;

    public void publish(ProjectRealtimeEvent event) {
        if (!redisEnabled) {
            publishToWebSocket(event);
            return;
        }

        try {
            stringRedisTemplate.convertAndSend(
                    projectEventsChannel,
                    objectMapper.writeValueAsString(event)
            );
        } catch (JsonProcessingException exception) {
            log.warn("프로젝트 실시간 이벤트 직렬화에 실패했습니다.", exception);
        } catch (RuntimeException exception) {
            log.warn("Redis 이벤트 발행에 실패해 로컬 WebSocket으로 대체합니다.", exception);
            publishToWebSocket(event);
        }
    }

    public void publishToWebSocket(ProjectRealtimeEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + event.projectId() + "/events",
                event
        );
    }
}
