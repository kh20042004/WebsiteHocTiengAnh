package com.english12smart.exception;

/**
 * ========== BAD REQUEST EXCEPTION ==========
 * Throw khi request không hợp lệ (400 Bad Request)
 * VD: Missing required fields, invalid format, etc.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
