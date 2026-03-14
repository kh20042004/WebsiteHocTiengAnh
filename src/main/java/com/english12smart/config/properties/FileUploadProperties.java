package com.english12smart.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * File Upload Configuration Properties
 * Mapping từ application.properties: file.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {
    
    /**
     * Thư mục lưu uploaded files
     * Default: uploads/
     */
    private String directory = "uploads/";
}