package com.english12smart.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.english12smart.entity.Lesson;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * ========== LESSON AUDIO SERVICE ==========
 * Service xử lý audio generation cho Lesson:
 * 1. Gọi Edge-TTS để generate audio từ content
 * 2. Upload audio lên Cloudinary
 * 3. Save audioUrl vào Lesson entity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LessonAudioService {

    // ========== Dependencies ==========
    private final EdgeTTSClient edgeTTSClient;
    private final LessonRepository lessonRepository;
    private final Cloudinary cloudinary;

    // ========== Constants ==========
    private static final String CLOUDINARY_FOLDER = "lesson-audio";
    private static final String DEFAULT_VOICE = "en-US-AriaNeural";

    // ========== Configuration ==========

    /**
     * Generate audio from Lesson content và save URL
     * @param lessonId - ID of lesson
     * @param voiceType - Voice for TTS (optional, use default if null)
     * @return Generated audio URL from Cloudinary
     * @throws Exception if lesson not found or generation fails
     */
    @Transactional
    public String generateAndSaveAudio(String lessonId, String voiceType) throws Exception {
        log.info("Starting audio generation for lesson: {}", lessonId);

        // Step 1: Fetch lesson
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        // Step 2: Validate content exists
        String content = lesson.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson content is empty");
        }

        // Use default voice if not specified
        if (voiceType == null || voiceType.trim().isEmpty()) {
            voiceType = DEFAULT_VOICE;
        }

        // Validate voice
        if (!edgeTTSClient.isValidVoice(voiceType)) {
            log.warn("Invalid voice: {}, using default", voiceType);
            voiceType = DEFAULT_VOICE;
        }

        try {
            // Step 3: Update lesson status to PENDING
            lesson.setAudioStatus("PENDING");
            lessonRepository.save(lesson);
            log.debug("Updated lesson audio status to PENDING");

            // Step 4: Generate audio using Edge-TTS
            log.info("Calling Edge-TTS service: voice={}, text_len={}", voiceType, content.length());
            EdgeTTSClient.GenerateAudioResponse ttsResponse = edgeTTSClient.generateAudio(
                    content,
                    voiceType,
                    "+0%",    // default rate
                    "+0Hz"    // default pitch
            );

            if (!ttsResponse.success) {
                throw new Exception("TTS generation failed: " + ttsResponse.error);
            }

            log.info("Audio generated: {} bytes", ttsResponse.size_bytes);

            // Step 5: Upload to Cloudinary
            String audioUrl = uploadToCloudinary(lessonId, ttsResponse.audio_full);
            log.info("Audio uploaded to Cloudinary: {}", audioUrl);

            // Step 6: Update lesson with audio URL
            lesson.setAudioUrl(audioUrl);
            lesson.setAudioText(content);  // Lưu content dùng để tạo audio
            lesson.setAudioStatus("COMPLETED");
            lesson.setAudioGeneratedAt(LocalDateTime.now());
            lesson.setAudioVoiceType(voiceType);
            lessonRepository.save(lesson);

            log.info("Lesson audio generation completed: lessonId={}, audioUrl={}", lessonId, audioUrl);
            return audioUrl;

        } catch (Exception e) {
            log.error("Error generating audio for lesson {}: {}", lessonId, e.getMessage(), e);

            // Update lesson status to FAILED
            try {
                lesson.setAudioStatus("FAILED");
                lessonRepository.save(lesson);
            } catch (Exception ex) {
                log.error("Failed to update lesson status: {}", ex.getMessage());
            }

            throw e;
        }
    }

    /**
     * Generate audio with custom rate and pitch
     * @param lessonId - ID of lesson
     * @param voiceType - Voice type
     * @param rate - Speech rate (e.g., '+50%', '-20%')
     * @param pitch - Speech pitch (e.g., '+10Hz', '-5Hz')
     * @return Generated audio URL
     * @throws Exception if generation fails
     */
    @Transactional
    public String generateAndSaveAudioWithSettings(
            String lessonId,
            String voiceType,
            String rate,
            String pitch) throws Exception {

        log.info("Generating audio with custom settings: lessonId={}, voice={}, rate={}, pitch={}",
                lessonId, voiceType, rate, pitch);

        // Fetch lesson
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        String content = lesson.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson content is empty");
        }

        if (voiceType == null || voiceType.trim().isEmpty()) {
            voiceType = DEFAULT_VOICE;
        }

        try {
            lesson.setAudioStatus("PENDING");
            lessonRepository.save(lesson);

            // Generate with custom settings
            EdgeTTSClient.GenerateAudioResponse ttsResponse = edgeTTSClient.generateAudio(
                    content,
                    voiceType,
                    rate != null ? rate : "+0%",
                    pitch != null ? pitch : "+0Hz"
            );

            if (!ttsResponse.success) {
                throw new Exception("TTS generation failed: " + ttsResponse.error);
            }

            // Upload to Cloudinary
            String audioUrl = uploadToCloudinary(lessonId, ttsResponse.audio_full);

            // Update lesson
            lesson.setAudioUrl(audioUrl);
            lesson.setAudioText(content);  // Lưu content dùng để tạo audio
            lesson.setAudioStatus("COMPLETED");
            lesson.setAudioGeneratedAt(LocalDateTime.now());
            lesson.setAudioVoiceType(voiceType);
            lessonRepository.save(lesson);

            log.info("Audio generated with custom settings: {}", audioUrl);
            return audioUrl;

        } catch (Exception e) {
            log.error("Error generating audio with custom settings: {}", e.getMessage(), e);
            lesson.setAudioStatus("FAILED");
            lessonRepository.save(lesson);
            throw e;
        }
    }

    /**
     * Generate audio from custom content text
     * Dùng khi giáo viên nhập nội dung audio mới thay vì dùng lesson.content
     * @param lessonId - ID of lesson
     * @param customContent - Custom text content to generate audio from
     * @param voiceType - Voice type
     * @param rate - Speech rate
     * @param pitch - Speech pitch
     * @return Generated audio URL
     * @throws Exception if generation fails
     */
    @Transactional
    public String generateAndSaveAudioFromCustomContent(
            String lessonId,
            String customContent,
            String voiceType,
            String rate,
            String pitch) throws Exception {

        log.info("Generating audio from custom content: lessonId={}, voice={}, rate={}, pitch={}",
                lessonId, voiceType, rate, pitch);

        // Fetch lesson
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        // Validate custom content
        if (customContent == null || customContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Custom audio content is empty");
        }

        if (voiceType == null || voiceType.trim().isEmpty()) {
            voiceType = DEFAULT_VOICE;
        }

        try {
            // Delete existing audio first
            deleteAudio(lessonId);
            
            lesson.setAudioStatus("PENDING");
            lessonRepository.save(lesson);

            // Generate audio from custom content
            EdgeTTSClient.GenerateAudioResponse ttsResponse = edgeTTSClient.generateAudio(
                    customContent,
                    voiceType,
                    rate != null ? rate : "+0%",
                    pitch != null ? pitch : "+0Hz"
            );

            if (!ttsResponse.success) {
                throw new Exception("TTS generation failed: " + ttsResponse.error);
            }

            // Upload to Cloudinary
            String audioUrl = uploadToCloudinary(lessonId, ttsResponse.audio_full);

            // Update lesson
            lesson.setAudioUrl(audioUrl);
            lesson.setAudioText(customContent);  // Lưu text dùng để tạo audio
            lesson.setAudioStatus("COMPLETED");
            lesson.setAudioGeneratedAt(LocalDateTime.now());
            lesson.setAudioVoiceType(voiceType);
            lessonRepository.save(lesson);

            log.info("Audio generated from custom content: {}", audioUrl);
            return audioUrl;

        } catch (Exception e) {
            log.error("Error generating audio from custom content: {}", e.getMessage(), e);
            lesson.setAudioStatus("FAILED");
            lessonRepository.save(lesson);
            throw e;
        }
    }

    /**
     * Delete existing audio for a lesson
     * @param lessonId - ID of lesson
     * @throws Exception if deletion fails
     */
    @Transactional
    public void deleteAudio(String lessonId) throws Exception {
        log.info("Deleting audio for lesson: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        String audioUrl = lesson.getAudioUrl();
        if (audioUrl != null && !audioUrl.isEmpty()) {
            try {
                // Delete from Cloudinary using the public_id
                String publicId = extractPublicIdFromUrl(audioUrl);
                if (publicId != null) {
                    Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    log.info("Deleted from Cloudinary: {}", result);
                }
            } catch (Exception e) {
                log.warn("Error deleting from Cloudinary: {}", e.getMessage());
            }
        }

        // Clear audio fields
        lesson.setAudioUrl(null);
        lesson.setAudioStatus(null);
        lesson.setAudioGeneratedAt(null);
        lesson.setAudioVoiceType(null);
        lessonRepository.save(lesson);

        log.info("Audio deleted for lesson: {}", lessonId);
    }

    /**
     * Check if lesson has audio
     * @param lessonId - ID of lesson
     * @return true if audio exists, false otherwise
     */
    public boolean hasAudio(String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        return lesson != null && lesson.getAudioUrl() != null && !lesson.getAudioUrl().isEmpty();
    }

    /**
     * Get audio URL for a lesson
     * @param lessonId - ID of lesson
     * @return Audio URL or null
     */
    public String getAudioUrl(String lessonId) {
        return lessonRepository.findById(lessonId)
                .map(Lesson::getAudioUrl)
                .orElse(null);
    }

    /**
     * Get audio status for a lesson
     * @param lessonId - ID of lesson
     * @return Audio status (PENDING, COMPLETED, FAILED) or null
     */
    public String getAudioStatus(String lessonId) {
        return lessonRepository.findById(lessonId)
                .map(Lesson::getAudioStatus)
                .orElse(null);
    }

    // ========== Private Helper Methods ==========

    /**
     * Upload audio to Cloudinary
     * @param lessonId - ID of lesson (for naming)
     * @param base64Audio - Base64 encoded audio data
     * @return Cloudinary URL
     * @throws Exception if upload fails
     */
    private String uploadToCloudinary(String lessonId, String base64Audio) throws Exception {
        log.debug("Uploading audio to Cloudinary for lesson: {}", lessonId);

        try {
            // Decode base64
            byte[] audioBytes = Base64.getDecoder().decode(base64Audio);

            // Create unique filename
            String filename = "lesson_" + lessonId + "_" + UUID.randomUUID().toString().substring(0, 8);

            // Upload bytes directly to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    audioBytes,
                    ObjectUtils.asMap(
                            "folder", CLOUDINARY_FOLDER,
                            "public_id", filename,
                            "resource_type", "auto",
                            "format", "mp3"
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Audio uploaded to Cloudinary: {}", secureUrl);

            return secureUrl;

        } catch (Exception e) {
            log.error("Error uploading to Cloudinary: {}", e.getMessage(), e);
            throw new Exception("Failed to upload audio: " + e.getMessage(), e);
        }
    }

    /**
     * Extract public_id from Cloudinary URL for deletion
     * URL format: https://res.cloudinary.com/[cloud]/image/upload/v[version]/[public_id]
     */
    private String extractPublicIdFromUrl(String url) {
        try {
            // Extract path after /upload/
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return null;

            String path = url.substring(uploadIndex + 8);  // 8 = "/upload/".length()

            // Remove version string if present (v1234567890/)
            path = path.replaceAll("v\\d+/", "");

            // Remove file extension
            path = path.replaceAll("\\.\\w+$", "");

            return path;
        } catch (Exception e) {
            log.warn("Error extracting public_id from URL: {}", url);
            return null;
        }
    }
}
