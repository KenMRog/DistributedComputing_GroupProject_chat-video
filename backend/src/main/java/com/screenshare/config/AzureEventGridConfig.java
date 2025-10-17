package com.screenshare.config;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureEventGridConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureEventGridConfig.class);
    
    @Value("${azure.eventgrid.endpoint:#{null}}")
    private String endpoint;
    
    @Value("${azure.eventgrid.key:#{null}}")
    private String key;
    
    // Support both kebab-case and dotted nested property names for topic endpoint
    // This improves compatibility with environment variable relaxed binding
    @Value("${azure.eventgrid.topic-endpoint:${azure.eventgrid.topic.endpoint:#{null}}}")
    private String topicEndpoint;
    
    @Bean
    @ConditionalOnProperty(name = {"azure.eventgrid.endpoint", "azure.eventgrid.key"})
    public EventGridPublisherClient eventGridPublisherClient() {
        logger.info("Creating Event Grid Publisher Client for endpoint: {}", endpoint);
        return new EventGridPublisherClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildEventGridEventPublisherClient();
    }
    
    @Bean
    @ConditionalOnProperty(name = {"azure.eventgrid.topic-endpoint", "azure.eventgrid.key"})
    public EventGridPublisherClient eventGridTopicPublisherClient() {
        logger.info("Creating Event Grid Topic Publisher Client for endpoint: {}", topicEndpoint);
        return new EventGridPublisherClientBuilder()
                .endpoint(topicEndpoint)
                .credential(new AzureKeyCredential(key))
                .buildEventGridEventPublisherClient();
    }

    // Alternate conditional to support property style: azure.eventgrid.topic.endpoint
    // Define the same bean name so the service can @Qualifier("eventGridTopicPublisherClient") consistently
    @Bean(name = "eventGridTopicPublisherClient")
    @ConditionalOnProperty(name = {"azure.eventgrid.topic.endpoint", "azure.eventgrid.key"})
    public EventGridPublisherClient eventGridTopicPublisherClientAlt() {
        logger.info("Creating Event Grid Topic Publisher Client (alt) for endpoint: {}", topicEndpoint);
        return new EventGridPublisherClientBuilder()
                .endpoint(topicEndpoint)
                .credential(new AzureKeyCredential(key))
                .buildEventGridEventPublisherClient();
    }
    
    @Bean
    public String eventGridEndpoint() {
        return endpoint;
    }
    
    @Bean
    public String eventGridKey() {
        return key;
    }
    
    @Bean
    public String eventGridTopicEndpoint() {
        return topicEndpoint;
    }
}
