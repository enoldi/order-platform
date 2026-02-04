package com.chaars.payment.controller;

import com.chaars.payment.domain.PaymentTransactionEntity;
import com.chaars.payment.repository.PaymentTransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Payment API", description = "Query payment status and transaction details (idempotent)")
@RestController
@RequestMapping("/api/v1/payments")
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

    @Operation(
            summary = "Get payment  by order ID",
            description = "Retrieves the payment for a given order ID. Returns the payment response or 404 if not found."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment transaction found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = paymentStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found for this orderId", content = @Content)
    })
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
