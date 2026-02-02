package com.chaars.inventory.controller;

import com.chaars.inventory.repository.ReservationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryQueryController {

    private final ReservationRepository reservationRepository;

    public InventoryQueryController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public record InventoryStatusResponse(UUID orderId, String reservationId, String status, String reason, String createdAt){}

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
