# Architecture

GuardianLink uses a client-server architecture.

The Android client pairs to the FastAPI server using a short-lived pairing ID. The server issues a random device ID and token. The device then uses that token for heartbeat, opt-in location updates and polling for safe commands such as Ring.

A QR code is only a pairing bootstrap. It is not a mechanism for silent installation or bypassing Android permissions.
