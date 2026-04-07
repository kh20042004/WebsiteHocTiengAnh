package com.english12smart.service;

import com.english12smart.dto.AISuggestionRequestDTO;
import com.english12smart.dto.AISuggestionResponseDTO;
import java.util.List;

/**
 * Interface cho AI Suggestion Service
 * Định nghĩa các phương thức để gợi ý AI
 */
public interface AISuggestionService {

    /**
     * Gợi ý tạo bài tập từ AI
     * @param request DTO chứa yêu cầu gợi ý
     * @return response với danh sách bài tập gợi ý
     */
    AISuggestionResponseDTO suggestExercises(AISuggestionRequestDTO request);

    /**
     * Gợi ý tạo bài kiểm tra từ AI
     * @param request DTO chứa yêu cầu gợi ý
     * @return response với bài kiểm tra gợi ý
     */
    AISuggestionResponseDTO suggestExam(AISuggestionRequestDTO request);

    /**
     * Gợi ý cải thiện nội dung bài tập hiện có
     * @param request DTO chứa nội dung ban đầu
     * @return response với nội dung cải thiện
     */
    AISuggestionResponseDTO improveContent(AISuggestionRequestDTO request);

    /**
     * Tạo giải thích cho một khái niệm
     * @param concept Khái niệm cần giải thích (ví dụ: "Present Perfect")
     * @return response với giải thích
     */
    AISuggestionResponseDTO generateExplanation(String concept);

    /**
     * Gợi ý mức độ khó cho một câu hỏi
     * @param question Nội dung câu hỏi
     * @return response với mức độ khó gợi ý
     */
    String suggestDifficulty(String question);

    /**
     * Lưu feedback từ giáo viên về gợi ý AI
     * @param suggestionLogId ID của gợi ý log
     * @param rating Rating từ 1-5
     * @param note Ghi chú từ giáo viên
     */
    void saveFeedback(String suggestionLogId, Integer rating, String note);

    /**
     * Lấy lịch sử gợi ý của giáo viên
     * @param teacherId ID của giáo viên
     * @return danh sách các gợi ý trước đó
     */
    List<Object> getSuggestionHistory(String teacherId);

    /**
     * Xóa một gợi ý log
     * @param suggestionLogId ID của gợi ý log
     */
    void deleteSuggestion(String suggestionLogId);
}
