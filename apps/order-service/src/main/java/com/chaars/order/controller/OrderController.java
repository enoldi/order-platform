package com.chaars.order.controller;

import com.chaars.order.domain.OrderEntity;
import com.chaars.order.messaging.RabbitNames;
import com.chaars.order.messaging.events.OrderCreatedEvent;
import com.chaars.order.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    public static final String X_CORRELATION_ID = "X-Correlation-Id";
    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrderController(OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public record createOrderRequest(String customerId, double amount){}

    @PostMapping
    public OrderCreatedEvent createOrder(@RequestBody createOrderRequest request,
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
