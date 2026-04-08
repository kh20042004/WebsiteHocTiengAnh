package com.english12smart.controller;

import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.service.EdgeTTSClient;
import com.english12smart.service.LessonAudioService;
import com.english12smart.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ========== LESSON AUDIO API CONTROLLER ==========
 * REST API để quản lý audio cho Lesson:
 * - Generate audio từ lesson content bằng Edge-TTS
 * - Upload lên Cloudinary
 * - Delete audio
 * - Check audio status
 *
 * Phân quyền:
 * - GET (audio-status, voices): Mọi user đã đăng nhập
 * - POST (generate-audio): Chỉ TEACHER (creator) và ADMIN
 * - DELETE: Chỉ TEACHER (creator) và ADMIN
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class LessonAudioController {

    // ========== Dependencies ==========
    private final LessonAudioService lessonAudioService;
    private final EdgeTTSClient edgeTTSClient;
    private final JwtTokenProvider jwtTokenProvider;

    // ========== Request/Response DTOs ==========

    /**
     * Request DTO for audio generation
     */
    public static class GenerateAudioRequest {
        public String content;  // Custom audio content (optional)
        public String voice;
        public String rate;
        public String pitch;

        public GenerateAudioRequest() {
        }

        public GenerateAudioRequest(String content, String voice, String rate, String pitch) {
            this.content = content;
            this.voice = voice;
            this.rate = rate;
            this.pitch = pitch;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getVoice() {
            return voice;
        }

        public void setVoice(String voice) {
            this.voice = voice;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }

        public String getPitch() {
            return pitch;
        }

        public void setPitch(String pitch) {
            this.pitch = pitch;
        }
    }

    /**
     * Response DTO for audio generation
     */
    public static class GenerateAudioResponse {
        public String lessonId;
        public String audioUrl;
        public String voice;
        public String generatedAt;
        public long sizeBytes;

        public GenerateAudioResponse(String lessonId, String audioUrl, String voice, String generatedAt, long sizeBytes) {
            this.lessonId = lessonId;
            this.audioUrl = audioUrl;
            this.voice = voice;
            this.generatedAt = generatedAt;
            this.sizeBytes = sizeBytes;
        }

        public String getLessonId() {
            return lessonId;
        }

        public void setLessonId(String lessonId) {
            this.lessonId = lessonId;
        }

        public String getAudioUrl() {
            return audioUrl;
        }

        public void setAudioUrl(String audioUrl) {
            this.audioUrl = audioUrl;
        }

        public String getVoice() {
            return voice;
        }

        public void setVoice(String voice) {
            this.voice = voice;
        }

        public String getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(String generatedAt) {
            this.generatedAt = generatedAt;
        }

        public long getSizeBytes() {
            return sizeBytes;
        }

        public void setSizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
        }
    }

    /**
     * Response DTO for audio status
     */
    public static class AudioStatusResponse {
        public String lessonId;
        public boolean hasAudio;
        public String audioUrl;
        public String audioStatus;
        public String voice;

        public AudioStatusResponse(String lessonId, boolean hasAudio, String audioUrl, String audioStatus, String voice) {
            this.lessonId = lessonId;
            this.hasAudio = hasAudio;
            this.audioUrl = audioUrl;
            this.audioStatus = audioStatus;
            this.voice = voice;
        }
    }

    // ========== API ENDPOINTS ==========

    /**
     * GET /api/tts/voices — Lấy danh sách các giọng khả dụng
     * Accessible by: Mọi user
     */
    @GetMapping("/tts/voices")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getAvailableVoices() {
        log.info("API: Lấy danh sách giọng TTS khả dụng");

        Map<String, Object> voices = new HashMap<>();
        voices.put("available_voices", new Object[]{
                new VoiceInfo("en-US-AriaNeural", "Aria (US)", "Female"),
                new VoiceInfo("en-US-BrianNeural", "Brian (US)", "Male"),
                new VoiceInfo("en-US-JennyNeural", "Jenny (US)", "Female"),
                new VoiceInfo("en-US-GuyNeural", "Guy (US)", "Male"),
                new VoiceInfo("en-GB-SoniaNeural", "Sonia (UK)", "Female"),
                new VoiceInfo("en-GB-ThomasNeural", "Thomas (UK)", "Male"),
                new VoiceInfo("en-GB-LibbyNeural", "Libby (UK)", "Female"),
                new VoiceInfo("en-AU-NatashaNeural", "Natasha (AU)", "Female"),
                new VoiceInfo("en-AU-WilliamNeural", "William (AU)", "Male"),
                new VoiceInfo("en-CA-ClaraNeural", "Clara (CA)", "Female"),
                new VoiceInfo("en-CA-LiamNeural", "Liam (CA)", "Male")
        });
        voices.put("default_voice", "en-US-AriaNeural");

        return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách giọng thành công", voices));
    }

    /**
     * POST /api/lessons/{lessonId}/generate-audio — Generate audio cho lesson
     * Accessible by: TEACHER (creator), ADMIN
     *
     * Request body example (with custom content):
     * {
     *   "content": "Custom audio text to generate",
     *   "voice": "en-US-AriaNeural",
     *   "rate": "+0%",
     *   "pitch": "+0Hz"
     * }
     * 
     * Nếu không có "content", sẽ dùng lesson.content
     */
    @PostMapping("/lessons/{lessonId}/generate-audio")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<?>> generateAudio(
            @PathVariable String lessonId,
            @RequestBody(required = false) GenerateAudioRequest request) {
        log.info("API: Generate audio cho lesson: {}", lessonId);

        try {
            // Default request if null
            if (request == null) {
                request = new GenerateAudioRequest();
            }

            String voice = request.getVoice();
            String rate = request.getRate() != null ? request.getRate() : "+0%";
            String pitch = request.getPitch() != null ? request.getPitch() : "+0Hz";

            // Generate audio
            String audioUrl;
            if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
                // Use custom content if provided
                audioUrl = lessonAudioService.generateAndSaveAudioFromCustomContent(
                        lessonId, 
                        request.getContent(), 
                        voice, 
                        rate, 
                        pitch
                );
            } else if (rate.equals("+0%") && pitch.equals("+0Hz")) {
                // Use lesson content with default settings
                audioUrl = lessonAudioService.generateAndSaveAudio(lessonId, voice);
            } else {
                // Use lesson content with custom settings
                audioUrl = lessonAudioService.generateAndSaveAudioWithSettings(lessonId, voice, rate, pitch);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("lessonId", lessonId);
            response.put("audioUrl", audioUrl);
            response.put("voice", voice);

            return ResponseEntity.ok(ApiResponseDTO.success("Audio được tạo thành công", response));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error(400, "Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating audio: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.error(500, "Error generating audio: " + e.getMessage()));
        }
    }

    /**
     * GET /api/lessons/{lessonId}/audio-status — Check audio status
     * Accessible by: Any authenticated user
     */
    @GetMapping("/lessons/{lessonId}/audio-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<?>> getAudioStatus(
            @PathVariable String lessonId) {
        log.info("API: Check audio status cho lesson: {}", lessonId);

        try {
            boolean hasAudio = lessonAudioService.hasAudio(lessonId);
            String audioUrl = lessonAudioService.getAudioUrl(lessonId);
            String audioStatus = lessonAudioService.getAudioStatus(lessonId);

            AudioStatusResponse response = new AudioStatusResponse(
                    lessonId,
                    hasAudio,
                    audioUrl,
                    audioStatus,
                    null
            );

            return ResponseEntity.ok(ApiResponseDTO.success("Lấy trạng thái audio thành công", response));

        } catch (Exception e) {
            log.error("Error getting audio status: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.error(500, "Error getting audio status"));
        }
    }

    /**
     * DELETE /api/lessons/{lessonId}/audio — Delete audio cho lesson
     * Accessible by: TEACHER (creator), ADMIN
     */
    @DeleteMapping("/lessons/{lessonId}/audio")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<?>> deleteAudio(
            @PathVariable String lessonId) {
        log.info("API: Delete audio cho lesson: {}", lessonId);

        try {
            lessonAudioService.deleteAudio(lessonId);

            Map<String, Object> response = new HashMap<>();
            response.put("lessonId", lessonId);
            response.put("message", "Audio đã được xóa thành công");

            return ResponseEntity.ok(ApiResponseDTO.success("Audio đã được xóa", response));

        } catch (Exception e) {
            log.error("Error deleting audio: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.error(500, "Error deleting audio: " + e.getMessage()));
        }
    }

    // ========== Helper Classes ==========

    /**
     * Voice info DTO
     */
    public static class VoiceInfo {
        public String code;
        public String name;
        public String gender;

        public VoiceInfo(String code, String name, String gender) {
            this.code = code;
            this.name = name;
            this.gender = gender;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getGender() {
            return gender;
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * Extract userId from JWT token
     */
    private String extractUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                return jwtTokenProvider.getUserIdFromToken(token);
            }
        }
        return null;
    }
}
