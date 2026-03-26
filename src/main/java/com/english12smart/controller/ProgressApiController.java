package com.english12smart.controller;

import com.english12smart.dto.ProgressDTO;
import com.english12smart.entity.User;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.ProgressService;
import com.english12smart.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ========== PROGRESS REST API ==========
 * API xử lý tiến độ học tập
 */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Slf4j
public class ProgressApiController {

    private final ProgressService progressService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * GET /api/progress/lesson/{lessonId}
     * Lấy tiến độ bài học của học sinh hiện tại
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<?> getLessonProgress(@PathVariable String lessonId, HttpServletRequest request) {
        try {
            String studentId = extractStudentIdFromRequest(request);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Unauthorized", "Vui lòng đăng nhập"));
            }

            ProgressDTO.LessonProgress progress = progressService.calculateLessonProgress(lessonId, studentId);
            return ResponseEntity.ok(progress);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tiến độ bài học: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error", e.getMessage()));
        }
    }

    /**
     * POST /api/progress/exercise-submit
     * Ghi nhận bài tập hoàn thành
     * {
     *     "exerciseId": "...",
     *     "score": 90,
     *     "maxScore": 100
     * }
     */
    @PostMapping("/exercise-submit")
    public ResponseEntity<?> submitExercise(@RequestBody ExerciseSubmitRequest request, HttpServletRequest httpRequest) {
        try {
            String studentId = extractStudentIdFromRequest(httpRequest);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Unauthorized", "Vui lòng đăng nhập"));
            }

            // Ghi nhận bài tập
            progressService.recordExerciseSubmission(
                    request.getExerciseId(),
                    studentId,
                    request.getScore(),
                    request.getMaxScore()
            );

            return ResponseEntity.ok(new SuccessResponse("Success", "Đã ghi nhận bài tập"));

        } catch (Exception e) {
            log.error("Lỗi khi ghi nhận bài tập: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error", e.getMessage()));
        }
    }

    /**
     * Lấy studentId từ JWT token trong request
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
            if (token == null) {
                var cookies = request.getCookies();
                if (cookies != null) {
                    for (var cookie : cookies) {
                        if ("token".equals(cookie.getName())) {
                            token = cookie.getValue();
                            break;
                        }
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
            log.warn("Lỗi khi lấy token từ request: {}", e.getMessage());
            return null;
        }
    }

    // ========== DTOs ==========

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExerciseSubmitRequest {
        private String exerciseId;
        private Integer score;
        private Integer maxScore;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SuccessResponse {
        private String status;
        private String message;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String status;
        private String message;
    }
}
