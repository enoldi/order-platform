package com.chaars.inventory.messaging;

import com.chaars.inventory.logging.MdcUtil;
import com.chaars.inventory.messaging.events.PaymentAuthorizedEvent;
import com.chaars.inventory.service.InventoryProcessor;
import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(InventoryConsumer.class);

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String MISSING_CORRELATION_ID = "missing-correlation-id";

    private final InventoryProcessor inventoryProcessor;

    InventoryConsumer(InventoryProcessor inventoryProcessor) {
        this.inventoryProcessor = inventoryProcessor;
    }

    @RabbitListener(queues = RabbitNames.Q_INVENTORY_PAYMENT_AUTH)
    public void onPaymentAuthorized(PaymentAuthorizedEvent event, Message message) {
        String correlationId = resolveCorrelationId(message);

        MdcUtil.withCorrelationId(correlationId, () -> {
            log.info("Received PaymentAuthorizedEvent for orderId {} amount {}", event.orderId(), event.amount());
            inventoryProcessor.handle(event, correlationId);
        });
    }

    private static String resolveCorrelationId(Message message) {
        String correlationId = message.getMessageProperties().getHeader(CORRELATION_ID_HEADER);
        return (correlationId != null) ? correlationId : MISSING_CORRELATION_ID;
    }
}
