package org.example.itemservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.itemservice.dto.InventoryUpdateRequest;
import org.example.itemservice.entity.Item;
import org.example.itemservice.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ResponseEntity<Item> getItem(@PathVariable String itemId) {
        return ResponseEntity.ok(itemService.getByItemId(itemId));
    }

    @PatchMapping("/{itemId}/inventory")
    public ResponseEntity<Item> updateInventory(@PathVariable String itemId,
                                                @Valid @RequestBody InventoryUpdateRequest request) {
        return ResponseEntity.ok(itemService.updateStock(itemId, request.getQuantity()));
    }
}
