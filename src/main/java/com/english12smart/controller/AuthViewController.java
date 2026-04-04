package com.english12smart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

/**
 * ========== AUTH VIEW CONTROLLER ==========
 * Controller để phục vụ các HTML page cho authentication
 * (Không phải REST API, mà là MVC controller)
 * 
 * Endpoint:
 * - GET /auth/forgot-password - Trang quên mật khẩu
 * - GET /auth/reset-password - Trang reset mật khẩu
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthViewController {

    // ========== FORGOT PASSWORD PAGE - GET /auth/forgot-password ==========
    /**
     * Phục vụ trang HTML quên mật khẩu
     * User nhập email để nhận link reset password
     * 
     * View: templates/auth/forgot-password.html
     * 
     * Flow:
     * 1. User access /auth/forgot-password
     * 2. Server return HTML form
     * 3. User nhập email và submit
     * 4. JavaScript gửi POST request đến /api/auth/forgot-password
     * 5. API trả về response success
     * 6. JavaScript hiển thị success message
     * 
     * @return HTML page - forgot-password.html
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        log.info("========== FORGOT PASSWORD PAGE LOAD ==========");
        log.info("User accessing forgot password page");

        // ========== Return view name ==========
        // Spring sẽ tìm file: src/main/resources/templates/auth/forgot-password.html
        return "auth/forgot-password";
    }

    // ========== RESET PASSWORD PAGE - GET /auth/reset-password ==========
    /**
     * Phục vụ trang HTML reset mật khẩu
     * User nhấp link từ email và sẽ được dẫn đến trang này
     * Token được truyền qua URL parameter: ?token=<reset-token>
     * 
     * View: templates/auth/reset-password.html
     * 
     * Flow:
     * 1. User click link từ email: /auth/reset-password?token=550e8400-e29b-41d4-a716-446655440000
     * 2. Server return HTML form + token
     * 3. JavaScript kiểm tra token từ URL
     * 4. Nếu token không tồn tại: hiển thị error message + link quay lại
     * 5. Nếu token có: hiển thị form reset password
     * 6. User nhập mật khẩu mới và submit
     * 7. JavaScript gửi POST request đến /api/auth/reset-password
     * 8. API xác thực token, nếu hợp lệ thì reset mật khẩu
     * 9. Hiển thị success message + link đăng nhập
     * 
     * @param token - Reset token từ URL (ví dụ: ?token=550e8400-e29b-41d4-a716-446655440000)
     * @param model - Model để truyền dữ liệu tới view (tuỳ chọn)
     * @return HTML page - reset-password.html
     */
    @GetMapping("/reset-password")
    public String showResetPasswordPage(
            @RequestParam(value = "token", required = false) String token,
            Model model) {
        
        log.info("========== RESET PASSWORD PAGE LOAD ==========");
        if (token != null && !token.isEmpty()) {
            log.info("Reset token received (first 8 chars): {}...", token.substring(0, Math.min(8, token.length())));
            // ========== Token validation sẽ được làm bởi JavaScript ở client side ==========
            // Token không cần validate ở server vì:
            // 1. Client sẽ submit form đến /api/auth/reset-password
            // 2. API endpoint sẽ validate token
            // 3. Nếu token hợp lệ, password sẽ được reset
            // 4. Nếu token không hợp lệ, API sẽ trả error
        } else {
            log.warn("Reset password page accessed without token");
        }

        // ========== Add token to model (tuỳ chọn, vì JavaScript cũng có thể lấy từ URL) ==========
        if (token != null) {
            model.addAttribute("token", token);
        }

        // ========== Return view name ==========
        // Spring sẽ tìm file: src/main/resources/templates/auth/reset-password.html
        return "auth/reset-password";
    }

    // ========== NOTES ==========
    /*
     * FORGOT PASSWORD FLOW:
     * 
     * 1. User click "Quên mật khẩu?" trên trang login
     * 2. GET /auth/forgot-password -> Server return form
     * 3. User nhập email và click "Gửi Link Reset"
     * 4. JavaScript POST /api/auth/forgot-password
     * 5. AuthService:
     *    - Tìm user theo email
     *    - Sinh reset token (UUID)
     *    - Lưu token + expires_at (1 giờ)
     *    - Gửi email chứa link: /auth/reset-password?token=<token>
     * 6. User nhận email
     * 7. User click link trong email
     * 8. GET /auth/reset-password?token=<token> -> Server return reset form
     * 9. User nhập mật khẩu mới và click "Lưu"
     * 10. JavaScript POST /api/auth/reset-password
     * 11. AuthService:
     *     - Tìm user theo token
     *     - Validate token (chưa hết hạn)
     *     - Hash mật khẩu mới
     *     - Update password, xóa token
     * 12. Return success response
     * 13. JavaScript redirect về /login
     * 
     * SECURITY FEATURES:
     * - Token là UUID (khó brute-force)
     * - Token chỉ có hiệu lực 1 giờ
     * - Token bị xóa khi reset thành công
     * - Password hashed với BCrypt (strength 10)
     * - Forgot password API không reveal email tồn tại hay không (security best practice)
     */
}
