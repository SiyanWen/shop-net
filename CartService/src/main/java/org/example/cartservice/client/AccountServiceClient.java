package org.example.cartservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "AccountService", fallbackFactory = AccountServiceClientFallbackFactory.class)
public interface AccountServiceClient {

    @GetMapping("/api/v1/auth/jwt/user")
    Map<String, Object> getUserByUsername(@RequestParam String username);
}
