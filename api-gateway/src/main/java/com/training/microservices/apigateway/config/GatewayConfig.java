package com.training.microservices.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${song.service.name}")
    private String songServiceName;

    @Value("${resource.service.name}")
    private String resourceServiceName;

    @Value("${storage.service.name}")
    private String storageServiceName;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(
                        "resources", r -> r.path("/resources/**")
                                .uri("lb://" + resourceServiceName)
                ).route(
                        "songs", r -> r.path("/songs/**")
                                .uri("lb://" + songServiceName)
                ).route(
                        "storage", r -> r.path("/storages/**")
                                .uri("lb://" + storageServiceName)
                ).build();

    }
}
