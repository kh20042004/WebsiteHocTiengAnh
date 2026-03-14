package com.english12smart.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cloudinary Configuration Properties
 * Mapping từ application.properties: cloudinary.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {
    
    /**
     * Cloudinary cloud name
     */
    private String cloudName;
    
    /**
     * Cloudinary API key
     */
    private String apiKey;
    
    /**
     * Cloudinary API secret
     */
    private String apiSecret;
    
    /**
     * Cloudinary upload folder
     * Default: english-12-smart
     */
    private String uploadFolder = "english-12-smart";
}