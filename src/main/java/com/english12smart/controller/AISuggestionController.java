package com.english12smart.controller;

import com.english12smart.dto.AISuggestionRequestDTO;
import com.english12smart.dto.AISuggestionResponseDTO;
import com.english12smart.service.AISuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho AI Suggestion API
 * Cung cấp các endpoint để giáo viên gợi ý bài tập/kiểm tra từ AI
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AISuggestionController {

    private final AISuggestionService aiSuggestionService;

    /**
     * Gợi ý bài tập từ AI
     * POST /api/ai/suggest-exercises
     */
    @PostMapping("/suggest-exercises")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AISuggestionResponseDTO> suggestExercises(
            @RequestBody AISuggestionRequestDTO request) {

        log.info("Nhận yêu cầu gợi ý bài tập: unit={}, type={}", request.getUnit(), request.getExerciseType());

        // Kiểm tra dữ liệu hợp lệ
        if (request.getQuantity() == null || request.getQuantity() <= 0 || request.getQuantity() > 20) {
            return ResponseEntity.badRequest()
                    .body(AISuggestionResponseDTO.builder()
                            .status("error")
                            .message("Số lượng bài tập phải từ 1 đến 20")
                            .build());
        }

        AISuggestionResponseDTO response = aiSuggestionService.suggestExercises(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gợi ý tạo bài kiểm tra từ AI
     * POST /api/ai/suggest-exam
     */
    @PostMapping("/suggest-exam")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AISuggestionResponseDTO> suggestExam(
            @RequestBody AISuggestionRequestDTO request) {

        log.info("Nhận yêu cầu gợi ý bài kiểm tra: unit={}, duration={}", request.getUnit(), request.getDuration());

        // Kiểm tra dữ liệu hợp lệ
        if (request.getTotalQuestions() == null || request.getTotalQuestions() <= 0 || request.getTotalQuestions() > 50) {
            return ResponseEntity.badRequest()
                    .body(AISuggestionResponseDTO.builder()
                            .status("error")
                            .message("Số câu hỏi phải từ 1 đến 50")
                            .build());
        }

        AISuggestionResponseDTO response = aiSuggestionService.suggestExam(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cải thiện nội dung bài tập
     * POST /api/ai/improve-content
     */
    @PostMapping("/improve-content")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AISuggestionResponseDTO> improveContent(
            @RequestBody AISuggestionRequestDTO request) {

        log.info("Nhận yêu cầu cải thiện nội dung");

        if (request.getOriginalContent() == null || request.getOriginalContent().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AISuggestionResponseDTO.builder()
                            .status("error")
                            .message("Nội dung không được để trống")
                            .build());
        }

        AISuggestionResponseDTO response = aiSuggestionService.improveContent(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo giải thích cho một khái niệm
     * POST /api/ai/explain
     */
    @PostMapping("/explain")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AISuggestionResponseDTO> explain(
            @RequestParam String concept) {

        log.info("Nhận yêu cầu giải thích: {}", concept);

        if (concept == null || concept.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AISuggestionResponseDTO.builder()
                            .status("error")
                            .message("Khái niệm không được để trống")
                            .build());
        }

        AISuggestionResponseDTO response = aiSuggestionService.generateExplanation(concept);
        return ResponseEntity.ok(response);
    }

    /**
     * Gợi ý mức độ khó
     * POST /api/ai/suggest-difficulty
     */
    @PostMapping("/suggest-difficulty")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, String>> suggestDifficulty(
            @RequestParam String question) {

        log.info("Gợi ý mức độ khó cho câu hỏi");

        String difficulty = aiSuggestionService.suggestDifficulty(question);

        Map<String, String> response = new HashMap<>();
        response.put("difficulty", difficulty);

        return ResponseEntity.ok(response);
    }

    /**
     * Lưu feedback cho một gợi ý
     * POST /api/ai/feedback/{suggestionLogId}
     */
    @PostMapping("/feedback/{suggestionLogId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, String>> saveFeedback(
            @PathVariable String suggestionLogId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String note) {

        log.info("Nhận feedback cho suggestion: {}, rating: {}", suggestionLogId, rating);

        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Rating phải từ 1 đến 5"));
        }

        try {
            aiSuggestionService.saveFeedback(suggestionLogId, rating, note);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Feedback được lưu"));
        } catch (Exception e) {
            log.error("Lỗi lưu feedback: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử gợi ý
     * GET /api/ai/history
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getHistory() {
        log.info("Lấy lịch sử gợi ý");

        try {
            // Note: Trong thực tế, cần implement để lấy teacherId từ SecurityContext
            List<Object> history = aiSuggestionService.getSuggestionHistory("current_teacher_id");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", history);
            response.put("count", history.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi lấy lịch sử: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * Xóa một gợi ý
     * DELETE /api/ai/suggestion/{suggestionLogId}
     */
    @DeleteMapping("/suggestion/{suggestionLogId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, String>> deleteSuggestion(
            @PathVariable String suggestionLogId) {

        log.info("Xóa gợi ý: {}", suggestionLogId);

        try {
            aiSuggestionService.deleteSuggestion(suggestionLogId);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Gợi ý đã xóa"));
        } catch (Exception e) {
            log.error("Lỗi xóa gợi ý: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * GET /api/ai/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "AI Service is running"));
    }
}
