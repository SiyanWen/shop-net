package org.example.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull
    private UUID userId;

    @NotEmpty
    @Valid
    private List<OrderItemDto> items;

    private String currency = "USD";

    private String shippingAddress;

    private String billingAddress;

    private Map<String, String> attributes;
}
