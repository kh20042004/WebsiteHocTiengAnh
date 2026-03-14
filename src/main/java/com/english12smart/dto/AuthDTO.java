package com.english12smart.dto;

import lombok.*;
import jakarta.validation.constraints.*;

/**
 * ========== AUTHENTICATION DTOs ==========
 * DTO classes để xử lý request/response trong authentication
 */

public class AuthDTO {

    /**
     * ========== REGISTER REQUEST DTO ==========
     * Dùng cho API POST /api/auth/register
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
        private String email; // Email người dùng

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6-50 ký tự")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số")
        private String password; // Mật khẩu (sẽ được hash)

        @NotBlank(message = "Họ tên không được để trống")
        @Size(min = 2, max = 100, message = "Họ tên phải từ 2-100 ký tự")
        private String fullName; // Tên đầy đủ

        @NotBlank(message = "Vai trò không được để trống")
        @Pattern(regexp = "^(STUDENT|TEACHER|ADMIN)$", message = "Vai trò phải là STUDENT, TEACHER hoặc ADMIN")
        private String role; // Vai trò: STUDENT, TEACHER, ADMIN
    }

    /**
     * ========== LOGIN REQUEST DTO ==========
     * Dùng cho API POST /api/auth/login
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email; // Email

        @NotBlank(message = "Mật khẩu không được để trống")
        private String password; // Mật khẩu
    }

    /**
     * ========== LOGIN RESPONSE DTO ==========
     * Response từ API POST /api/auth/login
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken; // JWT access token (1 hour)
        private String refreshToken; // JWT refresh token (7 days)
        private String tokenType; // Kiểu token: "Bearer"
        private long expiresIn; // Thời gian hết hạn (milliseconds)
        private UserDTO user; // Thông tin user
    }

    /**
     * ========== REFRESH TOKEN REQUEST DTO ==========
     * Dùng cho API POST /api/auth/refresh
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token không được để trống")
        private String refreshToken; // Refresh token cũ
    }

    /**
     * ========== REFRESH TOKEN RESPONSE DTO ==========
     * Response từ API POST /api/auth/refresh
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenResponse {
        private String accessToken; // Access token mới
        private String tokenType; // "Bearer"
        private long expiresIn; // Thời gian hết hạn (milliseconds)
    }

    /**
     * ========== VERIFY TOKEN RESPONSE DTO ==========
     * Response từ API GET /api/auth/verify
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyTokenResponse {
        private String userId; // User ID từ token
        private String email; // Email từ token
        private String role; // Role từ token
        private boolean isValid; // Token có hợp lệ hay không
    }

    /**
     * ========== CHANGE PASSWORD REQUEST DTO ==========
     * Dùng cho API POST /api/auth/change-password
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "Mật khẩu cũ không được để trống")
        private String oldPassword; // Mật khẩu cũ

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, max = 50, message = "Mật khẩu mới phải từ 6-50 ký tự")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "Mật khẩu mới phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số")
        private String newPassword; // Mật khẩu mới
    }
}
