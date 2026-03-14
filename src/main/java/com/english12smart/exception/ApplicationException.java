package com.english12smart.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * ========== CUSTOM APPLICATION EXCEPTION ==========
 * Base exception class cho ứng dụng
 */
@Getter
@Setter
public class ApplicationException extends RuntimeException {
    
    /**
     * HTTP status code
     */
    private int statusCode;
    
    /**
     * Error code (for client classification)
     */
    private String errorCode;
    
    /**
     * Constructor với message
     * 
     * @param message - Error message
     */
    public ApplicationException(String message) {
        super(message);
        this.statusCode = 500;
        this.errorCode = "INTERNAL_SERVER_ERROR";
    }
    
    /**
     * Constructor với statusCode và message
     * 
     * @param statusCode - HTTP status code
     * @param message - Error message
     */
    public ApplicationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = "ERROR";
    }
    
    /**
     * Constructor với statusCode, errorCode, và message
     * 
     * @param statusCode - HTTP status code
     * @param errorCode - Error code
     * @param message - Error message
     */
    public ApplicationException(int statusCode, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
    
    /**
     * Constructor với message và cause
     * 
     * @param message - Error message
     * @param cause - Root cause
     */
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
        this.errorCode = "INTERNAL_SERVER_ERROR";
    }
}