package com.training.microservices.resource.util;

import com.training.microservices.resource.exception.BadRequestException;

public final class ContentTypeValidator {

    private static final String MP3_MEDIA_TYPE = "audio/mpeg";

    private ContentTypeValidator() {
    }

    public static void validateMp3ContentType(String contentType) {
        if (contentType == null || !contentType.toLowerCase().contains(MP3_MEDIA_TYPE)) {
            String mimeType = extractMimeType(contentType);
            throw new BadRequestException(
                    "Invalid file format: " + mimeType + ". Only MP3 files are allowed"
            );
        }
    }

    private static String extractMimeType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "unknown";
        }
        return contentType.split(";")[0].trim();
    }
}
