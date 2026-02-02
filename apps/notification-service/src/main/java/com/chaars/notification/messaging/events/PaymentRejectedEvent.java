package com.chaars.notification.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record PaymentRejectedEvent(
        UUID orderId,
        String reason,
        Instant rejectedAt
) {}
