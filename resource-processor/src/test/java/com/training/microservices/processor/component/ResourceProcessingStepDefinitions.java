package com.training.microservices.processor.component;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.training.microservices.processor.component.ComponentTestConfiguration.WIRE_MOCK;

@CucumberContextConfiguration
@SpringBootTest(
        classes = ComponentTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.stream.default-binder=test",
                "spring.cloud.stream.bindings.resourceUploaded-in-0.destination=resource.uploaded",
                "spring.cloud.stream.bindings.resourceUploaded-in-0.group=resource-processor"
        }
)
@Import({TestChannelBinderConfiguration.class, ComponentTestConfiguration.class})
public class ResourceProcessingStepDefinitions {

    @Autowired
    private InputDestination inputDestination;

    @Before
    public void resetWireMock() {
        WireMock.configureFor("localhost", WIRE_MOCK.port());
        WIRE_MOCK.resetAll();
    }

    @After
    public void cleanWireMock() {
        WIRE_MOCK.resetAll();
    }

    @Given("Resource Service holds resource data for id {string}")
    public void resourceServiceHoldsResourceDataForId(String resourceId) {
        WIRE_MOCK.stubFor(get(urlEqualTo("/resources/" + resourceId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "audio/mpeg")
                        .withBody(new byte[]{1, 2, 3, 4})));

        WIRE_MOCK.stubFor(post(urlEqualTo("/songs"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":" + resourceId + "}")));
    }

    @When("a resource uploaded event with id {string} arrives on the queue")
    public void resourceUploadedEventArrivesOnTheQueue(String resourceId) {
        inputDestination.send(
                MessageBuilder.withPayload(Long.valueOf(resourceId)).build(),
                "resource.uploaded"
        );
    }

    @Then("Resource Processor should fetch the resource from Resource Service")
    public void resourceProcessorShouldFetchTheResourceFromResourceService() {
        WIRE_MOCK.verify(getRequestedFor(urlEqualTo("/resources/1")));
    }

    @Then("Resource Processor should store extracted metadata in Song Service")
    public void resourceProcessorShouldStoreExtractedMetadataInSongService() {
        WIRE_MOCK.verify(postRequestedFor(urlEqualTo("/songs"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(containing("\"id\":1"))
                .withRequestBody(containing("\"name\":\"Test Song\""))
                .withRequestBody(containing("\"artist\":\"Test Artist\""))
                .withRequestBody(containing("\"album\":\"Test Album\""))
                .withRequestBody(containing("\"year\":\"2024\"")));
    }
}
