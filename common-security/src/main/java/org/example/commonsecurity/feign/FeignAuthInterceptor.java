package org.example.commonsecurity.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.example.commonsecurity.service.ServiceAccountTokenProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(RequestInterceptor.class)
public class FeignAuthInterceptor implements RequestInterceptor {

    private final ServiceAccountTokenProvider serviceAccountTokenProvider;

    public FeignAuthInterceptor(ServiceAccountTokenProvider serviceAccountTokenProvider) {
        this.serviceAccountTokenProvider = serviceAccountTokenProvider;
    }

    @Override
    public void apply(RequestTemplate template) {
        // Always use the service account token for service-to-service calls.
        // Internal operations (e.g. inventory updates) require ROLE_SERVICE,
        // which a user's JWT does not carry.
        template.header("Authorization", "Bearer " + serviceAccountTokenProvider.getServiceToken());
    }
}
