# Edge-TTS Microservice

Python microservice for generating high-quality English audio from text using Microsoft Edge TTS voices.

## 📋 Features

- ✅ 11+ professional English voices (US, UK, Australian, Canadian)
- ✅ Adjustable speech rate and pitch
- ✅ MP3 audio output format
- ✅ Base64 response option for API integration
- ✅ Text length validation (max 5000 chars)
- ✅ CORS enabled for cross-origin requests
- ✅ Health check endpoint
- ✅ Docker support

## 🚀 Quick Start

### Option 1: Direct Python (Development)

```bash
# 1. Install dependencies
pip install -r requirements.txt

# 2. Create .env (optional)
cp .env.example .env

# 3. Run Flask server
python app.py

# Server running at: http://localhost:5000
```

### Option 2: Docker

```bash
# 1. Build image
docker build -t edge-tts-service .

# 2. Run container
docker run -p 5000:5000 edge-tts-service

# 3. Test
curl http://localhost:5000/health
```

### Option 3: Docker Compose (with Spring Boot)

```bash
# See docker-compose.yml in project root
docker-compose up -d
```

---

## 📡 API Endpoints

### 1. Health Check
```
GET /health
```

Response:
```json
{
  "status": "healthy",
  "service": "edge-tts-service",
  "timestamp": "2026-04-08T10:30:00.123456"
}
```

---

### 2. List Available Voices
```
GET /api/tts/voices
```

Response:
```json
{
  "available_voices": [
    {
      "code": "en-US-AriaNeural",
      "name": "Aria (US)",
      "gender": "Female"
    },
    {
      "code": "en-US-BrianNeural",
      "name": "Brian (US)",
      "gender": "Male"
    },
    ...
  ],
  "default_voice": "en-US-AriaNeural"
}
```

---

### 3. Generate Audio (MP3 File)
```
POST /api/tts/generate
Content-Type: application/json

Request JSON:
{
  "text": "Hello, how are you today?",
  "voice": "en-US-AriaNeural",
  "rate": "+0%",
  "pitch": "+0Hz"
}
```

**Response:** MP3 audio file (binary)

**Example using curl:**
```bash
curl -X POST http://localhost:5000/api/tts/generate \
  -H "Content-Type: application/json" \
  -d '{"text":"Hello world","voice":"en-US-AriaNeural"}' \
  -o audio.mp3
```

---

### 4. Generate Audio (Base64 Response)
```
POST /api/tts/generate-base64
Content-Type: application/json

Request JSON:
{
  "text": "Hello, how are you today?",
  "voice": "en-US-AriaNeural",
  "rate": "+0%",
  "pitch": "+0Hz"
}
```

Response JSON:
```json
{
  "success": true,
  "audio_full": "SUQz....",
  "mime_type": "audio/mpeg",
  "size_bytes": 12345,
  "generated_at": "2026-04-08T10:30:00",
  "voice": "en-US-AriaNeural"
}
```

---

## 🎙️ Available Voices

| Code | Name | Gender |
|------|------|--------|
| `en-US-AriaNeural` | Aria (US) | Female |
| `en-US-BrianNeural` | Brian (US) | Male |
| `en-US-JennyNeural` | Jenny (US) | Female |
| `en-US-GuyNeural` | Guy (US) | Male |
| `en-GB-SoniaNeural` | Sonia (UK) | Female |
| `en-GB-ThomasNeural` | Thomas (UK) | Male |
| `en-GB-LibbyNeural` | Libby (UK) | Female |
| `en-AU-NatashaNeural` | Natasha (AU) | Female |
| `en-AU-WilliamNeural` | William (AU) | Male |
| `en-CA-ClaraNeural` | Clara (CA) | Female |
| `en-CA-LiamNeural` | Liam (CA) | Male |

---

## ⚙️ Configuration

### Environment Variables

```env
# Server
PORT=5000                          # Service port
FLASK_ENV=development|production   # Flask environment

# TTS Settings
TTS_RATE="+0%"                     # Speech rate (-100% to +200%)
TTS_PITCH="+0Hz"                   # Pitch adjustment
DEFAULT_VOICE=en-US-AriaNeural    # Default voice if not specified
```

### Speech Rate

- `+0%` = Normal speed (1.0x)
- `+50%` = Faster (1.5x)
- `-50%` = Slower (0.5x)
- Range: -100% to +200%

### Speech Pitch

- `+0Hz` = Normal pitch
- `+10Hz` = Higher pitch
- `-10Hz` = Lower pitch

---

## 🧪 Testing

### Test Health
```bash
curl http://localhost:5000/health
```

### Test Voice List
```bash
curl http://localhost:5000/api/tts/voices
```

### Test Audio Generation
```bash
# Generate MP3 file
curl -X POST http://localhost:5000/api/tts/generate \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Welcome to English 12 Smart",
    "voice": "en-US-AriaNeural",
    "rate": "+0%",
    "pitch": "+0Hz"
  }' \
  -o lesson-audio.mp3

# Play the audio
# Windows: start lesson-audio.mp3
# macOS: open lesson-audio.mp3
# Linux: mpg123 lesson-audio.mp3
```

### Test with Python
```python
import requests

# Generate base64 audio
response = requests.post(
    'http://localhost:5000/api/tts/generate-base64',
    json={
        'text': 'Hello world',
        'voice': 'en-US-AriaNeural'
    }
)

data = response.json()
if data['success']:
    print(f"Audio generated: {data['size_bytes']} bytes")
    # Use data['audio_full'] in your application
```

---

## 📊 Error Codes

| Code | Message | Solution |
|------|---------|----------|
| 400 | Text is required | Provide text in request |
| 400 | Text cannot be empty | Remove empty/whitespace text |
| 413 | Text too long | Keep text under 5000 characters |
| 500 | Server error | Check service logs |

---

## 🔧 Development

### Run Tests
```bash
# (Tests can be added later)
pytest tests/
```

### View Logs
```bash
# Docker logs (follow)
docker logs -f edge-tts-service

# Flask logs
tail -f logs/server.log
```

### Stop Service
```bash
# Docker
docker stop edge-tts-service

# Python (Ctrl+C in terminal)
```

---

## 🐛 Troubleshooting

### Service Won't Start
```
Error: Address already in use (port 5000)

Solution:
1. Kill existing process: lsof -ti :5000 | xargs kill -9
2. Or use different port: PORT=5001 python app.py
```

### No Audio Generated
```
Error: NoAudioReceived

Solution:
1. Check internet connection (Edge-TTS needs internet)
2. Verify voice code is correct
3. Check text is not empty
4. Check service logs for details
```

### Timeout Issues
```
Request times out for long text

Solution:
1. Reduce text length (split into chunks)
2. Increase timeout in client (default 60s)
3. Use rate adjustment: "-50%" for slow speech
```

---

## 📦 Dependencies

- **flask** - Web framework
- **flask-cors** - Cross-Origin Resource Sharing
- **edge-tts** - Microsoft Edge TTS engine
- **python-dotenv** - Environment configuration
- **requests** - HTTP library
- **gunicorn** - WSGI server

---

## 📝 License

MIT License - See LICENSE file

---

## 🔗 Integration with Spring Boot

This service is designed to be called by the English 12 Smart Java backend:

1. **From Java**: `POST /api/lessons/{id}/generate-audio`
2. **Java calls**: `POST http://localhost:5000/api/tts/generate-base64`
3. **Returns**: Base64 audio + Cloudinary upload
4. **Saves**: Audio URL to MongoDB Lesson entity

See `LessonAudioService.java` for integration details.

---

## 📧 Support

For issues or questions about the TTS service:
1. Check logs: `docker logs edge-tts-service`
2. Test endpoint directly: `curl http://localhost:5000/health`
3. Verify network connectivity
4. Check Python version: `python --version` (requires 3.9+)
