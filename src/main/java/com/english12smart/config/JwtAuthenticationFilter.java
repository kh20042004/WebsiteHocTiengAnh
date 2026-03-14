package com.english12smart.config;

import com.english12smart.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * ========== JWT AUTHENTICATION FILTER ==========
 * Filter xử lý mỗi request để verify JWT token
 * 
 * Quy trình:
 * 1. Lấy Authorization header từ request
 * 2. Extract JWT token từ header (format: "Bearer <token>")
 * 3. Verify token
 * 4. Extract claims từ token (userId, email, role)
 * 5. Set authentication vào SecurityContext
 * 6. Tiếp tục xử lý request với authentication
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * ========== SKIP FILTER CHO PUBLIC ENDPOINTS ==========
     * Filter này không chạy cho các endpoint public
     * LƯU Ý: Không skip "/" để có thể đọc JWT token từ cookie và hiển thị user menu
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip filter cho các public endpoints (trừ "/" để có thể đọc token)
        return path.equals("/favicon.ico") ||
               path.startsWith("/auth/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/assets/") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/oauth2/") ||
               path.startsWith("/login/oauth2/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/.well-known/") ||
               (path.equals("/api/auth/login") && method.equals("POST")) ||
               (path.equals("/api/auth/register") && method.equals("POST")) ||
               (path.equals("/api/auth/refresh") && method.equals("POST")) ||
               (path.equals("/api/auth/verify") && method.equals("GET"));
    }

    /**
     * ========== FILTER LOGIC ==========
     * Được gọi cho mỗi request
     * 
     * @param request - HTTP request
     * @param response - HTTP response
     * @param filterChain - FilterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // ========== 1. Lấy JWT token từ Authorization header ==========
            String token = getJwtFromRequest(request);

            // ========== 2. Nếu không có token, tiếp tục request ==========
            if (token == null || token.isEmpty()) {
                log.debug("No JWT token found in request");
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("JWT token found, verifying...");

            // ========== 3. Verify token ==========
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("JWT token validation failed");
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("JWT token validated successfully");

            // ========== 4. Extract claims từ token ==========
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            log.debug("Extracted claims - userId: {}, email: {}, role: {}", userId, email, role);

            // ========== 5. Tạo Authentication object ==========
            // Tạo GrantedAuthority từ role
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role) // ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN
            );

            // Tạo UsernamePasswordAuthenticationToken
            // Dùng email làm principal để hiển thị trên UI
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                            email,           // principal (email để hiển thị)
                            null,            // credentials
                            authorities      // authorities/roles
                    );

            // ========== 6. Set authentication vào SecurityContext ==========
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authentication set for user: {} with role: {}", userId, role);

        } catch (Exception e) {
            log.error("Error in JWT authentication filter: {}", e.getMessage());
            // Tiếp tục xử lý request mặc dù có lỗi
            // Exception handler sẽ xử lý nếu endpoint cần authentication
        }

        // ========== 7. Tiếp tục với request ==========
        filterChain.doFilter(request, response);
    }

    /**
     * ========== HELPER: Lấy JWT token từ request ==========
     * Lấy token từ:
     * 1. Authorization header (format: Bearer <token>)
     * 2. Cookie "token"
     * 
     * @param request - HTTP request
     * @return JWT token (không có "Bearer ") hoặc null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // ========== 1. Kiểm tra Authorization header ==========
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("Token found in Authorization header");
            return authHeader.substring("Bearer ".length());
        }

        // ========== 2. Kiểm tra cookie "token" ==========
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    log.debug("Token found in cookie");
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}
