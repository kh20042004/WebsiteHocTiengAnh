package com.english12smart.config;

import com.english12smart.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ========== JWT ACCESS DENIED HANDLER ==========
 * Xử lý lỗi khi user không có quyền truy cập
 * - API requests (/api/**): trả về JSON 403
 * - Browser page requests: redirect về trang login
 */
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("Access denied for [{}]: {}", request.getRequestURI(), accessDeniedException.getMessage());

        if (isApiRequest(request)) {
            // ========== API request: trả JSON 403 ==========
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            final ApiResponse<?> body = ApiResponse.error(
                    "Bạn không có quyền truy cập tài nguyên này.",
                    403,
                    "FORBIDDEN"
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
