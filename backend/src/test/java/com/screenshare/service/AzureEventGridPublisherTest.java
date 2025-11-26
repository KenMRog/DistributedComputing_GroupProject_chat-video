package com.screenshare.service;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.ChatEvent;
import com.screenshare.model.HelloWorldEvent;
import com.screenshare.model.ScreenShareEvent;
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

    // ========== Chat Event Tests ==========

    @Test
    void testPublishChatEvent_Success() {
        // Arrange
        ChatEvent.ChatEventData data = new ChatEvent.ChatEventData();
        data.setRoomId(123L);
        data.setUserId(456L);
        data.setUsername("TestUser");
        data.setMessageId(789L);
        data.setMessageContent("Hello World");
        
        ChatEvent chatEvent = new ChatEvent("chat/room/123", ChatEvent.MESSAGE_SENT, data);
        
        // Act
        publisher.publishChatEvent(chatEvent);
        
        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisherClient, times(1)).sendEvents(eventCaptor.capture());
        
        List<EventGridEvent> events = eventCaptor.getValue();
        assertNotNull(events);
        assertEquals(1, events.size());
        
        EventGridEvent event = events.get(0);
        assertEquals(ChatEvent.MESSAGE_SENT, event.getEventType());
        assertEquals("chat/room/123", event.getSubject());
        assertNotNull(event.getData());
    }

    @Test
    void testPublishChatEvent_NullClient() {
        // Arrange
        when(topicPublisherClientProvider.getIfAvailable()).thenReturn(topicPublisherClient);
        AzureEventGridPublisher publisherWithNullClient = new AzureEventGridPublisher(
                null, topicPublisherClientProvider, objectMapper);
        
        ChatEvent.ChatEventData data = new ChatEvent.ChatEventData();
        data.setRoomId(123L);
        ChatEvent chatEvent = new ChatEvent("chat/room/123", ChatEvent.MESSAGE_SENT, data);
        
        // Act - Should not throw exception, just log warning
        assertDoesNotThrow(() -> publisherWithNullClient.publishChatEvent(chatEvent));
        
        // Assert - No event should be published
        verify(publisherClient, never()).sendEvents(anyList());
    }

    @Test
    void testPublishChatEventToTopic_Success() {
        // Arrange
        ChatEvent.ChatEventData data = new ChatEvent.ChatEventData();
        data.setRoomId(999L);
        data.setUserId(111L);
        data.setUsername("JoinedUser");
        
        ChatEvent chatEvent = new ChatEvent("chat/room/999", ChatEvent.USER_JOINED_ROOM, data);
        
        // Act
        publisher.publishChatEventToTopic(chatEvent);
        
        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(topicPublisherClient, times(1)).sendEvents(eventCaptor.capture());
        
        List<EventGridEvent> events = eventCaptor.getValue();
        assertNotNull(events);
        assertEquals(1, events.size());
        
        EventGridEvent event = events.get(0);
        assertEquals(ChatEvent.USER_JOINED_ROOM, event.getEventType());
        assertEquals("chat/room/999", event.getSubject());
    }

    @Test
    void testPublishChatEventToTopic_NullClient() {
        // Arrange
        when(topicPublisherClientProvider.getIfAvailable()).thenReturn(null);
        AzureEventGridPublisher publisherWithNullTopicClient = new AzureEventGridPublisher(
                publisherClient, topicPublisherClientProvider, objectMapper);
        
        ChatEvent.ChatEventData data = new ChatEvent.ChatEventData();
        ChatEvent chatEvent = new ChatEvent("chat/room/123", ChatEvent.MESSAGE_SENT, data);
        
        // Act - Should not throw exception, just log warning
        assertDoesNotThrow(() -> publisherWithNullTopicClient.publishChatEventToTopic(chatEvent));
        
        // Assert - No event should be published
        verify(topicPublisherClient, never()).sendEvents(anyList());
    }

    @Test
    void testPublishChatEvent_InviteSent() {
        // Arrange
        ChatEvent.ChatEventData data = new ChatEvent.ChatEventData();
        data.setUserId(100L);
        data.setInvitedUserId(200L);
        data.setInviteId(300L);
        data.setRoomId(400L);
        
        ChatEvent chatEvent = new ChatEvent("chat/invite/300", ChatEvent.INVITE_SENT, data);
        
        // Act
        publisher.publishChatEvent(chatEvent);
        
        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisherClient).sendEvents(eventCaptor.capture());
        
        EventGridEvent event = eventCaptor.getValue().get(0);
        assertEquals(ChatEvent.INVITE_SENT, event.getEventType());
        assertEquals("chat/invite/300", event.getSubject());
    }

    // ========== Screen Share Event Tests ==========

    @Test
    void testPublishScreenShareEvent_Success() {
        // Arrange
        ScreenShareEvent.ScreenShareEventData data = new ScreenShareEvent.ScreenShareEventData();
        data.setSessionId("session-123");
        data.setHostUserId(456L);
        data.setHostUsername("HostUser");
        data.setRoomId(789L);
        data.setParticipantCount(1);
        data.setSessionStatus("ACTIVE");
        
        ScreenShareEvent shareEvent = new ScreenShareEvent(
                "screenshare/session/session-123", 
                ScreenShareEvent.SESSION_STARTED, 
                data);
        
        // Act
        publisher.publishScreenShareEvent(shareEvent);
        
        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisherClient, times(1)).sendEvents(eventCaptor.capture());
        
        List<EventGridEvent> events = eventCaptor.getValue();
        assertNotNull(events);
        assertEquals(1, events.size());
        
        EventGridEvent event = events.get(0);
        assertEquals(ScreenShareEvent.SESSION_STARTED, event.getEventType());
        assertEquals("screenshare/session/session-123", event.getSubject());
        assertNotNull(event.getData());
    }

    @Test
    void testPublishScreenShareEvent_NullClient() {
        // Arrange
        when(topicPublisherClientProvider.getIfAvailable()).thenReturn(topicPublisherClient);
        AzureEventGridPublisher publisherWithNullClient = new AzureEventGridPublisher(
                null, topicPublisherClientProvider, objectMapper);
        
        ScreenShareEvent.ScreenShareEventData data = new ScreenShareEvent.ScreenShareEventData();
        data.setSessionId("session-456");
        ScreenShareEvent shareEvent = new ScreenShareEvent(
                "screenshare/session/session-456", 
                ScreenShareEvent.SESSION_STARTED, 
                data);
        
        // Act - Should not throw exception, just log warning
        assertDoesNotThrow(() -> publisherWithNullClient.publishScreenShareEvent(shareEvent));
        
        // Assert - No event should be published
        verify(publisherClient, never()).sendEvents(anyList());
    }

    @Test
    void testPublishScreenShareEventToTopic_Success() {
        // Arrange
        ScreenShareEvent.ScreenShareEventData data = new ScreenShareEvent.ScreenShareEventData();
        data.setSessionId("session-789");
        data.setParticipantUserId(111L);
        data.setParticipantUsername("Participant1");
        data.setParticipantCount(3);
        
        ScreenShareEvent shareEvent = new ScreenShareEvent(
                "screenshare/session/session-789", 
                ScreenShareEvent.PARTICIPANT_JOINED, 
                data);
        
        // Act
        publisher.publishScreenShareEventToTopic(shareEvent);
        
        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(topicPublisherClient, times(1)).sendEvents(eventCaptor.capture());
        
        List<EventGridEvent> events = eventCaptor.getValue();
        assertNotNull(events);
        assertEquals(1, events.size());
        
        EventGridEvent event = events.get(0);
        assertEquals(ScreenShareEvent.PARTICIPANT_JOINED, event.getEventType());
        assertEquals("screenshare/session/session-789", event.getSubject());
    }

    @Test
    void testPublishScreenShareEventToTopic_NullClient() {
        // Arrange
        when(topicPublisherClientProvider.getIfAvailable()).thenReturn(null);
        AzureEventGridPublisher publisherWithNullTopicClient = new AzureEventGridPublisher(
                publisherClient, topicPublisherClientProvider, objectMapper);
        
        ScreenShareEvent.ScreenShareEventData data = new ScreenShareEvent.ScreenShareEventData();
        ScreenShareEvent shareEvent = new ScreenShareEvent(
                "screenshare/session/test", 
                ScreenShareEvent.SESSION_STARTED, 
                data);
        
        // Act - Should not throw exception, just log warning
        assertDoesNotThrow(() -> publisherWithNullTopicClient.publishScreenShareEventToTopic(shareEvent));
        
        // Assert - No event should be published
        verify(topicPublisherClient, never()).sendEvents(anyList());
    }

    @Test
    void testPublishScreenShareEvent_SessionEnded() {
        // Arrange
        ScreenShareEvent.ScreenShareEventData data = new ScreenShareEvent.ScreenShareEventData();
        data.setSessionId("ending-session");
        data.setHostUserId(999L);
        data.setSessionStatus("COMPLETED");
        data.setParticipantCount(0);
        
        ScreenShareEvent shareEvent = new ScreenShareEvent(
                "screenshare/session/ending-session", 
                ScreenShareEvent.SESSION_ENDED, 
                data);
        
        // Act
        publisher.publishScreenShareEvent(shareEvent);
        
        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisherClient).sendEvents(eventCaptor.capture());
        
        EventGridEvent event = eventCaptor.getValue().get(0);
        assertEquals(ScreenShareEvent.SESSION_ENDED, event.getEventType());
        assertEquals("screenshare/session/ending-session", event.getSubject());
    }

    @Test
    void testChatEventStructure() {
        // Arrange
        ChatEvent.ChatEventData data = new ChatEvent.ChatEventData();
        data.setRoomId(555L);
        data.setMessageId(666L);
        data.setMessageContent("Test message content");
        
        ChatEvent chatEvent = new ChatEvent("chat/room/555", ChatEvent.MESSAGE_SENT, data);
        
        // Act
        publisher.publishChatEvent(chatEvent);
        
        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisherClient).sendEvents(eventCaptor.capture());
        
        EventGridEvent event = eventCaptor.getValue().get(0);
        
        // Verify all required fields
        assertNotNull(event.getId());
        assertNotNull(event.getEventType());
        assertNotNull(event.getSubject());
        assertNotNull(event.getEventTime());
        assertNotNull(event.getData());
    }

    @Test
    void testScreenShareEventStructure() {
        // Arrange
        ScreenShareEvent.ScreenShareEventData data = new ScreenShareEvent.ScreenShareEventData();
        data.setSessionId("struct-test-session");
        data.setResolution("1920x1080");
        data.setFrameRate(30);
        
        ScreenShareEvent shareEvent = new ScreenShareEvent(
                "screenshare/session/struct-test-session", 
                ScreenShareEvent.QUALITY_CHANGED, 
                data);
        
        // Act
        publisher.publishScreenShareEvent(shareEvent);
        
        // Assert
        ArgumentCaptor<List<EventGridEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisherClient).sendEvents(eventCaptor.capture());
        
        EventGridEvent event = eventCaptor.getValue().get(0);
        
        // Verify all required fields
        assertNotNull(event.getId());
        assertEquals(ScreenShareEvent.QUALITY_CHANGED, event.getEventType());
        assertEquals("screenshare/session/struct-test-session", event.getSubject());
        assertNotNull(event.getEventTime());
        assertNotNull(event.getData());
    }
}
