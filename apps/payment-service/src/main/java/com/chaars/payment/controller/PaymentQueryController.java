package com.chaars.payment.controller;

import com.chaars.payment.domain.PaymentTransactionEntity;
import com.chaars.payment.repository.PaymentTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentQueryController {

    private final PaymentTransactionRepository paymentTransactionRepository;

    public PaymentQueryController(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    public record paymentStatusResponse(
            UUID orderId,
            String paymentId,
            double amount,
            String status,
            String createdAt
    ){}

    @GetMapping("orders/{orderId}")
    public ResponseEntity<paymentStatusResponse> getOrderId(@PathVariable UUID orderId) {
        return paymentTransactionRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private paymentStatusResponse toResponse(PaymentTransactionEntity paymentTransactionEntity) {
        return new paymentStatusResponse(
                paymentTransactionEntity.getOrderId(),
                paymentTransactionEntity.getId().toString(),
                paymentTransactionEntity.getAmount(),
                paymentTransactionEntity.getStatus().name(),
                paymentTransactionEntity.getCreatedAt().toString()
        );
    }

}
