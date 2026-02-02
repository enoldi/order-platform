package com.chaars.notification.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record PaymentAuthorizedEvent(
        UUID orderId,
        String paymentId,
        double amount,
        Instant authorizedAt
) {
}
