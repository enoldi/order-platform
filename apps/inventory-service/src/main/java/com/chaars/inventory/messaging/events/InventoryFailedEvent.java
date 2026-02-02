package com.chaars.inventory.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record InventoryFailedEvent(
        UUID orderId,
        String reason,
        Instant failedAt
) {
}
