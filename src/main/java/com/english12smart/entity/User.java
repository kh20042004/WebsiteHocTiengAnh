package com.english12smart.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ========== USER ENTITY (MongoDB Document) ==========
 * Lưu trữ thông tin người dùng (học sinh, giáo viên, admin)
 * Collection name: users
 */
@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // ========== ID ==========
    /**
     * ID tự động sinh bởi MongoDB (ObjectId)
     * Format: 507f1f77bcf86cd799439011
     */
    @Id
    private String id;

    // ========== EMAIL & AUTHENTICATION ==========
    /**
     * Email của người dùng (unique, indexed)
     * Dùng để đăng nhập
     */
    @Indexed(unique = true)
    private String email;

    /**
     * Mật khẩu (đã hash bằng BCrypt)
     * Không bao giờ lưu mật khẩu plain text!
     */
    private String password;

    // ========== PERSONAL INFO ==========
    /**
     * Họ tên đầy đủ
     */
    private String fullName;

    /**
     * Avatar URL (từ Cloudinary)
     */
    private String avatarUrl;

    /**
     * Số điện thoại (tuỳ chọn)
     */
    private String phoneNumber;

    /**
     * Ngày sinh (tuỳ chọn)
     */
    private String dateOfBirth;

    // ========== ROLE & PERMISSIONS ==========
    /**
     * Vai trò người dùng
     * STUDENT: Học sinh
     * TEACHER: Giáo viên
     * ADMIN: Quản trị viên
     */
    private String role;

    // ========== STATUS ==========
    /**
     * Trạng thái active
     * true = tài khoản hoạt động
     * false = tài khoản đã bị khóa
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Email đã được xác minh?
     * true = đã xác minh
     * false = chưa xác minh (mới đăng ký)
     */
    @Builder.Default
    private Boolean isEmailVerified = false;

    // ========== TIMESTAMPS ==========
    /**
     * Thời gian tạo tài khoản (milliseconds)
     * Sử dụng: System.currentTimeMillis()
     */
    private Long createdAt;

    /**
     * Thời gian cập nhật lần cuối (milliseconds)
     */
    private Long updatedAt;

    /**
     * Lần đăng nhập cuối cùng (milliseconds)
     */
    private Long lastLoginAt;

    // ========== REFRESH TOKEN (Để refresh JWT) ==========
    /**
     * Refresh token (nếu dùng token rotation)
     * Để cho phép refresh access token mà không cần đăng nhập lại
     */
    private String refreshToken;

    /**
     * Hạn sử dụng refresh token
     */
    private Long refreshTokenExpiresAt;

    // ========== OPTIONAL: LEARNING STATS ==========
    /**
     * Tổng số giờ học (tính theo phút)
     */
    @Builder.Default
    private Integer totalLearningMinutes = 0;

    /**
     * Streak hiện tại (số ngày học liên tục)
     */
    @Builder.Default
    private Integer currentStreak = 0;

    /**
     * Streak dài nhất từ trước đến nay
     */
    @Builder.Default
    private Integer longestStreak = 0;

    /**
     * Ngày học cuối cùng (để tính streak)
     * Format: yyyy-MM-dd
     */
    private String lastLearningDate;

    /**
     * XP (Experience Point) tích lũy
     * Dùng cho gamification
     */
    @Builder.Default
    private Long totalXP = 0L;

    /**
     * Level hiện tại (dựa trên XP)
     * VD: Beginner (0 XP), Intermediate (1000 XP), Advanced (5000 XP)
     */
    @Builder.Default
    private String level = "Beginner";

    // ========== NOTES ==========
    /*
     * Cấu trúc mongoDB Document:
     * {
     *   "_id": ObjectId("..."),
     *   "email": "student@gmail.com",
     *   "password": "$2a$10$...", // BCrypt hash
     *   "fullName": "Nguyễn Văn A",
     *   "avatarUrl": "https://res.cloudinary.com/...",
     *   "role": "STUDENT",
     *   "isActive": true,
     *   "isEmailVerified": false,
     *   "createdAt": 1706703600000,
     *   "updatedAt": 1706703600000,
     *   "totalLearningMinutes": 120,
     *   "currentStreak": 7,
     *   "totalXP": 2500,
     *   "level": "Intermediate"
     * }
     */
}
