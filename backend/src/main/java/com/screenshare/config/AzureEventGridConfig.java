package com.screenshare.config;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureEventGridConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureEventGridConfig.class);
    
    @Value("${azure.eventgrid.endpoint:}")
    private String endpoint;
    
    @Value("${azure.eventgrid.key:}")
    private String key;
    
    @Value("${azure.eventgrid.topic-endpoint:}")
    private String topicEndpoint;
    
    @Bean
    public EventGridPublisherClient eventGridPublisherClient() {
        if (endpoint == null || endpoint.isEmpty() || key == null || key.isEmpty()) {
            logger.warn("Azure Event Grid endpoint or key is not configured");
            return null;
        }
        
        logger.info("Creating Event Grid Publisher Client for endpoint: {}", endpoint);
        return new EventGridPublisherClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildClient();
    }
    
    @Bean
    public EventGridPublisherClient eventGridTopicPublisherClient() {
        if (topicEndpoint == null || topicEndpoint.isEmpty() || key == null || key.isEmpty()) {
            logger.warn("Azure Event Grid topic endpoint or key is not configured");
            return null;
        }
        
        logger.info("Creating Event Grid Topic Publisher Client for endpoint: {}", topicEndpoint);
        return new EventGridPublisherClientBuilder()
                .endpoint(topicEndpoint)
                .credential(new AzureKeyCredential(key))
                .buildClient();
    }
    
    @Bean
    public String eventGridEndpoint() {
        return endpoint;
    }
    
    @Bean
    public String eventGridTopicEndpoint() {
        return topicEndpoint;
    }
}
