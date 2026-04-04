package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

/**
 * ========== ASSIGNMENT SUBMISSION ENTITY ==========
 * Entity lưu bài nộp của học sinh cho một assignment
 * 
 * Collection name: assignment_submissions
 * 
 * Flow:
 * 1. Assignment được tạo & công bố
 * 2. Học sinh truy cập assignment
 * 3. Học sinh làm bài & submit (tạo AssignmentSubmission với submitted_answers)
 * 4. Hệ thống có thể tự động chấm (AUTO mode) hoặc chờ giáo viên chấm (MANUAL mode)
 * 5. Giáo viên review, chấm điểm
 * 6. Học sinh xem kết quả & feedback
 * 
 * 1 Assignment có N AssignmentSubmissions (1 submission per student)
 */
@Document(collection = "assignment_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentSubmission {

    // ========== ID ==========
    /**
     * ID submission (MongoDB ObjectId)
     * Format: 507f1f77bcf86cd799439011
     */
    @Id
    private String id;

    // ========== REFERENCE ==========
    /**
     * Assignment ID (reference từ Assignment entity)
     * Index để tìm nhanh submissions của 1 assignment
     */
    @Indexed
    private String assignmentId;

    /**
     * Student ID (User entity)
     * Index để tìm submissions của 1 student
     */
    @Indexed
    private String studentId;

    /**
     * Tên học sinh (denormalized từ User entity)
     * Để hiển thị nhanh trong grading list
     */
    private String studentName;

    /**
     * Email học sinh (denormalized từ User entity)
     */
    private String studentEmail;

    /**
     * Classroom ID (reference từ Classroom entity)
     * Để biết assignment được giao cho lớp nào
     */
    @Indexed
    private String classroomId;

    /**
     * Teacher ID (reference từ User entity)
     * ID giáo viên giao assignment này
     */
    private String teacherId;

    // ========== SUBMISSION CONTENT ==========
    /**
     * Trạng thái submission
     * NOT_STARTED: Học sinh chưa bắt đầu
     * IN_PROGRESS: Đang làm
     * SUBMITTED: Đã nộp
     * GRADED: Đã được chấm
     */
    @Builder.Default
    private String status = "NOT_STARTED";

    /**
     * Câu trả lời của học sinh cho từng exercise
     * Structure: {exerciseId: "answer_content"}
     * VD: {
     *   "ex_001": "Option B",
     *   "ex_002": "She went to school",
     *   "ex_003": "True"
     * }
     */
    private java.util.Map<String, Object> submittedAnswers;

    /**
     * Thời gian học sinh bắt đầu làm bài (epoch millis)
     * Để track xem họ có đủ thời gian hay không
     */
    private Long startedAt;

    /**
     * Thời gian học sinh nộp bài (epoch millis)
     * Để check xem có quá hạn không
     */
    private Long submittedAt;

    // ========== GRADING ==========
    /**
     * Điểm số (0-10)
     * Null = chưa chấm
     * Set sau khi teacher chấm hoặc auto-grade
     */
    private Double score;

    /**
     * Tổng điểm từ auto-grading (nếu có)
     * VD: 8/10
     */
    private Integer autoScore;

    /**
     * Tổng điểm từ manual grading (nếu từ teacher chấm)
     * VD: 7/10
     */
    private Integer manualScore;

    /**
     * Feedback/Comments từ giáo viên
     * VD: "Bài làm tốt, nhưng có vài lỗi ngữ pháp"
     */
    private String feedback;

    /**
     * Ngày giáo viên chấm (epoch millis)
     */
    private Long gradedAt;

    /**
     * ID của giáo viên chấm (có thể khác với teacher ID nếu là admin)
     */
    private String gradedByTeacherId;

    // ========== TIME LIMIT ==========
    /**
     * Giới hạn thời gian làm bài (tính bằng phút)
     * Null = không giới hạn
     * VD: 60 = 60 phút
     */
    private Integer timeLimitMinutes;

    /**
     * Thời gian đã sử dụng (tính bằng giây)
     * Được update khi học sinh nộp bài
     */
    private Integer timeUsedSeconds;

    /**
     * Học sinh có quá hạn không?
     * true = nộp quá hạn
     * false = nộp đúng hạn
     */
    @Builder.Default
    private Boolean isLate = false;

    /**
     * Thời gian quá hạn (tính bằng phút)
     * Null = không quá hạn
     * VD: 15 = quá hạn 15 phút
     */
    private Integer lateMinutes;

    // ========== TIMESTAMPS ==========
    /**
     * Thời gian tạo submission (epoch millis)
     * Có thể là when student joins assignment
     */
    private Long createdAt;

    /**
     * Lần cập nhật cuối cùng (epoch millis)
     */
    private Long updatedAt;

    // ========== HELPER METHODS ==========
    /**
     * Format status thành display name
     */
    public String getStatusDisplay() {
        if (status == null) return "Chưa bắt đầu";
        return switch (status) {
            case "NOT_STARTED" -> "Chưa bắt đầu";
            case "IN_PROGRESS" -> "Đang làm";
            case "SUBMITTED" -> "Đã nộp";
            case "GRADED" -> "Đã chấm";
            default -> status;
        };
    }

    /**
     * Format status thành CSS badge class
     */
    public String getStatusBadgeClass() {
        if (status == null) return "bg-gray-100 text-gray-700";
        return switch (status) {
            case "NOT_STARTED" -> "bg-gray-100 text-gray-700";
            case "IN_PROGRESS" -> "bg-yellow-100 text-yellow-700";
            case "SUBMITTED" -> "bg-blue-100 text-blue-700";
            case "GRADED" -> "bg-green-100 text-green-700";
            default -> "bg-gray-100 text-gray-700";
        };
    }

    /**
     * Tính điểm final (ưu tiên manual score nếu có)
     */
    public Double getFinalScore() {
        if (manualScore != null) return manualScore.doubleValue();
        if (autoScore != null) return autoScore.doubleValue();
        return null;
    }
}
