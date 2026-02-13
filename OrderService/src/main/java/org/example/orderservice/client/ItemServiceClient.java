package org.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ItemService")
public interface ItemServiceClient {

    @PatchMapping("/api/items/{itemId}/inventory")
    ResponseEntity<Object> updateInventory(@PathVariable String itemId,
                                           @RequestBody Map<String, Integer> request);
}
