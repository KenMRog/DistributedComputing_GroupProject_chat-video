# ScreenShare Application

## Architecture

The application has the following components:

- **Frontend**: React.js application with Material-UI for the user interface
- **Backend**: Spring Boot REST API with WebSocket support
- **Database**: H2 (development) / PostgreSQL (production)
- **Message Queue**: AWS SQS for asynchronous processing
- **Notification Service**: AWS SNS for real-time notifications
- **WebSocket**: STOMP over SockJS for real-time communication

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java 17** or higher
- **Node.js 16** or higher
- **npm** or **yarn**
- **Maven 3.6** or higher
- **Git**

## Quick Start

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd DistributedComputing_GroupProject_chat-video
```

### 2. Backend Setup (Spring Boot)

Navigate to the backend directory and run:

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

**Available endpoints:**
- API Base URL: `http://localhost:8080/api`
- WebSocket Endpoint: `http://localhost:8080/api/ws`
- H2 Database Console: `http://localhost:8080/api/h2-console`

### 3. Frontend Setup (React)

In a new terminal, navigate to the frontend directory:

```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:3000`

## Configuration

### Backend Configuration

The backend configuration is in `backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
```

### Environment Variables

For production deployment, set the following environment variables:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/screenshare
DB_USERNAME=your_username
DB_PASSWORD=your_password

# AWS Services
AWS_REGION=us-east-1
SQS_QUEUE_URL=your_sqs_queue_url
SNS_TOPIC_ARN=your_sns_topic_arn
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
```

## Project Structure

```
├── backend/                    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/screenshare/
│   │   │   │   ├── ScreenshareBackendApplication.java
│   │   │   │   ├── config/          # Configuration classes
│   │   │   │   └── controller/      # REST and WebSocket controllers
│   │   │   └── resources/
│   │   │       └── application.yml  # Application configuration
│   │   └── test/                   # Test files
│   └── pom.xml                    # Maven dependencies
├── frontend/                     # React frontend
│   ├── public/
│   ├── src/
│   │   ├── components/           # React components
│   │   ├── context/             # React context providers
│   │   ├── App.js               # Main App component
│   │   └── index.js             # Entry point
│   └── package.json             # Node.js dependencies
└── README.md                    # This file
```


### WebSocket Endpoints

- **Connect**: `ws://localhost:8080/api/ws`
- **Chat Messages**: `/topic/public`
- **Screen Share**: `/topic/screenshare`

### REST Endpoints

- **Health Check**: `GET /api/actuator/health`
- **Metrics**: `GET /api/actuator/metrics`


## Alternative: Use Docker (No Local Installation Required)

If you don't want to install Java and Maven locally, you can use Docker instead:

### Prerequisites for Docker
1. Install Docker Desktop from https://www.docker.com/products/docker-desktop/
2. Make sure Docker Desktop is running

### Run with Docker
```powershell
# Start the entire application stack
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```