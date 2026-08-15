package com.training.microservices.resource.util;

import java.util.UUID;

public final class StorageKeyGenerator {

    private static final String MP3_EXTENSION = ".mp3";

    private StorageKeyGenerator() {
    }

    public static String generate() {
        return generate(null);
    }

    public static String generate(String path) {
        String fileName = UUID.randomUUID() + MP3_EXTENSION;
        if (path == null || path.isBlank() || "/".equals(path.trim())) {
            return fileName;
        }

        String normalized = path.trim()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
        return normalized.isEmpty() ? fileName : normalized + "/" + fileName;
    }
}
