package com.fitness.user_service.config;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("rabbitmq")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RabbitMQProperties {

    String uri;
    RabbitMQQueue queue = new RabbitMQQueue();
    RabbitMQExchange exchange = new RabbitMQExchange();
    RabbitMQRoutingKey routingKey = new RabbitMQRoutingKey();

    @Data
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class RabbitMQQueue {
        String userQueue;
    }

    @Data
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class RabbitMQExchange {
        String name;
        String type;
    }

    @Data
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class RabbitMQRoutingKey {
        String userRoutingKey;
    }
}
