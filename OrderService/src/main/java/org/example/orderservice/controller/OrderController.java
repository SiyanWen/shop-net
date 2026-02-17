package org.example.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.UpdateOrderRequest;
import org.example.orderservice.entity.OrderById;
import org.example.orderservice.entity.OrderByUser;
import org.example.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderById> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /api/orders — request: userId={}, items={}, currency={}",
                request.getUserId(), request.getItems(), request.getCurrency());
        OrderById order = orderService.createOrder(request);
        log.info("Order created successfully: orderId={}, total={}", order.getOrderId(), order.getTotal());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderById> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping
    public ResponseEntity<List<OrderByUser>> getOrdersByUser(@RequestParam UUID userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderById> updateOrder(
            @PathVariable UUID orderId,
            @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, request));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderById> cancelOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
