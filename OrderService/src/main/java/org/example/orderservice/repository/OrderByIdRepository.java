package org.example.orderservice.repository;

import org.example.orderservice.entity.OrderById;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface OrderByIdRepository extends CassandraRepository<OrderById, UUID> {
}
