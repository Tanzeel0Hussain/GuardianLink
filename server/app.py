from datetime import datetime, timedelta, timezone
from io import BytesIO
from pathlib import Path
from typing import Optional
import secrets
import sqlite3

import qrcode
from fastapi import FastAPI, HTTPException, Request, Header
from fastapi.responses import HTMLResponse, StreamingResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from pydantic import BaseModel

BASE = Path(__file__).resolve().parent
DB = BASE / "guardianlink.db"

app = FastAPI(title="GuardianLink API", version="0.1.0")
app.mount("/static", StaticFiles(directory=str(BASE / "static")), name="static")
templates = Jinja2Templates(directory=str(BASE / "templates"))

def utcnow():
    return datetime.now(timezone.utc)

def connect():
    c = sqlite3.connect(DB)
    c.row_factory = sqlite3.Row
    return c

def init_db():
    sql = (
        "CREATE TABLE IF NOT EXISTS pairing_sessions("
        "id TEXT PRIMARY KEY, created_at TEXT, expires_at TEXT, claimed INTEGER DEFAULT 0);"
        "CREATE TABLE IF NOT EXISTS devices("
        "id TEXT PRIMARY KEY, name TEXT, model TEXT, android_version TEXT, token TEXT UNIQUE,"
        "created_at TEXT, last_seen TEXT, latitude REAL, longitude REAL);"
        "CREATE TABLE IF NOT EXISTS commands("
        "id INTEGER PRIMARY KEY AUTOINCREMENT, device_id TEXT, command TEXT,"
        "status TEXT DEFAULT 'pending', created_at TEXT);"
    )
    with connect() as c:
        c.executescript(sql)

init_db()

class ClaimBody(BaseModel):
    device_name: str
    model: Optional[str] = None
    android_version: Optional[str] = None

class HeartbeatBody(BaseModel):
    battery: Optional[int] = None

class LocationBody(BaseModel):
    latitude: float
    longitude: float

def require_device(device_id: str, authorization: Optional[str]):
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(401, "Missing device token")
    token = authorization.split(" ", 1)[1]
    with connect() as c:
        row = c.execute(
            "SELECT id FROM devices WHERE id=? AND token=?",
            (device_id, token),
        ).fetchone()
    if not row:
        raise HTTPException(401, "Invalid device token")

@app.get("/", response_class=HTMLResponse)
def dashboard(request: Request):
    with connect() as c:
        devices = c.execute(
            "SELECT * FROM devices ORDER BY created_at DESC"
        ).fetchall()
    return templates.TemplateResponse(
        "index.html", {"request": request, "devices": devices}
    )

@app.post("/api/pairing/create")
def create_pairing(request: Request):
    pairing_id = secrets.token_urlsafe(18)
    created = utcnow()
    expires = created + timedelta(minutes=10)
    with connect() as c:
        c.execute(
            "INSERT INTO pairing_sessions VALUES(?,?,?,0)",
            (pairing_id, created.isoformat(), expires.isoformat()),
        )
    base = str(request.base_url).rstrip("/")
    return {
        "pairing_id": pairing_id,
        "expires_at": expires.isoformat(),
        "qr_url": f"{base}/api/pairing/{pairing_id}/qr",
    }

@app.get("/api/pairing/{pairing_id}/qr")
def pairing_qr(pairing_id: str, request: Request):
    with connect() as c:
        row = c.execute(
            "SELECT id FROM pairing_sessions WHERE id=?", (pairing_id,)
        ).fetchone()
    if not row:
        raise HTTPException(404, "Pairing session not found")

    base = str(request.base_url).rstrip("/")
    payload = f"guardianlink://pair?server={base}&pairing_id={pairing_id}"
    image = qrcode.make(payload)
    buf = BytesIO()
    image.save(buf, format="PNG")
    buf.seek(0)
    return StreamingResponse(buf, media_type="image/png")

@app.post("/api/pairing/{pairing_id}/claim")
def claim_pairing(pairing_id: str, body: ClaimBody):
    current = utcnow()
    with connect() as c:
        pair = c.execute(
            "SELECT * FROM pairing_sessions WHERE id=?", (pairing_id,)
        ).fetchone()
        if not pair:
            raise HTTPException(404, "Pairing session not found")
        if pair["claimed"]:
            raise HTTPException(409, "Pairing already claimed")
        if datetime.fromisoformat(pair["expires_at"]) < current:
            raise HTTPException(410, "Pairing expired")

        device_id = secrets.token_urlsafe(12)
        token = secrets.token_urlsafe(32)
        c.execute(
            "INSERT INTO devices VALUES(?,?,?,?,?,?,?,?,?)",
            (
                device_id, body.device_name, body.model, body.android_version,
                token, current.isoformat(), current.isoformat(), None, None
            ),
        )
        c.execute(
            "UPDATE pairing_sessions SET claimed=1 WHERE id=?", (pairing_id,)
        )
    return {"device_id": device_id, "device_token": token}

@app.post("/api/devices/{device_id}/heartbeat")
def heartbeat(
    device_id: str,
    body: HeartbeatBody,
    authorization: Optional[str] = Header(default=None),
):
    require_device(device_id, authorization)
    with connect() as c:
        c.execute(
            "UPDATE devices SET last_seen=? WHERE id=?",
            (utcnow().isoformat(), device_id),
        )
    return {"ok": True}

@app.post("/api/devices/{device_id}/location")
def location(
    device_id: str,
    body: LocationBody,
    authorization: Optional[str] = Header(default=None),
):
    require_device(device_id, authorization)
    with connect() as c:
        c.execute(
            "UPDATE devices SET latitude=?, longitude=?, last_seen=? WHERE id=?",
            (body.latitude, body.longitude, utcnow().isoformat(), device_id),
        )
    return {"ok": True}

@app.post("/api/devices/{device_id}/commands/ring")
def ring(device_id: str):
    with connect() as c:
        if not c.execute(
            "SELECT id FROM devices WHERE id=?", (device_id,)
        ).fetchone():
            raise HTTPException(404, "Device not found")
        c.execute(
            "INSERT INTO commands(device_id,command,status,created_at) "
            "VALUES(?, 'ring', 'pending', ?)",
            (device_id, utcnow().isoformat()),
        )
    return {"ok": True}

@app.get("/api/devices/{device_id}/commands/next")
def next_command(
    device_id: str,
    authorization: Optional[str] = Header(default=None),
):
    require_device(device_id, authorization)
    with connect() as c:
        row = c.execute(
            "SELECT * FROM commands WHERE device_id=? AND status='pending' "
            "ORDER BY id LIMIT 1",
            (device_id,),
        ).fetchone()
    if not row:
        return {"command": None}
    return {"id": row["id"], "command": row["command"]}

@app.post("/api/devices/{device_id}/commands/{command_id}/ack")
def ack(
    device_id: str,
    command_id: int,
    authorization: Optional[str] = Header(default=None),
):
    require_device(device_id, authorization)
    with connect() as c:
        c.execute(
            "UPDATE commands SET status='done' WHERE id=? AND device_id=?",
            (command_id, device_id),
        )
    return {"ok": True}
