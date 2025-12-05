package com.screenshare.service;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Service for sending messages using Azure Service Bus
 * Only created if Azure Service Bus connection string is configured
 */
@Service
@Profile("production")
@ConditionalOnExpression("!'${azure.servicebus.connection-string:}'.isEmpty()")
public class AzureMessageService {

    private static final Logger logger = LoggerFactory.getLogger(AzureMessageService.class);

    private final ServiceBusSenderClient queueSender;
    private final ServiceBusSenderClient topicSender;

    @Autowired
    public AzureMessageService(
            @Qualifier("serviceBusQueueSender") ServiceBusSenderClient queueSender,
            @Qualifier("serviceBusTopicSender") ServiceBusSenderClient topicSender) {
        this.queueSender = queueSender;
        this.topicSender = topicSender;
    }

    /**
     * Send a message to the Azure Service Bus Queue
     */
    public void sendToQueue(String messageBody) {
        try {
            ServiceBusMessage message = new ServiceBusMessage(messageBody);
            queueSender.sendMessage(message);
            logger.info("Message sent to queue: {}", messageBody);
        } catch (Exception e) {
            logger.error("Error sending message to queue", e);
            throw new RuntimeException("Failed to send message to queue", e);
        }
    }

    /**
     * Publish a message to the Azure Service Bus Topic
     */
    public void publishToTopic(String messageBody) {
        try {
            ServiceBusMessage message = new ServiceBusMessage(messageBody);
            topicSender.sendMessage(message);
            logger.info("Message published to topic: {}", messageBody);
        } catch (Exception e) {
            logger.error("Error publishing message to topic", e);
            throw new RuntimeException("Failed to publish message to topic", e);
        }
    }

    /**
     * Send a message with custom properties
     */
    public void sendToQueueWithProperties(String messageBody, String subject, 
                                          java.util.Map<String, Object> properties) {
        try {
            ServiceBusMessage message = new ServiceBusMessage(messageBody);
            message.setSubject(subject);
            
            if (properties != null) {
                properties.forEach((key, value) -> 
                    message.getApplicationProperties().put(key, value));
            }
            
            queueSender.sendMessage(message);
            logger.info("Message with properties sent to queue: {}", messageBody);
        } catch (Exception e) {
            logger.error("Error sending message with properties", e);
            throw new RuntimeException("Failed to send message with properties", e);
        }
    }

    /**
     * Publish a message to topic with custom properties
     */
    public void publishToTopicWithProperties(String messageBody, String subject,
                                             java.util.Map<String, Object> properties) {
        try {
            ServiceBusMessage message = new ServiceBusMessage(messageBody);
            message.setSubject(subject);
            
            if (properties != null) {
                properties.forEach((key, value) ->
                    message.getApplicationProperties().put(key, value));
            }
            
            topicSender.sendMessage(message);
            logger.info("Message with properties published to topic: {}", messageBody);
        } catch (Exception e) {
            logger.error("Error publishing message with properties", e);
            throw new RuntimeException("Failed to publish message with properties", e);
        }
    }
}


