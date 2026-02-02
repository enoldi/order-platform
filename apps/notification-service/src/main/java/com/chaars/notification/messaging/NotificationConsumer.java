package com.chaars.notification.messaging;

import com.chaars.notification.logging.MdcUtil;
import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(NotificationConsumer.class);

    @RabbitListener(queues = RabbitNames.Q_NOTIFICATION_ALL)
    public void onAnyEvent(Message event) {
        // prod-like: ici tu pourrais passer par headers/type, ou router vers le handlers
        String routingKey = event.getMessageProperties().getReceivedRoutingKey();
        Object replay = event.getMessageProperties().getHeader("x-idempotent-replay");
        Object idempotentKey = event.getMessageProperties().getHeader("x-idempotent-key");
        Object correlationId = event.getMessageProperties().getHeader("X-Correlation-Id");

        String body = new String(event.getBody());
        System.out.printf("[notification] rk= %s replay" +
                "= %s idempotentKey= %s correlationId= %s playload= %s%n", routingKey, replay, idempotentKey, correlationId, body);

        MdcUtil.withCorrelationId(String.valueOf(correlationId), () -> log.info("Received notification {}", body));
    }
}
