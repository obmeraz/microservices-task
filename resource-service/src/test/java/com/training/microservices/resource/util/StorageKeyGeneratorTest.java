package com.training.microservices.resource.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("StorageKeyGenerator Unit Tests")
class StorageKeyGeneratorTest {

    @Test
    void test_Generate_ReturnKeyWithMp3Extension() {
        // given

        // when
        String storageKey = StorageKeyGenerator.generate();

        // then
        assertThat(storageKey).endsWith(".mp3");
    }

    @Test
    void test_Generate_ReturnUniqueKeys() {
        // given

        // when
        String firstKey = StorageKeyGenerator.generate();
        String secondKey = StorageKeyGenerator.generate();

        // then
        assertThat(firstKey).isNotEqualTo(secondKey);
    }

    @Test
    void test_Generate_ReturnUuidPrefix() {
        // given

        // when
        String storageKey = StorageKeyGenerator.generate();
        String uuidPart = storageKey.substring(0, storageKey.length() - ".mp3".length());

        // then
        assertThat(uuidPart).matches(
                "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
        );
    }
}
