package org.example.cartservice.repository;

import org.example.cartservice.entity.OrderItemEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface OrderItemRepository extends ListCrudRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> getAllByCartId(Long cartId);

    OrderItemEntity findByCartIdAndItemId(Long cartId, String itemId);

    @Modifying
    @Query("DELETE FROM order_items WHERE cart_id = :cartId")
    void deleteByCartId(Long cartId);
}
