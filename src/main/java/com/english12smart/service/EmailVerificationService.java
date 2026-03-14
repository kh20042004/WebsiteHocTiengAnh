package com.english12smart.service;

import com.english12smart.entity.User;
import com.english12smart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ========== EMAIL VERIFICATION SERVICE ==========
 * Service xử lý xác thực email cho user mới đăng ký
 * 
 * Flow:
 * 1. User đăng ký → Tạo verification token
 * 2. Gửi email chứa link xác thực
 * 3. User click link → Verify token
 * 4. Kích hoạt tài khoản
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    // TODO: Thêm EmailService khi implement gửi email

    /**
     * Tạo verification token cho user
     * 
     * @param user - User cần verify
     * @return verification token (UUID)
     */
    public String createVerificationToken(User user) {
        log.info("Tạo verification token cho user: {}", user.getEmail());

        // Tạo token ngẫu nhiên
        String token = UUID.randomUUID().toString();

        // Lưu token vào user (cần thêm field verificationToken vào User entity)
        // user.setVerificationToken(token);
        // user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24)); // Token
        // hết hạn sau 24h
        // userRepository.save(user);

        log.info("Đã tạo verification token: {}", token);
        return token;
    }

    /**
     * Gửi email xác thực
     * 
     * @param user  - User nhận email
     * @param token - Verification token
     */
    public void sendVerificationEmail(User user, String token) {
        log.info("Gửi email xác thực đến: {}", user.getEmail());

        // Tạo verification link
        String verificationLink = "http://localhost:8080/auth/verify?token=" + token;

        // TODO: Implement gửi email
        // Tạm thời chỉ log ra
        log.info("Verification link: {}", verificationLink);
        log.info("Email content:");
        log.info("Subject: Xác nhận email - English 12 Smart");
        log.info("Body: Xin chào {},", user.getFullName());
        log.info("Vui lòng click vào link sau để xác nhận email của bạn:");
        log.info("{}", verificationLink);
        log.info("Link này sẽ hết hạn sau 24 giờ.");

        // Khi có EmailService:
        // emailService.sendEmail(
        // user.getEmail(),
        // "Xác nhận email - English 12 Smart",
        // emailTemplate(user.getFullName(), verificationLink)
        // );
    }

    /**
     * Verify token và kích hoạt tài khoản
     * 
     * @param token - Verification token
     * @return true nếu verify thành công, false nếu thất bại
     */
    public boolean verifyToken(String token) {
        log.info("Đang verify token: {}", token);

        // TODO: Implement logic verify
        // 1. Tìm user có token này
        // 2. Kiểm tra token còn hạn không
        // 3. Kích hoạt tài khoản
        // 4. Xóa token

        // User user = userRepository.findByVerificationToken(token)
        // .orElse(null);

        // if (user == null) {
        // log.warn("Không tìm thấy user với token: {}", token);
        // return false;
        // }

        // if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
        // log.warn("Token đã hết hạn: {}", token);
        // return false;
        // }

        // // Kích hoạt tài khoản
        // user.setEmailVerified(true);
        // user.setVerificationToken(null);
        // user.setVerificationTokenExpiry(null);
        // userRepository.save(user);

        // log.info("Đã verify thành công email cho user: {}", user.getEmail());
        // return true;

        // Tạm thời return true để test
        return true;
    }

    /**
     * Gửi lại email xác thực
     * 
     * @param email - Email của user
     * @return true nếu gửi thành công
     */
    public boolean resendVerificationEmail(String email) {
        log.info("Yêu cầu gửi lại email xác thực cho: {}", email);

        // TODO: Implement
        // 1. Tìm user theo email
        // 2. Kiểm tra user đã verify chưa
        // 3. Tạo token mới
        // 4. Gửi email

        return true;
    }

    /**
     * Template email xác thực (HTML)
     * 
     * @param fullName         - Tên user
     * @param verificationLink - Link xác thực
     * @return HTML email template
     */
    private String emailTemplate(String fullName, String verificationLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                        .button { display: inline-block; padding: 12px 30px; background: #10b981; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎓 English 12 Smart</h1>
                            <p>Xác nhận email của bạn</p>
                        </div>
                        <div class="content">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Cảm ơn bạn đã đăng ký tài khoản English 12 Smart!</p>
                            <p>Vui lòng click vào nút bên dưới để xác nhận địa chỉ email của bạn:</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">Xác nhận Email</a>
                            </div>
                            <p>Hoặc copy link sau vào trình duyệt:</p>
                            <p style="background: #e5e7eb; padding: 10px; border-radius: 5px; word-break: break-all;">%s</p>
                            <p><strong>Lưu ý:</strong> Link này sẽ hết hạn sau 24 giờ.</p>
                            <p>Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 English 12 Smart. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(fullName, verificationLink, verificationLink);
    }
}
