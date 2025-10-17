package com.screenshare.service;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.HelloWorldMessage;
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
}
