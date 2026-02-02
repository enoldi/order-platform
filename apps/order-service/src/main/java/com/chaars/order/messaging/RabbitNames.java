package com.chaars.order.messaging;

public final class RabbitNames {

    private RabbitNames() {}

    public static final String EXCHANGE="order.events";

    public static final String RK_ORDER_CREATED="order.created";
    public static final String RK_PAYMENT_AUTH="payment.authorized";
    public static final String RK_PAYMENT_REJECTED="payment.rejected";
    public static final String RK_INV_RESERVED="inventory.reserved";
    public static final String RK_INV_FAILED="inventory.failed";

    public static final String Q_PAYEMENT_ORDER_CREATED="payment.order.created.q";
    public static final String Q_INVENTORY_PAYMENT_AUTH="inventory.payment.authorized.q";
    public static final String Q_NOTIFICATION_ALL="notification.all.events.q";
}
