package com.yuhyeon.devwebide.realtime.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.realtime.dto.CrdtUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis에서 수신한 CRDT 업데이트를 WebSocket topic으로 전달합니다.
 */
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "app.redis",
        name = "enabled",
        havingValue = "true"
)
public class CrdtUpdateRedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final CrdtUpdatePublisher publisher;

    public CrdtUpdateRedisSubscriber(CrdtUpdatePublisher publisher) {
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
            CrdtUpdateMessage update =
                    objectMapper.readValue(payload, CrdtUpdateMessage.class);

            publisher.publishToWebSocket(update);
        } catch (Exception exception) {
            log.warn("Redis CRDT 업데이트 처리에 실패했습니다.", exception);
        }
    }
}
