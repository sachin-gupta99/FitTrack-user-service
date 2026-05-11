package com.fitness.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "fitness_exchange";
    public static final String USER_QUEUE = "user_queue";
    public static final String USER_ROUTING_KEY = "user_routing_key";

    @Bean
    public TopicExchange fitnessExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(USER_QUEUE, true);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter(objectMapper));
        return rabbitTemplate;
    }

    private MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new MessageConverter() {
            @Override
            @NonNull
            public Message toMessage(@NonNull Object object, @NonNull MessageProperties messageProperties) {
                try {
                    byte[] bytes = objectMapper.writeValueAsBytes(object);
                    messageProperties.setContentType("application/json");
                    return new Message(bytes, messageProperties);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert object to JSON", e);
                }
            }

            @Override
            @NonNull
            public Object fromMessage(@NonNull Message message) {
                throw new UnsupportedOperationException("Use specific conversion in listener");
            }
        };
    }

    @Bean
    public Binding userBinding(Queue userQueue, TopicExchange fitnessExchange) {
        return BindingBuilder
                .bind(userQueue)
                .to(fitnessExchange)
                .with(USER_ROUTING_KEY);
    }
}
