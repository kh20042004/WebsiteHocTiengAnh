package com.english12smart.config;

import com.english12smart.entity.User;
import com.english12smart.repository.UserRepository;
import com.english12smart.util.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ========== OAUTH2 SUCCESS HANDLER ==========
 * Xử lý sau khi đăng nhập Google thành công
 * 
 * Flow:
 * 1. User click "Đăng nhập bằng Google"
 * 2. Redirect đến Google login
 * 3. User đăng nhập Google
 * 4. Google redirect về app với user info
 * 5. Handler này được gọi
 * 6. Tạo/cập nhật user trong database
 * 7. Tạo JWT token
 * 8. Redirect đến dashboard
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 login thành công!");

        // Lấy thông tin user từ Google
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String googleId = oAuth2User.getAttribute("sub");

        log.info("User đăng nhập: email={}, name={}, googleId={}", email, name, googleId);

        // Tìm hoặc tạo user trong database
        User user = userRepository.findByEmail(email);
        if (user == null) {
            // Tạo user mới nếu chưa tồn tại
            user = createNewGoogleUser(email, name, picture, googleId);
        } else {
            // Cập nhật last login cho user đã tồn tại
            user.setLastLoginAt(System.currentTimeMillis());
            userRepository.save(user);
        }

        // Tạo JWT token
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Lưu refresh token vào DB
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiresAt(jwtTokenProvider.getRefreshTokenExpirationTime());
        userRepository.save(user);

        // ========== LƯU TOKEN VÀO COOKIE (bắt buộc cho server-side authentication) ==========
        // Tạo cookie cho JWT token (JwtAuthenticationFilter sẽ đọc cookie tên "token")
        jakarta.servlet.http.Cookie tokenCookie = new jakarta.servlet.http.Cookie("token", accessToken);
        tokenCookie.setHttpOnly(true); // Không cho JavaScript access (bảo mật)
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(3600); // 1 hour (khớp với JWT expiration)
        tokenCookie.setSecure(true); // Chỉ gửi qua HTTPS
        response.addCookie(tokenCookie);
        
        log.info("✅ JWT token saved to HttpOnly cookie (name='token')");


        // Redirect đến trang success để lưu token vào localStorage (cho client-side)
        String targetUrl = "/oauth2/redirect?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&name=" + URLEncoder.encode(name != null ? name : "", StandardCharsets.UTF_8)
                + "&token=" + accessToken
                + "&role=" + URLEncoder.encode(user.getRole(), StandardCharsets.UTF_8)
                + "&avatarUrl=" + URLEncoder.encode(user.getAvatarUrl() != null ? user.getAvatarUrl() : "", StandardCharsets.UTF_8);

        log.info("Redirect đến: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Tạo user mới từ thông tin Google
     * 
     * @param email    - Email từ Google
     * @param name     - Tên từ Google
     * @param picture  - Avatar URL từ Google
     * @param googleId - Google ID
     * @return User mới
     */
    private User createNewGoogleUser(String email, String name, String picture, String googleId) {
        log.info("Tạo user mới từ Google: {}", email);

        Long now = System.currentTimeMillis();

        User user = new User();
        user.setEmail(email);
        user.setFullName(name);
        user.setAvatarUrl(picture);
        // user.setGoogleId(googleId); // TODO: Thêm field googleId vào User entity nếu
        // cần
        user.setIsEmailVerified(true); // Google đã verify email rồi
        user.setRole("STUDENT"); // Mặc định là student (không có tiền tố ROLE_, JwtAuthenticationFilter tự thêm)
        user.setIsActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLoginAt(now);

        return userRepository.save(user);
    }
}
