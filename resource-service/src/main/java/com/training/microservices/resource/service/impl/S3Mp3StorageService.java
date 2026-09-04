package com.training.microservices.resource.service.impl;

import com.training.microservices.resource.service.Mp3StorageService;
import com.training.microservices.resource.util.S3ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3Mp3StorageService implements Mp3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3Mp3StorageService.class);

    private final S3Client s3Client;

    public S3Mp3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void upload(byte[] mp3Data, String bucket, String storageKey) {
        validatePayload(mp3Data);
        validateBucket(bucket);
        validateStorageKey(storageKey);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .contentType("audio/mpeg")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(mp3Data));
            log.debug("S3 upload completed: bucket={}, key={}, size={}", bucket, storageKey, mp3Data.length);
        } catch (S3Exception e) {
            log.error("S3 upload failed: bucket={}, key={}", bucket, storageKey, e);
            throw S3ExceptionMapper.map(e, "upload");
        }
    }

    @Override
    public byte[] download(String bucket, String storageKey) {
        validateBucket(bucket);
        validateStorageKey(storageKey);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes =
                    s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());

            byte[] data = objectBytes.asByteArray();
            log.debug("S3 download completed: bucket={}, key={}, size={}", bucket, storageKey, data.length);
            return data;
        } catch (S3Exception e) {
            log.error("S3 download failed: bucket={}, key={}", bucket, storageKey, e);
            throw S3ExceptionMapper.map(e, "download");
        }
    }

    @Override
    public void remove(String bucket, String storageKey) {
        validateBucket(bucket);
        validateStorageKey(storageKey);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.debug("S3 delete completed: bucket={}, key={}", bucket, storageKey);
        } catch (S3Exception e) {
            log.error("S3 delete failed: bucket={}, key={}", bucket, storageKey, e);
            throw S3ExceptionMapper.map(e, "delete");
        }
    }

    @Override
    public void move(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        validateBucket(sourceBucket);
        validateBucket(targetBucket);
        validateStorageKey(sourceKey);
        validateStorageKey(targetKey);

        try {
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(targetBucket)
                    .destinationKey(targetKey)
                    .build();
            s3Client.copyObject(copyObjectRequest);
            remove(sourceBucket, sourceKey);
            log.debug("S3 move completed: from={}/{} to={}/{}",
                    sourceBucket, sourceKey, targetBucket, targetKey);
        } catch (S3Exception e) {
            log.error("S3 move failed: from={}/{} to={}/{}",
                    sourceBucket, sourceKey, targetBucket, targetKey, e);
            throw S3ExceptionMapper.map(e, "move");
        }
    }

    private static void validatePayload(byte[] mp3Data) {
        if (mp3Data == null || mp3Data.length == 0) {
            throw new IllegalArgumentException("Byte array cannot be empty");
        }
    }

    private static void validateBucket(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("Bucket cannot be empty");
        }
    }

    private static void validateStorageKey(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("Storage key cannot be empty");
        }
    }
}
