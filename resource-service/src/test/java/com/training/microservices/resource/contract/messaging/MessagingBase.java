package com.training.microservices.resource.contract.messaging;

import com.training.microservices.resource.messaging.ResourceUploadedPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        classes = MessagingContractTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.stream.default-binder=test",
                "spring.cloud.stream.bindings.resourceUploaded-out-0.destination=resource.uploaded"
        }
)
@AutoConfigureMessageVerifier
@Import({TestChannelBinderConfiguration.class, ResourceUploadedPublisher.class})
public abstract class MessagingBase {

    @Autowired
    private ResourceUploadedPublisher publisher;

    public void publishResourceUploadedEvent() {
        publisher.publish(1L);
    }
}
