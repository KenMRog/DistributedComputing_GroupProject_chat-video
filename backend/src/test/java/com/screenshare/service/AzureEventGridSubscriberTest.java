package com.screenshare.service;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AzureEventGridSubscriberTest {

    private AzureEventGridSubscriber subscriber;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        subscriber = new AzureEventGridSubscriber(objectMapper);
    }

    @Test
    void testHandleEvent_HelloWorldEvent() {
        // Arrange
        EventGridEvent event = new EventGridEvent(
                "/test/subject",
                "HelloWorld.Test",
                BinaryData.fromString("{\"message\":\"Test data\"}"),
                "1.0"
        );
        event.setId(UUID.randomUUID().toString());
        event.setEventTime(OffsetDateTime.now());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> subscriber.handleEvent(event));
    }

    @Test
    void testHandleEvents_MultipleEvents() {
        // Arrange
        EventGridEvent event1 = new EventGridEvent(
                "/test/subject1",
                "HelloWorld.Test1",
                BinaryData.fromString("{\"message\":\"Test data 1\"}"),
                "1.0"
        );
        event1.setId(UUID.randomUUID().toString());
        event1.setEventTime(OffsetDateTime.now());

        EventGridEvent event2 = new EventGridEvent(
                "/test/subject2",
                "HelloWorld.Test2",
                BinaryData.fromString("{\"message\":\"Test data 2\"}"),
                "1.0"
        );
        event2.setId(UUID.randomUUID().toString());
        event2.setEventTime(OffsetDateTime.now());

        List<EventGridEvent> events = Arrays.asList(event1, event2);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> subscriber.handleEvents(events));
    }

    @Test
    void testHandleEvent_SubscriptionValidation() {
        // Arrange
        String validationCode = "test-validation-code-12345";
        String validationJson = String.format("{\"validationCode\":\"%s\"}", validationCode);

        EventGridEvent event = new EventGridEvent(
                "/test/subject",
                "Microsoft.EventGrid.SubscriptionValidationEvent",
                BinaryData.fromString(validationJson),
                "1.0"
        );
        event.setId(UUID.randomUUID().toString());
        event.setEventTime(OffsetDateTime.now());

        // Act & Assert - Should handle validation event without exception
        assertDoesNotThrow(() -> subscriber.handleEvent(event));
    }

    @Test
    void testHandleEvent_CustomEventType() {
        // Arrange
        EventGridEvent event = new EventGridEvent(
                "/custom/subject",
                "Custom.Event.Type",
                BinaryData.fromString("{\"customField\":\"customValue\"}"),
                "1.0"
        );
        event.setId(UUID.randomUUID().toString());
        event.setEventTime(OffsetDateTime.now());

        // Act & Assert - Should handle custom event without exception
        assertDoesNotThrow(() -> subscriber.handleEvent(event));
    }

    @Test
    void testHandleEvent_HelloWorldEventWithComplexData() {
        // Arrange
        String complexData = "{\"message\":\"Hello World\",\"sender\":\"TestSender\",\"timestamp\":123456789}";
        EventGridEvent event = new EventGridEvent(
                "/helloworld/complex",
                "HelloWorld.ComplexEvent",
                BinaryData.fromString(complexData),
                "1.0"
        );
        event.setId(UUID.randomUUID().toString());
        event.setEventTime(OffsetDateTime.now());

        // Act & Assert - Should handle complex data without exception
        assertDoesNotThrow(() -> subscriber.handleEvent(event));
    }

    @Test
    void testHandleEvents_EmptyList() {
        // Arrange
        List<EventGridEvent> events = Arrays.asList();

        // Act & Assert - Should handle empty list without exception
        assertDoesNotThrow(() -> subscriber.handleEvents(events));
    }

    @Test
    void testHandleEvent_AllEventFields() {
        // Arrange
        String eventId = UUID.randomUUID().toString();
        OffsetDateTime eventTime = OffsetDateTime.now();
        String subject = "/test/allfields";
        String eventType = "HelloWorld.AllFields";
        String data = "{\"field1\":\"value1\",\"field2\":\"value2\"}";

        EventGridEvent event = new EventGridEvent(
                subject,
                eventType,
                BinaryData.fromString(data),
                "1.0"
        );
        event.setId(eventId);
        event.setEventTime(eventTime);

        // Act & Assert - Verify event can be processed
        assertDoesNotThrow(() -> subscriber.handleEvent(event));
        
        // Verify event properties
        assertEquals(eventId, event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(subject, event.getSubject());
        assertEquals(eventTime, event.getEventTime());
        assertNotNull(event.getData());
    }

    @Test
    void testGetValidationResponse() throws Exception {
        // Arrange
        String validationCode = "validation-code-xyz-789";

        // Act
        String response = subscriber.getValidationResponse(validationCode);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains(validationCode));
        assertTrue(response.contains("validationResponse"));
    }

    @Test
    void testGetValidationResponse_NullCode() throws Exception {
        // Act
        String response = subscriber.getValidationResponse(null);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("validationResponse"));
    }

    @Test
    void testGetValidationResponse_EmptyCode() throws Exception {
        // Act
        String response = subscriber.getValidationResponse("");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("validationResponse"));
    }
}
