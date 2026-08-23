package com.training.microservices.resource.service;

public interface Mp3StorageService {

    void upload(byte[] mp3Data, String bucket, String storageKey);

    byte[] download(String bucket, String storageKey);

    void remove(String bucket, String storageKey);

    void move(String sourceBucket, String sourceKey, String targetBucket, String targetKey);
}
