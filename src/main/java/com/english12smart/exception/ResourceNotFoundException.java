package com.english12smart.exception;

/**
 * ========== RESOURCE NOT FOUND EXCEPTION ==========
 * Exception khi tài nguyên không tìm thấy
 */
public class ResourceNotFoundException extends ApplicationException {
    
    /**
     * Constructor
     * 
     * @param message - Error message
     */
    public ResourceNotFoundException(String message) {
        super(404, "NOT_FOUND", message);
    }
    
    /**
     * Constructor với resource type và ID
     * 
     * @param resourceType - Loại tài nguyên (e.g., "User", "Lesson")
     * @param id - ID của tài nguyên
     */
    public ResourceNotFoundException(String resourceType, String id) {
        super(404, "NOT_FOUND", resourceType + " với ID " + id + " không tìm thấy");
    }
}