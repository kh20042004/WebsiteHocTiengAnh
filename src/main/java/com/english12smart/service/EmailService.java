package com.english12smart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * ========== EMAIL SERVICE ==========
 * Service gửi email cho các chức năng:
 * - Verify email đăng ký
 * - Quên mật khẩu (forgot password)
 * - Thông báo khác
 * 
 * Cấu hình SMTP được lấy từ application.properties:
 * - spring.mail.host: SMTP server (ví dụ: smtp.gmail.com)
 * - spring.mail.port: SMTP port (ví dụ: 587)
 * - spring.mail.username: Email gửi
 * - spring.mail.password: Password (Google App Password nếu dùng Gmail)
 * 
 * Lưu ý: Để dùng Gmail, cần:
 * 1. Bật "Less secure app access" hoặc dùng Google App Password
 * 2. Bật 2FA trên Google Account
 * 3. Sinh "App Password" tại https://myaccount.google.com/apppasswords
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    // ========== Dependencies ==========
    private final JavaMailSender javaMailSender;

    /**
     * Email gửi (lấy từ application.properties)
     * spring.mail.username
     */
    @Value("${spring.mail.username:noreply@english12smart.com}")
    private String fromEmail;

    /**
     * Tên người gửi
     */
    @Value("${app.email.from-name:English 12 Smart}")
    private String fromName;

    /**
     * Domain/URL ứng dụng
     * Dùng cho link trong email
     */
    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;

    // ========== EMAIL QUÊN MẬT KHẨU ==========
    /**
     * Gửi email quên mật khẩu
     * 
     * @param email - Email nhận
     * @param userName - Tên người dùng
     * @param resetToken - Token reset (28 ký tự)
     * @throws MessagingException - Nếu gửi email thất bại
     * 
     * Flow:
     * 1. Tạo link reset: http://localhost:8080/auth/reset-password?token={resetToken}
     * 2. Soạn email HTML với link
     * 3. Gửi link đến email của user
     * 4. User click link để đổi mật khẩu
     * 5. Token có hiệu lực 1 giờ
     */
    public void sendForgotPasswordEmail(String email, String userName, String resetToken) throws MessagingException {
        log.info("Gửi email quên mật khẩu đến: {}", email);

        try {
            // ========== 1. Tạo link reset ==========
            String resetLink = appDomain + "/auth/reset-password?token=" + resetToken;
            log.debug("Reset link: {}", resetLink);

            // ========== 2. Soạn nội dung email HTML ==========
            String emailContent = buildForgotPasswordEmailContent(userName, resetLink);

            // ========== 3. Gửi email ==========
            sendHtmlEmail(email, "Đặt lại mật khẩu - English 12 Smart", emailContent);

            log.info("Email quên mật khẩu đã gửi thành công đến: {}", email);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email quên mật khẩu đến {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    // ========== EMAIL XÁC THỰC EMAIL ==========
    /**
     * Gửi email xác thực đăng ký
     * 
     * @param email - Email nhận
     * @param userName - Tên người dùng
     * @param verificationToken - Token xác thực
     * @throws MessagingException - Nếu gửi email thất bại
     */
    public void sendVerificationEmail(String email, String userName, String verificationToken) throws MessagingException {
        log.info("Gửi email xác thực đến: {}", email);

        try {
            // ========== 1. Tạo link verify ==========
            String verifyLink = appDomain + "/auth/verify?token=" + verificationToken;
            log.debug("Verify link: {}", verifyLink);

            // ========== 2. Soạn nội dung email HTML ==========
            String emailContent = buildVerificationEmailContent(userName, verifyLink);

            // ========== 3. Gửi email ==========
            sendHtmlEmail(email, "Xác thực email - English 12 Smart", emailContent);

            log.info("Email xác thực đã gửi thành công đến: {}", email);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email xác thực đến {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    // ========== EMAIL THÔNG BÁO ==========
    /**
     * Gửi email text đơn giản
     * 
     * @param to - Email nhận
     * @param subject - Tiêu đề email
     * @param content - Nội dung email (plain text)
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        log.info("Gửi email đơn giản đến: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            javaMailSender.send(message);
            log.info("Email đơn giản đã gửi thành công đến: {}", to);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email đơn giản đến {}: {}", to, e.getMessage(), e);
            // Không throw exception để tránh crash, chỉ log warning
        }
    }

    // ========== HELPER METHOD: Gửi email HTML ==========
    /**
     * Gửi email HTML
     * 
     * @param to - Email nhận
     * @param subject - Tiêu đề email
     * @param htmlContent - Nội dung email (HTML)
     * @throws MessagingException - Nếu gửi email thất bại
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        log.debug("Gửi email HTML đến: {} - Subject: {}", to, subject);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // ========== Set email details ==========
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content

            // ========== Gửi email ==========
            javaMailSender.send(message);
            log.debug("Email HTML đã gửi thành công");

        } catch (Exception e) {
            log.error("Lỗi khi gửi email HTML: {}", e.getMessage(), e);
            throw new MessagingException("Không thể gửi email", e);
        }
    }

    // ========== HELPER METHOD: Soạn nội dung email quên mật khẩu ==========
    /**
     * Xây dựng nội dung email HTML cho quên mật khẩu
     * 
     * @param userName - Tên người dùng
     * @param resetLink - Link reset password
     * @return - HTML content
     */
    private String buildForgotPasswordEmailContent(String userName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="vi" xmlns:th="http://www.thymeleaf.org">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Đặt lại mật khẩu</title>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f5f5f5; padding: 20px; }
                        .container { max-width: 500px; margin: 0 auto; background-color: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                        .header { background-color: #007bff; color: white; padding: 20px; border-radius: 6px; text-align: center; margin-bottom: 20px; }
                        .content { line-height: 1.6; color: #333; }
                        .btn { display: inline-block; background-color: #28a745; color: white; padding: 12px 30px; border-radius: 5px; text-decoration: none; margin: 20px 0; font-weight: bold; }
                        .btn:hover { background-color: #218838; }
                        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; text-align: center; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 10px; margin: 15px 0; border-radius: 4px; font-size: 14px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>🔐 Đặt lại mật khẩu</h2>
                        </div>
                        
                        <div class="content">
                            <p>Xin chào <strong>""" + (userName != null ? userName : "Bạn") + """
                                    </strong>,</p>
                            
                            <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. \
                            Hãy click vào nút dưới đây để tạo mật khẩu mới:</p>
                            
                            <center>
                                <a href=\"""" + resetLink + """
                                        \" class="btn">Đặt lại mật khẩu</a>
                            </center>
                            
                            <p>Hoặc copy link này vào trình duyệt:</p>
                            <p style="word-break: break-all; background-color: #f5f5f5; padding: 10px; border-radius: 4px; font-size: 12px;">
                                <code>""" + resetLink + """
                                        </code>
                            </p>
                            
                            <div class="warning">
                                ⚠️ <strong>Lưu ý:</strong> Link này chỉ có hiệu lực trong 1 giờ. \
                                Nếu hết hạn, bạn sẽ cần yêu cầu đặt lại mật khẩu lần nữa.
                            </div>
                            
                            <p style="color: #666; font-size: 14px;">
                                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này \
                                hoặc liên hệ với chúng tôi nếu có hoạt động lạ trên tài khoản.
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p>© 2024 English 12 Smart - Nền tảng học tiếng Anh lớp 12 thông minh</p>
                            <p>Đây là email tự động. Vui lòng không reply email này.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    // ========== HELPER METHOD: Soạn nội dung email xác thực ==========
    /**
     * Xây dựng nội dung email HTML cho xác thực email
     * 
     * @param userName - Tên người dùng
     * @param verifyLink - Link xác thực
     * @return - HTML content
     */
    private String buildVerificationEmailContent(String userName, String verifyLink) {
        return """
                <!DOCTYPE html>
                <html lang="vi" xmlns:th="http://www.thymeleaf.org">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Xác thực email</title>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f5f5f5; padding: 20px; }
                        .container { max-width: 500px; margin: 0 auto; background-color: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                        .header { background-color: #007bff; color: white; padding: 20px; border-radius: 6px; text-align: center; margin-bottom: 20px; }
                        .content { line-height: 1.6; color: #333; }
                        .btn { display: inline-block; background-color: #28a745; color: white; padding: 12px 30px; border-radius: 5px; text-decoration: none; margin: 20px 0; font-weight: bold; }
                        .btn:hover { background-color: #218838; }
                        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; text-align: center; }
                        .warning { background-color: #d1ecf1; border-left: 4px solid #0c5460; padding: 10px; margin: 15px 0; border-radius: 4px; font-size: 14px; color: #0c5460; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>✅ Xác thực email của bạn</h2>
                        </div>
                        
                        <div class="content">
                            <p>Xin chào <strong>""" + (userName != null ? userName : "Bạn") + """
                                    </strong>,</p>
                            
                            <p>Cảm ơn bạn đã đăng ký tài khoản English 12 Smart! \
                            Để hoàn tất đăng ký, vui lòng xác thực email của bạn \
                            bằng cách click vào nút dưới đây:</p>
                            
                            <center>
                                <a href=\"""" + verifyLink + """
                                        \" class="btn">Xác thực email</a>
                            </center>
                            
                            <p>Hoặc copy link này vào trình duyệt:</p>
                            <p style="word-break: break-all; background-color: #f5f5f5; padding: 10px; border-radius: 4px; font-size: 12px;">
                                <code>""" + verifyLink + """
                                        </code>
                            </p>
                            
                            <div class="warning">
                                ℹ️ <strong>Link này có hiệu lực 24 giờ.</strong>\
                                Nếu hết hạn, bạn cần đăng ký lại hoặc yêu cầu gửi lại email xác thực.
                            </div>
                            
                            <p style="color: #666; font-size: 14px;">
                                Nếu bạn không tạo tài khoản này, vui lòng bỏ qua email này.
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p>© 2024 English 12 Smart - Nền tảng học tiếng Anh lớp 12 thông minh</p>
                            <p>Đây là email tự động. Vui lòng không reply email này.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}
