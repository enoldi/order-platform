package com.chaars.inventory.service;

import com.chaars.inventory.domain.ReservationEntity;
import com.chaars.inventory.messaging.RabbitNames;
import com.chaars.inventory.messaging.events.InventoryFailedEvent;
import com.chaars.inventory.messaging.events.InventoryReservedEvent;
import com.chaars.inventory.messaging.events.PaymentAuthorizedEvent;
import com.chaars.inventory.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class InventoryProcessor {

    private final ReservationRepository reservationRepository;
    private final RabbitTemplate rabbitTemplate;

    public InventoryProcessor(ReservationRepository reservationRepository, RabbitTemplate rabbitTemplate) {
        this.reservationRepository = reservationRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void handle(PaymentAuthorizedEvent event, String correlationId) {
        // 1) deja traite ? republie outcome (replay = true)
        reservationRepository.findByOrderId(event.orderId()).ifPresent(existing -> {
            publishOutcome(existing, event.orderId(), correlationId, true);
            return;
        });

        // 2) logique de reservation (example)
        boolean ok = event.amount() < 50000; // a remplacer par le vraie logique stock
        ReservationEntity.Status status = ok ? ReservationEntity.Status.RESERVED : ReservationEntity.Status.FAILED;
        String reason = ok ? "ok" : "stock insuffisant";

        ReservationEntity reservation = new ReservationEntity(
                UUID.randomUUID(),
                event.orderId(),
                status,
                Instant.now(),
                reason
        );

        try {
            reservationRepository.saveAndFlush(reservation); // flush -> force contrainte unique maintenant
        } catch (DataIntegrityViolationException e) {
            // duplication / concurrence -> re-lire et republier outcome
            ReservationEntity existing = reservationRepository.findByOrderId(event.orderId()).orElseThrow(() -> e);
            publishOutcome(existing, event.orderId(), correlationId, true);
            return;
        }

        // 3) premier fois -> outcome replay = false
        publishOutcome(reservation, event.orderId(), correlationId, false);
    }

    private void publishOutcome(ReservationEntity reservation, UUID orderId, String correlationId, boolean replay) {
        if (reservation.getStatus() == ReservationEntity.Status.RESERVED) {
            InventoryReservedEvent inventoryReservedEvent = new InventoryReservedEvent(orderId, reservation.getId().toString(), Instant.now());
            publishwithHeaders(RabbitNames.RK_INV_RESERVED, inventoryReservedEvent, correlationId, replay, orderId.toString());
        } else {
            InventoryFailedEvent inventoryFailedEvent = new InventoryFailedEvent(orderId, reservation.getReason(), Instant.now());
            publishwithHeaders(RabbitNames.RK_INV_FAILED, inventoryFailedEvent, correlationId, replay, orderId.toString());
        }
    }

    private void publishwithHeaders(String routingKey, Object payload, String correlationId, boolean replay, String idempotentKey) {
        MessagePostProcessor messagePostProcessor = message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            messageProperties.setHeader("x-event-type", payload.getClass().getSimpleName());
            messageProperties.setHeader("x-idempotent-replay", replay);
            messageProperties.setHeader("x-idempotent-key", idempotentKey);
            messageProperties.setHeader("X-Correlation-Id", correlationId);
            messageProperties.setHeader("x-event-version", "v1");
            messageProperties.setTimestamp(java.util.Date.from(Instant.now()));
            return message;
        };
        rabbitTemplate.convertAndSend(RabbitNames.EXCHANGE, routingKey, payload, messagePostProcessor);
    }
}
