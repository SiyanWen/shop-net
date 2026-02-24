package org.example.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateOrderRequest {

    @Schema(example = "123 Main St, Springfield, IL 62701")
    private String shippingAddress;

    @Schema(example = "456 Elm St, Springfield, IL 62701")
    private String billingAddress;
}
