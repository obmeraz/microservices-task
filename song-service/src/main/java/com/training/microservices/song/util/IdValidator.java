package com.training.microservices.song.util;

import com.training.microservices.song.exception.BadRequestException;

public final class IdValidator {

    private IdValidator() {
    }

    public static void validatePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException(IdErrorMessages.invalidPathId(id));
        }
    }
}
