package com.training.microservices.processor.component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.training.microservices.processor.service.Mp3MetadataExtractor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@TestConfiguration
public class ComponentTestConfiguration {

    static final WireMockServer WIRE_MOCK = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        WIRE_MOCK.start();
    }

    @Bean
    @Primary
    Mp3MetadataExtractor mp3MetadataExtractor() {
        return new Mp3MetadataExtractor() {
            @Override
            public ExtractedMetadata extract(byte[] mp3Data) {
                return new ExtractedMetadata(
                        "Test Song",
                        "Test Artist",
                        "Test Album",
                        "210.0",
                        "2024"
                );
            }
        };
    }

    @Bean(name = "resourceServiceRestClient")
    RestClient resourceServiceRestClient() {
        return RestClient.builder()
                .baseUrl(WIRE_MOCK.baseUrl())
                .build();
    }

    @Bean(name = "songServiceRestClient")
    RestClient songServiceRestClient() {
        return RestClient.builder()
                .baseUrl(WIRE_MOCK.baseUrl())
                .build();
    }
}
