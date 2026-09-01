# 🛡️ GuardianLink

### Privacy-First Android Device Management Platform

GuardianLink is an open-source Android device management and cybersecurity project designed to allow users to pair and manage **their own authorized Android devices** from a laptop through a web-based dashboard.

The project uses **QR-based pairing**, a **FastAPI backend**, an **SQLite database**, and an **Android Kotlin client** to establish a connection between the Android device and the laptop/server.

GuardianLink is designed for educational purposes, personal device management, cybersecurity learning, and portfolio demonstration.

---

# 📌 Project Overview

GuardianLink creates a client-server environment where:

```text
Android Phone
      │
      │ Internet / Local Network
      │
      ▼
GuardianLink FastAPI Server
      │
      ▼
SQLite Database
      │
      ▼
Web Dashboard
      │
      ▼
Laptop / Desktop Browser
```

The laptop runs the GuardianLink server.

The Android device connects to that server after being paired.

Once paired, every Android device receives its own:

- Device ID
- Authentication Token
- Device Name
- Device Model
- Android Version
- Last Seen Information

This allows multiple Android devices to be connected to the same GuardianLink dashboard.

---

# ✨ Main Features

GuardianLink currently provides the following features:

### 📱 Multi-Device Management

Multiple Android devices can be registered with the same GuardianLink server.

For example:

```text
GuardianLink Dashboard

├── Pixel 7
├── Samsung Galaxy
├── OnePlus
└── Another Android Device
```

Each device receives a unique Device ID and authentication token.

---

### 📷 QR-Based Pairing

The GuardianLink server can generate a temporary QR pairing session.

The general pairing process is:

```text
Laptop
   │
   │ Generate Pairing Session
   ▼
QR Code
   │
   │ Scan / enter pairing information
   ▼
Android GuardianLink Client
   │
   │ Register Device
   ▼
GuardianLink Server
   │
   ▼
Unique Device ID + Token
```

Pairing sessions expire after a short period for security.

> QR pairing is used only for establishing an authorized connection. Android permissions still remain under the control of the Android operating system and device owner.

---

### 💓 Device Heartbeat

A paired Android device can send heartbeat requests to GuardianLink.

This allows the dashboard to maintain information such as:

```text
Device Name
Device Model
Android Version
Last Seen
Connection Status
```

For example:

```text
Pixel 7

Model: Pixel 7
Android: 16
Last Seen: 2026-09-01 12:30
```

---

### 📍 Consent-Based Location Sharing

GuardianLink contains an API endpoint for device location.

The Android application must receive location permission from the device owner before location information can be collected.

The server can store:

```text
Latitude
Longitude
Last Update Time
```

The dashboard can then provide a link to view the last shared location on a map.

---

### 🔔 Remote Ring Command

GuardianLink includes a safe remote command system.

The laptop can queue a:

```text
RING
```

command for a paired device.

The Android client can periodically check the GuardianLink server for pending commands.

The architecture is:

```text
Laptop Dashboard
      │
      │ Ring Device
      ▼
GuardianLink Server
      │
      │ Command Queue
      ▼
Android Client
      │
      ▼
Ring Device
```

This architecture can later be extended with other legitimate device-management functionality.

---

# 🏗️ Project Architecture

GuardianLink consists of three major components.

## 1. GuardianLink Server

Technology:

```text
Python
FastAPI
Uvicorn
SQLite
Jinja2
```

Responsibilities:

- Generate pairing sessions
- Generate QR codes
- Register Android devices
- Generate authentication tokens
- Receive heartbeat requests
- Receive authorized location updates
- Maintain command queue
- Display connected devices
- Provide web dashboard

---

## 2. Android Client

Technology:

```text
Kotlin
Android Studio
OkHttp
Android APIs
```

Responsibilities:

- Connect to GuardianLink server
- Pair device
- Store Device ID
- Store Device Token
- Send heartbeat
- Send authorized device information
- Check remote command queue

---

## 3. Web Dashboard

The dashboard is provided by the FastAPI server.

It allows the user to see registered devices from a browser.

Example:

```text
------------------------------------------------
                 GuardianLink
------------------------------------------------

My Devices

Pixel 7
Android 16
Last Seen: 12:30 PM

[ View Location ]     [ Ring Device ]


Samsung Galaxy
Android 15
Last Seen: 12:28 PM

[ View Location ]     [ Ring Device ]

------------------------------------------------
```

---

# 📂 Project Structure

After downloading or cloning GuardianLink, the project structure will look similar to:

```text
GuardianLink/
│
├── README.md
├── LICENSE
├── SECURITY.md
├── CONTRIBUTING.md
├── .gitignore
│
├── server/
│   │
│   ├── app.py
│   ├── requirements.txt
│   ├── run.sh
│   │
│   ├── templates/
│   │   └── index.html
│   │
│   └── static/
│       └── style.css
│
├── android/
│   │
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   │
│   └── app/
│       ├── build.gradle.kts
│       │
│       └── src/
│           └── main/
│               ├── AndroidManifest.xml
│               │
│               ├── java/
│               │   └── com/
│               │       └── guardianlink/
│               │           └── app/
│               │               └── MainActivity.kt
│               │
│               └── res/
│                   ├── layout/
│                   │   └── activity_main.xml
│                   │
│                   └── values/
│                       └── styles.xml
│
└── docs/
    ├── ARCHITECTURE.md
    ├── API.md
    ├── SECURITY.md
    └── ROADMAP.md
```

---

# ⚙️ Requirements

The GuardianLink server is primarily intended to run on Linux.

Recommended environment:

```text
Ubuntu
Kubuntu
Debian
Linux Mint
```

You will need:

```text
Python 3
Python venv
pip
Git
Android Studio
Android Phone
```

---

# 🚀 Complete Installation Guide

The following instructions show how to run GuardianLink from the beginning.

---

# Step 1 — Install Required Linux Packages

First update your package information:

```bash
sudo apt update
```

Install Python, pip, virtual environment support, and Git:

```bash
sudo apt install python3 python3-pip python3-venv git -y
```

Check Python:

```bash
python3 --version
```

Check Git:

```bash
git --version
```

---

# Step 2 — Clone GuardianLink

Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/GuardianLink.git
```

Replace:

```text
YOUR_USERNAME
```

with your actual GitHub username.

Then enter the project:

```bash
cd GuardianLink
```

Check the files:

```bash
ls
```

You should see folders such as:

```text
android
docs
server
```

---

# Step 3 — Open Server Directory

Run:

```bash
cd server
```

Check the files:

```bash
ls
```

You should see:

```text
app.py
requirements.txt
run.sh
templates
static
```

---

# Step 4 — Create Python Virtual Environment

Create a virtual environment:

```bash
python3 -m venv .venv
```

Activate it:

```bash
source .venv/bin/activate
```

After activation, your terminal should show something similar to:

```text
(.venv) user@computer:~/GuardianLink/server$
```

This means the Python virtual environment is active.

---

# Step 5 — Install Python Dependencies

Install all required packages:

```bash
pip install -r requirements.txt
```

This installs the GuardianLink server dependencies, including:

```text
FastAPI
Uvicorn
Jinja2
QRCode
Pillow
```

---

# Step 6 — Start GuardianLink Server

Run:

```bash
uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```

Alternatively:

```bash
./run.sh
```

If `run.sh` does not have execute permission:

```bash
chmod +x run.sh
```

Then:

```bash
./run.sh
```

---

# Step 7 — Open GuardianLink Dashboard

On the same laptop, open a browser and visit:

```text
http://127.0.0.1:8000
```

You should now see the GuardianLink dashboard.

The API documentation generated automatically by FastAPI is available at:

```text
http://127.0.0.1:8000/docs
```

This page is useful during development because it allows the GuardianLink REST API to be tested directly.

---

# 🌐 Connecting an Android Phone

If the Android phone and laptop are on the same Wi-Fi network, the phone must use the laptop's local network IP instead of:

```text
127.0.0.1
```

Because `127.0.0.1` on Android refers to the Android phone itself.

---

# Step 8 — Find Laptop IP Address

On Linux run:

```bash
hostname -I
```

Example result:

```text
192.168.1.10
```

Your GuardianLink server would then be available on the local network at:

```text
http://192.168.1.10:8000
```

The actual IP address will depend on your network.

---

# Step 9 — Test Server From Android

Make sure:

1. Laptop and phone are connected to the same Wi-Fi.
2. GuardianLink server is running.
3. Port `8000` is accessible.

Open the Android browser and enter:

```text
http://YOUR_LAPTOP_IP:8000
```

Example:

```text
http://192.168.1.10:8000
```

If the GuardianLink dashboard appears, communication between the phone and laptop is working.

---

# 📱 Android Application Setup

GuardianLink contains an Android Studio project inside:

```text
android/
```

Open Android Studio.

Select:

```text
Open
```

Then select:

```text
GuardianLink/android/
```

Wait for Gradle synchronization to complete.

---

# Step 10 — Connect Android Phone to Android Studio

On your Android phone enable:

```text
Settings
→ About Phone
→ Build Number
```

Tap Build Number multiple times until Developer Options are enabled.

Then open:

```text
Settings
→ Developer Options
→ USB Debugging
```

Enable USB Debugging.

Connect the phone to the laptop using USB.

Android will display an authorization prompt.

Approve the connection.

---

# Step 11 — Run GuardianLink Android Client

Inside Android Studio select your Android phone from the device list.

Click:

```text
Run ▶
```

Android Studio will build and install the GuardianLink development application onto your authorized device.

---

# 🔗 Device Pairing Process

Once both the server and Android client are running:

### On Laptop

Open:

```text
http://127.0.0.1:8000
```

Click:

```text
Create Pairing QR
```

GuardianLink will generate:

- Pairing ID
- QR Code
- Expiration time

---

### On Android

The starter client currently supports entering the server/pairing information required by the pairing flow.

Enter the GuardianLink server address.

Example:

```text
http://192.168.1.10:8000
```

Then enter the pairing ID generated by the dashboard.

Press:

```text
Pair Device
```

---

# 🔐 What Happens During Pairing?

The Android client sends information such as:

```text
Device Name
Device Model
Android Version
Pairing ID
```

The GuardianLink server verifies the pairing session.

If valid, the server generates:

```text
Unique Device ID
+
Random Device Authentication Token
```

Example conceptually:

```text
Device ID:
J5p9K2xQ

Device Token:
random-secure-token
```

These credentials identify the paired device.

---

# 🔑 Device Authentication

After pairing, protected device requests use:

```text
Authorization: Bearer DEVICE_TOKEN
```

This prevents a random device from simply pretending to be an already paired GuardianLink device.

The token should never be uploaded manually to GitHub or placed inside source code.

---

# 💓 Testing Heartbeat

After pairing the Android device, use:

```text
Send Heartbeat
```

from the Android starter client.

The Android device sends a request to:

```text
POST /api/devices/{device_id}/heartbeat
```

The GuardianLink server updates:

```text
Last Seen
```

for that device.

Refresh the dashboard to see the updated information.

---

# 🔔 Testing Remote Ring

From the GuardianLink dashboard click:

```text
Ring Device
```

The server adds a command to the command queue:

```text
ring
```

The Android client can then check:

```text
GET /api/devices/{device_id}/commands/next
```

In the current starter implementation, command checking is initiated through the Android client's **Check Commands** action.

A future version can replace this development approach with an appropriate Android background-delivery mechanism such as Firebase Cloud Messaging or carefully designed WorkManager tasks.

---

# 📍 Location Architecture

GuardianLink also provides:

```text
POST /api/devices/{device_id}/location
```

The endpoint accepts:

```json
{
    "latitude": 33.6844,
    "longitude": 73.0479
}
```

However, Android location information must only be collected after the user explicitly grants Android location permission.

The backend endpoint is included in the project, while the Android starter client can be extended to implement the full runtime-permission and location-provider workflow.

---

# 📱 Multiple Device Support

GuardianLink supports registering multiple devices.

For example:

```text
Laptop
  │
  └── GuardianLink
       │
       ├── Device 1 — Pixel 7
       │
       ├── Device 2 — Samsung Galaxy
       │
       └── Device 3 — OnePlus
```

Each device has its own:

```text
Device ID
Authentication Token
Device Information
Last Seen
Location
Command Queue
```

Therefore, multiple authorized phones can be managed independently.

---

# 🗄️ Database

GuardianLink automatically creates an SQLite database:

```text
guardianlink.db
```

The database is created when the FastAPI server starts.

It contains tables for:

```text
pairing_sessions
devices
commands
```

You do not need to manually create the database for the development version.

---

# 🛑 Stopping GuardianLink

To stop the FastAPI server press:

```text
CTRL + C
```

To leave the Python virtual environment:

```bash
deactivate
```

---

# ▶️ Running GuardianLink Again Later

You do **not** need to reinstall everything every time.

Open Terminal:

```bash
cd GuardianLink/server
```

Activate the virtual environment:

```bash
source .venv/bin/activate
```

Start GuardianLink:

```bash
uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```

Then open:

```text
http://127.0.0.1:8000
```

---

# 🧪 FastAPI API Documentation

FastAPI automatically provides Swagger API documentation.

Start GuardianLink and open:

```text
http://127.0.0.1:8000/docs
```

You can inspect and test the API endpoints from this interface.

Main endpoints include:

```text
POST   /api/pairing/create

GET    /api/pairing/{pairing_id}/qr

POST   /api/pairing/{pairing_id}/claim

POST   /api/devices/{device_id}/heartbeat

POST   /api/devices/{device_id}/location

POST   /api/devices/{device_id}/commands/ring

GET    /api/devices/{device_id}/commands/next

POST   /api/devices/{device_id}/commands/{command_id}/ack
```

---

# 🌍 Using GuardianLink Over the Internet

The development server should **not** simply be exposed directly to the public internet.

The command:

```bash
uvicorn app:app --host 0.0.0.0 --port 8000
```

is suitable for local development and testing.

For a real internet deployment, GuardianLink should first implement:

```text
HTTPS/TLS
Dashboard Authentication
Rate Limiting
Secure Token Storage
Audit Logging
Database Hardening
CSRF Protection
Credential Rotation
Production ASGI Deployment
```

A production architecture could look like:

```text
Android
   │
   │ HTTPS
   ▼
Internet
   │
   ▼
Reverse Proxy / TLS
   │
   ▼
GuardianLink API
   │
   ├── Authentication
   ├── Device Registry
   ├── Command Service
   └── Database
```

Do not expose the development server publicly before implementing these protections.

---

# 🔒 Security Model

GuardianLink follows a consent-based device-management model.

Important principles:

### 1. Device Authorization

Only devices intentionally paired by the owner should be registered.

### 2. Unique Tokens

Every device receives its own authentication token.

### 3. Temporary Pairing

Pairing sessions expire.

### 4. Permission Transparency

Android permissions remain controlled by Android.

### 5. No Hardcoded Secrets

Passwords, API keys, device tokens, and private credentials should never be committed to GitHub.

### 6. HTTPS for Production

Production communication should always use HTTPS.

---

# ⚠️ Important Security Boundaries

GuardianLink is intended for:

- Personal device management
- Authorized Android devices
- Cybersecurity education
- Security engineering research
- Portfolio demonstrations
- Lab environments

GuardianLink is **not designed to provide**:

- Silent application installation
- Hidden spyware behavior
- Covert camera access
- Covert microphone recording
- Keylogging
- Credential theft
- WhatsApp message extraction
- Android permission bypass
- Unauthorized device access
- Stealth persistence

Any powerful device-management functionality should remain transparent and authorized by the device owner.

---

# 🛡️ Recommended Production Improvements

Before using GuardianLink outside a local lab environment, the following improvements are recommended:

```text
1. HTTPS/TLS
2. User authentication
3. Password hashing
4. Secure session management
5. PostgreSQL
6. API rate limiting
7. Device-token rotation
8. Encrypted Android token storage
9. Audit logs
10. Firebase Cloud Messaging
11. Android WorkManager
12. WebSocket support
13. Certificate pinning
14. Signed Android releases
15. Automated security testing
```

---

# 🗺️ Development Roadmap

## Phase 1 — Core Platform

- [x] FastAPI backend
- [x] SQLite database
- [x] QR generation
- [x] Device registration
- [x] Unique device tokens
- [x] Multi-device dashboard
- [x] Heartbeat endpoint
- [x] Location API
- [x] Remote Ring command
- [x] Android Kotlin starter

---

## Phase 2 — Security

- [ ] Dashboard authentication
- [ ] HTTPS
- [ ] Token encryption
- [ ] Device-token rotation
- [ ] Audit logs
- [ ] Rate limiting
- [ ] PostgreSQL migration

---

## Phase 3 — Device Monitoring

- [ ] Battery percentage
- [ ] Charging status
- [ ] Network information
- [ ] Opt-in location history
- [ ] Device online/offline status
- [ ] Geofencing
- [ ] Lost-mode message

---

## Phase 4 — Real-Time Communication

- [ ] Firebase Cloud Messaging
- [ ] WebSockets
- [ ] WorkManager integration
- [ ] Real-time dashboard updates
- [ ] Command delivery status

---

## Phase 5 — Managed Device Edition

For legitimate organization-owned or fully managed Android devices:

- [ ] Android Enterprise research
- [ ] Device Policy Controller
- [ ] Managed-device enrollment
- [ ] Security policy management
- [ ] Compliant enterprise lock/wipe functionality

---

# 🛠️ Technologies Used

### Backend

```text
Python
FastAPI
Uvicorn
SQLite
Jinja2
```

### Android

```text
Kotlin
Android Studio
OkHttp
Android SDK
```

### Frontend

```text
HTML
CSS
JavaScript
Jinja2
```

### Security

```text
Random Device Tokens
Bearer Authentication
Temporary Pairing Sessions
Permission-Based Device Access
```

---

# 🐛 Troubleshooting

## `python3: command not found`

Install Python:

```bash
sudo apt install python3
```

---

## `No module named venv`

Run:

```bash
sudo apt install python3-venv
```

---

## `uvicorn: command not found`

Make sure the virtual environment is active:

```bash
source .venv/bin/activate
```

Then:

```bash
pip install -r requirements.txt
```

---

## Android Cannot Connect to Server

Do not use:

```text
http://127.0.0.1:8000
```

from a physical Android phone.

Find the laptop IP:

```bash
hostname -I
```

Then use something like:

```text
http://192.168.1.10:8000
```

Also make sure both devices are connected to the same network.

---

## Port 8000 Already in Use

Check:

```bash
sudo ss -ltnp | grep :8000
```

Or temporarily start GuardianLink on another port:

```bash
uvicorn app:app --host 0.0.0.0 --port 8080 --reload
```

Then open:

```text
http://127.0.0.1:8080
```

---

# 🤝 Contributing

Contributions are welcome.

Useful contribution areas include:

- Authentication
- Security improvements
- Android UI
- Dashboard UI
- API testing
- Automated tests
- Documentation
- Android Enterprise integration
- Device telemetry
- Audit logging
- Accessibility

All contributions should preserve GuardianLink's transparent and consent-based security model.

---

# 📜 License

GuardianLink is released under the **MIT License**.

See:

```text
LICENSE
```

for details.

---

# ⚖️ Responsible Use

GuardianLink must only be used with devices that you own or have explicit authorization to manage.

The project is intended for:

```text
Education
Research
Personal Device Management
Cybersecurity Learning
Portfolio Development
Authorized Testing
```

The user is responsible for following applicable laws and platform policies.

---

# 👨‍💻 Project Purpose

GuardianLink demonstrates practical knowledge of:

- Android development
- Python backend development
- REST APIs
- Client-server architecture
- Authentication
- Device registration
- QR-based pairing
- Database management
- Cybersecurity principles
- Secure software architecture
- Multi-device management

It can therefore be used as a cybersecurity/software engineering portfolio project while providing a foundation for further research into legitimate Android device-management technologies.

---

## 🛡️ GuardianLink

**Connect. Monitor. Protect.**

> A privacy-first approach to personal Android device management.
