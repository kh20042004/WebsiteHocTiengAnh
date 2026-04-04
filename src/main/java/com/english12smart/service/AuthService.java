package com.english12smart.service;

import com.english12smart.entity.User;
import com.english12smart.dto.UserDTO;
import com.english12smart.dto.AuthDTO;
import com.english12smart.repository.UserRepository;
import com.english12smart.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ========== AUTH SERVICE ==========
 * Service xử lý logic đăng ký, đăng nhập, refresh token
 * Sử dụng MongoDB qua UserRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    // ========== Dependencies ==========
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService; // Để gửi email forgot password, verify email

    // ========== REGISTER - Đăng ký tài khoản mới ==========
    /**
     * Đăng ký tài khoản mới
     * 
     * @param registerRequest - Request chứa email, password, fullName, role
     * @return UserDTO - Thông tin user sau khi đăng ký
     * @throws com.english12smart.exception.DuplicateResourceException - Nếu email
     *                                                                 đã tồn tại
     * 
     *                                                                 Quy trình:
     *                                                                 1. Kiểm tra
     *                                                                 email có tồn
     *                                                                 tại không
     *                                                                 2. Hash
     *                                                                 password
     *                                                                 3. Tạo user
     *                                                                 mới
     *                                                                 4. Lưu vào
     *                                                                 MongoDB
     *                                                                 5. Return
     *                                                                 UserDTO
     */
    @Transactional
    public UserDTO register(AuthDTO.RegisterRequest registerRequest) {
        log.info("Registering user with email: {}", registerRequest.getEmail());

        // ========== 1. Kiểm tra email tồn tại ==========
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Email already exists: {}", registerRequest.getEmail());
            throw new com.english12smart.exception.DuplicateResourceException(
                    "Email đã được đăng ký. Vui lòng sử dụng email khác.");
        }

        // ========== 2. Hash password ==========
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
        log.debug("Password hashed successfully");

        // ========== 3. Tạo user mới ==========
        long currentTime = System.currentTimeMillis();
        User newUser = User.builder()
                .email(registerRequest.getEmail())
                .password(hashedPassword)
                .fullName(registerRequest.getFullName())
                .role(registerRequest.getRole()) // STUDENT hoặc TEACHER
                .isActive(true)
                .isEmailVerified(false) // Mới đăng ký chưa xác minh email
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .totalLearningMinutes(0)
                .currentStreak(0)
                .longestStreak(0)
                .totalXP(0L)
                .level("Beginner")
                .build();

        // ========== 4. Lưu vào MongoDB ==========
        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // ========== 5. Return UserDTO (không trả password) ==========
        return convertToUserDTO(savedUser);
    }

    // ========== LOGIN - Đăng nhập ==========
    /**
     * Đăng nhập người dùng
     * 
     * @param loginRequest - Request chứa email và password
     * @return LoginResponse - Chứa user info, access token, refresh token
     * @throws RuntimeException - Nếu email không tồn tại hoặc mật khẩu sai
     * 
     *                          Quy trình:
     *                          1. Tìm user theo email
     *                          2. Verify password
     *                          3. Sinh JWT token
     *                          4. Update lastLoginAt
     *                          5. Return tokens & user info
     */
    @Transactional
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest loginRequest) {
        long startTime = System.currentTimeMillis();
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        // ========== 1. Tìm user theo email ==========
        long dbStartTime = System.currentTimeMillis();
        User user = userRepository.findByEmail(loginRequest.getEmail());
        log.info("Database query took: {} ms", System.currentTimeMillis() - dbStartTime);
        
        if (user == null) {
            log.warn("Login failed: user not found for email: {}", loginRequest.getEmail());
            throw new RuntimeException("Email hoặc mật khẩu không chính xác");
        }
        
        // Kiểm tra isActive (null được coi là true để tương thích với documents cũ)
        if (user.getIsActive() != null && !user.getIsActive()) {
            log.warn("Login failed: user inactive for email: {}", loginRequest.getEmail());
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        // ========== 2. Verify password ==========
        long passwordStartTime = System.currentTimeMillis();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for email: {}", loginRequest.getEmail());
            throw new RuntimeException("Email hoặc mật khẩu không chính xác");
        }
        log.info("Password verification took: {} ms", System.currentTimeMillis() - passwordStartTime);

        // ========== 3. Sinh JWT tokens ==========
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        long refreshTokenExpiresAt = jwtTokenProvider.getRefreshTokenExpirationTime();

        log.debug("Tokens generated successfully for user: {}", user.getId());

        // ========== 4. Update lastLoginAt ==========
        long updateStartTime = System.currentTimeMillis();
        user.setLastLoginAt(System.currentTimeMillis());
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiresAt(refreshTokenExpiresAt);
        userRepository.save(user);
        log.info("Update user took: {} ms", System.currentTimeMillis() - updateStartTime);

        log.info("User logged in successfully: {} - Total time: {} ms", user.getId(), System.currentTimeMillis() - startTime);

        // ========== 5. Return LoginResponse ==========
        return AuthDTO.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getTokenExpirationTime())
                .user(convertToUserDTO(user))
                .build();
    }

    // ========== REFRESH TOKEN - Làm mới access token ==========
    /**
     * Refresh access token bằng refresh token
     * 
     * @param refreshToken - Refresh token từ client
     * @return NewAccessToken - Access token mới
     * @throws RuntimeException - Nếu refresh token không hợp lệ
     */
    @Transactional
    public AuthDTO.RefreshTokenResponse refreshToken(String refreshToken) {
        log.info("Refreshing token...");

        // ========== 1. Verify refresh token ==========
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("Invalid refresh token");
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // ========== 2. Extract user ID từ refresh token ==========
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ========== 3. Sinh access token mới ==========
        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        log.info("Token refreshed successfully for user: {}", userId);

        // ========== 4. Return new access token ==========
        return AuthDTO.RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getTokenExpirationTime())
                .build();
    }

    // ========== LOGOUT - Đăng xuất ==========
    /**
     * Đăng xuất người dùng
     * Xóa refresh token
     * 
     * @param userId - User ID
     */
    @Transactional
    public void logout(String userId) {
        log.info("Logging out user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Xóa refresh token
        user.setRefreshToken(null);
        user.setRefreshTokenExpiresAt(null);
        userRepository.save(user);

        log.info("User logged out: {}", userId);
    }

    // ========== HELPER: Get User by Email ==========
    /**
     * Lấy user theo email
     * 
     * @param email - Email
     * @return User object
     */
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User không tồn tại");
        }
        return user;
    }

    // ========== HELPER: Get User by ID ==========
    /**
     * Lấy user theo ID
     * 
     * @param id - User ID
     * @return User object
     */
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    // ========== HELPER: Convert User -> UserDTO ==========
    /**
     * Convert User entity thành UserDTO (remove sensitive data)
     * 
     * @param user - User entity
     * @return UserDTO
     */
    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .createdAt(user.getCreatedAt())
                .totalLearningMinutes(user.getTotalLearningMinutes())
                .currentStreak(user.getCurrentStreak())
                .totalXP(user.getTotalXP())
                .level(user.getLevel())
                .build();
    }

    // ========== HELPER: Update User Profile ==========
    /**
     * Cập nhật profile user
     * 
     * @param userId        - User ID
     * @param updateRequest - Request chứa dữ liệu cập nhật
     * @return UserDTO sau khi cập nhật
     */
    @Transactional
    public UserDTO updateProfile(String userId, UserDTO updateRequest) {
        log.info("Updating profile for user: {}", userId);

        User user = getUserById(userId);

        // Cập nhật các field
        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }
        if (updateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }
        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }

        user.setUpdatedAt(System.currentTimeMillis());
        User updatedUser = userRepository.save(user);

        log.info("Profile updated for user: {}", userId);
        return convertToUserDTO(updatedUser);
    }

    // ========== FORGOT PASSWORD - Yêu cầu reset mật khẩu ==========
    /**
     * Xử lý yêu cầu quên mật khẩu (Forgot Password)
     * 
     * Flow:
     * 1. Tìm user theo email
     * 2. Sinh reset token (UUID)
     * 3. Lưu token + expiry time (1 giờ)
     * 4. Gửi email chứa link reset với token
     * 5. User click link để đến trang reset password
     * 
     * @param email - Email của user
     * @throws RuntimeException - Nếu email không tồn tại
     */
    @Transactional
    public void forgotPassword(String email) {
        log.info("========== FORGOT PASSWORD REQUEST ==========");
        log.info("Email: {}", email);

        // ========== 1. Tìm user theo email ==========
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.warn("Forgot password failed: Email không tồn tại: {}", email);
            // Bảo mật: không reveal email tồn tại hay không, trả về thông báo chung
            throw new RuntimeException("Nếu email tồn tại, link reset sẽ được gửi");
        }

        // ========== 2. Sinh reset token ==========
        // Token là UUID 36 ký tự, ít khả năng bị brute-force
        String resetToken = java.util.UUID.randomUUID().toString();
        log.debug("Generated reset token: {}", resetToken);

        // ========== 3. Lưu token + thời gian hết hạn (1 giờ) ==========
        long expiresAt = System.currentTimeMillis() + (60 * 60 * 1000); // 1 hour
        user.setResetToken(resetToken);
        user.setResetTokenExpiresAt(expiresAt);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
        log.debug("Reset token saved. Expires at: {}", expiresAt);

        // ========== 4. Gửi email reset password ==========
        try {
            // Gửi email chứa link reset password
            emailService.sendForgotPasswordEmail(email, user.getFullName(), resetToken);
            log.info("Forgot password email sent to: {}", email);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email forgot password: {}", e.getMessage(), e);
            // Xóa token nếu gửi email thất bại
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);
            userRepository.save(user);
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }

        log.info("========== FORGOT PASSWORD REQUEST SUCCESSFUL ==========");
    }

    // ========== RESET PASSWORD - Đặt lại mật khẩu ==========
    /**
     * Xử lý đặt lại mật khẩu (Reset Password)
     * 
     * Flow:
     * 1. Tìm user theo reset token
     * 2. Kiểm tra token còn hợp lệ không (chưa hết hạn)
     * 3. Kiểm tra mật khẩu mới và xác nhận trùng khớp
     * 4. Hash mật khẩu mới
     * 5. Lưu mật khẩu mới, xóa reset token
     * 6. Return thành công
     * 
     * @param token - Reset token (từ link email)
     * @param newPassword - Mật khẩu mới
     * @param confirmPassword - Xác nhận mật khẩu
     * @return ResetPasswordResponse - Kết quả reset
     * @throws RuntimeException - Nếu token không hợp lệ hoặc mật khẩu không khớp
     */
    @Transactional
    public AuthDTO.ResetPasswordResponse resetPassword(String token, String newPassword, String confirmPassword) {
        log.info("========== RESET PASSWORD REQUEST ==========");
        log.debug("Token: {}", token.substring(0, Math.min(8, token.length())) + "..."); // Log token một phần

        // ========== 1. Validation: Kiểm tra mật khẩu mới và xác nhận khớp ==========
        if (!newPassword.equals(confirmPassword)) {
            log.warn("Reset password failed: Passwords không khớp");
            throw new RuntimeException("Mật khẩu xác nhận không khớp với mật khẩu mới");
        }

        // ========== 2. Tìm user theo reset token ==========
        // Cần tạo custom query trong UserRepository: findByResetToken
        User user = userRepository.findByResetToken(token);
        if (user == null) {
            log.warn("Reset password failed: Token không hợp lệ hoặc không tồn tại");
            throw new RuntimeException("Link reset password không hợp lệ");
        }

        // ========== 3. Kiểm tra token còn hợp lệ không (chưa hết hạn) ==========
        long currentTime = System.currentTimeMillis();
        if (user.getResetTokenExpiresAt() == null || currentTime > user.getResetTokenExpiresAt()) {
            log.warn("Reset password failed: Token hết hạn cho user: {}", user.getEmail());
            // Xóa token hết hạn
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);
            userRepository.save(user);
            throw new RuntimeException("Link reset password đã hết hạn. Vui lòng yêu cầu lại.");
        }

        // ========== 4. Hash mật khẩu mới ==========
        String hashedPassword = passwordEncoder.encode(newPassword);
        log.debug("Password hashed successfully");

        // ========== 5. Cập nhật mật khẩu, xóa reset token ==========
        user.setPassword(hashedPassword);
        user.setResetToken(null);           // Xóa token sau khi sử dụng
        user.setResetTokenExpiresAt(null);  // Xóa thời gian hết hạn
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", user.getEmail());

        // ========== 6. Return kết quả ==========
        log.info("========== RESET PASSWORD REQUEST SUCCESSFUL ==========");
        return AuthDTO.ResetPasswordResponse.builder()
                .message("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.")
                .email(user.getEmail())
                .build();
    }

    // ========== HELPER: Change Password ==========
    /**
     * Đổi mật khẩu
     * 
     * @param userId      - User ID
     * @param oldPassword - Mật khẩu cũ
     * @param newPassword - Mật khẩu mới
     * @throws RuntimeException - Nếu mật khẩu cũ không chính xác
     */
    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

        User user = getUserById(userId);

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password change failed: old password incorrect for user: {}", userId);
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }

        // Hash new password
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }
}
