package org.example.orderservice.dto;

import lombok.Data;

@Data
public class UpdateOrderRequest {

    private String shippingAddress;

    private String billingAddress;
}
