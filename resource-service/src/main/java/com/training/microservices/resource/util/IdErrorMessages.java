package com.training.microservices.resource.util;

public final class IdErrorMessages {

    private IdErrorMessages() {
    }

    public static String invalidPathId(Object value) {
        return String.format("Invalid value '%s' for ID. Must be a positive integer", value);
    }

    public static String invalidCsvToken(String token) {
        return String.format("Invalid ID format: '%s'. Only positive integers are allowed", token);
    }

    public static String csvTooLong(int received, int maximum) {
        return String.format(
                "CSV string is too long: received %d characters, maximum allowed is %d",
                received,
                maximum
        );
    }
}
