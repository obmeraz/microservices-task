package com.training.microservices.resource.util;

import java.util.UUID;

public final class StorageKeyGenerator {

    private static final String MP3_EXTENSION = ".mp3";

    private StorageKeyGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID() + MP3_EXTENSION;
    }
}
