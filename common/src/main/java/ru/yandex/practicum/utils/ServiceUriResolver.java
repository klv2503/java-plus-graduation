package ru.yandex.practicum.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
@Slf4j
public class ServiceUriResolver {
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;

    public ServiceUriResolver(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        this.retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(3000L); // 3 секунды между попытками
        this.retryTemplate.setBackOffPolicy(backOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3); // Максимум 3 попытки
        this.retryTemplate.setRetryPolicy(retryPolicy);
    }

    //URI with host and port without path
    public URI getBaseUri(String serviceName) {
        ServiceInstance instance = retryTemplate.execute(context -> {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (instances == null || instances.isEmpty()) {
                log.warn("No instances of {} found, attempt {}", serviceName, context.getRetryCount() + 1);
                throw new IllegalStateException(serviceName + " not found in Eureka");
            }
            return instances.getFirst(); // здесь возможно round-robin / random
        });

        URI uri = instance.getUri();
        log.info("✅Discovered {} URI: {}", serviceName, uri);
        return uri;
    }

    //URI with host, port and path
    public URI makeUri(String serviceName, String path) {
        URI baseUri = getBaseUri(serviceName);
        return URI.create(baseUri.toString() + path);
    }
}
