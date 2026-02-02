package com.chaars.payment.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payment_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_payment_order_id", columnNames = {"order_id"})
)
public class PaymentTransactionEntity {

    public enum Status {
        AUTHORIZED, REJECTED
    }

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PaymentTransactionEntity() {}

    public PaymentTransactionEntity(UUID id, UUID orderId, double amount, Status status, Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }
    public UUID getId() {
        return id;
    }
    public UUID getOrderId() {
        return orderId;
    }
    public double getAmount() {
        return amount;
    }
    public Status getStatus() {
        return status;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
