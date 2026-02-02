package com.chaars.inventory.repository;

import com.chaars.inventory.domain.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    boolean existsByOrderId(UUID orderId);

    Optional<ReservationEntity> findByOrderId(UUID orderId);
}
