package com.chaars.payment.messaging;

import com.chaars.payment.logging.MdcUtil;
import com.chaars.payment.messaging.events.OrderCreatedEvent;
import com.chaars.payment.service.PayementProcessor;
import org.slf4j.Logger;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class PaymentConsumer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PaymentConsumer.class);

    private final PayementProcessor paymentProcessor;

    public PaymentConsumer(PayementProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    @RabbitListener(queues = RabbitNames.Q_PAYMENT_ORDER_CREATED)
    public void onOrderCreated(OrderCreatedEvent event, Message message) {
        if (event == null || event.orderId() == null) {
            // Contract violation / bad payload -> don't retry forever
            throw new AmqpRejectAndDontRequeueException("Invalid OrderCreatedEvent: orderId is null");
        }

        String correlationId = resolveCorrelationId(message);

        MdcUtil.withCorrelationId(correlationId, () -> {
            log.info("Received OrderCreatedEvent for orderId {} amount {}", event.orderId(), event.amount());
            paymentProcessor.handle(event, correlationId);
        });

    }

    private static String resolveCorrelationId(Message message) {
        if (message == null || message.getMessageProperties() == null) {
            return UUID.randomUUID().toString();
        }

        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        Object headerValue = null;

        if (headers != null) {
            headerValue = headers.get("X-Correlation-Id");
            if (headerValue == null) {
                headerValue = headers.get("x-correlation-id");
            }
        }

        if (headerValue != null) {
            return String.valueOf(headerValue);
        }

        // Also try AMQP correlationId property (not the same as a header)
        String amqpCorrelationId = message.getMessageProperties().getCorrelationId();
        if (amqpCorrelationId != null && !amqpCorrelationId.isBlank()) {
            return amqpCorrelationId;
        }

        return UUID.randomUUID().toString();
    }
}
