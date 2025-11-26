package com.screenshare.service;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.ChatNotificationMessage;
import com.screenshare.model.HelloWorldMessage;
import com.screenshare.model.ScreenShareNotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureServiceBusProducerTest {

    @Mock
    private ServiceBusSenderClient queueSenderClient;

    @Mock
    private ServiceBusSenderClient topicSenderClient;

    private ObjectMapper objectMapper;
    private AzureServiceBusProducer producer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        producer = new AzureServiceBusProducer(queueSenderClient, topicSenderClient, objectMapper);
    }

    @Test
    void testSendMessageToQueue_Success() throws Exception {
        // Arrange
        HelloWorldMessage message = new HelloWorldMessage("Test Message", "TestSender");
        
        // Act
        producer.sendMessageToQueue(message);
        
        // Assert
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(queueSenderClient, times(1)).sendMessage(messageCaptor.capture());
        
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertTrue(capturedMessage.getBody().toString().contains("Test Message"));
        assertTrue(capturedMessage.getBody().toString().contains("TestSender"));
    }

    @Test
    void testSendMessageToQueue_NullClient() {
        // Arrange
        AzureServiceBusProducer producerWithNullClient = new AzureServiceBusProducer(
                null, topicSenderClient, objectMapper);
        HelloWorldMessage message = new HelloWorldMessage("Test", "Sender");
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            producerWithNullClient.sendMessageToQueue(message);
        });
        
        assertEquals("Azure Service Bus is not configured", exception.getMessage());
    }

    @Test
    void testSendMessageToTopic_Success() throws Exception {
        // Arrange
        HelloWorldMessage message = new HelloWorldMessage("Topic Message", "TopicSender");
        
        // Act
        producer.sendMessageToTopic(message);
        
        // Assert
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(topicSenderClient, times(1)).sendMessage(messageCaptor.capture());
        
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertTrue(capturedMessage.getBody().toString().contains("Topic Message"));
        assertTrue(capturedMessage.getBody().toString().contains("TopicSender"));
    }

    @Test
    void testSendMessageToTopic_NullClient() {
        // Arrange
        AzureServiceBusProducer producerWithNullClient = new AzureServiceBusProducer(
                queueSenderClient, null, objectMapper);
        HelloWorldMessage message = new HelloWorldMessage("Test", "Sender");
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            producerWithNullClient.sendMessageToTopic(message);
        });
        
        assertEquals("Azure Service Bus is not configured", exception.getMessage());
    }

    @Test
    void testSendMessageToQueue_WithException() {
        // Arrange
        HelloWorldMessage message = new HelloWorldMessage("Test", "Sender");
        doThrow(new RuntimeException("Connection error")).when(queueSenderClient).sendMessage(any(ServiceBusMessage.class));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            producer.sendMessageToQueue(message);
        });
        
        assertTrue(exception.getMessage().contains("Failed to send message to Service Bus Queue"));
    }

    @Test
    void testSendMessageToTopic_WithException() {
        // Arrange
        HelloWorldMessage message = new HelloWorldMessage("Test", "Sender");
        doThrow(new RuntimeException("Connection error")).when(topicSenderClient).sendMessage(any(ServiceBusMessage.class));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            producer.sendMessageToTopic(message);
        });
        
        assertTrue(exception.getMessage().contains("Failed to send message to Service Bus Topic"));
    }

    @Test
    void testMessageSerialization() throws Exception {
        // Arrange
        HelloWorldMessage message = new HelloWorldMessage("Serialization Test", "SerializationSender");
        
        // Act
        producer.sendMessageToQueue(message);
        
        // Assert
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(queueSenderClient, times(1)).sendMessage(messageCaptor.capture());
        
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        String messageBody = capturedMessage.getBody().toString();
        
        // Verify the message is properly JSON formatted
        assertNotNull(messageBody);
        assertTrue(messageBody.contains("\"message\":\"Serialization Test\""));
        assertTrue(messageBody.contains("\"sender\":\"SerializationSender\""));
        assertTrue(messageBody.contains("\"timestamp\":"));
    }

    // ========== Chat Notification Tests ==========

    @Test
    void testSendChatNotification_Success() {
        // Arrange
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_NEW_MESSAGE, 123L);
        notification.setSenderUserId(456L);
        notification.setSenderUsername("TestUser");
        notification.setRoomId(789L);
        notification.setRoomName("Test Room");
        notification.setTitle("New Message");
        notification.setBody("You have a new message");
        notification.setPriority(ChatNotificationMessage.PRIORITY_HIGH);
        
        // Act
        producer.sendChatNotification(notification);
        
        // Assert
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(queueSenderClient, times(1)).sendMessage(messageCaptor.capture());
        
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertEquals("chat.notification", capturedMessage.getSubject());
        assertEquals(ChatNotificationMessage.TYPE_NEW_MESSAGE, 
                capturedMessage.getApplicationProperties().get("notificationType"));
        assertEquals(123L, capturedMessage.getApplicationProperties().get("recipientUserId"));
        assertEquals(ChatNotificationMessage.PRIORITY_HIGH, 
                capturedMessage.getApplicationProperties().get("priority"));
    }

    @Test
    void testSendChatNotification_NullClient() {
        // Arrange
        AzureServiceBusProducer producerWithNullClient = new AzureServiceBusProducer(
                null, topicSenderClient, objectMapper);
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_NEW_MESSAGE, 123L);
        
        // Act - Should not throw exception, just log warning
        assertDoesNotThrow(() -> producerWithNullClient.sendChatNotification(notification));
        
        // Assert - No message should be sent
        verify(queueSenderClient, never()).sendMessage(any());
    }

    @Test
    void testBroadcastChatNotification_Success() {
        // Arrange
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_USER_JOINED, 123L);
        notification.setRoomId(789L);
        notification.setRoomName("Test Room");
        notification.setTitle("User Joined");
        notification.setBody("A user has joined the room");
        notification.setPriority(ChatNotificationMessage.PRIORITY_NORMAL);
        
        // Act
        producer.broadcastChatNotification(notification);
        
        // Assert
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(topicSenderClient, times(1)).sendMessage(messageCaptor.capture());
        
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertEquals("chat.notification", capturedMessage.getSubject());
        assertEquals(ChatNotificationMessage.TYPE_USER_JOINED, 
                capturedMessage.getApplicationProperties().get("notificationType"));
        assertEquals(789L, capturedMessage.getApplicationProperties().get("roomId"));
    }

    @Test
    void testBroadcastChatNotification_NullClient() {
        // Arrange
        AzureServiceBusProducer producerWithNullClient = new AzureServiceBusProducer(
                queueSenderClient, null, objectMapper);
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_USER_JOINED, 123L);
        
        // Act - Should not throw exception, just log warning
        assertDoesNotThrow(() -> producerWithNullClient.broadcastChatNotification(notification));
        
        // Assert - No message should be sent
        verify(topicSenderClient, never()).sendMessage(any());
    }

    // ========== Screen Share Notification Tests ==========

    @Test
    void testSendScreenShareNotification_Success() {
        // Arrange
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_SESSION_STARTED, 123L);
        notification.setSessionId("session-456");
        notification.setHostUserId(789L);
        notification.setHostUsername("HostUser");
        notification.setRoomId(999L);
        notification.setTitle("Screen Share Started");
        notification.setBody("A screen share session has started");
        notification.setSessionStatus("ACTIVE");
        notification.setParticipantCount(5);
        notification.setPriority(ScreenShareNotificationMessage.PRIORITY_HIGH);
        
        // Act
        producer.sendScreenShareNotification(notification);
        
        // Assert
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(queueSenderClient, times(1)).sendMessage(messageCaptor.capture());
        
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertEquals("screenshare.notification", capturedMessage.getSubject());
        assertEquals(ScreenShareNotificationMessage.TYPE_SESSION_STARTED, 
                capturedMessage.getApplicationProperties().get("notificationType"));
        assertEquals(123L, capturedMessage.getApplicationProperties().get("recipientUserId"));
        assertEquals(ScreenShareNotificationMessage.PRIORITY_HIGH, 
                capturedMessage.getApplicationProperties().get("priority"));
    }

    @Test
    void testSendScreenShareNotification_NullClient() {
        // Arrange
        AzureServiceBusProducer producerWithNullClient = new AzureServiceBusProducer(
                null, topicSenderClient, objectMapper);
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_SESSION_STARTED, 123L);
        
        // Act - Should not throw exception, just log warning
        assertDoesNotThrow(() -> producerWithNullClient.sendScreenShareNotification(notification));
        
        // Assert - No message should be sent
        verify(queueSenderClient, never()).sendMessage(any());
    }

    @Test
    void testBroadcastScreenShareNotification_Success() {
        // Arrange
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_PARTICIPANT_JOINED, 123L);
        notification.setSessionId("session-789");
        notification.setHostUserId(456L);
        notification.setHostUsername("HostUser");
        notification.setTitle("Participant Joined");
        notification.setBody("A participant has joined the screen share");
        notification.setParticipantCount(3);
        notification.setPriority(ScreenShareNotificationMessage.PRIORITY_NORMAL);
        
        // Act
        producer.broadcastScreenShareNotification(notification);
        
        // Assert
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(topicSenderClient, times(1)).sendMessage(messageCaptor.capture());
        
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertEquals("screenshare.notification", capturedMessage.getSubject());
        assertEquals(ScreenShareNotificationMessage.TYPE_PARTICIPANT_JOINED, 
                capturedMessage.getApplicationProperties().get("notificationType"));
        assertEquals("session-789", capturedMessage.getApplicationProperties().get("sessionId"));
    }

    @Test
    void testBroadcastScreenShareNotification_NullClient() {
        // Arrange
        AzureServiceBusProducer producerWithNullClient = new AzureServiceBusProducer(
                queueSenderClient, null, objectMapper);
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_PARTICIPANT_JOINED, 123L);
        
        // Act - Should not throw exception, just log warning
        assertDoesNotThrow(() -> producerWithNullClient.broadcastScreenShareNotification(notification));
        
        // Assert - No message should be sent
        verify(topicSenderClient, never()).sendMessage(any());
    }

    @Test
    void testChatNotificationSerialization() throws Exception {
        // Arrange
        ChatNotificationMessage notification = new ChatNotificationMessage(
                ChatNotificationMessage.TYPE_INVITE_RECEIVED, 100L);
        notification.setSenderUserId(200L);
        notification.setSenderUsername("InviteSender");
        notification.setTitle("Room Invite");
        notification.setBody("You have been invited to a room");
        
        // Act
        producer.sendChatNotification(notification);
        
        // Assert
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(queueSenderClient).sendMessage(messageCaptor.capture());
        
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        String messageBody = capturedMessage.getBody().toString();
        
        // Verify JSON contains expected fields
        assertNotNull(messageBody);
        assertTrue(messageBody.contains("\"notificationType\":\"" + ChatNotificationMessage.TYPE_INVITE_RECEIVED + "\""));
        assertTrue(messageBody.contains("\"recipientUserId\":100"));
        assertTrue(messageBody.contains("\"senderUsername\":\"InviteSender\""));
        assertTrue(messageBody.contains("\"title\":\"Room Invite\""));
    }

    @Test
    void testScreenShareNotificationSerialization() throws Exception {
        // Arrange
        ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                ScreenShareNotificationMessage.TYPE_SESSION_ENDED, 300L);
        notification.setSessionId("end-session-123");
        notification.setHostUsername("EndHost");
        notification.setTitle("Session Ended");
        notification.setSessionStatus("COMPLETED");
        
        // Act
        producer.sendScreenShareNotification(notification);
        
        // Assert
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(queueSenderClient).sendMessage(messageCaptor.capture());
        
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        String messageBody = capturedMessage.getBody().toString();
        
        // Verify JSON contains expected fields
        assertNotNull(messageBody);
        assertTrue(messageBody.contains("\"notificationType\":\"" + ScreenShareNotificationMessage.TYPE_SESSION_ENDED + "\""));
        assertTrue(messageBody.contains("\"recipientUserId\":300"));
        assertTrue(messageBody.contains("\"sessionId\":\"end-session-123\""));
        assertTrue(messageBody.contains("\"sessionStatus\":\"COMPLETED\""));
    }
}
