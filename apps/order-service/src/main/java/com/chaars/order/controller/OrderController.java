package com.chaars.order.controller;

import com.chaars.order.domain.OrderEntity;
import com.chaars.order.messaging.RabbitNames;
import com.chaars.order.messaging.events.OrderCreatedEvent;
import com.chaars.order.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Order", description = "Order related endpoints")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    public static final String X_CORRELATION_ID = "X-Correlation-Id";
    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrderController(OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public record createOrderRequest(String customerId, double amount){}

    @Operation(
            summary = "Create an order",
            description = "Creates a new order with the provided customer ID and amount. Returns the created order event."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created and event published",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderCreatedEvent.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload", content = @Content)
    })
    @PostMapping
    public OrderCreatedEvent createOrder(@RequestBody createOrderRequest request,
                                         @Parameter(in = ParameterIn.HEADER,name = X_CORRELATION_ID, description = "Correlation ID for tracing", required = false)
                                         @RequestHeader(value = X_CORRELATION_ID, required = false) String correlationId) {

        if (correlationId == null || correlationId.isBlank()) correlationId = UUID.randomUUID().toString();

        OrderEntity order = new OrderEntity(UUID.randomUUID(), request.customerId(), request.amount(), Instant.now());
        orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), order.getCustomerId(), order.getAmount(), order.getCreatedAt());

        String correlationIdHeader = correlationId;

        rabbitTemplate.convertAndSend(RabbitNames.EXCHANGE, RabbitNames.RK_ORDER_CREATED, event, message -> {
            message.getMessageProperties().setHeader(X_CORRELATION_ID, correlationIdHeader);
            message.getMessageProperties().setHeader("x-idempotency-key", order.getId().toString());
            message.getMessageProperties().setHeader("x-event-type", "OrderCreatedEvent");
            return message;
        });

        return event;
    }

}
