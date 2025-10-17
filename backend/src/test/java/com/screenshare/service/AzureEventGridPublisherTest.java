package com.screenshare.service;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.HelloWorldEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureEventGridPublisherTest {

    @Mock
    private EventGridPublisherClient publisherClient;

    @Mock
    private ObjectProvider<EventGridPublisherClient> topicPublisherClientProvider;

    @Mock
    private EventGridPublisherClient topicPublisherClient;

    private ObjectMapper objectMapper;
    private AzureEventGridPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        when(topicPublisherClientProvider.getIfAvailable()).thenReturn(topicPublisherClient);
        publisher = new AzureEventGridPublisher(publisherClient, topicPublisherClientProvider, objectMapper);
    }

    @Test
    void testPublishEvent_Success() {
        // Arrange
        HelloWorldEvent helloWorldEvent = new HelloWorldEvent(
                "TestEvent",
                "/test/subject",
                "Test data"
        );

        // Act
        publisher.publishEvent(helloWorldEvent);

        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisherClient, times(1)).sendEvents(eventCaptor.capture());

        List<EventGridEvent> capturedEvents = eventCaptor.getValue();
        assertNotNull(capturedEvents);
        assertEquals(1, capturedEvents.size());

        EventGridEvent event = capturedEvents.get(0);
        assertEquals("TestEvent", event.getEventType());
        assertEquals("/test/subject", event.getSubject());
        assertEquals(helloWorldEvent.getId(), event.getId());
        assertNotNull(event.getEventTime());
    }

    @Test
    void testPublishEvent_NullClient() {
        // Arrange
        when(topicPublisherClientProvider.getIfAvailable()).thenReturn(topicPublisherClient);
        AzureEventGridPublisher publisherWithNullClient = new AzureEventGridPublisher(
                null, topicPublisherClientProvider, objectMapper);
        HelloWorldEvent event = new HelloWorldEvent("Test", "/test", "data");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            publisherWithNullClient.publishEvent(event);
        });

        assertEquals("Azure Event Grid is not configured", exception.getMessage());
    }

    @Test
    void testPublishEventToTopic_Success() {
        // Arrange
        HelloWorldEvent helloWorldEvent = new HelloWorldEvent(
                "TopicEvent",
                "/topic/subject",
                "Topic test data"
        );

        // Act
        publisher.publishEventToTopic(helloWorldEvent);

        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(topicPublisherClient, times(1)).sendEvents(eventCaptor.capture());

        List<EventGridEvent> capturedEvents = eventCaptor.getValue();
        assertNotNull(capturedEvents);
        assertEquals(1, capturedEvents.size());

        EventGridEvent event = capturedEvents.get(0);
        assertEquals("TopicEvent", event.getEventType());
        assertEquals("/topic/subject", event.getSubject());
        assertEquals(helloWorldEvent.getId(), event.getId());
    }

    @Test
    void testPublishEventToTopic_NullClient() {
        // Arrange
        when(topicPublisherClientProvider.getIfAvailable()).thenReturn(null);
        AzureEventGridPublisher publisherWithNullTopicClient = new AzureEventGridPublisher(
                publisherClient, topicPublisherClientProvider, objectMapper);
        HelloWorldEvent event = new HelloWorldEvent("Test", "/test", "data");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            publisherWithNullTopicClient.publishEventToTopic(event);
        });

        assertEquals("Azure Event Grid Topic is not configured", exception.getMessage());
    }

    @Test
    void testPublishEvent_WithException() {
        // Arrange
        HelloWorldEvent event = new HelloWorldEvent("Test", "/test", "data");
        doThrow(new RuntimeException("Connection error")).when(publisherClient).sendEvents(anyList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            publisher.publishEvent(event);
        });

        assertTrue(exception.getMessage().contains("Failed to publish event to Event Grid"));
    }

    @Test
    void testPublishEventToTopic_WithException() {
        // Arrange
        HelloWorldEvent event = new HelloWorldEvent("Test", "/test", "data");
        doThrow(new RuntimeException("Connection error")).when(topicPublisherClient).sendEvents(anyList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            publisher.publishEventToTopic(event);
        });

        assertTrue(exception.getMessage().contains("Failed to publish event to Event Grid Topic"));
    }

    @Test
    void testEventStructure() {
        // Arrange
        HelloWorldEvent helloWorldEvent = new HelloWorldEvent(
                "StructureTest",
                "/structure/test",
                "Testing event structure"
        );

        // Act
        publisher.publishEvent(helloWorldEvent);

        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisherClient).sendEvents(eventCaptor.capture());

        List<EventGridEvent> events = eventCaptor.getValue();
        EventGridEvent event = events.get(0);

        // Verify all required fields
        assertNotNull(event.getId());
        assertNotNull(event.getEventType());
        assertNotNull(event.getSubject());
        assertNotNull(event.getEventTime());
        assertNotNull(event.getData());
        assertEquals("1.0", event.getDataVersion());
    }

    @Test
    void testMultipleEventsPublished() {
        // Arrange
        HelloWorldEvent event1 = new HelloWorldEvent("Event1", "/test/1", "Data 1");
        HelloWorldEvent event2 = new HelloWorldEvent("Event2", "/test/2", "Data 2");

        // Act
        publisher.publishEvent(event1);
        publisher.publishEvent(event2);

        // Assert
        verify(publisherClient, times(2)).sendEvents(anyList());
    }
}
