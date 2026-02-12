package org.example.itemservice.repository;

import org.example.itemservice.entity.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ItemRepository extends MongoRepository<Item, String> {

    Optional<Item> findByItemId(String itemId);
}
