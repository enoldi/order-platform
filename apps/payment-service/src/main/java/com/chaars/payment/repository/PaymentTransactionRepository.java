package com.chaars.payment.repository;

import com.chaars.payment.domain.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID> {

    Optional<PaymentTransactionEntity> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
