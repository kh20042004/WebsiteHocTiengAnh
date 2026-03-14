package com.english12smart.controller;

import com.english12smart.entity.User;
import com.english12smart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller cho Landing Page
 * Xử lý các request đến trang chủ và các trang public
 */
@Controller
@RequiredArgsConstructor
public class LandingController {

    private final UserRepository userRepository;

    /**
     * Trang chủ - Landing Page
     * GET /
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return landing page template
     */
    @GetMapping("/")
    public String index(Model model) {
        addAuthenticationToModel(model);
        return "landing/index";
    }

    /**
     * Trang chủ - Landing Page (alternative route)
     * GET /home
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return landing page template
     */
    @GetMapping("/home")
    public String home(Model model) {
        addAuthenticationToModel(model);
        return "landing/index";
    }

    /**
     * Trang giới thiệu
     * GET /about
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return about page template
     */
    @GetMapping("/about")
    public String about(Model model) {
        addAuthenticationToModel(model);
        return "landing/about";
    }

    /**
     * Trang liên hệ
     * GET /contact
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return contact page template
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        addAuthenticationToModel(model);
        return "landing/contact";
    }

    /**
     * Trang tính năng
     * GET /features
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return features page template
     */
    @GetMapping("/features")
    public String features(Model model) {
        addAuthenticationToModel(model);
        return "landing/features";
    }

    /**
     * Helper method để thêm thông tin authentication vào model
     * 
     * @param model - Model để truyền dữ liệu
     */
    private void addAuthenticationToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("isAuthenticated", true);

            // Lấy email từ principal
            String email = authentication.getName();

            // Tìm user trong database để lấy fullName và role
            User user = userRepository.findByEmail(email);
            if (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) {
                model.addAttribute("username", user.getFullName());
            } else {
                // Fallback về email nếu không có fullName
                model.addAttribute("username", email);
            }

            // Xác định display name cho role
            String authorityCode = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse("ROLE_STUDENT");
            String userRoleDisplay = switch (authorityCode) {
                case "ROLE_TEACHER" -> "Giáo viên";
                case "ROLE_ADMIN" -> "Quản trị viên";
                default -> "Học sinh";
            };
            String userRoleCode = switch (authorityCode) {
                case "ROLE_TEACHER" -> "TEACHER";
                case "ROLE_ADMIN" -> "ADMIN";
                default -> "STUDENT";
            };
            model.addAttribute("userRole", userRoleDisplay);
            model.addAttribute("userRoleCode", userRoleCode);
            model.addAttribute("authorities", authentication.getAuthorities());
        } else {
            model.addAttribute("isAuthenticated", false);
        }
    }

    /**
     * Trang FAQs
     * GET /faq
     * 
     * @return FAQ page template
     */
    @GetMapping("/faq")
    public String faq() {
        // TODO: Create FAQ page template
        return "landing/faq";
    }

    /**
     * Trang điều khoản sử dụng
     * GET /terms
     * 
     * @return terms page template
     */
    @GetMapping("/terms")
    public String terms() {
        // TODO: Create terms page template
        return "landing/terms";
    }

    /**
     * Trang chính sách bảo mật
     * GET /privacy
     * 
     * @return privacy page template
     */
    @GetMapping("/privacy")
    public String privacy() {
        // TODO: Create privacy page template
        return "landing/privacy";
    }

    // ========== AUTHENTICATION PAGES ==========

    /**
     * Trang đăng nhập
     * GET /auth/login
     * 
     * @return unified auth page template (login view)
     */
    @GetMapping("/auth/login")
    public String login(org.springframework.ui.Model model) {
        model.addAttribute("view", "login");
        return "auth/auth";
    }

    /**
     * Trang đăng ký
     * GET /auth/register
     * 
     * @return unified auth page template (register view)
     */
    @GetMapping("/auth/register")
    public String register(org.springframework.ui.Model model) {
        model.addAttribute("view", "register");
        return "auth/auth";
    }

    /**
     * Trang xác nhận email
     * GET /auth/verify?token=xxx
     * 
     * @param token - Verification token
     * @param model - Model
     * @return verify page template
     */
    @GetMapping("/auth/verify")
    public String verifyEmail(@org.springframework.web.bind.annotation.RequestParam(required = false) String token,
            org.springframework.ui.Model model) {
        if (token == null || token.isEmpty()) {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Token không hợp lệ");
            return "auth/verify";
        }

        // TODO: Gọi EmailVerificationService để verify token
        // boolean verified = emailVerificationService.verifyToken(token);

        // Tạm thời giả lập verify thành công
        model.addAttribute("status", "success");
        model.addAttribute("message", "Email đã được xác nhận thành công!");

        return "auth/verify";
    }

    /**
     * Trang redirect sau khi đăng nhập Google thành công
     * GET /oauth2/redirect?email=xxx&name=xxx
     * 
     * @return oauth2 redirect template
     */
    @GetMapping("/oauth2/redirect")
    public String oauth2Redirect() {
        return "oauth2/redirect";
    }

    // ========== COMMON USER PAGES ==========

    /**
     * Trang thông tin cá nhân
     * GET /profile
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return profile page template
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        addAuthenticationToModel(model);
        return "profile";
    }

    /**
     * Trang cài đặt
     * GET /settings
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return settings page template
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        addAuthenticationToModel(model);
        return "settings";
    }
}
