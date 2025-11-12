package com.screenshare.service;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.ChatNotificationMessage;
import com.screenshare.model.HelloWorldMessage;
import com.screenshare.model.ScreenShareNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Service for sending notification messages to Azure Service Bus
 * Handles chat notifications and screen share notifications
 */
@Service
@ConditionalOnProperty(name = "azure.servicebus.connection-string", matchIfMissing = false)
public class AzureServiceBusProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureServiceBusProducer.class);
    
    private final ServiceBusSenderClient queueSenderClient;
    private final ServiceBusSenderClient topicSenderClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AzureServiceBusProducer(
            @Qualifier("serviceBusQueueSenderClient") ServiceBusSenderClient queueSenderClient,
            @Qualifier("serviceBusTopicSenderClient") ServiceBusSenderClient topicSenderClient,
            ObjectMapper objectMapper) {
        this.queueSenderClient = queueSenderClient;
        this.topicSenderClient = topicSenderClient;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Send a chat notification to the Service Bus Queue
     * Queue is typically used for guaranteed delivery of notifications to individual users
     */
    public void sendChatNotification(ChatNotificationMessage notification) {
        if (queueSenderClient == null) {
            logger.warn("Service Bus Queue Sender Client is not configured. Skipping chat notification.");
            return;
        }
        
        try {
            String messageBody = objectMapper.writeValueAsString(notification);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(messageBody);
            
            // Add custom properties for routing and filtering
            serviceBusMessage.setSubject("chat.notification");
            serviceBusMessage.getApplicationProperties().put("notificationType", notification.getNotificationType());
            serviceBusMessage.getApplicationProperties().put("recipientUserId", notification.getRecipientUserId());
            serviceBusMessage.getApplicationProperties().put("priority", notification.getPriority());
            
            queueSenderClient.sendMessage(serviceBusMessage);
            logger.info("Chat notification sent to queue: type={}, recipient={}", 
                    notification.getNotificationType(), notification.getRecipientUserId());
        } catch (Exception e) {
            logger.error("Error sending chat notification to queue", e);
            // Don't throw - this is a non-critical operation
        }
    }
    
    /**
     * Send a screen share notification to the Service Bus Queue
     */
    public void sendScreenShareNotification(ScreenShareNotificationMessage notification) {
        if (queueSenderClient == null) {
            logger.warn("Service Bus Queue Sender Client is not configured. Skipping screen share notification.");
            return;
        }
        
        try {
            String messageBody = objectMapper.writeValueAsString(notification);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(messageBody);
            
            // Add custom properties for routing and filtering
            serviceBusMessage.setSubject("screenshare.notification");
            serviceBusMessage.getApplicationProperties().put("notificationType", notification.getNotificationType());
            serviceBusMessage.getApplicationProperties().put("recipientUserId", notification.getRecipientUserId());
            serviceBusMessage.getApplicationProperties().put("priority", notification.getPriority());
            
            queueSenderClient.sendMessage(serviceBusMessage);
            logger.info("Screen share notification sent to queue: type={}, recipient={}", 
                    notification.getNotificationType(), notification.getRecipientUserId());
        } catch (Exception e) {
            logger.error("Error sending screen share notification to queue", e);
            // Don't throw - this is a non-critical operation
        }
    }
    
    /**
     * Broadcast a chat notification to the Service Bus Topic
     * Topic is used for fan-out scenarios (multiple subscribers)
     */
    public void broadcastChatNotification(ChatNotificationMessage notification) {
        if (topicSenderClient == null) {
            logger.warn("Service Bus Topic Sender Client is not configured. Skipping chat notification broadcast.");
            return;
        }
        
        try {
            String messageBody = objectMapper.writeValueAsString(notification);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(messageBody);
            
            // Add custom properties for routing and filtering
            serviceBusMessage.setSubject("chat.notification");
            serviceBusMessage.getApplicationProperties().put("notificationType", notification.getNotificationType());
            serviceBusMessage.getApplicationProperties().put("roomId", notification.getRoomId());
            serviceBusMessage.getApplicationProperties().put("priority", notification.getPriority());
            
            topicSenderClient.sendMessage(serviceBusMessage);
            logger.info("Chat notification broadcast to topic: type={}, room={}", 
                    notification.getNotificationType(), notification.getRoomId());
        } catch (Exception e) {
            logger.error("Error broadcasting chat notification to topic", e);
            // Don't throw - this is a non-critical operation
        }
    }
    
    /**
     * Broadcast a screen share notification to the Service Bus Topic
     */
    public void broadcastScreenShareNotification(ScreenShareNotificationMessage notification) {
        if (topicSenderClient == null) {
            logger.warn("Service Bus Topic Sender Client is not configured. Skipping screen share notification broadcast.");
            return;
        }
        
        try {
            String messageBody = objectMapper.writeValueAsString(notification);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(messageBody);
            
            // Add custom properties for routing and filtering
            serviceBusMessage.setSubject("screenshare.notification");
            serviceBusMessage.getApplicationProperties().put("notificationType", notification.getNotificationType());
            serviceBusMessage.getApplicationProperties().put("sessionId", notification.getSessionId());
            serviceBusMessage.getApplicationProperties().put("priority", notification.getPriority());
            
            topicSenderClient.sendMessage(serviceBusMessage);
            logger.info("Screen share notification broadcast to topic: type={}, session={}", 
                    notification.getNotificationType(), notification.getSessionId());
        } catch (Exception e) {
            logger.error("Error broadcasting screen share notification to topic", e);
            // Don't throw - this is a non-critical operation
        }
    }
    
    /**
     * Send a message to the Service Bus Queue (Legacy HelloWorld support)
     * @deprecated Use sendChatNotification or sendScreenShareNotification instead
     */
    @Deprecated
    public void sendMessageToQueue(HelloWorldMessage message) {
        if (queueSenderClient == null) {
            logger.error("Service Bus Queue Sender Client is not configured");
            throw new IllegalStateException("Azure Service Bus is not configured");
        }
        
        try {
            String messageBody = objectMapper.writeValueAsString(message);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(messageBody);
            
            queueSenderClient.sendMessage(serviceBusMessage);
            logger.info("Message sent to queue: {}", message);
        } catch (Exception e) {
            logger.error("Error sending message to queue", e);
            throw new RuntimeException("Failed to send message to Service Bus Queue", e);
        }
    }
    
    /**
     * Send a message to the Service Bus Topic (Legacy HelloWorld support)
     * @deprecated Use broadcastChatNotification or broadcastScreenShareNotification instead
     */
    @Deprecated
    public void sendMessageToTopic(HelloWorldMessage message) {
        if (topicSenderClient == null) {
            logger.error("Service Bus Topic Sender Client is not configured");
            throw new IllegalStateException("Azure Service Bus is not configured");
        }
        
        try {
            String messageBody = objectMapper.writeValueAsString(message);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(messageBody);
            
            topicSenderClient.sendMessage(serviceBusMessage);
            logger.info("Message sent to topic: {}", message);
        } catch (Exception e) {
            logger.error("Error sending message to topic", e);
            throw new RuntimeException("Failed to send message to Service Bus Topic", e);
        }
    }
}
