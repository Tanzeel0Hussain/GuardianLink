# GuardianLink

**Privacy-first, consent-based personal Android device management platform**

GuardianLink is an open-source portfolio project for pairing and managing **your own Android devices** from a laptop dashboard.

## GitHub About
Privacy-first Android device management with QR pairing, multi-device monitoring, consent-based location sharing and remote ring.

## Features
- QR-based device pairing
- Multiple devices on one dashboard
- Unique device ID and token
- Heartbeat / last-seen monitoring
- Consent-based location endpoint
- Remote Ring command
- FastAPI + SQLite backend
- Kotlin Android starter client
- Security and architecture documentation

## Safety Boundaries
GuardianLink intentionally does **not** include silent installation, hidden/stealth behavior, covert camera/microphone access, WhatsApp extraction, credential theft, permission bypassing, or destructive remote wipe outside legitimate managed-device deployments.

## Quick Start

```bash
cd server
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```

Open `http://127.0.0.1:8000`.

## Android
Open the `android/` folder in Android Studio. The starter client contains pairing, token storage, heartbeat and command polling.

## Suggested GitHub Topics
`android` `cybersecurity` `fastapi` `kotlin` `device-management` `qr-code` `privacy` `security` `python`

## Repository Description
> Privacy-first Android device management platform with QR pairing, multi-device monitoring, consent-based location sharing and remote ring.

## License
MIT.
