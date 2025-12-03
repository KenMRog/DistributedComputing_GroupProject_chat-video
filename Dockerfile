# Root Dockerfile for Render
# This Dockerfile builds the Spring Boot backend from the root directory

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy backend Maven files
COPY backend/pom.xml backend/
COPY backend/src backend/src

# Build the application using Maven (from image, no mvnw needed)
WORKDIR /app/backend
RUN mvn clean install -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/backend/target/screenshare-backend-1.0.0.jar app.jar

# Expose port (Render will set PORT environment variable)
EXPOSE 8080

# Run with PORT from environment variable
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]

