package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO cho bài kiểm tra gợi ý từ AI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSuggestionDTO {
    // Tiêu đề bài kiểm tra
    private String title;

    // Mô tả
    private String description;

    // Thời gian làm bài (phút)
    private Integer duration;

    // Mức độ khó
    private String difficulty;

    // Các phần của bài kiểm tra
    private List<ExamSectionDTO> sections;

    // Hướng dẫn chung cho học sinh
    private String instructions;

    /**
     * DTO cho một phần của bài kiểm tra
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExamSectionDTO {
        // Tên phần (ví dụ: "Grammar", "Vocabulary", "Reading Comprehension")
        private String name;

        // Mô tả phần
        private String description;

        // Số câu hỏi trong phần này
        private Integer questionCount;

        // Điểm cho mỗi câu (tính % của phần)
        private Double pointPerQuestion;

        // Danh sách câu hỏi
        private List<ExerciseSuggestionDTO> questions;
    }
}
