package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO cho một bài tập gợi ý từ AI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseSuggestionDTO {
    // Nội dung câu hỏi
    private String question;

    // Danh sách các lựa chọn (cho multiple choice)
    private List<String> options;

    // Chỉ số đáp án đúng
    private Integer correctAnswerIndex;

    // Nội dung đáp án (cho fill-blank hoặc essay)
    private String correctAnswer;

    // Giải thích chi tiết
    private String explanation;

    // Mức độ khó: "A1", "A2", "B1", "B2"
    private String difficulty;

    // Loại bài: "multiple_choice", "fill_blank", "matching", "essay"
    private String type;

    // Chủ đề ngữ pháp (ví dụ: "Present Perfect")
    private String grammaticalTopic;

    // Từ vựng chính
    private String keyVocabulary;
}
