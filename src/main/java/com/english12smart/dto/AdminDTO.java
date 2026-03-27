package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho admin APIs: quản lý user và thống kê hệ thống.
 */
public class AdminDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private String id;
        private String email;
        private String fullName;
        private String role;
        private Boolean isActive;
        private Boolean isEmailVerified;
        private Long createdAt;
        private Long lastLoginAt;
        private Long totalXP;
        private String level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserStatusRequest {
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRoleRequest {
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;
        private long adminUsers;
        private long teacherUsers;
        private long studentUsers;
        private long newUsersLast7Days;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentStats {
        private long totalUnits;
        private long activeUnits;
        private long totalClassrooms;
        private long totalAssignments;
        private long totalExams;
        private long totalExamSubmissions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStats {
        private UserStats users;
        private ContentStats content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserListResponse {
        private long total;
        private List<UserSummary> users;
    }
}