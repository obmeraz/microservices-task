package com.training.microservices.resource.service;

import com.training.microservices.resource.exception.BadRequestException;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mp3Validator Unit Tests")
class Mp3ValidatorTest {

    @Mock
    private Tika tika;

    private Mp3Validator mp3Validator;

    @BeforeEach
    void setUp() {
        mp3Validator = new Mp3Validator(tika);
    }

    @Test
    void test_ValidateWhenNull_ReturnException() {
        // given

        // when & then
        assertThatThrownBy(() -> mp3Validator.validate(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid MP3");
    }

    @Test
    void test_ValidateWhenEmpty_ReturnException() {
        // given
        byte[] mp3data = new byte[]{};

        // when & then
        assertThatThrownBy(() -> mp3Validator.validate(mp3data))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid MP3");
    }

    @Test
    void test_ValidateWhenMediaTypeIsNull_ReturnException() {
        // given
        byte[] mp3data = new byte[]{1, 2, 3};
        when(tika.detect(mp3data)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> mp3Validator.validate(mp3data))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid MP3");
    }

    @Test
    void test_ValidateWhenMediaTypeIsNotMp3_ReturnException() {
        // given
        byte[] mp3data = new byte[]{1, 2, 3};
        when(tika.detect(mp3data)).thenReturn("text/plain");

        // when & then
        assertThatThrownBy(() -> mp3Validator.validate(mp3data))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid MP3");
    }

    @Test
    void test_ValidateWhenTikaThrows_ReturnException() {
        // given
        byte[] mp3data = new byte[]{1, 2, 3};
        when(tika.detect(any(byte[].class))).thenThrow(new RuntimeException("tika failed"));

        // when & then
        assertThatThrownBy(() -> mp3Validator.validate(mp3data))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid MP3");
    }

    @Test
    void test_ValidateWhenMediaTypeContainsMpeg_DoesNotThrow() {
        // given
        byte[] mp3data = new byte[]{1, 2, 3};
        when(tika.detect(mp3data)).thenReturn("audio/mpeg");

        // when & then
        assertThatCode(() -> mp3Validator.validate(mp3data))
                .doesNotThrowAnyException();
    }

    @Test
    void test_ValidateWhenMediaTypeContainsMp3_DoesNotThrow() {
        // given
        byte[] mp3data = new byte[]{1, 2, 3};
        when(tika.detect(mp3data)).thenReturn("audio/mp3");

        // when & then
        assertThatCode(() -> mp3Validator.validate(mp3data))
                .doesNotThrowAnyException();
    }
}
