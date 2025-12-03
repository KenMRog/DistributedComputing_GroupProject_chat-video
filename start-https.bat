@echo off
REM Start Gatherly with HTTPS support
REM This script sets up HTTPS using nginx reverse proxy

echo ========================================
echo Gatherly HTTPS Setup
echo ========================================
echo.

REM Check if SSL certificates exist
if not exist "nginx\ssl\cert.pem" (
    echo SSL certificates not found!
    echo.
    echo Generating SSL certificates...
    call generate-ssl-cert.bat
    if errorlevel 1 (
        echo.
        echo Failed to generate certificates. Please check OpenSSL installation.
        pause
        exit /b 1
    )
    echo.
)

REM Get the local IP address for cross-device access
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    set LOCAL_IP=%%a
    goto :found
)
:found

REM Remove leading spaces
set LOCAL_IP=%LOCAL_IP: =%

if "%LOCAL_IP%"=="" (
    echo WARNING: Could not detect local IP address
    echo Using localhost only
    set REACT_APP_API_URL=https://localhost/api
) else (
    echo Detected local IP: %LOCAL_IP%
    echo Setting REACT_APP_API_URL to https://%LOCAL_IP%/api
    set REACT_APP_API_URL=https://%LOCAL_IP%/api
)

echo.
echo Starting Docker containers with HTTPS...
echo.
echo IMPORTANT: Access the app at:
if not "%LOCAL_IP%"=="" (
    echo   https://%LOCAL_IP%
)
echo   https://localhost
echo.
echo NOTE: Browsers will show a security warning for the self-signed certificate.
echo       Click "Advanced" and "Proceed" to continue.
echo.
echo Press Ctrl+C to stop the containers
echo.

REM Start docker-compose with HTTPS configuration
docker-compose -f docker-compose-https.yml up

