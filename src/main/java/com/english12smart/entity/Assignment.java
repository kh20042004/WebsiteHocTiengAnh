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
 * ========== ASSIGNMENT ENTITY ==========
 * Entity bài tập do giáo viên giao cho lớp/các lớp
 * 
 * Collection name: assignments
 * 
 * Flow:
 * 1. Giáo viên tạo assignment với exercises, deadline, chế độ chấm
 * 2. Giao cho một hoặc nhiều lớp (classroomIds)
 * 3. Học sinh submit assignment (tạo AssignmentSubmission)
 * 4. Giáo viên chấm điểm submission
 * 5. Tính toán statisticcs: submittedCount, gradedCount, averageScore
 */
@Document(collection = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    // ========== ID ==========
    @Id
    private String id;

    // ========== BASIC INFO ==========
    /**
     * Tiêu đề bài tập
     * VD: "Unit 1 - Reading Comprehension"
     */
    private String title;

    /**
     * Mô tả chi tiết bài tập
     * VD: "Đọc đoạn văn và trả lời câu hỏi"
     */
    private String description;

    /**
     * Loại bài tập: LISTENING, SPEAKING, READING, WRITING, GRAMMAR, VOCABULARY
     * Dùng để filter, display color badge
     */
    private String type;

    // ========== EXERCISE SELECTION ==========
    /**
     * List Exercise IDs được chọn cho assignment này
     * VD: ["ex_001", "ex_002", "ex_003"]
     * Bài tập này là một collection của các exercises từ database
     */
    private List<String> exerciseIds;

    /**
     * Unit ID (reference từ Unit entity)
     * Để hiểu context, chỉ nhằm mục đích thông tin
     */
    private String unitId;

    /**
     * Lesson ID (reference từ Lesson entity)
     * Để hiểu context, chỉ nhằm mục đích thông tin
     */
    private String lessonId;

    // ========== CLASSROOM ASSIGNMENT ==========
    /**
     * List Classroom IDs được giao assignment
     * Support multi-class assignment (IMPROVED)
     * VD: ["class_12a1", "class_12a2"]
     */
    private List<String> classroomIds;

    /**
     * (Deprecated - keep for backward compatibility)
     * Single classroom ID - dùng nếu classroomIds rỗng
     */
    @Indexed
    private String classroomId;

    /**
     * Tên lớp (denormalized - chỉ dùng khi single classroom)
     * Để hiển thị nhanh
     */
    private String classroomName;

    /**
     * List tên các lớp (denormalized từ classroomIds)
     * VD: ["12A1", "12A2"]
     */
    private List<String> classroomNames;

    // ========== TEACHER & ASSIGNMENT INFO ==========
    /**
     * ID giáo viên giao bài
     * Dùng để check permission
     */
    @Indexed
    private String teacherId;

    /**
     * Ngày giao bài (epoch millis)
     * Format: System.currentTimeMillis()
     */
    private Long assignedDate;

    /**
     * Hạn nộp (epoch millis)
     * Format: System.currentTimeMillis()
     */
    private Long dueDate;

    /**
     * Chế độ chấm điểm
     * AUTO: Tự động chấm (dùng answer key từ exercises)
     * MANUAL: Chấm tay (giáo viên phải chấm từng bài)
     */
    @Builder.Default
    private String gradingMode = "MANUAL";

    /**
     * Trạng thái assignment
     * DRAFT: Nháp, chưa công bố
     * ACTIVE: Đang hoạt động, học sinh có thể submit
     * CLOSED: Đã kết thúc, không thể submit nữa
     */
    @Builder.Default
    private String status = "ACTIVE";

    // ========== STATISTICS ==========
    /**
     * Tổng số học sinh trong lớp tại thời điểm giao
     * Tính từ classroom.studentIds.size()
     */
    @Builder.Default
    private Integer totalStudents = 0;

    /**
     * Số bài đã nộp
     * Calculate: count(assignments_submissions where status != null)
     */
    @Builder.Default
    private Integer submittedCount = 0;

    /**
     * Số bài chờ chấm
     * Calculate: count(assignments_submissions where status = "SUBMITTED")
     */
    @Builder.Default
    private Integer pendingCount = 0;

    /**
     * Số bài đã chấm
     * Calculate: count(assignments_submissions where score != null)
     */
    @Builder.Default
    private Integer gradedCount = 0;

    /**
     * Điểm trung bình của tất cả submissions (0-10)
     * Calculate: avg(assignments_submissions[].score)
     */
    @Builder.Default
    private Double averageScore = 0.0;

    /**
     * Tổng điểm của tất cả submissions (dùng để tính average)
     * Calculate: sum(assignments_submissions[].score)
     */
    @Builder.Default
    private Double totalScore = 0.0;

    // ========== TIMESTAMPS ==========
    /**
     * Thời gian tạo (epoch millis)
     */
    private Long createdAt;

    /**
     * Lần cập nhật cuối cùng (epoch millis)
     */
    private Long updatedAt;

    /** Display text cho type */
    public String getTypeDisplay() {
        if (type == null) return "Khác";
        return switch (type.toUpperCase()) {
            case "LISTENING" -> "Listening";
            case "SPEAKING" -> "Speaking";
            case "READING" -> "Reading";
            case "WRITING" -> "Writing";
            case "GRAMMAR" -> "Grammar";
            case "VOCABULARY" -> "Vocabulary";
            default -> type;
        };
    }

    /** CSS class cho type badge */
    public String getTypeBadgeClass() {
        if (type == null) return "bg-slate-50 text-slate-700";
        return switch (type.toUpperCase()) {
            case "LISTENING" -> "bg-blue-50 text-blue-700";
            case "SPEAKING" -> "bg-purple-50 text-purple-700";
            case "READING" -> "bg-emerald-50 text-emerald-700";
            case "WRITING" -> "bg-amber-50 text-amber-700";
            case "GRAMMAR" -> "bg-rose-50 text-rose-700";
            case "VOCABULARY" -> "bg-cyan-50 text-cyan-700";
            default -> "bg-slate-50 text-slate-700";
        };
    }

    /** Số bài chưa chấm */
    public int getPendingCount() {
        return Math.max(0, (submittedCount != null ? submittedCount : 0) - (gradedCount != null ? gradedCount : 0));
    }

    /** Display cho status */
    public String getStatusDisplay() {
        if (status == null) return "Đang mở";
        return switch (status.toUpperCase()) {
            case "CLOSED" -> "Đã đóng";
            case "DRAFT" -> "Bản nháp";
            default -> "Đang mở";
        };
    }

    /** CSS cho status badge */
    public String getStatusBadgeClass() {
        if (status == null) return "bg-emerald-50 text-emerald-700";
        return switch (status.toUpperCase()) {
            case "CLOSED" -> "bg-slate-100 text-slate-600";
            case "DRAFT" -> "bg-amber-50 text-amber-700";
            default -> "bg-emerald-50 text-emerald-700";
        };
    }
}
