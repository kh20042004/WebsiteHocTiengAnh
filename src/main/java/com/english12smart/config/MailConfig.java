package com.english12smart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * ========== MAIL CONFIGURATION ==========
 * Tạo JavaMailSender bean nếu email chưa được configure
 * 
 * Nếu không configure spring.mail.host, sẽ dùng mock bean
 * để tránh lỗi dependency injection
 */
@Configuration
@Slf4j
public class MailConfig {

    /**
     * Tạo mock JavaMailSender nếu email chưa được configure
     * 
     * Điều kiện: @ConditionalOnMissingBean(JavaMailSender.class)
     * - Chỉ tạo bean này nếu không có bean JavaMailSender nào khác
     * - Nếu spring.mail.host được configure, Spring sẽ auto-create JavaMailSender, 
     *   điều kiện không thỏa mãn, bean này sẽ bị skip
     */
    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender mockJavaMailSender() {
        log.warn("⚠️  Email service không được configure. Tạo mock JavaMailSender.");
        log.warn("📧 Để bật email, thêm cấu hình spring.mail.* vào application.properties");
        
        // Dùng JavaMailSenderImpl với config cơ bản
        // Email sẽ không được gửi thực sự nhưng app vẫn hoạt động bình thường
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(1025); // Mock SMTP port (không cần server chạy)
        
        log.info("✅ Mock JavaMailSender được khởi tạo thành công");
        return mailSender;
    }
}
