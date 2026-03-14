package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity bài tập do giáo viên giao cho lớp
 */
@Document(collection = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    private String id;

    /** Tiêu đề bài tập */
    private String title;

    /** Mô tả chi tiết */
    private String description;

    /** Loại bài tập: LISTENING, SPEAKING, READING, WRITING, GRAMMAR, VOCABULARY */
    private String type;

    /** ID lớp học được giao */
    @Indexed
    private String classroomId;

    /** Tên lớp (denormalized để hiển thị nhanh) */
    private String classroomName;

    /** ID giáo viên giao bài */
    @Indexed
    private String teacherId;

    /** Ngày giao (epoch millis) */
    private Long assignedDate;

    /** Hạn nộp (epoch millis) */
    private Long dueDate;

    /** Trạng thái: ACTIVE | CLOSED | DRAFT */
    @Builder.Default
    private String status = "ACTIVE";

    /** Tổng số học sinh trong lớp tại thời điểm giao */
    @Builder.Default
    private Integer totalStudents = 0;

    /** Số bài đã nộp */
    @Builder.Default
    private Integer submittedCount = 0;

    /** Số bài đã chấm */
    @Builder.Default
    private Integer gradedCount = 0;

    /** Điểm trung bình (0-10) */
    @Builder.Default
    private Double averageScore = 0.0;

    private Long createdAt;
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
