package org.example.cartservice.repository;

import org.example.cartservice.entity.CartEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.math.BigDecimal;

public interface CartRepository extends ListCrudRepository<CartEntity, Long> {

    CartEntity getByUserId(String userId);

    @Modifying
    @Query("UPDATE carts SET total_price = :totalPrice WHERE id = :cartId")
    void updateTotalPrice(Long cartId, BigDecimal totalPrice);
}
