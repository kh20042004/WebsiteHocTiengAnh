package com.english12smart.exception;

/**
 * ========== TOKEN EXPIRED EXCEPTION ==========
 * Throw khi JWT token đã hết hạn (401 Unauthorized)
 */
public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
