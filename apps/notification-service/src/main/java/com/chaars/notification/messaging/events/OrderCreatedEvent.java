package com.chaars.notification.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String customerId,
        double amount,
        Instant createdAt
) { }
