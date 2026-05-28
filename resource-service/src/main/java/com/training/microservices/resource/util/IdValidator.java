package com.training.microservices.resource.util;

import com.training.microservices.resource.exception.BadRequestException;

public final class IdValidator {

    private IdValidator() {
    }

    public static void validatePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException(IdErrorMessages.invalidPathId(id));
        }
    }
}
