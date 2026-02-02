package com.chaars.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public OrderEntity(UUID id, String customerId, double amount, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public OrderEntity() {

    }

    public UUID getId() {
        return id;
    }
    public String getCustomerId() {
        return customerId;
    }
    public double getAmount() {
        return amount;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
