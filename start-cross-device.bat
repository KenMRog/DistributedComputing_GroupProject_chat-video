@echo off
REM Cross-Device Startup Script for Windows
REM This script helps you start Gatherly for cross-device access

echo ========================================
echo Gatherly Cross-Device Setup
echo ========================================
echo.

REM Get the local IP address
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    set LOCAL_IP=%%a
    goto :found
)
:found

REM Remove leading spaces
set LOCAL_IP=%LOCAL_IP: =%

if "%LOCAL_IP%"=="" (
    echo ERROR: Could not detect local IP address
    echo Please manually set REACT_APP_API_URL environment variable
    echo Example: set REACT_APP_API_URL=http://192.168.1.100:8080/api
    pause
    exit /b 1
)

echo Detected local IP: %LOCAL_IP%
echo.
echo Setting REACT_APP_API_URL to http://%LOCAL_IP%:8080/api
echo.

REM Set the environment variable
set REACT_APP_API_URL=http://%LOCAL_IP%:8080/api

echo Starting Docker containers...
echo.
echo IMPORTANT: Other devices should access the app at:
echo   http://%LOCAL_IP%:3000
echo.
echo Press Ctrl+C to stop the containers
echo.

REM Start docker-compose with the environment variable
docker-compose up

