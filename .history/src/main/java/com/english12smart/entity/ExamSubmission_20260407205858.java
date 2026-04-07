package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Entity lưu bài làm của học sinh cho một đề thi.
 * Mỗi học sinh chỉ có tối đa 1 submission cho mỗi đề thi.
 */
@Document(collection = "exam_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSubmission {

    @Id
    private String id;

    /** ID đề thi mà học sinh tham gia */
    @Indexed
    private String examId;

    /** Tiêu đề đề thi (lưu dư thừa để hiển thị nhanh khi tra cứu lịch sử) */
    private String examTitle;

    /** ID học sinh làm bài */
    @Indexed
    private String studentId;

    /** Tên học sinh (lưu dư thừa để bảng kết quả của giáo viên hiển thị nhanh) */
    private String studentName;

    /** ID lớp học (để lọc kết quả theo lớp) */
    @Indexed
    private String classroomId;

    /**
     * Bản đồ lưu câu trả lời của học sinh.
     * Key: questionIndex (số thứ tự câu hỏi)
     * Value: câu trả lời học sinh chọn/nhập
     */
    @Builder.Default
    private Map<Integer, String> answers = new HashMap<>();

    /** Điểm đạt được (tổng điểm các câu đúng) */
    @Builder.Default
    private Integer score = 0;

    /** Điểm tối đa của đề thi */
    @Builder.Default
    private Integer totalScore = 0;

    /** Tỷ lệ phần trăm điểm (score / totalScore * 100) */
    @Builder.Default
    private Double percentage = 0.0;

    /**
     * Trạng thái bài làm:
     * - IN_PROGRESS: học sinh đang làm bài
     * - SUBMITTED: đã nộp, đang chờ chấm (tự động)
     * - GRADED: đã chấm xong, có điểm số
     */
    @Builder.Default
    private String status = "IN_PROGRESS";

    /** Thời điểm bắt đầu làm bài (epoch milliseconds) */
    private Long startedAt;

    /** Thời điểm nộp bài (epoch milliseconds) */
    private Long submittedAt;

    /** Thời gian thực tế làm bài tính bằng giây */
    private Integer timeTakenSeconds;

    // ======================================================================
    // Helper methods: Các phương thức hiển thị
    // ======================================================================

    /** Định dạng thời gian làm bài thành MM:SS hoặc Xm Ys */
    public String getTimeTakenDisplay() {
        // Nếu timeTakenSeconds không được set, thử tính từ startedAt và submittedAt
        Integer displaySeconds = timeTakenSeconds;
        if (displaySeconds == null || displaySeconds <= 0) {
            if (startedAt != null && submittedAt != null && submittedAt > startedAt) {
                displaySeconds = (int) ((submittedAt - startedAt) / 1000);
            } else {
                return "N/A";
            }
        }
        
        int minutes = displaySeconds / 60;
        int seconds = displaySeconds % 60;
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    /** Text hiển thị kết quả dạng phân số: "8/10" */
    public String getScoreDisplay() {
        return score + "/" + totalScore;
    }

    /** Text hiển thị phần trăm: "80.0%" */
    public String getPercentageDisplay() {
        return String.format("%.1f%%", percentage != null ? percentage : 0.0);
    }

    /** Số câu đã trả lời */
    public int getAnsweredCount() {
        return answers == null ? 0 : (int) answers.values().stream()
                .filter(a -> a != null && !a.isBlank())
                .count();
    }
}
