# HTTPS Quick Start - 3 Steps

## Step 1: Generate Certificate (One-time setup)

**Windows:**
```bash
generate-ssl-cert.bat
```

**Linux/Mac:**
```bash
chmod +x generate-ssl-cert.sh
./generate-ssl-cert.sh
```

## Step 2: Start with HTTPS

**Windows:**
```bash
start-https.bat
```

**Linux/Mac:**
```bash
chmod +x start-https.sh
./start-https.sh
```

## Step 3: Access the App

Open your browser:
```
https://localhost
```

Or from another device:
```
https://YOUR_SERVER_IP
```

**⚠️ Important:** Click "Advanced" → "Proceed" when you see the security warning (this is normal for self-signed certificates).

---

## That's It! 🎉

Screen sharing should now work in Firefox!

For detailed information, see [HTTPS_SETUP_GUIDE.md](HTTPS_SETUP_GUIDE.md)

