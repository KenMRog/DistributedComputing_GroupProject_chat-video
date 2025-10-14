#!/bin/bash

echo "🚀 Starting ScreenShare Application..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 16 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

echo "✅ Prerequisites check passed!"

# Start backend
echo "🔧 Starting Spring Boot backend..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
sleep 10

# Start frontend
echo "🎨 Starting React frontend..."
cd ../frontend
npm install
npm start &
FRONTEND_PID=$!

echo "🎉 Application started successfully!"
echo "📱 Frontend: http://localhost:3000"
echo "🔧 Backend: http://localhost:8080"
echo "🗄️  H2 Console: http://localhost:8080/api/h2-console"
echo ""
echo "Press Ctrl+C to stop the application"

# Wait for user interrupt
trap "echo '🛑 Stopping application...'; kill $BACKEND_PID $FRONTEND_PID; exit" INT
wait
