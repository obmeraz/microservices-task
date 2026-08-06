package com.training.microservices.processor.contract;

import com.training.microservices.processor.client.ResourceServiceClient;
import com.training.microservices.processor.config.RetryConfig;
import com.training.microservices.processor.consumer.ResourceUploadedConsumer;
import com.training.microservices.processor.service.ResourceProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.StubTrigger;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = {
                ResourceServiceContractTest.ContractTestApplication.class,
                ResourceServiceClient.class,
                RetryConfig.class,
                ResourceServiceContractTest.ClientConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.stream.default-binder=test",
                "spring.cloud.stream.bindings.resourceUploaded-in-0.destination=resource.uploaded",
                "spring.cloud.stream.bindings.resourceUploaded-in-0.group=resource-processor"
        }
)
@AutoConfigureStubRunner(
        ids = "com.training.microservices:resource-service:1.0.0-SNAPSHOT:stubs",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({TestChannelBinderConfiguration.class, ResourceUploadedConsumer.class})
@DisplayName("Consumer contracts: Resource Service stubs")
class ResourceServiceContractTest {

    @MockBean
    private ResourceProcessingService resourceProcessingService;

    @Autowired
    private ResourceServiceClient resourceServiceClient;

    @Autowired
    private StubTrigger stubTrigger;

    @Test
    void http_GetResource_UsesProducerStub() {
        assertThat(resourceServiceClient.getResourceData(1L))
                .isEqualTo(new byte[]{1, 2, 3, 4});
    }

    @Test
    void messaging_ResourceUploaded_UsesProducerStub() {
        stubTrigger.trigger("resource_uploaded");
        verify(resourceProcessingService, timeout(2000)).process(1L);
    }

    @SpringBootApplication
    static class ContractTestApplication {
    }

    @TestConfiguration
    static class ClientConfig {

        @Bean(name = "resourceServiceRestClient")
        RestClient resourceServiceRestClient(StubFinder stubFinder) {
            return RestClient.builder()
                    .baseUrl(stubFinder.findStubUrl("resource-service").toString())
                    .build();
        }
    }
}
