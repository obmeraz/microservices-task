package com.training.microservices.resource.messaging;

import com.training.microservices.resource.exception.MessagePublishException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResourceUploadedPublisher Unit Tests")
class ResourceUploadedPublisherTest {

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private ResourceUploadedPublisher resourceUploadedPublisher;

    @Test
    void test_PublishWhenSendSucceeds_DoesNotThrow() {
        // given
        Long resourceId = 1L;
        when(streamBridge.send("resourceUploaded-out-0", resourceId)).thenReturn(true);

        // when & then
        assertThatCode(() -> resourceUploadedPublisher.publish(resourceId))
                .doesNotThrowAnyException();
        verify(streamBridge).send("resourceUploaded-out-0", resourceId);
    }

    @Test
    void test_PublishWhenSendReturnsFalse_ReturnException() {
        // given
        Long resourceId = 1L;
        when(streamBridge.send("resourceUploaded-out-0", resourceId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> resourceUploadedPublisher.publish(resourceId))
                .isInstanceOf(MessagePublishException.class)
                .hasMessage("Failed to publish resource.uploaded event for id=1");
    }

    @Test
    void test_PublishWhenSendThrows_ReturnException() {
        // given
        Long resourceId = 1L;
        when(streamBridge.send("resourceUploaded-out-0", resourceId))
                .thenThrow(new RuntimeException("broker down"));

        // when & then
        assertThatThrownBy(() -> resourceUploadedPublisher.publish(resourceId))
                .isInstanceOf(MessagePublishException.class)
                .hasMessage("Failed to publish resource.uploaded event for id=1")
                .hasCauseInstanceOf(RuntimeException.class);
    }
}
