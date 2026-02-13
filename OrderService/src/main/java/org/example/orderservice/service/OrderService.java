package org.example.orderservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.client.ItemServiceClient;
import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.OrderEvent;
import org.example.orderservice.dto.OrderItemDto;
import org.example.orderservice.dto.UpdateOrderRequest;
import org.example.orderservice.entity.OrderById;
import org.example.orderservice.entity.OrderByUser;
import org.example.orderservice.entity.OrderByUserKey;
import org.example.orderservice.entity.OrderItem;
import org.example.orderservice.exception.InvalidOrderStateException;
import org.example.orderservice.exception.OrderNotFoundException;
import org.example.orderservice.repository.OrderByIdRepository;
import org.example.orderservice.repository.OrderByUserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final String TOPIC = "order-events";
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("5.99");

    private final OrderByIdRepository orderByIdRepository;
    private final OrderByUserRepository orderByUserRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ItemServiceClient itemServiceClient;

    public OrderById createOrder(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();

        // Decrement inventory for each item via ItemService
        List<OrderItemDto> succeededItems = new ArrayList<>();
        for (OrderItemDto dto : request.getItems()) {
            try {
                itemServiceClient.updateInventory(dto.getItemId(),
                        Map.of("quantity", -dto.getQuantity()));
                succeededItems.add(dto);
            } catch (FeignException e) {
                log.error("Failed to decrement inventory for item {}: {}",
                        dto.getItemId(), e.getMessage());
                // Roll back already-decremented items
                for (OrderItemDto succeeded : succeededItems) {
                    try {
                        itemServiceClient.updateInventory(succeeded.getItemId(),
                                Map.of("quantity", succeeded.getQuantity()));
                    } catch (FeignException rollbackEx) {
                        log.error("Failed to roll back inventory for item {}: {}",
                                succeeded.getItemId(), rollbackEx.getMessage());
                    }
                }
                throw new RuntimeException(
                        "Insufficient inventory for item " + dto.getItemId(), e);
            }
        }

        List<OrderItem> items = request.getItems().stream()
                .map(dto -> new OrderItem(dto.getItemId(), dto.getName(), dto.getQuantity(), dto.getPrice()))
                .toList();

        BigDecimal subtotal = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).add(SHIPPING_FEE).setScale(2, RoundingMode.HALF_UP);

        OrderById order = OrderById.builder()
                .orderId(orderId)
                .userId(request.getUserId())
                .createdAt(now)
                .status("CREATED")
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .items(items)
                .subtotal(subtotal)
                .tax(tax)
                .shippingFee(SHIPPING_FEE)
                .total(total)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .attributes(request.getAttributes())
                .build();

        orderByIdRepository.save(order);

        OrderByUser orderByUser = OrderByUser.builder()
                .key(new OrderByUserKey(request.getUserId(), now, orderId))
                .status("CREATED")
                .total(total)
                .currency(order.getCurrency())
                .build();

        orderByUserRepository.save(orderByUser);

        publishEvent(order);
        log.info("Order created: {}", orderId);
        return order;
    }

    public OrderById getOrder(UUID orderId) {
        return orderByIdRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    public List<OrderByUser> getOrdersByUser(UUID userId) {
        return orderByUserRepository.findByKeyUserId(userId);
    }

    public OrderById updateOrder(UUID orderId, UpdateOrderRequest request) {
        OrderById order = getOrder(orderId);

        if (!"CREATED".equals(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Cannot update order in status: " + order.getStatus());
        }

        if (request.getShippingAddress() != null) {
            order.setShippingAddress(request.getShippingAddress());
        }
        if (request.getBillingAddress() != null) {
            order.setBillingAddress(request.getBillingAddress());
        }
        if (request.getAttributes() != null) {
            order.setAttributes(request.getAttributes());
        }

        return orderByIdRepository.save(order);
    }

    public OrderById cancelOrder(UUID orderId) {
        OrderById order = getOrder(orderId);

        if ("CANCELLED".equals(order.getStatus())) {
            return order; // idempotent
        }

        if (!"CREATED".equals(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus("CANCELLED");
        order = orderByIdRepository.save(order);

        updateOrderByUserStatus(order);
        publishEvent(order);
        log.info("Order cancelled: {}", orderId);
        return order;
    }

    public void markOrderPaid(UUID orderId, String paymentRef) {
        OrderById order = getOrder(orderId);

        if (!"CREATED".equals(order.getStatus())) {
            log.warn("Cannot mark order {} as PAID — current status: {}", orderId, order.getStatus());
            return;
        }

        order.setStatus("PAID");
        order.setPaymentRef(paymentRef);
        order = orderByIdRepository.save(order);

        updateOrderByUserStatus(order);
        publishEvent(order);
        log.info("Order marked as PAID: {}", orderId);
    }

    private void updateOrderByUserStatus(OrderById order) {
        List<OrderByUser> userOrders = orderByUserRepository.findByKeyUserId(order.getUserId());
        userOrders.stream()
                .filter(o -> o.getKey().getOrderId().equals(order.getOrderId()))
                .findFirst()
                .ifPresent(o -> {
                    o.setStatus(order.getStatus());
                    orderByUserRepository.save(o);
                });
    }

    private void publishEvent(OrderById order) {
        OrderEvent event = OrderEvent.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .total(order.getTotal())
                .currency(order.getCurrency())
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send(TOPIC, order.getOrderId().toString(), event);
        log.info("Published event to {}: status={}", TOPIC, event.getStatus());
    }
}
