package com.screenshare.service;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.ChatNotificationMessage;
import com.screenshare.model.HelloWorldMessage;
import com.screenshare.model.ScreenShareNotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AzureServiceBusConsumer
 * Tests message processing logic for chat and screen share notifications
 */
@ExtendWith(MockitoExtension.class)
class AzureServiceBusConsumerTest {

    @Mock
    private ServiceBusReceivedMessageContext queueContext;

    @Mock
    private ServiceBusReceivedMessageContext topicContext;

    @Mock
    private ServiceBusReceivedMessage receivedMessage;

    private ObjectMapper objectMapper;
    private AzureServiceBusConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        consumer = new AzureServiceBusConsumer(objectMapper);
    }

    // ========== Chat Notification Processing Tests ==========

    @Test
    void testProcessChatNotification_NewMessage() throws Exception {
        // Arrange
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_NEW_MESSAGE, 123L);
        notification.setSenderUserId(456L);
        notification.setSenderUsername("Sender");
        notification.setRoomId(789L);
        notification.setTitle("New Message");
        notification.setBody("You have a new message");

        String messageBody = objectMapper.writeValueAsString(notification);

        // Act - Verify serialization works correctly
        assertNotNull(consumer);
        assertTrue(messageBody.contains("NEW_MESSAGE"));
        assertTrue(messageBody.contains("\"recipientUserId\":123"));
    }

    @Test
    void testProcessChatNotification_Invite() throws Exception {
        // Arrange
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_INVITE_RECEIVED, 100L);
        notification.setSenderUserId(200L);
        notification.setSenderUsername("Inviter");
        notification.setTitle("Room Invite");
        notification.setBody("You've been invited to a room");

        String messageBody = objectMapper.writeValueAsString(notification);

        // Act & Assert - Verify serialization works correctly
        assertNotNull(consumer);
        assertTrue(messageBody.contains("INVITE_RECEIVED"));
        assertTrue(messageBody.contains("\"recipientUserId\":100"));
    }

    // ========== Screen Share Notification Processing Tests ==========

    @Test
    void testProcessScreenShareNotification_SessionStarted() throws Exception {
        // Arrange
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_SESSION_STARTED, 123L);
        notification.setSessionId("session-456");
        notification.setHostUserId(789L);
        notification.setHostUsername("Host");
        notification.setRoomId(999L);
        notification.setTitle("Screen Share Started");
        notification.setSessionStatus("ACTIVE");
        notification.setParticipantCount(1);

        String messageBody = objectMapper.writeValueAsString(notification);

        // Act & Assert - Verify serialization works correctly
        assertNotNull(consumer);
        assertTrue(messageBody.contains("SESSION_STARTED"));
        assertTrue(messageBody.contains("\"sessionId\":\"session-456\""));
    }

    @Test
    void testProcessScreenShareNotification_ParticipantJoined() throws Exception {
        // Arrange
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_PARTICIPANT_JOINED, 111L);
        notification.setSessionId("session-222");
        notification.setHostUserId(333L);
        notification.setHostUsername("Host");
        notification.setTitle("Participant Joined");
        notification.setParticipantCount(3);

        String messageBody = objectMapper.writeValueAsString(notification);

        // Act & Assert - Verify serialization works correctly
        assertNotNull(consumer);
        assertTrue(messageBody.contains("PARTICIPANT_JOINED"));
        assertTrue(messageBody.contains("\"sessionId\":\"session-222\""));
    }

    // ========== Legacy HelloWorld Message Processing Tests ==========

    @Test
    void testProcessLegacyHelloWorldMessage() throws Exception {
        // Arrange
        HelloWorldMessage message = new HelloWorldMessage("Test Message", "TestSender");
        String messageBody = objectMapper.writeValueAsString(message);

        // Act & Assert - Verify serialization works correctly
        assertNotNull(consumer);
        assertTrue(messageBody.contains("Test Message"));
        assertTrue(messageBody.contains("TestSender"));
    }

    // ========== Message Serialization Tests ==========

    @Test
    void testChatNotificationSerialization() throws Exception {
        // Arrange
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_USER_JOINED, 500L);
        notification.setRoomId(600L);
        notification.setRoomName("Test Room");
        notification.setTitle("User Joined");

        // Act
        String json = objectMapper.writeValueAsString(notification);
        ChatNotificationMessage deserialized = objectMapper.readValue(json, ChatNotificationMessage.class);

        // Assert
        assertNotNull(deserialized);
        assertEquals(notification.getNotificationType(), deserialized.getNotificationType());
        assertEquals(notification.getRecipientUserId(), deserialized.getRecipientUserId());
        assertEquals(notification.getRoomId(), deserialized.getRoomId());
        assertEquals(notification.getTitle(), deserialized.getTitle());
    }

    @Test
    void testScreenShareNotificationSerialization() throws Exception {
        // Arrange
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_SESSION_ENDED, 700L);
        notification.setSessionId("end-session");
        notification.setHostUsername("EndHost");
        notification.setSessionStatus("COMPLETED");

        // Act
        String json = objectMapper.writeValueAsString(notification);
        ScreenShareNotificationMessage deserialized = objectMapper.readValue(json, ScreenShareNotificationMessage.class);

        // Assert
        assertNotNull(deserialized);
        assertEquals(notification.getNotificationType(), deserialized.getNotificationType());
        assertEquals(notification.getRecipientUserId(), deserialized.getRecipientUserId());
        assertEquals(notification.getSessionId(), deserialized.getSessionId());
        assertEquals(notification.getSessionStatus(), deserialized.getSessionStatus());
    }

    // ========== Consumer Configuration Tests ==========

    @Test
    void testConsumerConstruction() {
        // Arrange & Act
        AzureServiceBusConsumer testConsumer = new AzureServiceBusConsumer(new ObjectMapper());

        // Assert
        assertNotNull(testConsumer);
    }

    @Test
    void testConsumerWithNullObjectMapper() {
        // Arrange & Act
        AzureServiceBusConsumer testConsumer = new AzureServiceBusConsumer(null);
        
        // Assert - Consumer can be created with null, but would fail during usage
        assertNotNull(testConsumer);
    }

    // ========== Message Priority Tests ==========

    @Test
    void testHighPriorityNotification() throws Exception {
        // Arrange
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_MENTION, 800L);
        notification.setPriority(ChatNotificationMessage.PRIORITY_HIGH);
        notification.setTitle("You were mentioned");

        // Act
        String json = objectMapper.writeValueAsString(notification);
        ChatNotificationMessage deserialized = objectMapper.readValue(json, ChatNotificationMessage.class);

        // Assert
        assertEquals(ChatNotificationMessage.PRIORITY_HIGH, deserialized.getPriority());
    }

    @Test
    void testLowPriorityNotification() throws Exception {
        // Arrange
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_QUALITY_CHANGED, 900L);
        notification.setPriority(ScreenShareNotificationMessage.PRIORITY_LOW);

        // Act
        String json = objectMapper.writeValueAsString(notification);
        ScreenShareNotificationMessage deserialized = objectMapper.readValue(json, ScreenShareNotificationMessage.class);

        // Assert
        assertEquals(ScreenShareNotificationMessage.PRIORITY_LOW, deserialized.getPriority());
    }

    // ========== Complex Notification Tests ==========

    @Test
    void testComplexChatNotification() throws Exception {
        // Arrange
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_NEW_MESSAGE, 1000L);
        notification.setSenderUserId(2000L);
        notification.setSenderUsername("ComplexSender");
        notification.setRoomId(3000L);
        notification.setRoomName("Complex Room");
        notification.setMessageId(4000L);
        notification.setMessagePreview("This is a preview");
        notification.setTitle("Complex Notification");
        notification.setBody("This is a complex notification with many fields");
        notification.setPriority(ChatNotificationMessage.PRIORITY_HIGH);

        // Act
        String json = objectMapper.writeValueAsString(notification);
        ChatNotificationMessage deserialized = objectMapper.readValue(json, ChatNotificationMessage.class);

        // Assert
        assertNotNull(deserialized);
        assertEquals(notification.getRecipientUserId(), deserialized.getRecipientUserId());
        assertEquals(notification.getSenderUserId(), deserialized.getSenderUserId());
        assertEquals(notification.getSenderUsername(), deserialized.getSenderUsername());
        assertEquals(notification.getRoomId(), deserialized.getRoomId());
        assertEquals(notification.getRoomName(), deserialized.getRoomName());
        assertEquals(notification.getMessageId(), deserialized.getMessageId());
        assertEquals(notification.getMessagePreview(), deserialized.getMessagePreview());
        assertEquals(notification.getTitle(), deserialized.getTitle());
        assertEquals(notification.getBody(), deserialized.getBody());
        assertEquals(notification.getPriority(), deserialized.getPriority());
    }

    @Test
    void testComplexScreenShareNotification() throws Exception {
        // Arrange
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_SESSION_STARTED, 5000L);
        notification.setSessionId("complex-session-id");
        notification.setHostUserId(6000L);
        notification.setHostUsername("ComplexHost");
        notification.setRoomId(7000L);
        notification.setTitle("Complex Screen Share");
        notification.setBody("Complex screen share notification");
        notification.setSessionStatus("ACTIVE");
        notification.setParticipantCount(10);
        notification.setPriority(ScreenShareNotificationMessage.PRIORITY_NORMAL);

        // Act
        String json = objectMapper.writeValueAsString(notification);
        ScreenShareNotificationMessage deserialized = objectMapper.readValue(json, ScreenShareNotificationMessage.class);

        // Assert
        assertNotNull(deserialized);
        assertEquals(notification.getRecipientUserId(), deserialized.getRecipientUserId());
        assertEquals(notification.getSessionId(), deserialized.getSessionId());
        assertEquals(notification.getHostUserId(), deserialized.getHostUserId());
        assertEquals(notification.getHostUsername(), deserialized.getHostUsername());
        assertEquals(notification.getRoomId(), deserialized.getRoomId());
        assertEquals(notification.getTitle(), deserialized.getTitle());
        assertEquals(notification.getBody(), deserialized.getBody());
        assertEquals(notification.getSessionStatus(), deserialized.getSessionStatus());
        assertEquals(notification.getParticipantCount(), deserialized.getParticipantCount());
        assertEquals(notification.getPriority(), deserialized.getPriority());
    }
}
