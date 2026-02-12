package org.example.itemservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class Item {

    @Id
    private String id;

    @Indexed(unique = true)
    private String itemId;

    @TextIndexed
    private String name;

    private BigDecimal unitPrice;
    private List<String> pictureUrls;

    @Indexed(unique = true, sparse = true)
    private String upc;

    private int stockQty;

    private Instant createdAt;
    private Instant updatedAt;
}
