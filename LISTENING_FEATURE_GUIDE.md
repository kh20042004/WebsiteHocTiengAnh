# Listening Exercises Implementation Guide

## Overview
This guide explains how to implement and use the Listening (Luyện Nghe) feature in the English 12 Smart platform.

The feature consists of:
1. **Lesson-level Audio** - Auto-generated audio from lesson content
2. **Exercise-level Audio** - Audio specifically for listening comprehension exercises
3. **Question-level Audio** - Individual audio files for each question

---

## Architecture

### Component Diagram
```
┌─ Python Microservice (Port 5000) ──────────────┐
│  Edge-TTS: Text → Audio Conversion             │
│  - Input: Text content                          │
│  - Output: MP3 audio (Base64 encoded)          │
│  - 11+ professional English voices              │
└────────────────────────────────────────────────┘
           ↓
┌─ Java Spring Boot Service Layer ───────────────┐
│  1. EdgeTTSClient: Call Python service          │
│  2. LessonAudioService: Save to DB + Cloudinary│
│  3. LessonAudioController: REST endpoints       │
└────────────────────────────────────────────────┘
           ↓
┌─ Database (MongoDB) ──────────────────────────┐
│  Lesson:                                        │
│  - audioUrl: String (Cloudinary URL)           │
│  - audioGeneratedAt: LocalDateTime             │
│  - audioVoiceType: String                      │
│  - audioStatus: String (PENDING|COMPLETED)     │
│                                                 │
│  Exercise:                                     │
│  - audioUrl: String                            │
│  - listeningPrompt: String                     │
│  - listeningRepeatCount: Integer              │
│  - transcriptionText: String                  │
│  - showTranscriptionAfter: Boolean            │
│                                                 │
│  Question:                                     │
│  - audioUrl: String (question-specific audio) │
│  - audioTimeoutSeconds: Integer               │
└────────────────────────────────────────────────┘
           ↓
┌─ Frontend (Thymeleaf + JavaScript) ──────────┐
│  lesson.html:                                  │
│  - [Generate Audio] button (for teachers)     │
│  - Enhanced audio player (speed, loop, etc)   │
│                                                 │
│  JavaScript:                                   │
│  - audio-generator.js: Trigger generation      │
│  - audio-player.js: Player controls           │
└────────────────────────────────────────────────┘
```

---

## Entity Changes

### Lesson Entity
Added fields for audio generation tracking:
```java
private LocalDateTime audioGeneratedAt;  // When audio was generated
private String audioVoiceType;           // Voice used (e.g., en-US-AriaNeural)
private String audioStatus;              // PENDING, COMPLETED, FAILED
```

### Exercise Entity
Added fields for listening exercises:
```java
private String audioUrl;                 // Audio file for exercise
private String listeningPrompt;         // Specific instructions
private Integer listeningRepeatCount;   // How many times to replay (-1 = unlimited)
private String transcriptionText;        // Transcript of audio
private Boolean showTranscriptionAfter;  // Show transcript after attempt
```

### Question Entity
Added fields for question-level audio:
```java
private String audioUrl;                 // Audio for this specific question
private Integer audioTimeoutSeconds;     // Max listen time
```

---

## REST API Endpoints

### 1. Generate Audio for Lesson
```
POST /api/lessons/{lessonId}/generate-audio
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Body:
{
  "voice": "en-US-AriaNeural",  // Optional: default is en-US-AriaNeural
  "rate": "+0%",                 // Optional: speech rate
  "pitch": "+0Hz"                // Optional: pitch adjustment
}

Response:
{
  "success": true,
  "message": "Audio generated successfully",
  "data": {
    "lessonId": "lesson123",
    "audioUrl": "https://res.cloudinary.com/.../lesson_123.mp3",
    "voice": "en-US-AriaNeural",
    "generatedAt": "2026-04-08T10:30:00"
  }
}
```

### 2. Check Audio Status
```
GET /api/lessons/{lessonId}/audio-status
Authorization: Bearer <JWT_TOKEN>

Response:
{
  "success": true,
  "data": {
    "lessonId": "lesson123",
    "hasAudio": true,
    "audioUrl": "https://res.cloudinary.com/.../lesson_123.mp3",
    "audioStatus": "COMPLETED",
    "voice": "en-US-AriaNeural"
  }
}
```

### 3. Delete Audio
```
DELETE /api/lessons/{lessonId}/audio
Authorization: Bearer <JWT_TOKEN>

Response:
{
  "success": true,
  "message": "Audio deleted successfully",
  "data": {
    "lessonId": "lesson123",
    "message": "Audio has been deleted"
  }
}
```

### 4. Get Available Voices
```
GET /api/tts/voices

Response:
{
  "success": true,
  "data": {
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
}
```

---

## Frontend Features

### 1. Lesson Audio Section
Teachers can generate audio from lesson content:

```html
<!-- If audio doesn't exist -->
<button data-action="generate-audio" data-lesson-id="lesson123">
  <iconify-icon icon="mdi:microphone-plus-outline"></iconify-icon>
  Generate Audio (AI Voice)
</button>

<!-- If audio exists -->
<div class="audio-section">
  <audio controls data-enhanced-player="true">
    <source src="https://cloudinary.com/.../audio.mp3" type="audio/mpeg">
  </audio>
  <!-- Enhanced controls automatically added by JavaScript -->
  <!-- Speed: 0.75x, 1x, 1.25x, 1.5x, 2x -->
  <!-- Loop checkbox -->
  <!-- Download button -->
</div>
```

### 2. Teacher UI
- [Generate Audio] button with loading state
- Success/error messages with toasts
- Re-generate with custom voice selection
- Delete audio function

### 3. Student Audio Player
Enhanced HTML5 audio player with:
- **Speed Control**: 0.75x, 1x, 1.25x, 1.5x, 2x
- **Loop Mode**: Repeat audio continuously
- **Download**: Save audio to device
- **Transcription Toggle**: Show/hide lesson content

### 4. JavaScript Classes
- `AudioGenerator`: Handles TTS generation requests
- `EnhancedAudioPlayer`: Adds player controls to HTML5 `<audio>` tag

---

## Server Configuration

### pom.xml Dependencies
```xml
<!-- Already included:
  - spring-boot-starter-web
  - spring-boot-starter-security
  - spring-cloud-starter-feign (for REST calls)
  - spring-boot-starter-data-mongodb
-->

<!-- Add if needed:
  - commons-io (for file operations)
  - rest-assured (for testing)
-->
```

### application.properties
```properties
# TTS Service Configuration
tts.service.url=http://localhost:5000
tts.default.voice=en-US-AriaNeural
tts.default.rate=+0
tts.default.pitch=+0Hz
tts.max.text.length=5000
tts.connect.timeout=30000

# Cloudinary Configuration (already present)
cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET
```

### RestTemplate Bean
Ensure RestTemplate is available as Spring bean:
```java
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Optional: add interceptors, error handlers, etc.
        return restTemplate;
    }
}
```

---

## Usage Workflow

### For Teachers

#### 1. Generate Audio for Lesson
1. Open lesson editor
2. Click [Generate Audio] button
3. Select voice (optional, defaults to Aria)
4. Wait for generation to complete (30-60 seconds depending on text length)
5. Audio URL is automatically saved to lesson.audioUrl

#### 2. View Generated Audio
- Audio appears in lesson.html under "Listening" section
- Teacher can download or share the URL

#### 3. Manage Audio
- **Re-generate**: Click [Regenerate] to create with different voice
- **Delete**: Click [Delete] to remove and freed up Cloudinary space

#### 4. Create Listening Exercise
1. Create Exercise with type: LISTENING_COMPREHENSION
2. Set `audioUrl` (can use lesson audio or upload separate file)
3. Set `listeningPrompt` (e.g., "Listen and choose the correct answer")
4. Set `listeningRepeatCount` (how many times to allow replay)
5. Add questions of any format (multiple choice, short answer, etc.)
6. Optionally set `showTranscriptionAfter` to reveal content after attempt

### For Students

#### 1. Listen to Lesson Audio
1. Navigate to lesson
2. Scroll to "Listening" section
3. Click play button
4. Use controls:
   - Slow down audio if needed (0.75x)
   - Loop to repeat multiple times
   - Download for offline listening
   - Toggle to see lesson content

#### 2. Do Listening Exercise
1. Click on listening exercise
2. Listen to audio (can replay up to N times)
3. Answer questions
4. Submit answers
5. (Optional) View transcript if enabled by teacher

---

## Error Handling

### Common Errors

#### 1. TTS Service Not Running
```
Error: Cannot connect to TTS service

Solution:
1. Start Python service: python python-services/edge-tts-service/app.py
2. Check if running on http://localhost:5000/health
3. Configure correct TTS_SERVICE_URL in application.properties
```

#### 2. Text Too Long
```
Error: Text too long (6000 characters). Maximum is 5000.

Solution:
1. Shorten lesson content
2. Or increase TTS_MAX_TEXT_LENGTH config (not recommended)
```

#### 3. Cloudinary Upload Failed
```
Error: Failed to upload audio

Solution:
1. Verify Cloudinary credentials are correct
2. Check Cloudinary account has enough storage
3. Ensure CORS is configured properly
```

#### 4. Audio Not Generating
```
Error: TTS generation failed

Solution:
1. Check Python service logs
2. Verify lesson content is not empty
3. Ensure voice code is valid
4. Check network connectivity
```

---

## Performance Considerations

### Caching
- Audio URLs are cached in MongoDB (lesson.audioUrl)
- Don't regenerate unless content changes significantly
- Cloudinary provides CDN caching automatically

### Optimization
- Text limit: 5000 characters per audio file
- Long lessons: Split into multiple lessons
- Audio file size: ~1-3 MB per minute of speech
- Bandwidth: Stream from Cloudinary CDN (not from server)

### Scalability
- Python TTS service can run in Docker container
- Horizontal scaling: Run multiple Python service instances behind load balancer
- Database indexes on `audioUrl` and `audioStatus` fields

---

## Testing

### Unit Tests
```java
@Test
public void testGenerateAudio() {
    // Mock EdgeTTSClient
    // Mock LessonRepository
    // Test: generateAndSaveAudio(lessonId, voiceType)
    // Assert: lesson.audioUrl is set and not null
}

@Test
public void testDeleteAudio() {
    // Setup lesson with audioUrl
    // Call deleteAudio()
    // Assert: lesson.audioUrl is null
}

@Test
public void testValidateTextLength() {
    // Test: text > 5000 chars should throw exception
}
```

### Integration Tests
```bash
# Start Python service
docker run -p 5000:5000 edge-tts-service

# Test API endpoint
curl -X POST http://localhost:8080/api/lessons/1/generate-audio \
  -H "Authorization: Bearer token" \
  -H "Content-Type: application/json" \
  -d '{"voice":"en-US-AriaNeural"}'

# Check response
# Should return: {"success":true,"audioUrl":"https://...","generatedAt":"..."}
```

---

## Future Enhancements

### Phase 8 (Audio Player Controls)
- ✓ Speed control (0.75x - 2x)
- ✓ Loop/repeat
- ✓ Download
- Planned: Playback position indicator
- Planned: Volume control
- Planned: Audio spectrum visualizer

### Phase 9 (Testing & Error Handling)
- Comprehensive error messages
- Retry logic for failed generations
- Fallback to default voice if unavailable
- Async audio generation (background job)

### Phase 10 (Deployment)
- Docker setup for both Java and Python services
- Kubernetes manifests for cloud deployment
- CI/CD pipeline integration
- Monitoring and alerts

### Additional Ideas
- Automatic transcription (speech-to-text)
- Text-to-speech accuracy scoring
- Student recording (speech practice)
- Voice comparison (student vs. native speaker)
- Pronunciation analysis

---

## Support & Troubleshooting

### Useful Commands

#### Check TTS Service Health
```bash
curl http://localhost:5000/health
```

#### Test Voice List
```bash
curl http://localhost:5000/api/tts/voices
```

#### Generate Test Audio
```bash
curl -X POST http://localhost:5000/api/tts/generate \
  -H "Content-Type: application/json" \
  -d '{
    "text":"Hello world",
    "voice":"en-US-AriaNeural"
  }' \
  -o test-audio.mp3
```

#### View Logs
```bash
# Java application
tail -f logs/spring-boot.log

# Python service (if running locally)
# Output in terminal

# Docker containers
docker logs -f edge-tts-service
docker logs -f english-12-smart
```

---

## References

- [Edge-TTS GitHub](https://github.com/rany2/edge-tts)
- [Cloudinary Java SDK](https://cloudinary.com/documentation/java_integration)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
- [HTML5 Audio API](https://developer.mozilla.org/en-US/docs/Web/API/HTMLAudioElement)

---

**Last Updated**: April 8, 2026  
**Version**: 1.0
