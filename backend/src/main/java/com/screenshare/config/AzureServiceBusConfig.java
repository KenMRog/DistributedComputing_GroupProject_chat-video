package com.screenshare.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureServiceBusConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureServiceBusConfig.class);
    
    @Value("${azure.servicebus.connection-string:#{null}}")
    private String connectionString;
    
    @Value("${azure.servicebus.queue-name:helloworld-queue}")
    private String queueName;
    
    @Value("${azure.servicebus.topic-name:helloworld-topic}")
    private String topicName;
    
    @Value("${azure.servicebus.subscription-name:helloworld-subscription}")
    private String subscriptionName;
    
    @Bean
    @ConditionalOnProperty(name = "azure.servicebus.connection-string")
    public ServiceBusSenderClient serviceBusQueueSenderClient() {
        logger.info("Creating Service Bus Queue Sender Client for queue: {}", queueName);
        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();
    }
    
    @Bean
    @ConditionalOnProperty(name = "azure.servicebus.connection-string")
    public ServiceBusSenderClient serviceBusTopicSenderClient() {
        logger.info("Creating Service Bus Topic Sender Client for topic: {}", topicName);
        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .topicName(topicName)
                .buildClient();
    }
    
    @Bean
    public String serviceBusConnectionString() {
        return connectionString;
    }
    
    @Bean
    public String serviceBusQueueName() {
        return queueName;
    }
    
    @Bean
    public String serviceBusTopicName() {
        return topicName;
    }
    
    @Bean
    public String serviceBusSubscriptionName() {
        return subscriptionName;
    }
}
