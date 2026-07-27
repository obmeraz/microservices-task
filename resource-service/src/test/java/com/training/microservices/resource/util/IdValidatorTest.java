package com.training.microservices.resource.util;

import com.training.microservices.resource.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdValidator Unit Tests")
class IdValidatorTest {

    @Test
    void test_ValidatePositiveIdWhenNull_ReturnException() {
        // given

        // when & then
        assertThatThrownBy(() -> IdValidator.validatePositiveId(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(IdErrorMessages.invalidPathId(null));
    }

    @Test
    void test_ValidatePositiveIdWhenZero_ReturnException() {
        // given
        Long id = 0L;

        // when & then
        assertThatThrownBy(() -> IdValidator.validatePositiveId(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(IdErrorMessages.invalidPathId(id));
    }

    @Test
    void test_ValidatePositiveIdWhenNegative_ReturnException() {
        // given
        Long id = -5L;

        // when & then
        assertThatThrownBy(() -> IdValidator.validatePositiveId(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(IdErrorMessages.invalidPathId(id));
    }

    @Test
    void test_ValidatePositiveIdWhenPositive_ReturnNothing() {
        // given
        Long id = 1L;

        // when & then
        assertThatCode(() -> IdValidator.validatePositiveId(id))
                .doesNotThrowAnyException();
    }
}
