package com.chaars.payment.service;

import com.chaars.payment.domain.PaymentTransactionEntity;
import com.chaars.payment.messaging.RabbitNames;
import com.chaars.payment.messaging.events.OrderCreatedEvent;
import com.chaars.payment.messaging.events.PaymentAuthorizedEvent;
import com.chaars.payment.messaging.events.PaymentRejectedEvent;
import com.chaars.payment.repository.PaymentTransactionRepository;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PayementProcessor {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private RabbitTemplate rabbitTemplate;

    public PayementProcessor(PaymentTransactionRepository paymentTransactionRepository, RabbitTemplate rabbitTemplate) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void handle(OrderCreatedEvent event, String correlationId) {
        //1) Fast-path: deja traite?
        paymentTransactionRepository.findByOrderId(event.orderId()).ifPresent(existing -> {
            publishOutComeFromExisting(existing, event.orderId(), true, correlationId);
            return;
        });

        //2) Nouveau traitement (idempotent par contraireunique BD)
        boolean ok = event.amount() <= 5000;
        PaymentTransactionEntity.Status status = ok
                ? PaymentTransactionEntity.Status.AUTHORIZED
                : PaymentTransactionEntity.Status.REJECTED;

        PaymentTransactionEntity paymentTransactionEntity = new PaymentTransactionEntity(
                UUID.randomUUID(),
                event.orderId(),
                event.amount(),
                status,
                Instant.now());

        try {
            paymentTransactionRepository.saveAndFlush(paymentTransactionEntity); // flush -> force contrainte unique maintenant
        } catch (DataIntegrityViolationException e) {
            // un autre thread/consumer a traite en parallèle (ou redelivery)
            PaymentTransactionEntity existing = paymentTransactionRepository.findByOrderId(event.orderId())
                    .orElseThrow(() -> e);
            publishOutComeFromExisting(existing, event.orderId(), true, correlationId);
            return;
        }

        //Publier outcome du "premier" traitement
        publishOutComeFromExisting(paymentTransactionEntity, event.orderId(), false, correlationId);
    }

    private void publishOutComeFromExisting(PaymentTransactionEntity paymentTransactionEntity, UUID orderId, boolean replay, String correlationId) {

        String idempotentKey = orderId.toString();
        if (paymentTransactionEntity.getStatus() == PaymentTransactionEntity.Status.AUTHORIZED) {
            PaymentAuthorizedEvent paymentAuthorizedEvent = new PaymentAuthorizedEvent(
                    orderId,
                    paymentTransactionEntity.getId().toString(),
                    paymentTransactionEntity.getAmount(),
                    Instant.now()
            );
            publishWithHeaders(RabbitNames.RK_PAYMENT_AUTH, paymentAuthorizedEvent, replay, idempotentKey, correlationId);
        } else {
            PaymentRejectedEvent paymentRejectedEvent = new PaymentRejectedEvent(
                    orderId,
                    "Payment rejected (idempotent outcome)",
                    Instant.now()
            );
            publishWithHeaders(RabbitNames.RK_PAYMENT_REJECTED, paymentRejectedEvent, replay, idempotentKey, correlationId);
        }
    }

    private void publishWithHeaders(String routingKey, Object payload, boolean idempotentReplay, String idempotentKey, String correlationId) {
        MessagePostProcessor messagePostProcessor = message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            messageProperties.setHeader("x-event-type", payload.getClass().getSimpleName());
            messageProperties.setHeader("x-idempotent-replay", idempotentReplay);
            messageProperties.setHeader("x-idempotent-key", idempotentKey);
            messageProperties.setHeader("X-Correlation-Id", correlationId);

            messageProperties.setHeader("x-event-version", "v1");
            messageProperties.setTimestamp(java.util.Date.from(Instant.now()));
            return message;
        };

        rabbitTemplate.convertAndSend(RabbitNames.EXCHANGE, routingKey, payload, messagePostProcessor);
    }
}
