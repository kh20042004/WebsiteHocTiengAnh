package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ExerciseSubmission - Bài nộp bài tập của học sinh
 * Tracking khi học sinh làm xong bài tập
 */
@Document(collection = "exercise_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseSubmission {

    /** ID bài nộp */
    private String id;

    /** ID học sinh */
    private String studentId;

    /** ID bài tập */
    private String exerciseId;

    /** ID bài học (từ Exercise.lessonId) */
    private String lessonId;

    /** ID Unit (từ Lesson.unitId) */
    private String unitId;

    /** Điểm số */
    private Integer score;

    /** Điểm tối đa */
    private Integer maxScore;

    /** Trạng thái: COMPLETED, FAILED, IN_PROGRESS */
    private String status = "COMPLETED";

    /** Thời gian nộp (milliseconds) */
    private Long submittedAt;

    /** Thời gian tạo */
    private Long createdAt;

    /** Ghi chú */
    private String notes;

    // ========== SPEAKING EXERCISES FIELDS ==========
    /**
     * URL audio ghi âm (upload lên Cloudinary)
     * Dành cho bài tập SPEAKING_EXERCISE
     */
    private String audioUrl;

    /**
     * Text do Web Speech API trích xuất từ audio ghi âm
     * Ví dụ: "Hello, my name is John"
     */
    private String userTranscript;

    /**
     * Đáp án chuẩn để so sánh
     * Lưu lại từ Exercise.correctPhrase tại thời điểm nộp
     */
    private String correctAnswer;

    /**
     * Điểm chính xác (0-100%)
     * Tính toán dựa trên similarity giữa userTranscript và correctAnswer
     */
    private Double accuracyScore;

    /**
     * Nhận xét tự động từ SpeakingService
     * Ví dụ: "Good pronunciation! Missed word: 'name'"
     */
    private String feedback;
}
