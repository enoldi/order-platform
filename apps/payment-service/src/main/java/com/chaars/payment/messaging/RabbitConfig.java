package com.chaars.payment.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitConfig {

    private static Queue queueWithDlq(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }

    @Bean
    public Queue paymentOrderCreatedQueue() {
        return queueWithDlq(RabbitNames.Q_PAYMENT_ORDER_CREATED);
    }

    @Bean
    public Queue paymentOrderCreatedDlq() {
        return QueueBuilder.durable(RabbitNames.Q_PAYMENT_ORDER_CREATED + ".dlq").build();
    }

    @Bean
    public Binding bindOrderCreated(TopicExchange eventsExchange) {
        return BindingBuilder.bind(paymentOrderCreatedQueue())
                .to(eventsExchange)
                .with(RabbitNames.RK_ORDER_CREATED);
    }
}
