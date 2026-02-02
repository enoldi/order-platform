package com.chaars.order.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record InventoryFailedEvent(
        UUID orderId,
        String reason,
        Instant failedAt
) {
}
