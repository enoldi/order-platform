package com.chaars.payment.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record InventoryReservedEvent(
        UUID orderId,
        String reservationId,
        Instant reservedAt
) {
}
