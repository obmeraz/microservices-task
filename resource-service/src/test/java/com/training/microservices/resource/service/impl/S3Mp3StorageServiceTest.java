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

    private static final String BUCKET_NAME = "staging-storage";

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
    }

    @Autowired
    private S3Mp3StorageService s3Mp3StorageService;

    @Autowired
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        ensureBucketExists(BUCKET_NAME);
    }

    @Test
    void test_UploadAndDownload_ReturnSameBytes() {
        byte[] mp3Data = new byte[]{1, 2, 3, 4, 5};
        String storageKey = "files/integration-upload-download.mp3";

        s3Mp3StorageService.upload(mp3Data, BUCKET_NAME, storageKey);
        byte[] downloaded = s3Mp3StorageService.download(BUCKET_NAME, storageKey);

        assertThat(downloaded).isEqualTo(mp3Data);
    }

    @Test
    void test_Remove_DeletesObject() {
        byte[] mp3Data = new byte[]{9, 8, 7};
        String storageKey = "files/integration-remove.mp3";
        s3Mp3StorageService.upload(mp3Data, BUCKET_NAME, storageKey);

        s3Mp3StorageService.remove(BUCKET_NAME, storageKey);

        assertThatThrownBy(() -> s3Mp3StorageService.download(BUCKET_NAME, storageKey))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource file not found in storage");
    }

    @Test
    void test_Move_CopiesToTargetAndRemovesSource() {
        String permanentBucket = "permanent-storage";
        ensureBucketExists(permanentBucket);

        byte[] mp3Data = new byte[]{1, 2, 3};
        String sourceKey = "files/source.mp3";
        String targetKey = "files/target.mp3";
        s3Mp3StorageService.upload(mp3Data, BUCKET_NAME, sourceKey);

        s3Mp3StorageService.move(BUCKET_NAME, sourceKey, permanentBucket, targetKey);

        assertThat(s3Mp3StorageService.download(permanentBucket, targetKey)).isEqualTo(mp3Data);
        assertThatThrownBy(() -> s3Mp3StorageService.download(BUCKET_NAME, sourceKey))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void test_DownloadWhenKeyMissing_ReturnResourceNotFoundException() {
        String storageKey = "missing-key.mp3";

        assertThatThrownBy(() -> s3Mp3StorageService.download(BUCKET_NAME, storageKey))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource file not found in storage");
    }

    @Test
    void test_UploadWhenDataEmpty_ReturnException() {
        byte[] mp3Data = new byte[]{};
        String storageKey = "empty.mp3";

        assertThatThrownBy(() -> s3Mp3StorageService.upload(mp3Data, BUCKET_NAME, storageKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Byte array cannot be empty");
    }

    @Test
    void test_UploadWhenStorageKeyBlank_ReturnException() {
        byte[] mp3Data = new byte[]{1, 2, 3};
        String storageKey = "  ";

        assertThatThrownBy(() -> s3Mp3StorageService.upload(mp3Data, BUCKET_NAME, storageKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Storage key cannot be empty");
    }

    @Test
    void test_UploadWhenBucketBlank_ReturnException() {
        byte[] mp3Data = new byte[]{1, 2, 3};
        String storageKey = "file.mp3";

        assertThatThrownBy(() -> s3Mp3StorageService.upload(mp3Data, " ", storageKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bucket cannot be empty");
    }

    @Test
    void test_DownloadWhenStorageKeyBlank_ReturnException() {
        String storageKey = "";

        assertThatThrownBy(() -> s3Mp3StorageService.download(BUCKET_NAME, storageKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Storage key cannot be empty");
    }

    @Test
    void test_RemoveWhenStorageKeyBlank_ReturnException() {
        String storageKey = null;

        assertThatThrownBy(() -> s3Mp3StorageService.remove(BUCKET_NAME, storageKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Storage key cannot be empty");
    }

    private void ensureBucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException ex) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }
}
