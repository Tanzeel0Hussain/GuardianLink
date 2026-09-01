# API

- `POST /api/pairing/create` — create pairing session
- `GET /api/pairing/{id}/qr` — return QR PNG
- `POST /api/pairing/{id}/claim` — register device
- `POST /api/devices/{id}/heartbeat` — update last-seen
- `POST /api/devices/{id}/location` — opt-in location update
- `POST /api/devices/{id}/commands/ring` — queue Ring
- `GET /api/devices/{id}/commands/next` — get pending command
- `POST /api/devices/{id}/commands/{cmd}/ack` — acknowledge command
