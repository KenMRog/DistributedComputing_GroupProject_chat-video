# ScreenShare Application

> ** Quick Start with Azure SQL**: See [QUICK_START_AZURE.md](QUICK_START_AZURE.md) for a 5-minute setup guide!

## Architecture

The application has the following components:

- **Frontend**: React.js application with Material-UI for the user interface
- **Backend**: Spring Boot REST API with WebSocket support
- **Database**: H2 (development) / Azure SQL Server (production)
- **Message Queue**: Azure Service Bus for asynchronous processing
- **Notification Service**: Azure Service Bus Topics for real-time notifications
- **Storage**: Azure Blob Storage for file storage (optional)
- **WebSocket**: STOMP over SockJS for real-time communication

## Cloud Platform

This application is deployed on **Microsoft Azure** with the following services:
- **Azure SQL Database**: Managed relational database
- **Azure Service Bus**: Enterprise messaging service
- **Azure Blob Storage**: Object storage for files (optional)
- **Azure App Service**: Web application hosting (recommended for deployment)

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java 17** or higher
- **Node.js 16** or higher
- **npm** or **yarn**
- **Maven 3.6** or higher
- **Git**

## Quick Start

### Option 1: Local Development (H2 Database)

For quick local testing with in-memory database:

**Windows:**
```bash
start.bat
```

**Linux/Mac:**
```bash
chmod +x start.sh
./start.sh
```

This will start both frontend and backend with H2 in-memory database.

### Option 2: Production Mode (Azure SQL Database)

To run with Azure SQL Server:

1. **Set up environment variables** (see `azure-env-template.txt`):
   ```bash
   # Windows PowerShell
   $env:AZURE_SQL_PASSWORD="your_password"
   $env:AZURE_SERVICEBUS_CONNECTION_STRING="your_connection_string"
   
   # Linux/Mac
   export AZURE_SQL_PASSWORD="your_password"
   export AZURE_SERVICEBUS_CONNECTION_STRING="your_connection_string"
   ```

2. **Run the Azure start script:**
   
   **Windows:**
   ```bash
   start-azure.bat
   ```
   
   **Linux/Mac:**
   ```bash
   chmod +x start-azure.sh
   ./start-azure.sh
   ```

### Option 3: Manual Setup

#### Backend Setup (Spring Boot)

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

#### Frontend Setup (React)

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

For production deployment with Azure, set the following environment variables:

```bash

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


## Alternative: Use Docker

If you don't want to install Java and Maven locally, you can use Docker instead:

### Prerequisites for Docker
1. Install Docker Desktop from https://www.docker.com/products/docker-desktop/
2. Make sure Docker Desktop is running

### Run with Docker (Development Mode - H2)
```bash
# Start the entire application stack
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

### Run with Docker (Production Mode - Azure)
```bash
# Create a .env file with your Azure credentials (see azure-env-template.txt)
# Then run:
docker-compose --env-file .env up -d

# Or set environment variables inline:
SPRING_PROFILES_ACTIVE=production AZURE_SQL_PASSWORD=your_password docker-compose up -d
```

## 📖 Additional Documentation

- **[MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md)** - Overview of AWS to Azure migration
- **[AZURE_SETUP.md](AZURE_SETUP.md)** - Detailed Azure services setup guide
- **[azure-env-template.txt](azure-env-template.txt)** - Environment variables template

## 🔧 Configuration Profiles

The application supports two Spring profiles:

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` (default) | H2 in-memory | Local development, testing |
| `production` | Azure SQL Server | Production, Azure deployment |

To switch profiles:
```bash
# Windows
set SPRING_PROFILES_ACTIVE=production

# Linux/Mac
export SPRING_PROFILES_ACTIVE=production
```