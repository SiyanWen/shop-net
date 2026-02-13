package org.example.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;

@Table("orders_by_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderByUser {

    @PrimaryKey
    private OrderByUserKey key;

    private String status;

    private BigDecimal total;

    private String currency;
}
