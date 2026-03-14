package com.english12smart.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ========== GOOGLE CLOUD CONFIGURATION PROPERTIES ==========
 * Mapping các properties google.cloud.* từ application.properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "google.cloud")
public class GoogleCloudProperties {
    
    /**
     * Google Cloud Project ID
     */
    private String projectId;
    
    /**
     * Đường dẫn đến file credentials JSON
     */
    private String credentialsPath;
}
