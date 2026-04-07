package com.english12smart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * ========== EDGE TTS CLIENT SERVICE ==========
 * Service gọi Python Edge-TTS Microservice để generate audio từ text
 * Endpoint: http://localhost:5000/api/tts/generate-base64
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EdgeTTSClient {

    // ========== Dependencies ==========
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ========== Configuration ==========
    @Value("${tts.service.url:http://localhost:5000}")
    private String ttsServiceUrl;

    @Value("${tts.default.voice:en-US-AriaNeural}")
    private String defaultVoice;

    @Value("${tts.default.rate:+0%}")
    private String defaultRate;

    @Value("${tts.default.pitch:+0Hz}")
    private String defaultPitch;

    @Value("${tts.max.text.length:5000}")
    private Integer maxTextLength;

    @Value("${tts.connect.timeout:30000}")
    private Integer connectTimeout;

    // ========== Constants ==========
    private static final String GENERATE_ENDPOINT = "/api/tts/generate-base64";
    private static final String HEALTH_ENDPOINT = "/health";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    // ========== DTO Classes ==========

    /**
     * Request DTO cho TTS generation
     */
    public static class GenerateAudioRequest {
        public String text;
        public String voice;
        public String rate;
        public String pitch;

        public GenerateAudioRequest(String text, String voice, String rate, String pitch) {
            this.text = text;
            this.voice = voice;
            this.rate = rate;
            this.pitch = pitch;
        }
    }

    /**
     * Response DTO từ TTS service
     */
    public static class GenerateAudioResponse {
        public boolean success;
        public String audio_full;  // Base64 encoded audio
        public String audio_base64;  // Preview (first 100 chars)
        public String mime_type;
        public long size_bytes;
        public String generated_at;
        public String voice;
        public String error;
    }

    /**
     * Health check response
     */
    public static class HealthCheckResponse {
        public String status;
        public String service;
        public String timestamp;
    }

    // ========== Public Methods ==========

    /**
     * Check if TTS service is healthy
     * @return true if service is running, false otherwise
     */
    public boolean isServiceHealthy() {
        try {
            log.debug("Checking TTS service health: {}", ttsServiceUrl);
            String healthUrl = ttsServiceUrl + HEALTH_ENDPOINT;
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            boolean healthy = response.getStatusCode().is2xxSuccessful();
            log.info("TTS service health check: {}", healthy ? "healthy" : "unhealthy");
            return healthy;
        } catch (Exception e) {
            log.warn("TTS service health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate audio from text using Edge-TTS
     * @param text - Text to convert to speech
     * @param voice - Voice type (e.g., 'en-US-AriaNeural')
     * @return GenerateAudioResponse with base64 audio
     * @throws TTSException if generation fails
     */
    public GenerateAudioResponse generateAudio(String text, String voice) throws TTSException {
        return generateAudio(text, voice, defaultRate, defaultPitch);
    }

    /**
     * Generate audio from text with custom rate and pitch
     * @param text - Text to convert to speech
     * @param voice - Voice type
     * @param rate - Speech rate (e.g., '+50%', '-20%')
     * @param pitch - Speech pitch (e.g., '+10Hz', '-5Hz')
     * @return GenerateAudioResponse with base64 audio
     * @throws TTSException if generation fails
     */
    public GenerateAudioResponse generateAudio(String text, String voice, String rate, String pitch) throws TTSException {
        // Validation
        if (text == null || text.trim().isEmpty()) {
            throw new TTSException("Text cannot be empty");
        }

        if (text.length() > maxTextLength) {
            throw new TTSException("Text too long: " + text.length() + " characters. Maximum is " + maxTextLength);
        }

        if (voice == null || voice.trim().isEmpty()) {
            voice = defaultVoice;
        }

        log.info("Generating audio: voice={}, text_len={}, rate={}, pitch={}", 
                voice, text.length(), rate, pitch);

        try {
            return generateAudioWithRetry(text, voice, rate, pitch, 0);
        } catch (TTSException e) {
            log.error("Audio generation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during audio generation: {}", e.getMessage(), e);
            throw new TTSException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Validate if voice is valid
     * @param voice - Voice code
     * @return true if voice is valid, false otherwise
     */
    public boolean isValidVoice(String voice) {
        // Liste of supported voices
        String[] supportedVoices = {
                "en-US-AriaNeural",
                "en-US-BrianNeural",
                "en-US-JennyNeural",
                "en-US-GuyNeural",
                "en-GB-SoniaNeural",
                "en-GB-ThomasNeural",
                "en-GB-LibbyNeural",
                "en-AU-NatashaNeural",
                "en-AU-WilliamNeural",
                "en-CA-ClaraNeural",
                "en-CA-LiamNeural"
        };

        for (String supportedVoice : supportedVoices) {
            if (supportedVoice.equals(voice)) {
                return true;
            }
        }
        return false;
    }

    // ========== Private Methods ==========

    /**
     * Generate audio with retry logic
     */
    private GenerateAudioResponse generateAudioWithRetry(
            String text, String voice, String rate, String pitch, int retryCount) throws TTSException {

        if (!isServiceHealthy()) {
            throw new TTSException("TTS service is not available");
        }

        try {
            // Create request
            GenerateAudioRequest request = new GenerateAudioRequest(text, voice, rate, pitch);

            // Call TTS service
            GenerateAudioResponse response = callTTSService(request);

            if (response.success) {
                log.info("Audio generated successfully: {} bytes", response.size_bytes);
                return response;
            } else {
                String errorMsg = response.error != null ? response.error : "Unknown error";
                throw new TTSException("TTS generation failed: " + errorMsg);
            }

        } catch (TTSException e) {
            // Retry logic for transient errors
            if (retryCount < MAX_RETRIES && e.isRetryable()) {
                log.warn("Retrying audio generation (attempt {}/{}): {}", 
                        retryCount + 1, MAX_RETRIES, e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS * (retryCount + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return generateAudioWithRetry(text, voice, rate, pitch, retryCount + 1);
            }
            throw e;
        }
    }

    /**
     * Call TTS service and parse response
     */
    private GenerateAudioResponse callTTSService(GenerateAudioRequest request) throws TTSException {
        try {
            String url = ttsServiceUrl + GENERATE_ENDPOINT;
            log.debug("Calling TTS service: {}", url);

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<GenerateAudioRequest> httpRequest = new HttpEntity<>(request, headers);

            // Call service
            ResponseEntity<GenerateAudioResponse> response = restTemplate.postForEntity(
                    url,
                    httpRequest,
                    GenerateAudioResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new TTSException("TTS service returned error: " + response.getStatusCode());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400) {
                log.warn("Bad request to TTS service: {}", e.getResponseBodyAsString());
                throw new TTSException("Invalid request to TTS service: " + e.getMessage());
            } else if (e.getStatusCode().value() == 413) {
                log.warn("Text too long for TTS service");
                throw new TTSException("Text too long");
            } else {
                log.warn("HTTP client error from TTS service: {}", e.getMessage());
                throw new TTSException("TTS service error: " + e.getMessage());
            }
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.warn("TTS service error (retryable): {}", e.getMessage());
            throw new TTSException("TTS service error: " + e.getMessage(), true);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.warn("Cannot connect to TTS service (retryable): {}", e.getMessage());
            throw new TTSException("Cannot connect to TTS service", true);
        } catch (Exception e) {
            log.error("Error calling TTS service: {}", e.getMessage(), e);
            throw new TTSException("Error calling TTS service: " + e.getMessage(), e);
        }
    }

    // ========== Custom Exception ==========

    /**
     * Exception for TTS-related errors
     */
    public static class TTSException extends Exception {
        private boolean retryable = false;

        public TTSException(String message) {
            super(message);
        }

        public TTSException(String message, boolean retryable) {
            super(message);
            this.retryable = retryable;
        }

        public TTSException(String message, Throwable cause) {
            super(message, cause);
        }

        public boolean isRetryable() {
            return retryable;
        }
    }
}
