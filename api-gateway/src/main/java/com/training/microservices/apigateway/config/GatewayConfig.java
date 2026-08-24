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
                ).route(
                        "resource-prometheus", r -> r.path("/actuator/prometheus/resource-service")
                                .filters(f -> f.setPath("/actuator/prometheus"))
                                .uri("lb://" + resourceServiceName)
                ).route(
                        "song-prometheus", r -> r.path("/actuator/prometheus/song-service")
                                .filters(f -> f.setPath("/actuator/prometheus"))
                                .uri("lb://" + songServiceName)
                ).route(
                        "storage-prometheus", r -> r.path("/actuator/prometheus/storage-service")
                                .filters(f -> f.setPath("/actuator/prometheus"))
                                .uri("lb://" + storageServiceName)
                ).route(
                        "processor-prometheus", r -> r.path("/actuator/prometheus/resource-processor")
                                .filters(f -> f.setPath("/actuator/prometheus"))
                                .uri("lb://resource-processor")
                ).build();
    }
}
