package org.example.cartservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AccountServiceClientFallbackFactory implements FallbackFactory<AccountServiceClient> {

    @Override
    public AccountServiceClient create(Throwable cause) {
        return new AccountServiceClient() {
            @Override
            public Map<String, Object> getUserByUsername(String username) {
                log.error("Fallback: failed to get user {}: {}", username, cause.getMessage());
                throw new RuntimeException("AccountService unavailable: " + cause.getMessage(), cause);
            }
        };
    }
}
