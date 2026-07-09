package com.training.microservices.resource.service.impl;

import com.training.microservices.resource.service.Mp3StorageService;
import com.training.microservices.resource.util.S3ExceptionMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3Mp3StorageService implements Mp3StorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Mp3StorageService(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void upload(byte[] mp3Data, String storageKey) {
        if (mp3Data == null || mp3Data.length == 0) {
            throw new IllegalArgumentException("Byte array cannot be empty");
        }
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("Storage key cannot be empty");
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .contentType("audio/mpeg")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(mp3Data));
        } catch (S3Exception e) {
            throw S3ExceptionMapper.map(e, "upload");
        }
    }

    @Override
    public byte[] download(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("Storage key cannot be empty");
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes =
                    s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());

            return objectBytes.asByteArray();
        } catch (S3Exception e) {
            throw S3ExceptionMapper.map(e, "download");
        }
    }

    @Override
    public void remove(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("Storage key cannot be empty");
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw S3ExceptionMapper.map(e, "delete");
        }
    }
}
