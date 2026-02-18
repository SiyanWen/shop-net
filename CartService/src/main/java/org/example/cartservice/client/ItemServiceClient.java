package org.example.cartservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "ItemService", fallbackFactory = ItemServiceClientFallbackFactory.class)
public interface ItemServiceClient {

    @GetMapping("/api/items/{itemId}")
    Map<String, Object> getItem(@PathVariable String itemId);
}
