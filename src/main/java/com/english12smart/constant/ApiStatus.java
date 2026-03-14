package com.english12smart.constant;

/**
 * ========== API RESPONSE STATUS ENUM ==========
 * Các status code cho API responses
 */
public enum ApiStatus {
    
    /**
     * Request thành công
     */
    SUCCESS(200, "Thành công"),
    
    /**
     * Tạo mới thành công
     */
    CREATED(201, "Tạo mới thành công"),
    
    /**
     * Bad request - dữ liệu không hợp lệ
     */
    BAD_REQUEST(400, "Dữ liệu không hợp lệ"),
    
    /**
     * Unauthorized - không được authenticate
     */
    UNAUTHORIZED(401, "Không được xác thực"),
    
    /**
     * Forbidden - không có quyền
     */
    FORBIDDEN(403, "Bị cấm truy cập"),
    
    /**
     * Not found - tài nguyên không tồn tại
     */
    NOT_FOUND(404, "Không tìm thấy"),
    
    /**
     * Conflict - xung đột (e.g., email đã tồn tại)
     */
    CONFLICT(409, "Dữ liệu bị xung đột"),
    
    /**
     * Internal server error
     */
    INTERNAL_SERVER_ERROR(500, "Lỗi máy chủ");
    
    private final int code;
    private final String message;
    
    ApiStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}