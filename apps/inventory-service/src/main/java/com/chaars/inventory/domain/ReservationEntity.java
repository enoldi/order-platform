package com.chaars.inventory.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "reservations",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_order_id", columnNames = {"order_id"})
)
public class ReservationEntity {

    public enum Status {
        RESERVED, FAILED
    }

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt;

    @Column(name = "reason", nullable = false)
    private String reason; // "ok" si reserved, sinon message

    protected ReservationEntity() {}

    public ReservationEntity(UUID id, UUID orderId, Status status, Instant reservedAt, String reason) {
        this.id = id;
        this.orderId = orderId;
        this.reservedAt = reservedAt;
        this.status = status;
        this.reason = reason;
    }

    public UUID getId() {
        return id;
    }
    public UUID getOrderId() {
        return orderId;
    }
    public Instant getReservedAt() {
        return reservedAt;
    }
    public String getReason() {
        return reason;
    }
    public Status getStatus() {
        return status;
    }
}
