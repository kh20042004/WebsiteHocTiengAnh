package com.english12smart.config;

import com.english12smart.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ========== JWT AUTHENTICATION ENTRY POINT ==========
 * Xử lý lỗi khi authentication thất bại
 * - API requests (/api/**): trả về JSON 401
 * - Browser page requests: redirect về trang login
 */
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.error("Authentication failed for [{}]: {}", request.getRequestURI(), authException.getMessage());

        if (isApiRequest(request)) {
            // ========== API request: trả JSON 401 ==========
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            final ApiResponse<?> body = ApiResponse.error(
                    "Yêu cầu xác thực. Vui lòng đăng nhập.",
                    401,
                    "UNAUTHORIZED"
            );
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        } else {
            // ========== Browser page request: redirect về login ==========
            response.sendRedirect("/auth/login");
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");
        return uri.startsWith("/api/")
                || "XMLHttpRequest".equals(xRequestedWith)
                || (accept != null && accept.contains("application/json") && !accept.contains("text/html"));
    }
}
