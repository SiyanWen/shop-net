package org.example.commonsecurity.service;

import jakarta.annotation.PostConstruct;
import org.example.commonsecurity.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceAccountTokenProvider {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    private String serviceToken;

    public ServiceAccountTokenProvider(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostConstruct
    public void init() {
        // Generate a long-lived token (30 days) for service-to-service calls
        long thirtyDaysMs = 30L * 24 * 60 * 60 * 1000;
        serviceToken = jwtTokenProvider.generateToken(serviceName, List.of("ROLE_SERVICE"), thirtyDaysMs);
    }

    public String getServiceToken() {
        return serviceToken;
    }
}
