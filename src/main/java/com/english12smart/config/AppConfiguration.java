package com.english12smart.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * ApplicationConfig — Cấu hình các Bean cho ứng dụng
 * - RestTemplate: Gọi Google Gemini API và Python TTS service
 * - Cloudinary: Upload audio files to CDN
 */
@Configuration
public class AppConfiguration {

    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api-key:}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api-secret:}")
    private String cloudinaryApiSecret;

    /**
     * RestTemplate Bean — Được sử dụng trong AISuggestionService và EdgeTTSClient
     * Cấu hình timeout: connect = 10s, read = 120s (cho TTS generation)
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(120))  // Long timeout cho TTS
                .build();
    }

    /**
     * Cloudinary Bean — File upload & CDN service cho LessonAudioService
     */
    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudinaryCloudName);
        config.put("api_key", cloudinaryApiKey);
        config.put("api_secret", cloudinaryApiSecret);
        return new Cloudinary(config);
    }

}
