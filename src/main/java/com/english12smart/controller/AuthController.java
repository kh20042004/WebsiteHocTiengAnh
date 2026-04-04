package com.english12smart.controller;

import com.english12smart.dto.AuthDTO;
import com.english12smart.dto.ApiResponse;
import com.english12smart.service.AuthService;
import com.english12smart.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.validation.Valid;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * ========== AUTH CONTROLLER ==========
 * Controller xử lý các request về đăng ký, đăng nhập, refresh token, logout
 * Endpoint: /api/auth/*
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    // ========== Dependencies ==========
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    // ========== REGISTER - POST /api/auth/register ==========
    /**
     * API đăng ký tài khoản mới
     * 
     * Request Body:
     * {
     * "email": "student@example.com",
     * "password": "Password123!",
     * "fullName": "Nguyễn Văn A",
     * "role": "STUDENT"
     * }
     * 
     * Response: 200 OK
     * {
     * "status": "success",
     * "message": "Đăng ký thành công",
     * "data": {
     * "id": "507f1f77bcf86cd799439011",
     * "email": "student@example.com",
     * "fullName": "Nguyễn Văn A",
     * "role": "STUDENT"
     * }
     * }
     * 
     * @param registerRequest - DTO chứa email, password, fullName, role
     * @return ApiResponse - Kết quả đăng ký
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody AuthDTO.RegisterRequest registerRequest) {
        try {
            log.info("Register request for email: {}", registerRequest.getEmail());

            // ========== Call service (validation handled by @Valid) ==========
            var userDTO = authService.register(registerRequest);

            // ========== Return response ==========
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Đăng ký thành công", userDTO));

        } catch (RuntimeException e) {
            log.error("Register error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== LOGIN - POST /api/auth/login ==========
    /**
     * API đăng nhập
     * 
     * Request Body:
     * {
     * "email": "student@example.com",
     * "password": "Password123!"
     * }
     * 
     * Response: 200 OK
     * {
     * "status": "success",
     * "message": "Đăng nhập thành công",
     * "data": {
     * "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     * "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     * "tokenType": "Bearer",
     * "expiresIn": 3600000,
     * "user": {
     * "id": "507f1f77bcf86cd799439011",
     * "email": "student@example.com",
     * "fullName": "Nguyễn Văn A",
     * "role": "STUDENT"
     * }
     * }
     * }
     * 
     * @param loginRequest - DTO chứa email và password
     * @return ApiResponse - Chứa access token, refresh token, user info
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody AuthDTO.LoginRequest loginRequest, HttpServletResponse response) {
        try {
            log.info("Login request for email: {}", loginRequest.getEmail());

            // ========== Call service (validation handled by @Valid) ==========
            var loginResponse = authService.login(loginRequest);

            // ========== Set token in HTTP-only cookie ==========
            jakarta.servlet.http.Cookie tokenCookie = new jakarta.servlet.http.Cookie("token", loginResponse.getAccessToken());
            tokenCookie.setHttpOnly(true);  // Prevent JavaScript access
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(3600);   // 1 hour
            response.addCookie(tokenCookie);

            log.info("Login successful for email: {}, token set in cookie", loginRequest.getEmail());

            // ========== Return response ==========
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", loginResponse));

        } catch (RuntimeException e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== REFRESH TOKEN - POST /api/auth/refresh ==========
    /**
     * API refresh access token
     * 
     * Request Body:
     * {
     * "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * 
     * Response: 200 OK
     * {
     * "status": "success",
     * "message": "Token refreshed successfully",
     * "data": {
     * "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     * "tokenType": "Bearer",
     * "expiresIn": 3600000
     * }
     * }
     * 
     * @param refreshTokenRequest - DTO chứa refresh token
     * @return ApiResponse - Access token mới
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refreshToken(
            @Valid @RequestBody AuthDTO.RefreshTokenRequest refreshTokenRequest) {
        try {
            log.info("Refresh token request");

            // ========== Call service (validation handled by @Valid) ==========
            var newTokenResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());

            // ========== Return response ==========
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", newTokenResponse));

        } catch (RuntimeException e) {
            log.error("Refresh token error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== LOGOUT - POST /api/auth/logout ==========
    /**
     * API đăng xuất
     * Cần authorization (JWT token)
     * 
     * Header: Authorization: Bearer <token>
     * 
     * Response: 200 OK
     * {
     * "status": "success",
     * "message": "Đăng xuất thành công"
     * }
     * 
     * @param request - HTTP request (để lấy user từ JWT token)
     * @return ApiResponse
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request) {
        try {
            log.info("Logout request");

            // ========== Extract user ID từ JWT token ==========
            String token = getTokenFromRequest(request);
            String userId = jwtTokenProvider.getUserIdFromToken(token);

            // ========== Call service ==========
            authService.logout(userId);

            // ========== Return response ==========
            return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));

        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== VERIFY TOKEN - GET /api/auth/verify ==========
    /**
     * API kiểm tra token có hợp lệ hay không
     * 
     * Header: Authorization: Bearer <token>
     * 
     * Response: 200 OK
     * {
     * "status": "success",
     * "message": "Token is valid",
     * "data": {
     * "userId": "507f1f77bcf86cd799439011",
     * "email": "student@example.com",
     * "role": "STUDENT"
     * }
     * }
     * 
     * @param request - HTTP request
     * @return ApiResponse
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyToken(HttpServletRequest request) {
        try {
            log.info("Verify token request");

            // ========== Extract token từ header ==========
            String token = getTokenFromRequest(request);
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Token không được tìm thấy"));
            }

            // ========== Verify token ==========
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token không hợp lệ hoặc đã hết hạn"));
            }

            // ========== Extract claims ==========
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("userId", userId);
            tokenInfo.put("email", email);
            tokenInfo.put("role", role);
            tokenInfo.put("isValid", true);

            return ResponseEntity.ok(ApiResponse.success("Token is valid", tokenInfo));

        } catch (Exception e) {
            log.error("Token verification error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token không hợp lệ"));
        }
    }

    // ========== LOGOUT - GET /api/auth/logout ==========
    /**
     * API đăng xuất
     * Xóa token cookie và redirect về trang chủ
     * 
     * @param response - HTTP response
     * @return Redirect về trang chủ
     */
    @GetMapping("/logout")
    public RedirectView logout(HttpServletResponse response) {
        try {
            log.info("Logout request");

            // Xóa token cookie
            jakarta.servlet.http.Cookie tokenCookie = new jakarta.servlet.http.Cookie("token", null);
            tokenCookie.setHttpOnly(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(0); // Xóa cookie
            response.addCookie(tokenCookie);

            log.info("Logout successful, token cookie cleared");

            // Redirect về trang chủ
            return new RedirectView("/?logout=true");

        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return new RedirectView("/");
        }
    }

    // ========== FORGOT PASSWORD - POST /api/auth/forgot-password ==========
    /**
     * API quên mật khẩu (Forgot Password)
     * User nhập email để nhận link reset password
     * 
     * Request Body:
     * {
     *   "email": "student@example.com"
     * }
     * 
     * Response: 200 OK
     * {
     *   "status": "success",
     *   "message": "Email reset password đã được gửi",
     *   "data": {
     *     "message": "Email reset password đã được gửi. Vui lòng kiểm tra email.",
     *     "email": "student@example.com"
     *   }
     * }
     * 
     * Lưu ý: 
     * - Link reset chỉ có hiệu lực 1 giờ
     * - Mỗi email chỉ có 1 reset token hợp lệ tại 1 thời điểm
     * - Nếu yêu cầu lại, token cũ sẽ bị ghi đè
     * 
     * @param forgotRequest - DTO chứa email
     * @return ApiResponse - Kết quả yêu cầu
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @Valid @RequestBody AuthDTO.ForgotPasswordRequest forgotRequest) {
        try {
            log.info("Forgot password request for email: {}", forgotRequest.getEmail());

            // ========== Call service (validation handled by @Valid) ==========
            authService.forgotPassword(forgotRequest.getEmail());

            // ========== Return response ==========
            // Bảo mật: Luôn trả về thông báo thành công dù email có tồn tại hay không
            AuthDTO.ForgotPasswordResponse response = AuthDTO.ForgotPasswordResponse.builder()
                    .message("Nếu email tồn tại, link reset password sẽ được gửi. Vui lòng kiểm tra email.")
                    .email(forgotRequest.getEmail())
                    .build();

            log.info("Forgot password request processed for email: {}", forgotRequest.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Yêu cầu đã được xử lý", response));

        } catch (RuntimeException e) {
            log.error("Forgot password error: {}", e.getMessage());
            // Bảo mật: Không reveal thông tin chi tiết
            return ResponseEntity.ok(ApiResponse.success("Yêu cầu đã được xử lý", 
                    AuthDTO.ForgotPasswordResponse.builder()
                        .message("Nếu email tồn tại, link reset password sẽ được gửi. Vui lòng kiểm tra email.")
                        .email(forgotRequest.getEmail())
                        .build()));

        } catch (Exception e) {
            log.error("Unexpected error during forgot password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== RESET PASSWORD - POST /api/auth/reset-password ==========
    /**
     * API đặt lại mật khẩu (Reset Password)
     * User nhập reset token + mật khẩu mới để reset password
     * 
     * Request Body:
     * {
     *   "token": "550e8400-e29b-41d4-a716-446655440000",
     *   "newPassword": "NewPassword123",
     *   "confirmPassword": "NewPassword123"
     * }
     * 
     * Response: 200 OK
     * {
     *   "status": "success",
     *   "message": "Mật khẩu đã được đặt lại",
     *   "data": {
     *     "message": "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.",
     *     "email": "student@example.com"
     *   }
     * }
     * 
     * Error cases:
     * - 400: Token không hợp lệ / đã hết hạn
     * - 400: Mật khẩu xác nhận không khớp
     * - 400: Mật khẩu không thỏa mãn điều kiện
     * 
     * Lưu ý:
     * - Token chỉ có hiệu lực 1 giờ
     * - Token sẽ bị xóa sau khi reset thành công
     * - Mật khẩu phải từ 6-50 ký tự, chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số
     * 
     * @param resetRequest - DTO chứa token, newPassword, confirmPassword
     * @return ApiResponse - Kết quả reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid @RequestBody AuthDTO.ResetPasswordRequest resetRequest) {
        try {
            log.info("Reset password request with token: {}", 
                    resetRequest.getToken().substring(0, Math.min(8, resetRequest.getToken().length())) + "...");

            // ========== Call service (validation handled by @Valid) ==========
            var resetResponse = authService.resetPassword(
                    resetRequest.getToken(),
                    resetRequest.getNewPassword(),
                    resetRequest.getConfirmPassword());

            // ========== Return response ==========
            log.info("Password reset successful for email: {}", resetResponse.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Mật khẩu đã được đặt lại", resetResponse));

        } catch (RuntimeException e) {
            log.error("Reset password error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error during reset password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== HELPER: Extract token từ Authorization header ==========
    /**
     * Lấy JWT token từ Authorization header
     * Format: Authorization: Bearer <token>
     * 
     * @param request - HTTP request
     * @return Token (không có "Bearer ") hoặc null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length());
        }
        return null;
    }
}
