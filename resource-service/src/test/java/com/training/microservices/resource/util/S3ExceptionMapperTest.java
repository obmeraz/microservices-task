package com.training.microservices.resource.util;

import com.training.microservices.resource.exception.ApiException;
import com.training.microservices.resource.exception.ResourceNotFoundException;
import com.training.microservices.resource.exception.StorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3ExceptionMapper Unit Tests")
class S3ExceptionMapperTest {

    @Test
    void test_MapWhenNoSuchKey_ReturnResourceNotFoundException() {
        // given
        S3Exception exception = s3Exception("NoSuchKey", "The specified key does not exist");

        // when
        ApiException result = S3ExceptionMapper.map(exception, "download");

        // then
        assertThat(result)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource file not found in storage");
    }

    @Test
    void test_MapWhenOtherError_ReturnStorageException() {
        // given
        S3Exception exception = s3Exception("AccessDenied", "Access Denied");

        // when
        ApiException result = S3ExceptionMapper.map(exception, "upload");

        // then
        assertThat(result)
                .isInstanceOf(StorageException.class)
                .hasMessage("Failed to upload MP3 in storage: Access Denied");
    }

    private static S3Exception s3Exception(String errorCode, String errorMessage) {
        return (S3Exception) S3Exception.builder()
                .statusCode(400)
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())
                .build();
    }
}
