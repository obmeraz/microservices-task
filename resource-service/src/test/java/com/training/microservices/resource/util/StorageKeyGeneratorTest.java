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
        String storageKey = StorageKeyGenerator.generate();

        assertThat(storageKey).endsWith(".mp3");
    }

    @Test
    void test_Generate_ReturnUniqueKeys() {
        String firstKey = StorageKeyGenerator.generate();
        String secondKey = StorageKeyGenerator.generate();

        assertThat(firstKey).isNotEqualTo(secondKey);
    }

    @Test
    void test_Generate_ReturnUuidPrefix() {
        String storageKey = StorageKeyGenerator.generate();
        String uuidPart = storageKey.substring(0, storageKey.length() - ".mp3".length());

        assertThat(uuidPart).matches(
                "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
        );
    }

    @Test
    void test_GenerateWithPath_PrefixesNormalizedPath() {
        String storageKey = StorageKeyGenerator.generate("/files");

        assertThat(storageKey).startsWith("files/");
        assertThat(storageKey).endsWith(".mp3");
    }
}
