package com.chaars.inventory.logging;

import org.slf4j.MDC;

public final class MdcUtil {
    private MdcUtil() {}

    public static void withCorrelationId(String correlationId, Runnable runnable) {

        if (correlationId != null && !correlationId.isBlank()) MDC.put("correlationId", correlationId);

        try {
            runnable.run();
        } finally {
            MDC.remove("correlationId");
        }
    }
}
