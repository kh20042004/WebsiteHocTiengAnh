package com.english12smart.exception;

/**
 * ========== AUTHENTICATION EXCEPTION ==========
 * Exception khi authentication thất bại
 */
public class AuthenticationException extends ApplicationException {
    
    /**
     * Constructor
     * 
     * @param message - Error message
     */
    public AuthenticationException(String message) {
        super(401, "AUTHENTICATION_FAILED", message);
    }
}