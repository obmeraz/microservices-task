package com.training.microservices.resource.util;

import com.training.microservices.resource.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentTypeValidator Unit Tests")
class ContentTypeValidatorTest {

    @Test
    void test_ValidateMp3ContentTypeWhenNull_ReturnException() {
        // given

        // when & then
        assertThatThrownBy(() -> ContentTypeValidator.validateMp3ContentType(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid file format: unknown. Only MP3 files are allowed");

    }

    @Test
    void test_ValidateMp3ContentTypeWhenContainsWrongMediaType_ReturnException() {
        // given
        String contentType = "text";

        // when & then
        assertThatThrownBy(() -> ContentTypeValidator.validateMp3ContentType(contentType))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid file format: text. Only MP3 files are allowed");

    }

    @Test
    void test_ValidateMp3ContentTypeWhenContainsCorrectMediaType_ReturnNothing() {
        // given
        String contentType = "audio/mpeg";

        // when & then
        assertThatCode(() -> ContentTypeValidator.validateMp3ContentType(contentType))
                .doesNotThrowAnyException();

    }

}