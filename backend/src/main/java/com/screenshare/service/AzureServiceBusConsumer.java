package com.screenshare.service;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.HelloWorldMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AzureServiceBusConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureServiceBusConsumer.class);
    
    @Value("${azure.servicebus.connection-string:}")
    private String connectionString;
    
    @Value("${azure.servicebus.queue-name}")
    private String queueName;
    
    @Value("${azure.servicebus.topic-name}")
    private String topicName;
    
    @Value("${azure.servicebus.subscription-name}")
    private String subscriptionName;
    
    private final ObjectMapper objectMapper;
    private ServiceBusProcessorClient queueProcessorClient;
    private ServiceBusProcessorClient topicProcessorClient;
    
    @Autowired
    public AzureServiceBusConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void startConsumer() {
        if (connectionString == null || connectionString.isEmpty()) {
            logger.warn("Azure Service Bus connection string is not configured. Consumer will not start.");
            return;
        }
        
        // Start Queue Consumer
        startQueueConsumer();
        
        // Start Topic Subscription Consumer
        startTopicSubscriptionConsumer();
    }
    
    private void startQueueConsumer() {
        try {
            logger.info("Starting Service Bus Queue Consumer for queue: {}", queueName);
            
            queueProcessorClient = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .processor()
                    .queueName(queueName)
                    .processMessage(this::processQueueMessage)
                    .processError(this::processError)
                    .buildProcessorClient();
            
            queueProcessorClient.start();
            logger.info("Service Bus Queue Consumer started successfully");
        } catch (Exception e) {
            logger.error("Failed to start Queue Consumer", e);
        }
    }
    
    private void startTopicSubscriptionConsumer() {
        try {
            logger.info("Starting Service Bus Topic Subscription Consumer for topic: {}, subscription: {}", 
                    topicName, subscriptionName);
            
            topicProcessorClient = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .processor()
                    .topicName(topicName)
                    .subscriptionName(subscriptionName)
                    .processMessage(this::processTopicMessage)
                    .processError(this::processError)
                    .buildProcessorClient();
            
            topicProcessorClient.start();
            logger.info("Service Bus Topic Subscription Consumer started successfully");
        } catch (Exception e) {
            logger.error("Failed to start Topic Subscription Consumer", e);
        }
    }
    
    private void processQueueMessage(ServiceBusReceivedMessageContext context) {
        try {
            String messageBody = context.getMessage().getBody().toString();
            logger.info("Received message from queue: {}", messageBody);
            
            HelloWorldMessage message = objectMapper.readValue(messageBody, HelloWorldMessage.class);
            logger.info("Processed HelloWorld message from queue: {}", message);
            
            // Complete the message
            context.complete();
        } catch (Exception e) {
            logger.error("Error processing queue message", e);
            context.abandon();
        }
    }
    
    private void processTopicMessage(ServiceBusReceivedMessageContext context) {
        try {
            String messageBody = context.getMessage().getBody().toString();
            logger.info("Received message from topic subscription: {}", messageBody);
            
            HelloWorldMessage message = objectMapper.readValue(messageBody, HelloWorldMessage.class);
            logger.info("Processed HelloWorld message from topic: {}", message);
            
            // Complete the message
            context.complete();
        } catch (Exception e) {
            logger.error("Error processing topic message", e);
            context.abandon();
        }
    }
    
    private void processError(com.azure.messaging.servicebus.ServiceBusErrorContext context) {
        logger.error("Error occurred while processing message: {}", context.getException().getMessage());
        logger.error("Error source: {}", context.getErrorSource());
    }
    
    @PreDestroy
    public void stopConsumer() {
        if (queueProcessorClient != null) {
            logger.info("Stopping Service Bus Queue Consumer");
            queueProcessorClient.close();
        }
        
        if (topicProcessorClient != null) {
            logger.info("Stopping Service Bus Topic Subscription Consumer");
            topicProcessorClient.close();
        }
    }
}
