package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request gợi ý AI
 * Giáo viên sử dụng để yêu cầu gợi ý tạo bài tập hoặc bài kiểm tra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AISuggestionRequestDTO {
    // Loại gợi ý: "exercise" hoặc "exam"
    private String type;

    // Unit hoặc chủ đề (ví dụ: "Unit 5: Future Tenses")
    private String unit;

    // Mức độ kỹ năng: "A1", "A2", "B1", "B2"
    private String skillLevel;

    // Số lượng bài tập/câu hỏi cần tạo
    private Integer quantity;

    // Loại bài tập: "multiple_choice", "fill_blank", "essay", "matching"
    private String exerciseType;

    // Loại bài kiểm tra: "midterm", "final", "popup_quiz"
    private String examType;

    // Thời gian làm bài (phút) - dùng cho exam
    private Integer duration;

    // Tổng số câu hỏi - dùng cho exam
    private Integer totalQuestions;

    // Yêu cầu tùy chỉnh từ giáo viên (ví dụ: "Focus on phrasal verbs")
    private String customPrompt;

    // Nội dung ban đầu để cải thiện (dùng cho "improve" type)
    private String originalContent;
}
