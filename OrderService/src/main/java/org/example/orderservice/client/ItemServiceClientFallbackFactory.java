package org.example.orderservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ItemServiceClientFallbackFactory implements FallbackFactory<ItemServiceClient> {

    @Override
    public ItemServiceClient create(Throwable cause) {
        return new ItemServiceClient() {
            @Override
            public ResponseEntity<Object> updateInventory(String itemId, Map<String, Integer> request) {
                log.error("Fallback: failed to update inventory for item {}: {}", itemId, cause.getMessage());
                throw new RuntimeException("ItemService unavailable: " + cause.getMessage(), cause);
            }
        };
    }
}
