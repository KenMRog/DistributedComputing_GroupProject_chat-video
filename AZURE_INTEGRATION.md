# Azure Service Bus and Event Grid Integration

This document explains how to use the Azure Service Bus and Event Grid integration in the Spring Boot backend.

## Overview

The backend now supports:
- **Azure Service Bus**: Queue and Topic/Subscription messaging patterns
- **Azure Event Grid**: Event publishing and webhook subscription handling

## Configuration

### Environment Variables

Set the following environment variables or update `application.yml`:

#### Azure Service Bus
```bash
AZURE_SERVICEBUS_CONNECTION_STRING=<your-connection-string>
AZURE_SERVICEBUS_QUEUE_NAME=helloworld-queue
AZURE_SERVICEBUS_TOPIC_NAME=helloworld-topic
AZURE_SERVICEBUS_SUBSCRIPTION_NAME=helloworld-subscription
```

#### Azure Event Grid
```bash
AZURE_EVENTGRID_ENDPOINT=<your-event-grid-endpoint>
AZURE_EVENTGRID_KEY=<your-event-grid-access-key>
AZURE_EVENTGRID_TOPIC_ENDPOINT=<your-topic-endpoint>
```

## Architecture

### Azure Service Bus

#### Components
1. **AzureServiceBusConfig**: Configuration class that creates Service Bus sender clients
2. **AzureServiceBusProducer**: Service for sending messages to queues and topics
3. **AzureServiceBusConsumer**: Service that automatically starts listeners for queues and topics
4. **AzureServiceBusController**: REST API endpoints for testing

#### Message Flow
- **Producer**: Sends messages to Queue or Topic
- **Consumer**: Automatically receives and processes messages (starts on application startup)

### Azure Event Grid

#### Components
1. **AzureEventGridConfig**: Configuration class for Event Grid publisher clients
2. **AzureEventGridPublisher**: Service for publishing events
3. **AzureEventGridSubscriber**: Service for handling incoming webhook events
4. **AzureEventGridController**: REST API endpoints and webhook handler

#### Event Flow
- **Publisher**: Publishes events to Event Grid
- **Subscriber**: Receives events via webhook endpoint

## API Endpoints

### Azure Service Bus

#### Send Hello World to Queue
```bash
GET http://localhost:8080/api/azure/servicebus/queue/hello
```

#### Send Hello World to Topic
```bash
GET http://localhost:8080/api/azure/servicebus/topic/hello
```

#### Send Custom Message to Queue
```bash
POST http://localhost:8080/api/azure/servicebus/queue/send
Content-Type: application/json

{
  "message": "Custom message",
  "sender": "YourName"
}
```

#### Send Custom Message to Topic
```bash
POST http://localhost:8080/api/azure/servicebus/topic/send
Content-Type: application/json

{
  "message": "Custom message",
  "sender": "YourName"
}
```

#### Health Check
```bash
GET http://localhost:8080/api/azure/servicebus/health
```

### Azure Event Grid

#### Publish Hello World Event
```bash
GET http://localhost:8080/api/azure/eventgrid/publish/hello
```

#### Publish Hello World Event to Topic
```bash
GET http://localhost:8080/api/azure/eventgrid/topic/hello
```

#### Publish Custom Event
```bash
POST http://localhost:8080/api/azure/eventgrid/publish
Content-Type: application/json

{
  "eventType": "Custom.Event.Type",
  "subject": "/custom/subject",
  "data": "Custom event data"
}
```

#### Publish Custom Event to Topic
```bash
POST http://localhost:8080/api/azure/eventgrid/topic/publish
Content-Type: application/json

{
  "eventType": "Custom.Topic.Event",
  "subject": "/custom/topic/subject",
  "data": "Custom topic event data"
}
```

#### Webhook Endpoint (for Event Grid subscriptions)
```bash
POST http://localhost:8080/api/azure/eventgrid/webhook
```

#### Health Check
```bash
GET http://localhost:8080/api/azure/eventgrid/health
```

## Testing the Integration

### 1. Service Bus Queue Test

1. Start the application
2. Send a message to the queue:
   ```bash
   curl http://localhost:8080/api/azure/servicebus/queue/hello
   ```
3. Check the logs - you should see:
   - "Message sent to queue" (from Producer)
   - "Received message from queue" (from Consumer)

### 2. Service Bus Topic Test

1. Start the application
2. Send a message to the topic:
   ```bash
   curl http://localhost:8080/api/azure/servicebus/topic/hello
   ```
3. Check the logs - you should see:
   - "Message sent to topic" (from Producer)
   - "Received message from topic subscription" (from Consumer)

### 3. Event Grid Test

1. Start the application
2. Publish an event:
   ```bash
   curl http://localhost:8080/api/azure/eventgrid/publish/hello
   ```
3. Check the logs - you should see "Event published to Event Grid"

### 4. Event Grid Webhook Test

1. Configure an Event Grid subscription to point to your webhook:
   ```
   https://your-domain.com/api/azure/eventgrid/webhook
   ```
2. When events are published to Event Grid, they will be received at the webhook endpoint
3. Check the logs for "Received events from Event Grid webhook"

## Azure Setup Requirements

### Service Bus Setup

1. Create an Azure Service Bus namespace
2. Create a Queue named `helloworld-queue`
3. Create a Topic named `helloworld-topic`
4. Create a Subscription on the topic named `helloworld-subscription`
5. Copy the connection string from the namespace

### Event Grid Setup

1. **Option A: Event Grid Domain**
   - Create an Event Grid Domain
   - Copy the endpoint and access key

2. **Option B: Event Grid Topic**
   - Create an Event Grid Topic
   - Copy the topic endpoint and access key

3. **For Webhooks (Optional)**
   - Create an Event Grid subscription
   - Set the endpoint to: `https://your-domain.com/api/azure/eventgrid/webhook`
   - The webhook will handle subscription validation automatically

## Dependencies Added

The following Azure SDK dependencies were added to `build.gradle`:

```gradle
implementation 'com.azure:azure-messaging-servicebus:7.15.0'
implementation 'com.azure:azure-messaging-eventgrid:4.21.0'
implementation 'com.azure:azure-core:1.45.0'
```

## Code Structure

```
com/screenshare/
├── config/
│   ├── AzureServiceBusConfig.java       # Service Bus client configuration
│   └── AzureEventGridConfig.java        # Event Grid client configuration
├── controller/
│   ├── AzureServiceBusController.java   # Service Bus REST endpoints
│   └── AzureEventGridController.java    # Event Grid REST endpoints
├── service/
│   ├── AzureServiceBusProducer.java     # Send messages to Service Bus
│   ├── AzureServiceBusConsumer.java     # Receive messages from Service Bus
│   ├── AzureEventGridPublisher.java     # Publish events to Event Grid
│   └── AzureEventGridSubscriber.java    # Handle Event Grid webhook events
└── model/
    ├── HelloWorldMessage.java           # Message DTO for Service Bus
    └── HelloWorldEvent.java             # Event DTO for Event Grid
```

## Troubleshooting

### Service Bus Consumer Not Starting
- Check that `AZURE_SERVICEBUS_CONNECTION_STRING` is set
- Verify queue and topic names match your Azure resources
- Check application logs for connection errors

### Event Grid Publishing Fails
- Verify endpoint and key are correct
- Check network connectivity to Azure
- Ensure the Event Grid resource exists

### Webhook Not Receiving Events
- Ensure your endpoint is publicly accessible
- Check that the Event Grid subscription is configured correctly
- Verify the webhook URL includes `/api/azure/eventgrid/webhook`

## Next Steps

1. Configure your Azure resources
2. Set environment variables
3. Run the application with Gradle: `./gradlew bootRun`
4. Test the endpoints using curl or Postman
5. Monitor the logs to see messages being sent and received

## Running the Application

```bash
cd backend
./gradlew bootRun
```

Or on Windows:
```cmd
cd backend
gradlew.bat bootRun
```
