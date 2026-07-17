package com.training.microservices.processor.config;

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
        return RestClient.builder();
    }

    @Bean("songServiceRestClient")
    public RestClient songServiceRestClient(
            RestClient.Builder loadBalancedRestClientBuilder,
            @Value("${song.service.name}") String songServiceName
    ) {
        return loadBalancedRestClientBuilder
                .clone()
                .baseUrl("http://" + songServiceName)
                .build();
    }

    @Bean("resourceServiceRestClient")
    public RestClient resourceServiceRestClient(
            RestClient.Builder loadBalancedRestClientBuilder,
            @Value("${resource.service.name}") String resourceServiceName
    ) {
        return loadBalancedRestClientBuilder
                .clone()
                .baseUrl("http://" + resourceServiceName)
                .build();
    }
}
