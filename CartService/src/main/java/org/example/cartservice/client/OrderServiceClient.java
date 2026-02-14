package org.example.cartservice.client;

import org.example.cartservice.dto.CreateOrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "OrderService")
public interface OrderServiceClient {

    @PostMapping("/api/orders")
    Map<String, Object> createOrder(@RequestBody CreateOrderRequest request);
}
