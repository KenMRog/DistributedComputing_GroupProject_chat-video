package com.screenshare.service;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.HelloWorldMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
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
     * Send a message to the Service Bus Queue
     */
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
     * Send a message to the Service Bus Topic
     */
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
