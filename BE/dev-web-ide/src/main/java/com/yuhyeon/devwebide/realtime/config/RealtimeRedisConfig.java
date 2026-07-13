package com.yuhyeon.devwebide.realtime.config;

import com.yuhyeon.devwebide.realtime.service.ProjectRealtimeRedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis Pub/Sub 기반 프로젝트 이벤트 수신 설정입니다.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.redis",
        name = "enabled",
        havingValue = "true"
)
public class RealtimeRedisConfig {

    @Value("${app.redis.project-events-channel}")
    private String projectEventsChannel;

    @Bean
    public RedisMessageListenerContainer projectRealtimeRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ProjectRealtimeRedisSubscriber subscriber
    ) {
        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                subscriber,
                new ChannelTopic(projectEventsChannel)
        );

        return container;
    }
}
