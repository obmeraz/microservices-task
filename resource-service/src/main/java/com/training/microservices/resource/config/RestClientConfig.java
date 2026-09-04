package com.training.microservices.resource.config;

import com.training.microservices.resource.trace.TraceId;
import com.training.microservices.resource.trace.TraceIdContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    String traceId = TraceIdContext.get();
                    if (traceId != null && !traceId.isBlank()) {
                        request.getHeaders().add(TraceId.HEADER, traceId);
                    }
                    return execution.execute(request, body);
                });
    }

    @Bean
    public RestClient songServiceRestClient(
            RestClient.Builder loadBalancedRestClientBuilder,
            @Value("${song.service.name}") String songServiceName
    ) {
        return loadBalancedRestClientBuilder
                .clone()
                .baseUrl("http://" + songServiceName)
                .build();
    }

    @Bean("storageServiceRestClient")
    public RestClient storageServiceRestClient(
            RestClient.Builder loadBalancedRestClientBuilder,
            @Value("${storage.service.name}") String storageServiceName
    ) {
        return loadBalancedRestClientBuilder
                .clone()
                .baseUrl("http://" + storageServiceName)
                .build();
    }
}
