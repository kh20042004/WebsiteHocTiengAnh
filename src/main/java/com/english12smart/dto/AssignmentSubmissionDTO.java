package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * ========== ASSIGNMENT SUBMISSION DTOs ==========
 * DTOs cho AssignmentSubmission (bài nộp)
 * 
 * Dùng cho:
 * - Student submit: SubmitRequest → SubmitResponse
 * - Teacher grade: GradeRequest → GradeResponse
 * - View submissions: SubmissionResponse
 */
public class AssignmentSubmissionDTO {

    /**
     * ========== SUBMIT REQUEST DTO ==========
     * Dùng cho API POST /api/assignment/{assignmentId}/submit
     * Học sinh nộp bài
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmitRequest {
        /**
         * Câu trả lời của học sinh
         * Structure: {exerciseId: "answer_content"}
         * VD: {
         *   "ex_001": "Option B",
         *   "ex_002": "She went to school",
         *   "ex_003": "2024-03-29"
         * }
         */
        private Map<String, Object> submittedAnswers;

        /**
         * Thời gian đã sử dụng (giây)
         * Để check có quá time limit không
         * VD: 2400 (40 phút)
         */
        private Integer timeUsedSeconds;
    }

    /**
     * ========== SUBMIT RESPONSE DTO ==========
     * Trả về sau khi student submit
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmitResponse {
        private String submissionId; // Submission ID vừa tạo
        private String assignmentId;
        private String studentId;
        private String status; // SUBMITTED
        private Long submittedAt; // Thời gian nộp
        private Double score; // Null nếu chưa chấm
        private String feedback; // Null nếu chưa chấm
        private String message; // "Nộp bài thành công"
        private Boolean isLate; // Có quá hạn không
        private Integer lateMinutes; // Quá hạn bao nhiêu phút
    }

    /**
     * ========== GRADE REQUEST DTO ==========
     * Dùng cho API PUT /api/assignment/{assignmentId}/submission/{submissionId}/grade
     * Giáo viên chấm bài
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeRequest {
        /**
         * Điểm số (0-10)
         * Chứ không phải điểm tuyệt đối
         */
        private Double score;

        /**
         * Feedback/Comments từ giáo viên
         * VD: "Bài làm tốt, nhưng có vài lỗi ngữ pháp"
         */
        private String feedback;
    }

    /**
     * ========== GRADE RESPONSE DTO ==========
     * Trả về sau khi teacher grade
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeResponse {
        private String submissionId;
        private String assignmentId;
        private String studentId;
        private String studentName;
        private Double score; // Điểm vừa cập nhật
        private String feedback;
        private Long gradedAt; // Thời gian chấm
        private String message; // "Chấm bài thành công"
    }

    /**
     * ========== SUBMISSION RESPONSE DTO ==========
     * Dùng khi lấy chi tiết 1 submission
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionResponse {
        private String submissionId;
        private String assignmentId;
        private String studentId;
        private String studentName;
        private String studentEmail;
        private String classroomId;

        // ========== Submission Content ==========
        /**
         * Câu trả lời của học sinh
         */
        private Map<String, Object> submittedAnswers;

        /**
         * Trạng thái submission
         * NOT_STARTED, IN_PROGRESS, SUBMITTED, GRADED
         */
        private String status;
        private String statusDisplay;
        private String statusBadgeClass;

        // ========== Time Info ==========
        private Long startedAt;
        private Long submittedAt;
        private Integer timeLimitMinutes;
        private Integer timeUsedSeconds;
        private Boolean isLate;
        private Integer lateMinutes;

        // ========== Grading Info ==========
        private Double score; // Null nếu chưa chấm
        private Integer autoScore; // Auto-grade score
        private Integer manualScore; // Manual grade score
        private String feedback;
        private Long gradedAt;
        private String gradedByTeacherName;

        // ========== Display ==========
        private String submittedAtDisplay;
        private String gradedAtDisplay;
    }

    /**
     * ========== SUBMISSION LIST ITEM DTO ==========
     * Dùng khi giáo viên xem danh sách submissions cần chấm
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionListItemDTO {
        private String submissionId;
        private String assignmentId;
        private String studentId;
        private String studentName;
        private String studentEmail;

        // ========== Status ==========
        private String status;
        private String statusDisplay;
        private String statusBadgeClass;

        // ========== Submission Time ==========
        private Long submittedAt;
        private String submittedAtDisplay;
        private Boolean isLate;
        private Integer lateMinutes;

        // ========== Grading ==========
        private Double score; // Null nếu chưa chấm
        private String feedback;
        private Long gradedAt;
    }

    /**
     * ========== SUBMISSION STATS DTO ==========
     * Thống kê chung cho 1 assignment
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionStatsDTO {
        private String assignmentId;
        private Integer totalStudents; // Số học sinh trong lớp
        private Integer submittedCount; // Số bài nộp
        private Integer pendingCount; // Số bài chờ chấm
        private Integer gradedCount; // Số bài đã chấm
        private Double averageScore; // Điểm trung bình
        private Double maxScore; // Điểm cao nhất
        private Double minScore; // Điểm thấp nhất
    }
}
