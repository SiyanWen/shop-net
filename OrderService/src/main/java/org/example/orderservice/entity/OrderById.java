package org.example.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Table("orders_by_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderById {

    @PrimaryKey("order_id")
    private UUID orderId;

    @Column("user_id")
    private UUID userId;

    @Column("created_at")
    private Instant createdAt;

    private String status;

    private String currency;

    private List<OrderItem> items;

    private BigDecimal subtotal;

    private BigDecimal tax;

    @Column("shipping_fee")
    private BigDecimal shippingFee;

    private BigDecimal total;

    @Column("shipping_address")
    private String shippingAddress;

    @Column("billing_address")
    private String billingAddress;

    @Column("payment_ref")
    private String paymentRef;

    private Map<String, String> attributes;
}
