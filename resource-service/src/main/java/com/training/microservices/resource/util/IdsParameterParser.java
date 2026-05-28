package com.training.microservices.resource.util;

import com.training.microservices.resource.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public final class IdsParameterParser {

    private static final int MAX_LENGTH = 200;

    private IdsParameterParser() {
    }

    public static List<Long> parse(String idsParameter) {
        if (idsParameter == null || idsParameter.isBlank()) {
            throw new BadRequestException("Invalid ID parameter");
        }
        if (idsParameter.length() > MAX_LENGTH) {
            throw new BadRequestException(IdErrorMessages.csvTooLong(idsParameter.length(), MAX_LENGTH));
        }

        String[] parts = idsParameter.split(",");
        List<Long> ids = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                throw new BadRequestException(IdErrorMessages.invalidCsvToken(trimmed));
            }
            try {
                long id = Long.parseLong(trimmed);
                if (id <= 0) {
                    throw new BadRequestException(IdErrorMessages.invalidCsvToken(trimmed));
                }
                ids.add(id);
            } catch (NumberFormatException ex) {
                throw new BadRequestException(IdErrorMessages.invalidCsvToken(trimmed));
            }
        }
        return ids;
    }
}
