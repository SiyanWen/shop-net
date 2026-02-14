package org.example.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {

    @NotBlank
    private String itemId;

    @NotBlank
    private String name;

    @Min(1)
    private int quantity;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
}
