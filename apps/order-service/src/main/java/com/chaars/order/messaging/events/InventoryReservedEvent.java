package com.chaars.order.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record InventoryReservedEvent(
        UUID orderId,
        String reservationId,
        Instant reservedAt
) {
}
