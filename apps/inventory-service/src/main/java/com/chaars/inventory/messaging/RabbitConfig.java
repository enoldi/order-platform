package com.chaars.inventory.messaging;

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
    public Queue inventoryPaymentAuthorizedQueue() {
        return queueWithDlq(RabbitNames.Q_INVENTORY_PAYMENT_AUTH);
    }

    @Bean
    public Queue inventoryPaymentAuthDlq() {
        return QueueBuilder.durable(RabbitNames.Q_INVENTORY_PAYMENT_AUTH + ".dlq").build();
    }

    @Bean
    public Binding bindPaymentAuth(TopicExchange eventsExchange) {
        return BindingBuilder.bind(inventoryPaymentAuthorizedQueue())
                .to(eventsExchange)
                .with(RabbitNames.RK_INV_RESERVED);
    }
}
