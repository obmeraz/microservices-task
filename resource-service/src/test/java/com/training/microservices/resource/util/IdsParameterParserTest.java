package com.training.microservices.resource.util;

import com.training.microservices.resource.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdsParameterParser Unit Tests")
class IdsParameterParserTest {

    @Test
    void test_ParseWhenNull_ReturnException() {
        // given

        // when & then
        assertThatThrownBy(() -> IdsParameterParser.parse(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid ID parameter");
    }

    @Test
    void test_ParseWhenBlank_ReturnException() {
        // given
        String idsParameter = "   ";

        // when & then
        assertThatThrownBy(() -> IdsParameterParser.parse(idsParameter))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid ID parameter");
    }

    @Test
    void test_ParseWhenTooLong_ReturnException() {
        // given
        String idsParameter = "1".repeat(201);

        // when & then
        assertThatThrownBy(() -> IdsParameterParser.parse(idsParameter))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(IdErrorMessages.csvTooLong(201, 200));
    }

    @Test
    void test_ParseWhenEmptyToken_ReturnException() {
        // given
        String idsParameter = "1,,2";

        // when & then
        assertThatThrownBy(() -> IdsParameterParser.parse(idsParameter))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(IdErrorMessages.invalidCsvToken(""));
    }

    @Test
    void test_ParseWhenNotNumber_ReturnException() {
        // given
        String idsParameter = "1,abc";

        // when & then
        assertThatThrownBy(() -> IdsParameterParser.parse(idsParameter))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(IdErrorMessages.invalidCsvToken("abc"));
    }

    @Test
    void test_ParseWhenNonPositiveId_ReturnException() {
        // given
        String idsParameter = "1,0";

        // when & then
        assertThatThrownBy(() -> IdsParameterParser.parse(idsParameter))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(IdErrorMessages.invalidCsvToken("0"));
    }

    @Test
    void test_ParseWhenValidIds_ReturnList() {
        // given
        String idsParameter = "1, 2,3";

        // when
        List<Long> ids = IdsParameterParser.parse(idsParameter);

        // then
        assertThat(ids).containsExactly(1L, 2L, 3L);
    }
}
