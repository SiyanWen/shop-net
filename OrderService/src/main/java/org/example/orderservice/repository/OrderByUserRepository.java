package org.example.orderservice.repository;

import org.example.orderservice.entity.OrderByUser;
import org.example.orderservice.entity.OrderByUserKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderByUserRepository extends CassandraRepository<OrderByUser, OrderByUserKey> {

    @Query("SELECT * FROM orders_by_user WHERE user_id = ?0")
    List<OrderByUser> findByKeyUserId(UUID userId);
}
