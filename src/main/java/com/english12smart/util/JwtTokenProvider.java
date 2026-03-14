package com.english12smart.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ========== JWT TOKEN PROVIDER ==========
 * Utility class để tạo, verify, và parse JWT tokens
 * Sử dụng JJWT (JSON Web Token) library (v0.12.x+)
 * 
 * Token structure:
 * - Access Token: 1 giờ hạn
 * - Refresh Token: 7 ngày hạn
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    // ========== Configuration từ application.properties ==========
    @Value("${jwt.secret:your-secret-key-english-12-smart-learning-platform-2024-must-be-at-least-256-bits-long}")
    private String secretKeyString;

    @Value("${jwt.expiration:3600000}") // Default: 1 hour (3600000 ms)
    private long tokenExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // Default: 7 days (604800000 ms)
    private long refreshTokenExpiration;

    /**
     * Get SecretKey - Lazy initialization
     * Modern JJWT requires SecretKey for signing/verification
     * 
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        // Ensure secret key is long enough for HS512 (at least 256 bits = 32 bytes)
        if (secretKeyString.length() < 32) {
            log.warn("Secret key is too short for HS512, padding with default");
            secretKeyString = secretKeyString + "english12smart-secure-secret-key-for-production";
        }
        return Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    // ========== CREATE ACCESS TOKEN ==========
    /**
     * Tạo JWT access token
     * 
     * @param userId - User ID từ MongoDB
     * @param email - Email của user
     * @param role - Role của user (STUDENT, TEACHER, ADMIN)
     * @return JWT token (String)
     * 
     * Token sẽ chứa:
     * - sub (subject): userId
     * - email
     * - role
     * - iat (issued at): thời gian tạo
     * - exp (expiration): thời gian hết hạn
     */
    public String generateToken(String userId, String email, String role) {
        try {
            log.debug("Generating access token for userId: {} email: {}", userId, email);

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + tokenExpiration);

            String token = Jwts.builder()
                    // ========== Claims (dữ liệu trong token) ==========
                    .setSubject(userId) // user id
                    .claim("email", email)
                    .claim("role", role)
                    // ========== Timestamps ==========
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    // ========== Sign token với SecretKey ==========
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();

            log.debug("Access token generated successfully");
            return token;

        } catch (Exception e) {
            log.error("Error generating access token: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo token");
        }
    }

    // ========== CREATE REFRESH TOKEN ==========
    /**
     * Tạo JWT refresh token
     * Refresh token có hạn lâu hơn (7 ngày) so với access token (1 giờ)
     * 
     * @param userId - User ID
     * @return JWT refresh token
     */
    public String generateRefreshToken(String userId) {
        try {
            log.debug("Generating refresh token for userId: {}", userId);

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

            String token = Jwts.builder()
                    .setSubject(userId)
                    .claim("type", "refresh")
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();

            log.debug("Refresh token generated successfully");
            return token;

        } catch (Exception e) {
            log.error("Error generating refresh token: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo refresh token");
        }
    }

    // ========== VALIDATE TOKEN ==========
    /**
     * Kiểm tra token có hợp lệ hay không
     * 
     * @param token - JWT token
     * @return true nếu token hợp lệ, false nếu hết hạn hoặc bị modify
     */
    public boolean validateToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                log.warn("Token is null or empty");
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            log.debug("Token is valid");
            return true;

        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }

    // ========== VALIDATE REFRESH TOKEN ==========
    /**
     * Kiểm tra refresh token có hợp lệ hay không
     * 
     * @param token - Refresh token
     * @return true nếu hợp lệ
     */
    public boolean validateRefreshToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Check if it's actually a refresh token
            if (!"refresh".equals(claims.get("type"))) {
                log.warn("Token is not a refresh token");
                return false;
            }

            log.debug("Refresh token is valid");
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ========== GET USER ID FROM TOKEN ==========
    /**
     * Lấy user ID từ token
     * 
     * @param token - JWT token
     * @return User ID (subject)
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            throw new RuntimeException("Không thể trích xuất user ID từ token");
        }
    }

    // ========== GET EMAIL FROM TOKEN ==========
    /**
     * Lấy email từ token
     * 
     * @param token - JWT token
     * @return Email
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return (String) claims.get("email");
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting email from token: {}", e.getMessage());
            throw new RuntimeException("Không thể trích xuất email từ token");
        }
    }

    // ========== GET ROLE FROM TOKEN ==========
    /**
     * Lấy role từ token
     * 
     * @param token - JWT token
     * @return Role (STUDENT, TEACHER, ADMIN)
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return (String) claims.get("role");
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting role from token: {}", e.getMessage());
            throw new RuntimeException("Không thể trích xuất role từ token");
        }
    }

    // ========== GET EXPIRATION TIME (ms) ==========
    /**
     * Lấy thời gian hết hạn của access token (milliseconds)
     * 
     * @return Milliseconds
     */
    public long getTokenExpirationTime() {
        return tokenExpiration;
    }

    // ========== GET REFRESH TOKEN EXPIRATION TIME ==========
    /**
     * Lấy thời gian hết hạn của refresh token (milliseconds)
     * 
     * @return Milliseconds
     */
    public long getRefreshTokenExpirationTime() {
        return refreshTokenExpiration;
    }

    // ========== GET ALL CLAIMS FROM TOKEN ==========
    /**
     * Lấy tất cả claims từ token
     * 
     * @param token - JWT token
     * @return Claims object
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting claims from token: {}", e.getMessage());
            throw new RuntimeException("Không thể trích xuất claims từ token");
        }
    }
}
