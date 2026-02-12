package org.example.itemservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryUpdateRequest {

    @NotNull(message = "quantity is required")
    private Integer quantity;
}
