package org.example.cartservice.client;

import lombok.extern.slf4j.Slf4j;
import org.example.cartservice.dto.CreateOrderRequest;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class OrderServiceClientFallbackFactory implements FallbackFactory<OrderServiceClient> {

    @Override
    public OrderServiceClient create(Throwable cause) {
        return new OrderServiceClient() {
            @Override
            public Map<String, Object> createOrder(CreateOrderRequest request) {
                log.error("Fallback: failed to create order: {}", cause.getMessage());
                throw new RuntimeException("OrderService unavailable: " + cause.getMessage(), cause);
            }
        };
    }
}
