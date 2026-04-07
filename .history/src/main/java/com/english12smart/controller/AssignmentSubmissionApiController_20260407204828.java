package com.english12smart.controller;

import com.english12smart.dto.ApiResponse;
import com.english12smart.dto.AssignmentSubmissionDTO;
import com.english12smart.entity.User;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.AssignmentSubmissionService;
import com.english12smart.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ========== ASSIGNMENT SUBMISSION API CONTROLLER ==========
 * REST API để xử lý submission & grading
 * 
 * Endpoints:
 * - POST /api/assignment/{assignmentId}/submit - Student nộp bài
 * - PUT /api/assignment/{assignmentId}/submission/{submissionId}/grade - Teacher chấm bài
 * - GET /api/assignment/{assignmentId}/submissions - Teacher xem danh sách nộp
 * - GET /api/assignment/{assignmentId}/submission/{submissionId} - Xem chi tiết nộp
 * 
 * Auth:
 * - Student: Phải là user của hệ thống (STUDENT ROLE)
 * - Teacher: Phải là teacher giao assignment hoặc admin
 */
@RestController
@RequestMapping("/api/assignment")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AssignmentSubmissionApiController {

    // ========== DEPENDENCIES ==========
    private final AssignmentSubmissionService submissionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.english12smart.service.AssignmentService assignmentService;
    private final UserRepository userRepository;

    // ========== GET ASSIGNMENT DETAILS - GET /api/assignment/{assignmentId} ==========
    /**
     * Lấy thông tin chi tiết một bài tập (dùng cho student làm bài)
     * Endpoint: GET /api/assignment/{assignmentId}
     */
    @GetMapping("/{assignmentId}")
    public org.springframework.http.ResponseEntity<com.english12smart.dto.ApiResponseDTO<com.english12smart.dto.AssignmentDTO.Response>> getAssignment(
            @PathVariable String assignmentId) {
        log.info("API: Lấy thông tin Assignment: {}", assignmentId);
        
        com.english12smart.dto.AssignmentDTO.Response assignment = assignmentService.getAssignmentById(assignmentId);
        return org.springframework.http.ResponseEntity.ok(
            com.english12smart.dto.ApiResponseDTO.success("Lấy thông tin bài tập thành công", assignment));
    }

    // ========== SUBMIT ASSIGNMENT - POST /api/assignment/{assignmentId}/submit ==========
    /**
     * API nộp bài cho assignment
     * Student gửi câu trả lời của mình
     * 
     * Endpoint: POST /api/assignment/{assignmentId}/submit
     * Auth: Bearer token (Student)
     * 
     * Request Body:
     * {
     *   "submittedAnswers": {
     *     "ex_001": "Option B",
     *     "ex_002": "She went to school",
     *     "ex_003": "2024-03-29"
     *   },
     *   "timeUsedSeconds": 2400
     * }
     * 
     * Response: 200 OK
     * {
     *   "status": "success",
     *   "message": "Nộp bài thành công",
     *   "data": {
     *     "submissionId": "...",
     *     "assignmentId": "...",
     *     "studentId": "...",
     *     "status": "SUBMITTED",
     *     "submittedAt": 1706707200000,
     *     "score": null,
     *     "feedback": null,
     *     "message": "Nộp bài thành công",
     *     "isLate": false,
     *     "lateMinutes": null
     *   }
     * }
     * 
     * Error cases:
     * - 404: Assignment không tồn tại
     * - 401: Không có token hoặc token invalid
     * - 400: Deadline đã qua, không thể nộp nữa (Optional)
     * 
     * @param assignmentId - Assignment ID
     * @param request - SubmitRequest (answers + timeUsed)
     * @param httpRequest - HTTP request để lấy token
     * @return ApiResponse<SubmitResponse>
     */
    @PostMapping("/{assignmentId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<?>> submitAssignment(
            @PathVariable String assignmentId,
            @Valid @RequestBody AssignmentSubmissionDTO.SubmitRequest request,
            HttpServletRequest httpRequest) {
        try {
            log.info("========== SUBMIT ASSIGNMENT REQUEST ==========");
            log.info("Assignment ID: {}", assignmentId);

            // ========== Lấy student ID từ JWT token ==========
            String token = getTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token không tìm thấy"));
            }

            String studentId = jwtTokenProvider.getUserIdFromToken(token);
            log.info("Student ID: {}", studentId);

            // ========== Call service ==========
            var response = submissionService.submitAssignment(assignmentId, studentId, request);

            // ========== Return response ==========
            log.info("Student {} submitted assignment {}", studentId, assignmentId);
            return ResponseEntity.ok(ApiResponse.success("Nộp bài thành công", response));

        } catch (com.english12smart.exception.ResourceNotFoundException e) {
            log.error("Submit error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (com.english12smart.exception.BadRequestException e) {
            log.error("Submit error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during submit", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== GRADE SUBMISSION - PUT /api/assignment/{assignmentId}/submission/{submissionId}/grade ==========
    /**
     * API chấm bài (Teacher grade student submission)
     * Giáo viên chấm điểm cho 1 bài nộp
     * 
     * Endpoint: PUT /api/assignment/{assignmentId}/submission/{submissionId}/grade
     * Auth: Bearer token (Teacher)
     * 
     * Request Body:
     * {
     *   "score": 8.5,
     *   "feedback": "Bài làm tốt, nhưng có vài lỗi ngữ pháp"
     * }
     * 
     * Response: 200 OK
     * {
     *   "status": "success",
     *   "message": "Chấm bài thành công",
     *   "data": {
     *     "submissionId": "...",
     *     "assignmentId": "...",
     *     "studentId": "...",
     *     "studentName": "Nguyễn Văn A",
     *     "score": 8.5,
     *     "feedback": "Bài làm tốt, nhưng có vài lỗi ngữ pháp",
     *     "gradedAt": 1706707200000,
     *     "message": "Chấm bài thành công"
     *   }
     * }
     * 
     * Error cases:
     * - 404: Submission hoặc Assignment không tồn tại
     * - 401: Không phải teacher của assignment này
     * - 400: Điểm không hợp lệ (< 0 hoặc > 10)
     * 
     * @param assignmentId - Assignment ID (để validate)
     * @param submissionId - Submission ID
     * @param request - GradeRequest (score + feedback)
     * @param httpRequest - HTTP request để lấy token
     * @return ApiResponse<GradeResponse>
     */
    @PutMapping("/{assignmentId}/submission/{submissionId}/grade")
    public ResponseEntity<ApiResponse<?>> gradeSubmission(
            @PathVariable String assignmentId,
            @PathVariable String submissionId,
            @Valid @RequestBody AssignmentSubmissionDTO.GradeRequest request,
            HttpServletRequest httpRequest) {
        try {
            log.info("========== GRADE SUBMISSION REQUEST ==========");
            log.info("Assignment ID: {}, Submission ID: {}", assignmentId, submissionId);

            // ========== Lấy teacher ID từ JWT token ==========
            String token = getTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token không tìm thấy"));
            }

            String teacherId = jwtTokenProvider.getUserIdFromToken(token);
            log.info("Teacher ID: {}", teacherId);

            // ========== Call service ==========
            var response = submissionService.gradeSubmission(submissionId, teacherId, request);

            // ========== Return response ==========
            log.info("Teacher {} graded submission {} with score {}", teacherId, submissionId, request.getScore());
            return ResponseEntity.ok(ApiResponse.success("Chấm bài thành công", response));

        } catch (com.english12smart.exception.ResourceNotFoundException e) {
            log.error("Grade error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (com.english12smart.exception.BadRequestException e) {
            log.error("Grade error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during grade", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== GET SUBMISSIONS - GET /api/assignment/{assignmentId}/submissions ==========
    /**
     * API lấy danh sách bài nộp cho 1 assignment
     * Giáo viên xem tất cả bài nộp của học sinh để chấm
     * 
     * Endpoint: GET /api/assignment/{assignmentId}/submissions
     * Auth: Bearer token (Teacher)
     * 
     * Response: 200 OK
     * {
     *   "status": "success",
     *   "message": "Danh sách bài nộp",
     *   "data": [
     *     {
     *       "submissionId": "...",
     *       "assignmentId": "...",
     *       "studentId": "...",
     *       "studentName": "Nguyễn Văn A",
     *       "studentEmail": "a@example.com",
     *       "status": "GRADED",
     *       "statusDisplay": "Đã chấm",
     *       "statusBadgeClass": "bg-green-100 text-green-700",
     *       "submittedAt": 1706707200000,
     *       "submittedAtDisplay": "29/03/2024 10:00",
     *       "isLate": false,
     *       "lateMinutes": null,
     *       "score": 8.5,
     *       "feedback": "Bài tốt",
     *       "gradedAt": 1706707300000
     *     },
     *     ...
     *   ]
     * }
     * 
     * @param assignmentId - Assignment ID
     * @param httpRequest - HTTP request để lấy token
     * @return ApiResponse<List<SubmissionListItemDTO>>
     */
    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getSubmissions(
            @PathVariable String assignmentId,
            HttpServletRequest httpRequest) {
        try {
            log.info("========== GET SUBMISSIONS REQUEST ==========");
            log.info("Assignment ID: {}", assignmentId);

            // ========== Validate assignment ID ==========
            if (assignmentId == null || assignmentId.isEmpty()) {
                log.error("Assignment ID is null or empty");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("ID bài tập không hợp lệ"));
            }

            // ========== Lấy teacher ID từ SecurityContext (Spring Security) ==========
            String teacherId = getCurrentUserId();
            log.info("Teacher ID from SecurityContext: {}", teacherId);

            // ========== Call service ==========
            log.debug("Calling submissionService.getSubmissionsByAssignment()");
            List<AssignmentSubmissionDTO.SubmissionListItemDTO> submissions = null;
            try {
                submissions = submissionService.getSubmissionsByAssignment(assignmentId, teacherId);
            } catch (Exception e) {
                log.error("Service error: {}", e.getMessage(), e);
                throw e;
            }

            // ========== Validate submissions response ==========
            if (submissions == null) {
                log.warn("Submissions list is null from service");
                submissions = new java.util.ArrayList<>();
            }

            // Filter out null items from conversion errors
            submissions = submissions.stream()
                    .filter(item -> item != null)
                    .collect(java.util.stream.Collectors.toList());

            log.info("Retrieved {} submissions for assignment {} (after filtering nulls)", submissions.size(), assignmentId);

            // ========== Return response ==========
            return ResponseEntity.ok(ApiResponse.success("Danh sách bài nộp", submissions));

        } catch (com.english12smart.exception.BadRequestException e) {
            log.error("Bad request error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (com.english12smart.exception.ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting submissions for assignment {}: {}", 
                    assignmentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi: " + e.getMessage()));
        }
    }

    // ========== GET SUBMISSION - GET /api/assignment/submission/{submissionId} ==========
    /**
     * API lấy chi tiết 1 submission
     * Dùng để xem chi tiết bài nộp (student xem kết quả, teacher xem để chấm)
     * 
     * Endpoint: GET /api/assignment/submission/{submissionId}
     * Auth: Bearer token (Student hoặc Teacher)
     * 
     * Response: 200 OK
     * {
     *   "status": "success",
     *   "message": "Chi tiết bài nộp",
     *   "data": {
     *     "submissionId": "...",
     *     "assignmentId": "...",
     *     "studentId": "...",
     *     "studentName": "Nguyễn Văn A",
     *     "studentEmail": "a@example.com",
     *     "classroomId": "...",
     *     "submittedAnswers": {
     *       "ex_001": "Option B",
     *       "ex_002": "She went to school"
     *     },
     *     "status": "GRADED",
     *     "statusDisplay": "Đã chấm",
     *     "submittedAt": 1706707200000,
     *     "submittedAtDisplay": "29/03/2024 10:00",
     *     "score": 8.5,
     *     "feedback": "Bài tốt",
     *     "gradedAt": 1706707300000,
     *     "gradedByTeacherName": "Trần Thị B"
     *   }
     * }
     * 
     * @param submissionId - Submission ID
     * @return ApiResponse<SubmissionResponse>
     */
    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<ApiResponse<?>> getSubmission(
            @PathVariable String submissionId) {
        try {
            log.info("========== GET SUBMISSION DETAIL REQUEST ==========");
            log.info("Submission ID: {}", submissionId);

            // ========== Call service ==========
            var submission = submissionService.getSubmission(submissionId);

            // ========== Return response ==========
            return ResponseEntity.ok(ApiResponse.success("Chi tiết bài nộp", submission));

        } catch (com.english12smart.exception.ResourceNotFoundException e) {
            log.error("Get submission error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== GET MY SUBMISSION - GET /api/assignment/{assignmentId}/my-submission ==========
    /**
     * API lấy submission của student hiện tại cho 1 assignment
     * Dùng để student xem kết quả bài nộp của mình
     * 
     * Endpoint: GET /api/assignment/{assignmentId}/my-submission
     * Auth: Bearer token (Student)
     * 
     * Response: 200 OK
     * {
     *   "status": "success",
     *   "message": "Chi tiết bài nộp",
     *   "data": {
     *     "submissionId": "...",
     *     "assignmentId": "...",
     *     "studentId": "...",
     *     "studentName": "...",
     *     "studentEmail": "...",
     *     "submittedAnswers": { ... },
     *     "status": "GRADED",
     *     "statusDisplay": "Đã chấm",
     *     "submittedAt": 1706707200000,
     *     "submittedAtDisplay": "29/03/2024 10:00",
     *     "timeUsedSeconds": 1200,
     *     "isLate": false,
     *     "lateMinutes": null,
     *     "score": 8.5,
     *     "feedback": "Bài tốt",
     *     "gradedAt": 1706707300000,
     *     "gradedByTeacherName": "..."
     *   }
     * }
     * 
     * Error cases:
     * - 404: Submission không tìm thấy (student chưa nộp)
     * - 401: Không có token hoặc token không hợp lệ
     * 
     * @param assignmentId - Assignment ID
     * @param httpRequest - HTTP request để lấy token
     * @return ApiResponse<SubmissionResponse>
     */
    @GetMapping("/{assignmentId}/my-submission")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<?>> getMySubmission(
            @PathVariable String assignmentId,
            HttpServletRequest httpRequest) {
        try {
            log.info("========== GET MY SUBMISSION REQUEST ==========");
            log.info("Assignment ID: {}", assignmentId);

            // ========== Lấy student ID từ JWT token ==========
            String token = getTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token không tìm thấy"));
            }

            String studentId = jwtTokenProvider.getUserIdFromToken(token);
            log.info("Student ID: {}", studentId);

            // ========== Call service ==========
            // Giả sử AssignmentSubmissionService có method: getMySubmission(assignmentId, studentId)
            // Nếu chưa có, cần tạo thêm
            var submission = submissionService.getMySubmission(assignmentId, studentId);

            // ========== Return response ==========
            log.info("Retrieved submission for student {} in assignment {}", studentId, assignmentId);
            return ResponseEntity.ok(ApiResponse.success("Chi tiết bài nộp", submission));

        } catch (com.english12smart.exception.ResourceNotFoundException e) {
            log.error("Get my submission error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting my submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Đã xảy ra lỗi. Vui lòng thử lại"));
        }
    }

    // ========== HELPER: Extract token từ request ==========
    /**
     * Lấy JWT token từ Authorization header
     * Format: Authorization: Bearer <token>
     * 
     * @param request - HTTP request
     * @return Token (không có "Bearer ") hoặc null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length());
        }
        return null;
    }
}
