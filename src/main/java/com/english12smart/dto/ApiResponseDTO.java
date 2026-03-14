package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.english12smart.constant.ApiStatus;

/**
 * ========== API RESPONSE DTO ==========
 * Standard response format cho mọi API endpoints
 * 
 * Ví dụ:
 * {
 *   "statusCode": 200,
 *   "message": "Thành công",
 *   "data": {...},
 *   "timestamp": 1704067200000
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponseDTO<T> {
    
    /**
     * HTTP status code
     * 200: Success
     * 201: Created
     * 400: Bad Request
     * 401: Unauthorized
     * 404: Not Found
     * 500: Internal Server Error
     */
    private int statusCode;
    
    /**
     * Response message (message in tiếng Việt cho user)
     */
    private String message;
    
    /**
     * Response data (có thể là null)
     */
    private T data;
    
    /**
     * Response timestamp (milliseconds)
     */
    private long timestamp;
    
    /**
     * Error details (chỉ dùng khi có error)
     */
    private String error;
    
    /**
     * Request path (dùng cho debugging)
     */
    private String path;
    
    /**
     * Constructor cho success response
     * 
     * @param statusCode - HTTP status code
     * @param message - Response message
     * @param data - Response data
     */
    public ApiResponseDTO(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Static factory method cho success response
     * 
     * @param data - Response data
     * @return ApiResponseDTO
     */
    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>(200, "Thành công", data);
    }
    
    /**
     * Static factory method cho success response với custom message
     * 
     * @param message - Custom message
     * @param data - Response data
     * @return ApiResponseDTO
     */
    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>(200, message, data);
    }
    
    /**
     * Static factory method cho created response
     * 
     * @param data - Response data
     * @return ApiResponseDTO
     */
    public static <T> ApiResponseDTO<T> created(T data) {
        return new ApiResponseDTO<>(201, "Tạo mới thành công", data);
    }
    
    /**
     * Static factory method cho error response
     * 
     * @param statusCode - HTTP status code
     * @param message - Error message
     * @return ApiResponseDTO
     */
    public static <T> ApiResponseDTO<T> error(int statusCode, String message) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.setStatusCode(statusCode);
        response.setMessage(message);
        response.setError(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
    
    /**
     * Static factory method cho error response với details
     * 
     * @param statusCode - HTTP status code
     * @param message - Error message
     * @param errorDetails - Error details
     * @return ApiResponseDTO
     */
    public static <T> ApiResponseDTO<T> error(int statusCode, String message, String errorDetails) {
        ApiResponseDTO<T> response = error(statusCode, message);
        response.setError(errorDetails);
        return response;
    }
    
    /**
     * Check nếu response là success
     * 
     * @return true nếu status code là 2xx
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Check nếu response là error
     * 
     * @return true nếu status code không phải 2xx
     */
    public boolean isError() {
        return !isSuccess();
    }
}