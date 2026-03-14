package com.english12smart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Assignment (bài tập)
 */
public class AssignmentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Tiêu đề không được để trống")
        private String title;

        private String description;

        @NotBlank(message = "Loại bài tập không được để trống")
        private String type;

        @NotBlank(message = "Lớp học không được để trống")
        private String classroomId;

        private Long dueDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private String description;
        private String type;
        private String classroomId;
        private Long dueDate;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String id;
        private String title;
        private String description;
        private String type;
        private String typeDisplay;
        private String typeBadgeClass;
        private String classroomId;
        private String classroomName;
        private String teacherId;
        private Long assignedDate;
        private Long dueDate;
        private String dueDateDisplay;
        private String status;
        private String statusDisplay;
        private String statusBadgeClass;

        @Builder.Default
        private Integer totalStudents = 0;
        @Builder.Default
        private Integer submittedCount = 0;
        @Builder.Default
        private Integer gradedCount = 0;
        @Builder.Default
        private Integer pendingCount = 0;
        @Builder.Default
        private Double averageScore = 0.0;

        private Long createdAt;
    }
}
