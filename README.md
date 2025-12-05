Gatherly


This repository contains a full-stack chat and screen-sharing application (backend + frontend) used for a distributed computing group project. It provides real-time chat, group and direct-message rooms, invites, and live screen sharing.

Contents
- `backend/` — Spring Boot (Java 17) backend (REST APIs, WebSocket configuration, JPA entities).
- `frontend/` — React frontend (MUI components, WebSocket/Socket.io client, auth UI).
- `docker-compose.yml` — Compose file to run the full stack locally (backend + frontend).

Quick overview
- Backend: Spring Boot 3.x, Spring Data JPA, H2 for development/tests, Azure SQL configuration for production.
- Frontend: React, Material UI.
- Tests: JUnit/Spring Boot Test for backend, basic integration tests added under `backend/src/test`.

Local development (no Docker)
1. Prerequisites
   - Java 17 (JDK)
   - Maven
   - Node.js (v16+ recommended) and npm or yarn

2. Start backend (dev profile using H2)
```powershell
cd backend
mvn -Dspring.profiles.active=dev spring-boot:run
```

The backend runs on `http://localhost:8080/api` (context-path `/api`). Health endpoint: `GET /api/auth/health`.

3. Start frontend
```powershell
cd frontend
# using npm
npm install
npm start

# or using yarn
yarn
yarn start
```

The React dev server runs on `http://localhost:3000` by default.

Running with Docker Compose
--------------------------
Use Docker Desktop (or Docker engine) and Docker Compose to run the entire stack locally.

1. Make sure Docker is running (Docker Desktop on Windows). Verify:
```powershell
docker version
docker info
```

2. Build images and start services (clean build, no cache):
```powershell
# from repo root
docker compose build --no-cache
docker compose up -d --force-recreate --remove-orphans
```

3. View logs and status
```powershell
docker compose ps
docker compose logs -f backend
docker compose logs -f frontend
```

4. Stop and remove containers (keep volumes):
```powershell
docker compose down
# or remove volumes as well (destructive):
# docker compose down --volumes
```
