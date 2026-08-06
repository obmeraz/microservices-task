package com.training.microservices.processor.component;

import com.training.microservices.processor.config.RestClientConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackages = {
                "com.training.microservices.processor.client",
                "com.training.microservices.processor.config",
                "com.training.microservices.processor.consumer",
                "com.training.microservices.processor.service",
                "com.training.microservices.processor.mapper"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RestClientConfig.class
        )
)
public class ComponentTestApplication {
}
