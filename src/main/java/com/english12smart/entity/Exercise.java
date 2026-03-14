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
 * ========== EXERCISE ENTITY (MongoDB Document) ==========
 * Đại diện cho một bài tập luyện tập trong bài học
 * Mỗi Lesson có thể có nhiều Exercise
 * Collection name: exercises
 */
@Document(collection = "exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    // ========== ID ==========
    /**
     * ID tự động sinh bởi MongoDB (ObjectId)
     */
    @Id
    private String id;

    // ========== LIÊN KẾT ==========
    /**
     * ID của Lesson cha
     */
    @Indexed
    private String lessonId;

    /**
     * ID của Unit (để query thuận tiện)
     */
    @Indexed
    private String unitId;

    // ========== THÔNG TIN CƠ BẢN ==========
    /**
     * Tiêu đề bài tập (ví dụ: "Exercise 1: Choose the correct answer")
     */
    private String title;

    /**
     * Hướng dẫn làm bài (ví dụ: "Chọn đáp án đúng nhất")
     */
    private String instruction;

    /**
     * Loại bài tập:
     * MULTIPLE_CHOICE    - Trắc nghiệm (chọn 1 đáp án)
     * FILL_IN_BLANK      - Điền vào chỗ trống
     * TRUE_FALSE         - Đúng/Sai
     * MATCHING           - Nối từ
     * ORDERING           - Sắp xếp câu
     * SHORT_ANSWER       - Trả lời ngắn
     */
    private String type;

    /**
     * Danh sách câu hỏi trong bài tập
     */
    private List<Question> questions;

    // ========== CẤU HÌNH ==========
    /**
     * Số thứ tự bài tập trong Lesson
     */
    private Integer orderIndex;

    /**
     * Số điểm tối đa của bài tập
     */
    private Integer maxScore;

    /**
     * Điểm XP nhận được khi hoàn thành bài tập
     */
    private Integer xpReward;

    /**
     * Thời gian làm bài (phút), null = không giới hạn
     */
    private Integer timeLimitMinutes;

    // ========== TRẠNG THÁI ==========
    /**
     * Trạng thái: true = hiển thị, false = ẩn
     */
    private Boolean isActive;

    // ========== THỜI GIAN ==========
    /**
     * Thời điểm tạo (milliseconds)
     */
    private Long createdAt;

    /**
     * Thời điểm cập nhật gần nhất (milliseconds)
     */
    private Long updatedAt;

    /**
     * ID của giáo viên/admin tạo bài tập
     */
    private String createdBy;

    // ========== INNER CLASS: CÂU HỎI ==========
    /**
     * Class đại diện cho một câu hỏi trong bài tập
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Question {

        /**
         * ID câu hỏi (số thứ tự, ví dụ: 1, 2, 3...)
         */
        private Integer questionIndex;

        /**
         * Nội dung câu hỏi
         */
        private String questionText;

        /**
         * Các lựa chọn (dành cho MULTIPLE_CHOICE, TRUE_FALSE, MATCHING)
         * Key là nhãn (A, B, C, D), Value là nội dung
         */
        private List<String> options;

        /**
         * Đáp án đúng
         * - MULTIPLE_CHOICE: "A", "B", "C", hoặc "D"
         * - TRUE_FALSE: "true" hoặc "false"
         * - FILL_IN_BLANK: từ cần điền
         * - SHORT_ANSWER: đáp án mẫu
         */
        private String correctAnswer;

        /**
         * Giải thích đáp án (hiển thị sau khi làm bài)
         */
        private String explanation;

        /**
         * Điểm cho câu hỏi này (mặc định = maxScore / số câu)
         */
        private Integer score;
    }
}
