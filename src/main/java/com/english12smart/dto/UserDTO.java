package com.english12smart.dto;

import lombok.*;

/**
 * ========== USER DTO ==========
 * DTO dùng cho response - không trả password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;                    // User ID từ MongoDB
    private String email;                 // Email
    private String fullName;              // Tên đầy đủ
    private String avatarUrl;             // Avatar URL
    private String role;                  // STUDENT, TEACHER, ADMIN
    private String phoneNumber;           // Số điện thoại
    private Boolean isActive;             // Có hoạt động hay không
    private Boolean isEmailVerified;      // Email được xác minh hay không
    private Long createdAt;               // Ngày tạo (milliseconds)
    private Integer totalLearningMinutes; // Tổng phút học
    private Integer currentStreak;        // Streak hiện tại
    private Long totalXP;                 // Tổng XP
    private String level;                 // Level học tập
}
