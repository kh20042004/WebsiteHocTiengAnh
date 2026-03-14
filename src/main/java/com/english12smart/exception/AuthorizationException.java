package com.english12smart.exception;

/**
 * ========== AUTHORIZATION EXCEPTION ==========
 * Exception khi không có quyền truy cập
 */
public class AuthorizationException extends ApplicationException {
    
    /**
     * Constructor
     * 
     * @param message - Error message
     */
    public AuthorizationException(String message) {
        super(403, "AUTHORIZATION_FAILED", message);
    }
}