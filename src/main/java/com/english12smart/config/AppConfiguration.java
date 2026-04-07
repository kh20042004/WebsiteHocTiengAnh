package com.english12smart.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

/**
 * ApplicationConfig — Cấu hình các Bean cho ứng dụng
 * Định nghĩa RestTemplate bean để gọi Google Gemini API
 */
@Configuration
public class AppConfiguration {

    /**
     * RestTemplate Bean — Được sử dụng trong AISuggestionService
     * Cấu hình timeout và retry logic nếu cần
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

}
