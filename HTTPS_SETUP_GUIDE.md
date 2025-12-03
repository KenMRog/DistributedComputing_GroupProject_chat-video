# HTTPS Setup Guide for Gatherly

This guide will help you set up HTTPS for your Gatherly application, enabling screen sharing in Firefox and other browsers that require secure contexts.

## Quick Start (5 minutes)

### Step 1: Generate SSL Certificate

**Windows:**
```bash
generate-ssl-cert.bat
```

**Linux/Mac:**
```bash
chmod +x generate-ssl-cert.sh
./generate-ssl-cert.sh
```

This creates self-signed certificates in `nginx/ssl/` directory.

### Step 2: Start with HTTPS

```bash
docker-compose -f docker-compose-https.yml up
```

Or use the automated script:

**Windows:**
```bash
start-https.bat
```

**Linux/Mac:**
```bash
chmod +x start-https.sh
./start-https.sh
```

### Step 3: Access the Application

Open your browser and go to:
```
https://localhost
```

Or from another device on your network:
```
https://YOUR_SERVER_IP
```

**Important:** Browsers will show a security warning because it's a self-signed certificate. This is normal for local development:
1. Click "Advanced" or "Show Details"
2. Click "Proceed to localhost (unsafe)" or "Accept the Risk and Continue"

## Prerequisites

### OpenSSL (for certificate generation)

**Windows:**
- Option 1: Install Git for Windows (includes OpenSSL) - **Recommended!**
- Option 2: Download from https://slproweb.com/products/Win32OpenSSL.html
- Option 3: Use WSL (Windows Subsystem for Linux)

**Linux:**
```bash
sudo apt-get install openssl  # Ubuntu/Debian
# or
sudo yum install openssl     # CentOS/RHEL
```

**macOS:**
```bash
brew install openssl
# or (usually pre-installed)
# openssl version
```

## How It Works

1. **Nginx Reverse Proxy**: Acts as an HTTPS endpoint on port 443
2. **SSL Termination**: Nginx handles SSL/TLS encryption
3. **Proxy to Services**: Forwards requests to frontend (port 3000) and backend (port 8080)
4. **WebSocket Support**: Properly configured for WebSocket connections

## Architecture

```
Internet/Network
    ↓
Nginx (Port 443 - HTTPS)
    ├──→ Frontend (Port 3000 - HTTP internal)
    └──→ Backend (Port 8080 - HTTP internal)
```

## Configuration

### For Cross-Device Access

When accessing from another device, you need to:

1. **Find your server's IP address:**
   ```bash
   # Windows
   ipconfig
   
   # Linux/Mac
   hostname -I
   ```

2. **Access via IP:**
   ```
   https://YOUR_SERVER_IP
   ```

3. **Accept the certificate warning** on the client device (self-signed certificate)

### Environment Variables

The HTTPS setup uses these environment variables:

- `REACT_APP_API_URL`: Set to `https://YOUR_SERVER_IP/api` for cross-device access
- `ALLOWED_ORIGINS`: Set to `*` or specific HTTPS origins

Example:
```bash
# Windows PowerShell
$env:REACT_APP_API_URL="https://192.168.1.100/api"
docker-compose -f docker-compose-https.yml up

# Linux/Mac
export REACT_APP_API_URL="https://192.168.1.100/api"
docker-compose -f docker-compose-https.yml up
```

## Troubleshooting

### Certificate Generation Fails

**Error: "OpenSSL is not installed"**
- Install OpenSSL (see Prerequisites above)
- Or use WSL on Windows
- **Windows users:** The script automatically detects Git's OpenSSL installation!

### Browser Shows "Not Secure" Warning

This is **normal** for self-signed certificates. Click "Advanced" → "Proceed" to continue.

### Can't Access from Another Device

1. **Check firewall**: Ensure ports 80 and 443 are open
2. **Check IP address**: Make sure you're using the correct server IP
3. **Accept certificate**: You'll need to accept the certificate warning on each device

### WebSocket Connection Fails

1. Check browser console for errors
2. Verify `ALLOWED_ORIGINS` includes your HTTPS origin
3. Check nginx logs: `docker-compose -f docker-compose-https.yml logs nginx`

### Port Already in Use

If ports 80 or 443 are already in use:

1. **Stop other services** using those ports
2. **Or modify nginx ports** in `docker-compose-https.yml`:
   ```yaml
   ports:
     - "8443:443"  # Use 8443 instead of 443
   ```
   Then access via `https://localhost:8443`

## Production Deployment

For production, you should:

1. **Use a real SSL certificate** (Let's Encrypt, commercial CA)
2. **Update nginx configuration** to use the real certificate
3. **Set proper CORS origins** (not `*`)
4. **Use a domain name** instead of IP addresses

### Let's Encrypt (Free SSL)

For production, you can use Let's Encrypt:

```bash
# Install certbot
sudo apt-get install certbot

# Generate certificate
sudo certbot certonly --standalone -d yourdomain.com

# Copy certificates to nginx/ssl/
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem nginx/ssl/cert.pem
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem nginx/ssl/key.pem
```

## File Structure

After setup, you should have:

```
project-root/
├── nginx/
│   ├── nginx.conf          # Nginx configuration
│   └── ssl/
│       ├── cert.pem        # SSL certificate
│       └── key.pem         # Private key
├── docker-compose-https.yml
├── generate-ssl-cert.bat   # Windows certificate generator
└── generate-ssl-cert.sh    # Linux/Mac certificate generator
```

## Testing Screen Sharing

Once HTTPS is set up:

1. Access the app via `https://localhost` or `https://YOUR_IP`
2. Accept the certificate warning
3. Try screen sharing in Firefox - it should work now!
4. Check browser console - should see "✅ Connected to WebSocket"

## Switching Between HTTP and HTTPS

- **HTTP (original)**: `docker-compose up`
- **HTTPS (new)**: `docker-compose -f docker-compose-https.yml up`

Both can coexist - just use different ports or stop one before starting the other.

## Security Notes

⚠️ **Self-signed certificates are for development only!**

- Browsers will show security warnings
- Not suitable for production
- Use Let's Encrypt or commercial certificates for production

## Summary

✅ **What you get:**
- HTTPS enabled on port 443
- Screen sharing works in Firefox
- Secure WebSocket connections
- Cross-device access support

✅ **Time to set up:** ~5 minutes
✅ **Difficulty:** Easy (just run the scripts)

Enjoy secure screen sharing! 🎉

