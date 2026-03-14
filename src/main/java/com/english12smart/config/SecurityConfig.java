package com.english12smart.config;

import com.english12smart.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * ========== SECURITY CONFIGURATION ==========
 * Cấu hình Spring Security với JWT token authentication
 * 
 * Quy trình:
 * 1. Dùng JWT token thay vì session (stateless)
 * 2. Request phải có Authorization header với JWT token
 * 3. JwtAuthenticationFilter parse token từ header
 * 4. Lấy user từ MongoDB dựa trên token
 * 5. Set authentication vào SecurityContext
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, // Enable @PreAuthorize/@PostAuthorize
                securedEnabled = true, // Enable @Secured
                jsr250Enabled = true // Enable @RolesAllowed
)
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtTokenProvider jwtTokenProvider;
        private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

        // ========== PASSWORD ENCODER - Mã hóa mật khẩu ==========
        /**
         * Dùng BCrypt để hash mật khẩu
         * Bạn KHÔNG nên lưu mật khẩu ở dạng plain text
         * Strength = 10 là cân bằng tốt giữa bảo mật và hiệu suất
         * 
         * @return PasswordEncoder bean
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                log.info("Initializing BCryptPasswordEncoder with strength 10");
                return new BCryptPasswordEncoder(10); // 10 là độ mạnh cân bằng (thay vì 12)
        }

        // ========== JWT AUTHENTICATION FILTER ==========
        /**
         * Filter để verify JWT token từ mỗi request
         * 
         * @return JwtAuthenticationFilter bean
         */
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
                log.info("Initializing JwtAuthenticationFilter");
                return new JwtAuthenticationFilter(jwtTokenProvider);
        }

        // ========== SECURITY FILTER CHAIN - Cấu hình HTTP Security ==========
        /**
         * Cấu hình HTTP security:
         * - CSRF: Disable (vì dùng JWT token)
         * - Session: Stateless (không dùng session)
         * - CORS: Enable
         * - Authorization rules: Định nghĩa endpoint nào public, nào private
         * - JWT Filter: Thêm JWT filter vào chain
         * 
         * @param http - HttpSecurity
         * @return SecurityFilterChain
         * @throws Exception
         */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                log.info("Configuring security filter chain");

                http
                                // ========== CSRF: Disable vì dùng JWT token ==========
                                .csrf(csrf -> csrf.disable())

                                // ========== CORS: Enable ==========
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // ========== Session: Stateless - Không dùng session ==========
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // ========== Exception Handling ==========
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                                                .accessDeniedHandler(new JwtAccessDeniedHandler()))

                                // ========== Authorization Rules ==========
                                .authorizeHttpRequests(authz -> authz
                                                // Public endpoints (không cần authentication)
                                                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/home",
                                                                "/api/public/**")
                                                .permitAll()
                                                .requestMatchers("/error").permitAll() // Spring Boot error page
                                                .requestMatchers(HttpMethod.GET, "/auth/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/auth/register",
                                                                "/api/auth/login", "/api/auth/refresh")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/auth/verify", "/api/auth/logout").permitAll()

                                                // OAuth2 endpoints - Đăng nhập Google
                                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                                                // Static resources
                                                .requestMatchers("/assets/**", "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                                                "/.well-known/**")
                                                .permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                                                // Dashboard endpoints - cần authentication và role tương ứng
                                                .requestMatchers("/dashboard").authenticated()
                                                .requestMatchers("/dashboard/student/**")
                                                .hasAnyAuthority("ROLE_STUDENT", "ROLE_ADMIN")
                                                .requestMatchers("/dashboard/teacher/**")
                                                .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                                                .requestMatchers("/dashboard/admin").hasAuthority("ROLE_ADMIN")

                                                // Trang học tập cho học sinh
                                                .requestMatchers("/learn/**")
                                                .hasAnyAuthority("ROLE_STUDENT", "ROLE_ADMIN")

                                                // API Content - GET public (học sinh đọc), POST/PUT/DELETE cần TEACHER/ADMIN
                                                .requestMatchers(HttpMethod.GET, "/api/units/**", "/api/lessons/**", "/api/exercises/**").authenticated()
                                                .requestMatchers(HttpMethod.POST, "/api/units/**", "/api/lessons/**", "/api/exercises/**")
                                                .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/units/**", "/api/lessons/**", "/api/exercises/**")
                                                .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/units/**", "/api/lessons/**", "/api/exercises/**")
                                                .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")

                                                // Admin API endpoints - cần role ADMIN
                                                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                                                // Teacher API endpoints - cần role TEACHER
                                                .requestMatchers("/api/teacher/**")
                                                .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")

                                                // Student API endpoints - cần role STUDENT
                                                .requestMatchers("/api/student/**")
                                                .hasAnyAuthority("ROLE_STUDENT", "ROLE_ADMIN")

                                                // Profile endpoints - cần authentication
                                                .requestMatchers(HttpMethod.GET, "/api/profile/**").authenticated()
                                                .requestMatchers(HttpMethod.PUT, "/api/profile/**").authenticated()

                                                // Media upload - cần authentication
                                                .requestMatchers(HttpMethod.POST, "/api/media/**").authenticated()
                                                .requestMatchers(HttpMethod.DELETE, "/api/media/**").authenticated()

                                                // Các endpoint khác cần authentication
                                                .anyRequest().authenticated())

                                // ========== OAuth2 Login - Đăng nhập bằng Google ==========
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/auth/login") // Trang login tùy chỉnh
                                                .successHandler(oAuth2LoginSuccessHandler) // Handler sau khi đăng nhập
                                                                                           // thành công
                                                .failureUrl("/auth/login?error=oauth2") // URL khi đăng nhập thất bại
                                )

                                // ========== Add JWT Filter ==========
                                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

                log.info("Security filter chain configured successfully");
                return http.build();
        }

        // ========== CORS CONFIGURATION ==========
        /**
         * Cấu hình CORS cho phép request từ các domain khác nhau
         * 
         * @return CorsConfigurationSource bean
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                log.info("Configuring CORS");

                CorsConfiguration configuration = new CorsConfiguration();

                // ========== Allowed Origins ==========
                // FIX: Khi allowCredentials=true, không thể dùng setAllowedOriginPatterns("*")
                // Dùng setAllowedOrigins với List cụ thể thay vì regex pattern
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:3000", // React dev server
                                "http://localhost:8080", // Spring Boot
                                "http://127.0.0.1:8080", // Local
                                "http://127.0.0.1:3000", // Local alt
                                "https://yourdomain.com" // Production domain
                ));

                // ========== Allowed HTTP Methods ==========
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

                // ========== Allowed Headers ==========
                configuration.setAllowedHeaders(Arrays.asList(
                                "Content-Type",
                                "Authorization",
                                "X-Requested-With",
                                "Accept",
                                "Origin"));

                // ========== Exposed Headers ==========
                configuration.setExposedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "X-Total-Count"));

                // ========== Credentials ==========
                configuration.setAllowCredentials(true);

                // ========== Max Age ==========
                configuration.setMaxAge(3600L); // 1 hour

                // ========== Register Configuration ==========
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

        // ========== AUTHENTICATION MANAGER ==========
        /**
         * Bean cho AuthenticationManager
         * Có thể dùng trong authentication endpoints
         * 
         * @param http - HttpSecurity
         * @return AuthenticationManager
         * @throws Exception
         */
        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
                log.info("Initializing AuthenticationManager");
                return http.getSharedObject(AuthenticationManagerBuilder.class)
                                .build();
        }
}
