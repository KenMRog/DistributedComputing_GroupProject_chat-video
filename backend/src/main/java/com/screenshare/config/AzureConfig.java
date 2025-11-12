package com.screenshare.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Azure Services Configuration
 * Configures Azure Service Bus and Azure Blob Storage clients
 */
@Configuration
@Profile("production")
public class AzureConfig {

    @Value("${azure.servicebus.connection-string:}")
    private String serviceBusConnectionString;

    @Value("${azure.servicebus.queue-name:screenshare-queue}")
    private String queueName;

    @Value("${azure.servicebus.topic-name:screenshare-topic}")
    private String topicName;

    @Value("${azure.storage.connection-string:}")
    private String storageConnectionString;

    /**
     * Creates a Service Bus Sender Client for sending messages to a queue
     * This replaces AWS SQS functionality
     * Only created if connection string is configured
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnExpression(
        "!'${azure.servicebus.connection-string:}'.isEmpty()"
    )
    public ServiceBusSenderClient serviceBusQueueSender() {
        if (serviceBusConnectionString == null || serviceBusConnectionString.isEmpty()) {
            throw new IllegalStateException("Azure Service Bus connection string is not configured");
        }
        
        return new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .sender()
                .queueName(queueName)
                .buildClient();
    }

    /**
     * Creates a Service Bus Sender Client for publishing messages to a topic
     * This replaces AWS SNS functionality
     * Only created if connection string is configured
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnExpression(
        "!'${azure.servicebus.connection-string:}'.isEmpty()"
    )
    public ServiceBusSenderClient serviceBusTopicSender() {
        if (serviceBusConnectionString == null || serviceBusConnectionString.isEmpty()) {
            throw new IllegalStateException("Azure Service Bus connection string is not configured");
        }
        
        return new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .sender()
                .topicName(topicName)
                .buildClient();
    }

    /**
     * Creates a Blob Service Client for Azure Blob Storage
     * This can be used for storing screen share recordings or file uploads
     * Only created if connection string is configured
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnExpression(
        "!'${azure.storage.connection-string:}'.isEmpty()"
    )
    public BlobServiceClient blobServiceClient() {
        if (storageConnectionString == null || storageConnectionString.isEmpty()) {
            // Storage is optional, return null if not configured
            return null;
        }
        
        return new BlobServiceClientBuilder()
                .connectionString(storageConnectionString)
                .buildClient();
    }
}


