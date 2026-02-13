package org.example.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import java.math.BigDecimal;

@UserDefinedType("order_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String itemId;
    private String name;
    private int quantity;
    private BigDecimal price;
}
