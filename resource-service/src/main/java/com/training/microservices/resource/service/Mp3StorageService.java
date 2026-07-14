package com.training.microservices.resource.service;

public interface Mp3StorageService {

    void upload(byte[] mp3Data, String storageKey);

    byte[] download(String storageKey);

    void remove(String storageKey);
}
