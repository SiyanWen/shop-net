package org.example.cartservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ItemServiceClientFallbackFactory implements FallbackFactory<ItemServiceClient> {

    @Override
    public ItemServiceClient create(Throwable cause) {
        return new ItemServiceClient() {
            @Override
            public Map<String, Object> getItem(String itemId) {
                log.error("Fallback: failed to get item {}: {}", itemId, cause.getMessage());
                throw new RuntimeException("ItemService unavailable: " + cause.getMessage(), cause);
            }
        };
    }
}
