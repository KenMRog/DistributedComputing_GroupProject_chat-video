#!/bin/bash
# Cross-Device Startup Script for Linux/Mac
# This script helps you start Gatherly for cross-device access

echo "========================================"
echo "Gatherly Cross-Device Setup"
echo "========================================"
echo ""

# Get the local IP address (works on Linux and Mac)
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OS
    LOCAL_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -n 1)
else
    # Linux
    LOCAL_IP=$(hostname -I | awk '{print $1}')
fi

if [ -z "$LOCAL_IP" ]; then
    echo "ERROR: Could not detect local IP address"
    echo "Please manually set REACT_APP_API_URL environment variable"
    echo "Example: export REACT_APP_API_URL=http://192.168.1.100:8080/api"
    exit 1
fi

echo "Detected local IP: $LOCAL_IP"
echo ""
echo "Setting REACT_APP_API_URL to http://$LOCAL_IP:8080/api"
echo ""

# Export the environment variable
export REACT_APP_API_URL="http://$LOCAL_IP:8080/api"

echo "Starting Docker containers..."
echo ""
echo "IMPORTANT: Other devices should access the app at:"
echo "  http://$LOCAL_IP:3000"
echo ""
echo "Press Ctrl+C to stop the containers"
echo ""

# Start docker-compose with the environment variable
docker-compose up

