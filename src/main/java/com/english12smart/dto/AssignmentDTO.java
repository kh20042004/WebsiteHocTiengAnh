package com.english12smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * ========== ASSIGNMENT DTOs ==========
 * DTOs cho Assignment (bài tập)
 * 
 * CreateRequest: Form tạo assignment từ teacher
 * UpdateRequest: Form cập nhật assignment
 * Response: Trả về thông tin assignment cho client
 */
public class AssignmentDTO {

    /**
     * ========== CREATE REQUEST DTO ==========
     * Dùng cho API POST /api/assignment/create
     * Học sinh nộp bài
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Tiêu đề không được để trống")
        private String title; // Tiêu đề bài tập

        private String description; // Mô tả chi tiết

        @NotBlank(message = "Loại bài tập không được để trống")
        private String type; // LISTENING, SPEAKING, READING, WRITING, GRAMMAR, VOCABULARY

        /**
         * List Exercise IDs để giao cho bài tập này
         * VD: ["ex_001", "ex_002", "ex_003"]
         */
        @NotNull(message = "Phải chọn ít nhất 1 bài tập")
        private List<String> exerciseIds;

        private String unitId; // Unit ID (để reference, optional)
        private String lessonId; // Lesson ID (để reference, optional)

        /**
         * List Classroom IDs để giao assignment
         * Support multi-class (IMPROVED)
         * VD: ["class_12a1", "class_12a2"]
         */
        @NotNull(message = "Phải chọn ít nhất 1 lớp")
        private List<String> classroomIds;

        @NotNull(message = "Hạn nộp không được để trống")
        private Long dueDate; // Hạn nộp (epoch millis)

        /**
         * Chế độ chấm điểm
         * AUTO: Tự động chấm
         * MANUAL: Chấm tay
         */
        @Builder.Default
        private String gradingMode = "MANUAL";

        /**
         * Giới hạn thời gian (phút)
         * Null = không giới hạn
         */
        private Integer timeLimitMinutes;
    }

    /**
     * ========== UPDATE REQUEST DTO ==========
     * Dùng cho API PUT /api/assignment/{id}
     * Giáo viên cập nhật assignment
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private String description;
        private String type;
        private List<String> exerciseIds;
        private List<String> classroomIds;
        private Long dueDate;
        private String gradingMode;
        private Integer timeLimitMinutes;
        private String status; // ACTIVE, CLOSED, DRAFT
    }

    /**
     * ========== RESPONSE DTO ==========
     * Trả về cho client khi get assignment info
     */
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

        // ========== Exercise Info ==========
        private List<String> exerciseIds; // Exercise IDs

        // ========== Classroom Info ==========
        private List<String> classroomIds; // Multi-class support
        private List<String> classroomNames;
        private String classroomId; // Backward compatibility
        private String classroomName; // Backward compatibility

        // ========== Teacher Info ==========
        private String teacherId;

        // ========== Dates ==========
        private Long assignedDate;
        private Long dueDate;
        private String dueDateDisplay;

        // ========== Status & Grading ==========
        private String status;
        private String statusDisplay;
        private String statusBadgeClass;
        private String gradingMode; // AUTO, MANUAL

        // ========== Statistics ==========
        @Builder.Default
        private Integer totalStudents = 0;
        @Builder.Default
        private Integer submittedCount = 0;
        @Builder.Default
        private Integer pendingCount = 0;
        @Builder.Default
        private Integer gradedCount = 0;
        @Builder.Default
        private Double averageScore = 0.0;

        private Long createdAt;
    }

    /**
     * ========== SUBMISSION REQUEST DTO ==========
     * Dùng cho API POST /api/assignment/{id}/submit
     * Học sinh nộp bài
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionRequest {
        /**
         * Câu trả lời của học sinh
         * Structure: {exerciseId: "answer"}
         * VD: {
         *   "ex_001": "Option B",
         *   "ex_002": "She went to school"
         * }
         */
        @NotNull(message = "Không có câu trả lời")
        private Map<String, Object> submittedAnswers;

        /**
         * Thời gian đã sử dụng (giây)
         * Để check quá thời gian giới hạn hay không
         */
        private Integer timeUsedSeconds;
    }

    /**
     * ========== SUBMISSION RESPONSE DTO ==========
     * Trả về cho student sau khi submit
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionResponse {
        private String submissionId;
        private String assignmentId;
        private String studentId;
        private String status;
        private Long submittedAt;
        private Double score; // Null nếu chưa chấm
        private String feedback; // Null nếu chưa chấm
        private String message; // "Nộp bài thành công" hoặc lỗi
    }

    /**
     * ========== GRADING REQUEST DTO ==========
     * Dùng cho API PUT /api/assignment/{id}/grade
     * Giáo viên chấm bài
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradingRequest {
        private String submissionId; // Submission ID cần chấm

        @NotNull(message = "Điểm số không được để trống")
        private Double score; // Điểm (0-10)

        private String feedback; // Comments từ giáo viên (optional)
    }

    /**
     * ========== SUBMISSION LIST RESPONSE ==========
     * Dùng khi giáo viên xem danh sách submissions cần chấm
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionListResponse {
        private String submissionId;
        private String assignmentId;
        private String studentId;
        private String studentName;
        private String studentEmail;
        private String status;
        private Long submittedAt;
        private Double score;
        private Boolean isLate;
        private Integer lateMinutes;
        private String statusDisplay;
        private String statusBadgeClass;
    }
}
