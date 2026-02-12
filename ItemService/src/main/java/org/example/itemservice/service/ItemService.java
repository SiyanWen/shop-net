package org.example.itemservice.service;

import lombok.RequiredArgsConstructor;
import org.example.itemservice.entity.Item;
import org.example.itemservice.exception.ItemNotFoundException;
import org.example.itemservice.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Item getByItemId(String itemId) {
        return itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    public Item updateStock(String itemId, int quantity) {
        Item item = getByItemId(itemId);
        int newQty = item.getStockQty() + quantity;
        if (newQty < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: " + item.getStockQty() + ", requested: " + (-quantity));
        }
        item.setStockQty(newQty);
        item.setUpdatedAt(Instant.now());
        return itemRepository.save(item);
    }
}
