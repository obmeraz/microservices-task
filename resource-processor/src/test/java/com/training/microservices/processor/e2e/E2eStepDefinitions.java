package com.training.microservices.processor.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assumptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@CucumberContextConfiguration
@ContextConfiguration(classes = E2eStepDefinitions.EmptyConfig.class)
public class E2eStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static final String RESOURCE_SERVICE_BASE_URL = "http://127.0.0.1:8080";
    private static final String SONG_SERVICE_BASE_URL = "http://127.0.0.1:8081";

    private Long uploadedResourceId;

    @Given("the microservices stack is available")
    public void theMicroservicesStackIsAvailable() {
        Assumptions.assumeTrue(
                isReachable(RESOURCE_SERVICE_BASE_URL + "/resources/0"),
                () -> "Resource Service is not reachable at " + RESOURCE_SERVICE_BASE_URL
                        + ". Start the stack with: docker-compose up --build"
        );
        Assumptions.assumeTrue(
                isReachable(SONG_SERVICE_BASE_URL + "/songs/0"),
                () -> "Song Service is not reachable at " + SONG_SERVICE_BASE_URL
                        + ". Start the stack with: docker-compose up --build"
        );
    }

    @When("I upload a valid MP3 file to Resource Service")
    public void iUploadAValidMp3FileToResourceService() throws Exception {
        byte[] mp3 = readClasspathBytes("e2e/valid-sample.mp3");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESOURCE_SERVICE_BASE_URL + "/resources"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "audio/mpeg")
                .POST(HttpRequest.BodyPublishers.ofByteArray(mp3))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
                .as("Resource upload response: %s", response.body())
                .isEqualTo(200);

        uploadedResourceId = MAPPER.readTree(response.body()).path("id").asLong();
        assertThat(uploadedResourceId).isPositive();
    }

    @Then("Song Service eventually returns metadata for the uploaded resource")
    public void songServiceEventuallyReturnsMetadataForTheUploadedResource() {
        await()
                .atMost(Duration.ofSeconds(90))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(SONG_SERVICE_BASE_URL + "/songs/" + uploadedResourceId))
                            .timeout(Duration.ofSeconds(5))
                            .GET()
                            .build();

                    HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
                    assertThat(response.statusCode())
                            .as("Song GET response: %s", response.body())
                            .isEqualTo(200);

                    JsonNode song = MAPPER.readTree(response.body());
                    assertThat(song.path("id").asLong()).isEqualTo(uploadedResourceId);
                    assertThat(song.path("name").asText()).isNotBlank();
                    assertThat(song.path("artist").asText()).isNotBlank();
                });
    }

    private static boolean isReachable(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HTTP.send(request, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static byte[] readClasspathBytes(String classpathLocation) throws Exception {
        var resource = E2eStepDefinitions.class.getClassLoader().getResource(classpathLocation);
        assertThat(resource).as("Missing classpath resource %s", classpathLocation).isNotNull();
        return Files.readAllBytes(Path.of(resource.toURI()));
    }

    @Configuration
    static class EmptyConfig {
    }
}
