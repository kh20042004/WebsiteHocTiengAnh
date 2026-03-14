package com.english12smart.exception;

/**
 * ========== VALIDATION EXCEPTION ==========
 * Exception khi validation không thành công
 */
public class ValidationException extends ApplicationException {
    
    /**
     * Constructor
     * 
     * @param message - Error message
     */
    public ValidationException(String message) {
        super(400, "VALIDATION_ERROR", message);
    }
    
    /**
     * Constructor với field và message
     * 
     * @param field - Tên field
     * @param message - Error message
     */
    public ValidationException(String field, String message) {
        super(400, "VALIDATION_ERROR", "Field '" + field + "': " + message);
    }
}