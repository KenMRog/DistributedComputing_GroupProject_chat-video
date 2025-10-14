@echo off
echo 🚀 Starting ScreenShare Application...

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java is not installed. Please install Java 17 or higher.
    pause
    exit /b 1
)

REM Check if Node.js is installed
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js is not installed. Please install Node.js 16 or higher.
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven is not installed. Please install Maven 3.6 or higher.
    pause
    exit /b 1
)

echo ✅ Prerequisites check passed!

REM Start backend
echo 🔧 Starting Spring Boot backend...
cd backend

REM Check if Maven is available
mvn --version >nul 2>&1
if %errorlevel% equ 0 (
    echo Using Maven to start backend...
    start "Backend" cmd /k "mvn spring-boot:run"
) else (
    REM Check if Gradle wrapper is available
    if exist gradlew.bat (
        echo Using Gradle to start backend...
        start "Backend" cmd /k "gradlew.bat bootRun"
    ) else (
        echo ❌ Neither Maven nor Gradle found. Please install Maven or use Docker.
        echo Run: docker-compose up -d
        pause
        exit /b 1
    )
)

REM Wait for backend to start
echo ⏳ Waiting for backend to start...
timeout /t 10 /nobreak >nul

REM Start frontend
echo 🎨 Starting React frontend...
cd ..\frontend
call npm install
start "Frontend" cmd /k "npm start"

echo 🎉 Application started successfully!
echo 📱 Frontend: http://localhost:3000
echo 🔧 Backend: http://localhost:8080
echo 🗄️  H2 Console: http://localhost:8080/api/h2-console
echo.
echo Press any key to exit...
pause >nul
