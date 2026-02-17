package org.example.cartservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provides a SecurityContext-propagating ExecutorService so that
 * Resilience4j circuit-breaker threads inherit the caller's
 * SecurityContext (which now carries the JWT in Authentication.credentials).
 */
@Configuration
public class FeignCircuitBreakerConfig {

    @Bean
    public ExecutorService circuitBreakerExecutorService() {
        return new DelegatingSecurityContextExecutorService(
                Executors.newCachedThreadPool());
    }
}
