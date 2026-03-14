package com.english12smart.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ========== GENERIC API RESPONSE DTO ==========
 * Dùng cho tất cả API responses
 * Giúp response có format đồng nhất
 * 
 * Example Success Response:
 * {
 *   "status": "success",
 *   "message": "Đăng nhập thành công",
 *   "data": { ... },
 *   "timestamp": 1704067200000
 * }
 * 
 * Example Error Response:
 * {
 *   "status": "error",
 *   "message": "Email hoặc mật khẩu không chính xác",
 *   "error": "UNAUTHORIZED",
 *   "timestamp": 1704067200000
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Status: "success" hoặc "error"
     */
    private String status;

    /**
     * Message thân thiện với người dùng
     */
    private String message;

    /**
     * Data trả về (generic type T)
     */
    private T data;

    /**
     * Error code (chỉ có khi status = "error")
     */
    private String error;

    /**
     * Timestamp khi response được tạo (ms)
     */
    private long timestamp;

    /**
     * HTTP status code
     */
    private int code;

    // ========== HELPER: Success Response (không có data) ==========
    /**
     * Tạo success response mà không có data
     * 
     * @param message - Message
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .code(200)
                .build();
    }

    // ========== HELPER: Success Response (có data) ==========
    /**
     * Tạo success response với data
     * 
     * @param message - Message
     * @param data - Dữ liệu
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .code(200)
                .build();
    }

    // ========== HELPER: Success Response (có data + code) ==========
    /**
     * Tạo success response với data và HTTP code
     * 
     * @param message - Message
     * @param data - Dữ liệu
     * @param code - HTTP status code
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data, int code) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .code(code)
                .build();
    }

    // ========== HELPER: Error Response ==========
    /**
     * Tạo error response
     * 
     * @param message - Error message
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .code(400)
                .error("BAD_REQUEST")
                .build();
    }

    // ========== HELPER: Error Response (với code) ==========
    /**
     * Tạo error response với HTTP code
     * 
     * @param message - Error message
     * @param code - HTTP status code
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, int code) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .code(code)
                .error(getErrorType(code))
                .build();
    }

    // ========== HELPER: Error Response (với code + error type) ==========
    /**
     * Tạo error response với HTTP code và error type
     * 
     * @param message - Error message
     * @param code - HTTP status code
     * @param error - Error type (UNAUTHORIZED, FORBIDDEN, NOT_FOUND, etc.)
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, int code, String error) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .code(code)
                .error(error)
                .build();
    }

    // ========== HELPER: Map HTTP code to error type ==========
    /**
     * Chuyển đổi HTTP code thành error type
     * 
     * @param code - HTTP status code
     * @return Error type string
     */
    private static String getErrorType(int code) {
        return switch (code) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 500 -> "INTERNAL_SERVER_ERROR";
            case 502 -> "BAD_GATEWAY";
            case 503 -> "SERVICE_UNAVAILABLE";
            default -> "ERROR";
        };
    }
}
