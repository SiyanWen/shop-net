package org.example.orderservice.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateOrderRequest {

    private String shippingAddress;

    private String billingAddress;

    private Map<String, String> attributes;
}
