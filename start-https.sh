#!/bin/bash
# Start Gatherly with HTTPS support
# This script sets up HTTPS using nginx reverse proxy

echo "========================================"
echo "Gatherly HTTPS Setup"
echo "========================================"
echo ""

# Check if SSL certificates exist
if [ ! -f "nginx/ssl/cert.pem" ]; then
    echo "SSL certificates not found!"
    echo ""
    echo "Generating SSL certificates..."
    ./generate-ssl-cert.sh
    if [ $? -ne 0 ]; then
        echo ""
        echo "Failed to generate certificates. Please check OpenSSL installation."
        exit 1
    fi
    echo ""
fi

# Get the local IP address for cross-device access
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OS
    LOCAL_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -n 1)
else
    # Linux
    LOCAL_IP=$(hostname -I | awk '{print $1}')
fi

if [ -z "$LOCAL_IP" ]; then
    echo "WARNING: Could not detect local IP address"
    echo "Using localhost only"
    export REACT_APP_API_URL="https://localhost/api"
else
    echo "Detected local IP: $LOCAL_IP"
    echo "Setting REACT_APP_API_URL to https://$LOCAL_IP/api"
    export REACT_APP_API_URL="https://$LOCAL_IP/api"
fi

echo ""
echo "Starting Docker containers with HTTPS..."
echo ""
echo "IMPORTANT: Access the app at:"
if [ ! -z "$LOCAL_IP" ]; then
    echo "  https://$LOCAL_IP"
fi
echo "  https://localhost"
echo ""
echo "NOTE: Browsers will show a security warning for the self-signed certificate."
echo "      Click 'Advanced' and 'Proceed' to continue."
echo ""
echo "Press Ctrl+C to stop the containers"
echo ""

# Start docker-compose with HTTPS configuration
docker-compose -f docker-compose-https.yml up

