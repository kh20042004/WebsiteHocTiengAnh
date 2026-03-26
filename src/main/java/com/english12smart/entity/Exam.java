package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đề thi do giáo viên tạo
 * Mỗi đề thi có một mã PIN 5 số ngẫu nhiên dùng để học sinh tham gia
 */
@Document(collection = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    private String id;

    /** Tiêu đề đề thi, VD: "Kiểm tra Unit 1 - Vocabulary" */
    private String title;

    /** Mô tả / hướng dẫn làm bài */
    private String description;

    /** ID giáo viên tạo đề thi */
    @Indexed
    private String teacherId;

    /** ID lớp học được giao đề thi */
    @Indexed
    private String classroomId;

    /** Tên lớp (lưu dư thừa để hiển thị nhanh, tránh JOIN) */
    private String classroomName;

    /**
     * Mã PIN 5 chữ số để học sinh nhập vào thi
     * Được sinh ngẫu nhiên và đảm bảo không trùng
     */
    @Indexed(unique = true)
    private String pinCode;

    /**
     * Thời gian làm bài tính bằng phút
     * null = không giới hạn thời gian
     */
    private Integer timeLimitMinutes;

    /**
     * Trạng thái đề thi:
     * - DRAFT: bản nháp, chưa mở cho học sinh
     * - ACTIVE: đang mở, học sinh có thể vào thi
     * - CLOSED: đã đóng, không nhận thêm bài nộp
     */
    @Builder.Default
    private String status = "DRAFT";

    /** Danh sách câu hỏi trong đề thi */
    @Builder.Default
    private List<ExamQuestion> questions = new ArrayList<>();

    /** Tổng số học sinh trong lớp tại thời điểm tạo đề */
    @Builder.Default
    private Integer totalStudents = 0;

    /** Số bài đã nộp */
    @Builder.Default
    private Integer submittedCount = 0;

    /** ID giáo viên tạo đề (alias của teacherId, để rõ ngữ nghĩa) */
    private String createdBy;

    /** Thời điểm tạo (epoch milliseconds) */
    private Long createdAt;

    /** Thời điểm cập nhật lần cuối (epoch milliseconds) */
    private Long updatedAt;

    // ======================================================================
    // Inner class: Câu hỏi trong đề thi
    // ======================================================================

    /**
     * Đại diện cho một câu hỏi trong đề thi.
     * Câu hỏi có thể được import từ Exercise có sẵn hoặc tạo mới hoàn toàn.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExamQuestion {

        /** Số thứ tự câu hỏi (bắt đầu từ 0) */
        private Integer questionIndex;

        /** Nội dung câu hỏi */
        private String questionText;

        /**
         * Loại câu hỏi:
         * - MULTIPLE_CHOICE: trắc nghiệm nhiều lựa chọn
         * - TRUE_FALSE: đúng/sai
         * - FILL_IN_BLANK: điền vào chỗ trống
         */
        private String type;

        /** Các lựa chọn trả lời (dùng cho MULTIPLE_CHOICE và TRUE_FALSE) */
        @Builder.Default
        private List<String> options = new ArrayList<>();

        /** Đáp án đúng (so sánh không phân biệt hoa/thường khi chấm) */
        private String correctAnswer;

        /** Giải thích đáp án, hiện sau khi học sinh nộp bài */
        private String explanation;

        /** Điểm của câu này */
        @Builder.Default
        private Integer score = 1;

        /**
         * ID của Exercise gốc nếu câu hỏi này được import từ ngân hàng bài tập.
         * null nếu câu hỏi được tạo mới trực tiếp cho đề thi này.
         */
        private String sourceExerciseId;
    }

    // ======================================================================
    // Helper methods: Các phương thức hiển thị
    // ======================================================================

    /** Tổng điểm tối đa của toàn bộ đề thi */
    public int getTotalScore() {
        if (questions == null) return 0;
        return questions.stream()
                .mapToInt(q -> q.getScore() != null ? q.getScore() : 1)
                .sum();
    }

    /** Số lượng câu hỏi */
    public int getQuestionCount() {
        return questions == null ? 0 : questions.size();
    }

    /** Text hiển thị trạng thái */
    public String getStatusDisplay() {
        if (status == null) return "Bản nháp";
        return switch (status.toUpperCase()) {
            case "ACTIVE" -> "Đang mở";
            case "CLOSED" -> "Đã đóng";
            default -> "Bản nháp";
        };
    }

    /** CSS class Tailwind cho badge trạng thái */
    public String getStatusBadgeClass() {
        if (status == null) return "bg-amber-50 text-amber-700";
        return switch (status.toUpperCase()) {
            case "ACTIVE" -> "bg-emerald-50 text-emerald-700";
            case "CLOSED" -> "bg-slate-100 text-slate-600";
            default -> "bg-amber-50 text-amber-700";
        };
    }

    /** Text hiển thị thời gian làm bài */
    public String getTimeLimitDisplay() {
        if (timeLimitMinutes == null) return "Không giới hạn";
        return timeLimitMinutes + " phút";
    }
}
