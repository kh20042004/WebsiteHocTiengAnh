package com.english12smart.controller;

import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.entity.Exercise;
import com.english12smart.entity.ExerciseSubmission;
import com.english12smart.entity.User;
import com.english12smart.repository.ExerciseRepository;
import com.english12smart.repository.ExerciseSubmissionRepository;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.SpeakingService;
import com.english12smart.util.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ========== SPEAKING EXERCISE CONTROLLER ==========
 * API xử lý bài tập nói (SPEAKING_EXERCISE)
 * - Submit speaking exercise với audio + transcript
 * - Tính độ chính xác tự động
 * - Lưu kết quả vào MongoDB
 */
@RestController
@RequestMapping("/api/speaking")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class SpeakingExerciseController {

    // ========== Dependencies ==========
    private final SpeakingService speakingService;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // ========== API ENDPOINTS ==========

    /**
     * POST /api/speaking/exercises/{exerciseId}/submit
     * Nộp bài tập nói với audio + transcript
     *
     * Request:
     * - audio: MultipartFile (file mp3/wav)
     * - userTranscript: String (text được trích xuất từ Web Speech API)
     *
     * Response:
     * {
     *   "status": "success",
     *   "message": "...",
     *   "data": {
     *     "submissionId": "...",
     *     "exerciseId": "...",
     *     "score": 85,
     *     "maxScore": 100,
     *     "accuracy": 85.5,
     *     "feedback": "Good pronunciation!",
     *     "correctAnswer": "Hello, my name is John",
     *     "userTranscript": "Hello, my name is john",
     *     "audioUrl": "https://cloudinary.com/...",
     *     "passed": true
     *   }
     * }
     */
    @PostMapping("/exercises/{exerciseId}/submit")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<ApiResponseDTO<SpeakingSubmissionResponse>> submitSpeakingExercise(
            @PathVariable String exerciseId,
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("transcript") String userTranscript,
            HttpServletRequest httpRequest) {
        try {
            log.info("Student submitting speaking exercise: {}", exerciseId);

            // 1. Lấy student ID từ token
            String studentId = extractStudentIdFromRequest(httpRequest);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDTO.error(401, "Vui lòng đăng nhập"));
            }

            // 2. Lấy bài tập từ database
            Exercise exercise = exerciseRepository.findById(exerciseId)
                    .orElseThrow(() -> new IllegalArgumentException("Bài tập không tồn tại"));

            // 3. Kiểm tra loại bài tập
            if (!"SPEAKING_EXERCISE".equals(exercise.getType())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDTO.error(400, 
                            "Bài tập này không phải SPEAKING_EXERCISE"));
            }

            // 4. Lấy correctPhrase từ exercise
            String correctPhrase = exercise.getCorrectPhrase();
            if (correctPhrase == null || correctPhrase.isEmpty()) {
                log.warn("Exercise {} không có correctPhrase", exerciseId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDTO.error(400, "Bài tập chưa được thiết lập đáp án"));
            }

            // 5. Upload audio lên Cloudinary
            String audioUrl = speakingService.uploadAudioToCloudinary(audioFile);
            log.info("Audio uploaded: {}", audioUrl);

            // 6. Tính độ chính xác
            Double accuracy = speakingService.calculateAccuracy(userTranscript, correctPhrase);
            log.info("Accuracy calculated: {}%", accuracy);

            // 7. Sinh feedback
            Double minAccuracy = exercise.getMinAccuracy() != null ? exercise.getMinAccuracy() : 60.0;
            String feedback = speakingService.generateFeedback(userTranscript, correctPhrase, accuracy, minAccuracy);

            // 8. Tính điểm (based on accuracy)
            Integer maxScore = exercise.getMaxScore() != null ? exercise.getMaxScore() : 100;
            Integer score = (int) ((accuracy / 100) * maxScore);

            // 9. Tạo ExerciseSubmission
            ExerciseSubmission submission = ExerciseSubmission.builder()
                    .studentId(studentId)
                    .exerciseId(exerciseId)
                    .lessonId(exercise.getLessonId())
                    .unitId(exercise.getUnitId())
                    .audioUrl(audioUrl)
                    .userTranscript(userTranscript)
                    .correctAnswer(correctPhrase)
                    .accuracyScore(accuracy)
                    .feedback(feedback)
                    .score(score)
                    .maxScore(maxScore)
                    .status(speakingService.isPassed(accuracy, minAccuracy) ? "COMPLETED" : "COMPLETED")
                    .submittedAt(System.currentTimeMillis())
                    .createdAt(System.currentTimeMillis())
                    .build();

            // 10. Lưu vào database
            submissionRepository.save(submission);
            log.info("Submission saved: {}", submission.getId());

            // 11. Build response
            SpeakingSubmissionResponse response = SpeakingSubmissionResponse.builder()
                    .submissionId(submission.getId())
                    .exerciseId(exerciseId)
                    .score(score)
                    .maxScore(maxScore)
                    .accuracy(accuracy)
                    .feedback(feedback)
                    .correctAnswer(correctPhrase)
                    .userTranscript(userTranscript)
                    .audioUrl(audioUrl)
                    .passed(speakingService.isPassed(accuracy, minAccuracy))
                    .build();

            return ResponseEntity.ok(
                    ApiResponseDTO.success("Nộp bài tập nói thành công!", response)
            );

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Error submitting speaking exercise: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error(500, e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Lấy studentId từ JWT token
     */
    private String extractStudentIdFromRequest(HttpServletRequest request) {
        try {
            String token = null;

            // Lấy từ Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            // Nếu không có, lấy từ cookie
            if (token == null && request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                User user = userRepository.findByEmail(email);
                if (user != null) {
                    return user.getId();
                }
            }

            return null;
        } catch (Exception e) {
            log.warn("Error extracting student ID: {}", e.getMessage());
            return null;
        }
    }

    // ========== DTOs ==========

    /**
     * Response DTO cho speaking exercise submission
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpeakingSubmissionResponse {
        /** ID của bài nộp */
        private String submissionId;

        /** ID của bài tập */
        private String exerciseId;

        /** Điểm số đạt được */
        private Integer score;

        /** Điểm tối đa */
        private Integer maxScore;

        /** Độ chính xác (%) */
        private Double accuracy;

        /** Nhận xét tự động */
        private String feedback;

        /** Đáp án chuẩn */
        private String correctAnswer;

        /** Transcript của học sinh */
        private String userTranscript;

        /** URL audio ghi âm */
        private String audioUrl;

        /** Có vượt qua yêu cầu hay không */
        private Boolean passed;
    }
}
