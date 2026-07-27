package com.training.microservices.resource.service.impl;

import com.training.microservices.resource.config.S3Config;
import com.training.microservices.resource.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@SpringBootTest(classes = {S3Config.class, S3Mp3StorageService.class})
@Testcontainers
@DisplayName("S3 Local stack integration test with test container")
class S3Mp3StorageServiceTest {

    private static final String BUCKET_NAME = "resource-service-mp3";

    @Container
    static LocalStackContainer localStackContainer = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.4.0")
    ).withServices(S3);

    @DynamicPropertySource
    static void registerAwsProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.s3.endpoint", () -> localStackContainer.getEndpointOverride(S3).toString());
        registry.add("aws.s3.region", localStackContainer::getRegion);
        registry.add("aws.s3.access-key", localStackContainer::getAccessKey);
        registry.add("aws.s3.secret-key", localStackContainer::getSecretKey);
        registry.add("aws.s3.bucket-name", () -> BUCKET_NAME);
    }

    @Autowired
    private S3Mp3StorageService s3Mp3StorageService;

    @Autowired
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        ensureBucketExists();
    }

    @Test
    void test_UploadAndDownload_ReturnSameBytes() {
        // given
        byte[] mp3Data = new byte[]{1, 2, 3, 4, 5};
        String storageKey = "integration-upload-download.mp3";

        // when
        s3Mp3StorageService.upload(mp3Data, storageKey);
        byte[] downloaded = s3Mp3StorageService.download(storageKey);

        // then
        assertThat(downloaded).isEqualTo(mp3Data);
    }

    @Test
    void test_Remove_DeletesObject() {
        // given
        byte[] mp3Data = new byte[]{9, 8, 7};
        String storageKey = "integration-remove.mp3";
        s3Mp3StorageService.upload(mp3Data, storageKey);

        // when
        s3Mp3StorageService.remove(storageKey);

        // then
        assertThatThrownBy(() -> s3Mp3StorageService.download(storageKey))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource file not found in storage");
    }

    @Test
    void test_DownloadWhenKeyMissing_ReturnResourceNotFoundException() {
        // given
        String storageKey = "missing-key.mp3";

        // when & then
        assertThatThrownBy(() -> s3Mp3StorageService.download(storageKey))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource file not found in storage");
    }

    @Test
    void test_UploadWhenDataEmpty_ReturnException() {
        // given
        byte[] mp3Data = new byte[]{};
        String storageKey = "empty.mp3";

        // when & then
        assertThatThrownBy(() -> s3Mp3StorageService.upload(mp3Data, storageKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Byte array cannot be empty");
    }

    @Test
    void test_UploadWhenStorageKeyBlank_ReturnException() {
        // given
        byte[] mp3Data = new byte[]{1, 2, 3};
        String storageKey = "  ";

        // when & then
        assertThatThrownBy(() -> s3Mp3StorageService.upload(mp3Data, storageKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Storage key cannot be empty");
    }

    @Test
    void test_DownloadWhenStorageKeyBlank_ReturnException() {
        // given
        String storageKey = "";

        // when & then
        assertThatThrownBy(() -> s3Mp3StorageService.download(storageKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Storage key cannot be empty");
    }

    @Test
    void test_RemoveWhenStorageKeyBlank_ReturnException() {
        // given
        String storageKey = null;

        // when & then
        assertThatThrownBy(() -> s3Mp3StorageService.remove(storageKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Storage key cannot be empty");
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(BUCKET_NAME).build());
        } catch (NoSuchBucketException ex) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
        }
    }
}
