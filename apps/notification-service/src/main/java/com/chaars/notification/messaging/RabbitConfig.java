package com.chaars.notification.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfig {

    private static Queue queueWithDlq(String name) {
        return QueueBuilder.durable(name)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", "",
                        "x-dead-letter-routing-key", name + ".dlq"
                ))
                .build();
    }

    @Bean
    public Queue notificationAllQueue() {
        return queueWithDlq(RabbitNames.Q_NOTIFICATION_ALL);
    }

    @Bean
    public Queue notificationAllDlq() {
        return QueueBuilder.durable(RabbitNames.Q_NOTIFICATION_ALL + ".dlq").build();
    }

    @Bean
    public Binding bindAllEvents(TopicExchange exchange) {
        return BindingBuilder.bind(notificationAllQueue())
                .to(exchange)
                .with("#");
    }
}
