package com.english12smart.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties
 * Mapping từ application.properties: jwt.*
 * 
 * Usage:
 * @Autowired
 * private JwtProperties jwtProperties;
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT secret key cho signing/verification
     * Default: your-secret-key-english-12-smart-learning-platform-2024-must-be-at-least-256-bits-long
     */
    private String secret = "your-secret-key-english-12-smart-learning-platform-2024-must-be-at-least-256-bits-long";
    
    /**
     * Access token expiration time (milliseconds)
     * Default: 3600000 (1 hour)
     */
    private long expiration = 3600000;
    
    /**
     * Refresh token expiration time (milliseconds)
     * Default: 604800000 (7 days)
     */
    private long refreshExpiration = 604800000;
}