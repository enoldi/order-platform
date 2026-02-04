package com.chaars.inventory.controller;

import com.chaars.inventory.repository.ReservationRepository;
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

@Tag(name = "Inventory API", description = "Query inventory status and reservation details (idempotent)")
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryQueryController {

    private final ReservationRepository reservationRepository;

    public InventoryQueryController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public record InventoryStatusResponse(UUID orderId, String reservationId, String status, String reason, String createdAt){}

    @Operation(
            summary = "Get inventory status by order ID",
            description = "Retrieves the inventory status for a given order ID. Returns the inventory status response or 404 if not found."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory status found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventoryStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventory status not found", content = @Content)
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<InventoryStatusResponse> getStatus(@PathVariable UUID orderId) {
        return reservationRepository.findByOrderId(orderId)
                .map(reservation -> new InventoryStatusResponse(
                        reservation.getOrderId(),
                        reservation.getId().toString(),
                        reservation.getStatus().name(),
                        reservation.getReason(),
                        reservation.getReservedAt().toString()
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
