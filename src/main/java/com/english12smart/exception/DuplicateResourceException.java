package com.english12smart.exception;

/**
 * ========== DUPLICATE RESOURCE EXCEPTION ==========
 * Throw khi tạo resource đã tồn tại (409 Conflict)
 * VD: Email đã được đăng ký, username đã tồn tại
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
