package com.english12smart.controller;

import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.dto.ExamDTO;
import com.english12smart.dto.ExamSubmissionDTO;
import com.english12smart.entity.User;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho tính năng đề thi (Exam)
 * Base URL: /api/exams
 *
 * Endpoints cho giáo viên:
 * - POST   /api/exams                      → Tạo đề thi mới
 * - GET    /api/exams                      → Lấy danh sách đề thi của giáo viên
 * - GET    /api/exams/{examId}/results     → Xem kết quả các bài làm
 * - PUT    /api/exams/{examId}/status      → Mở/đóng đề thi
 * - DELETE /api/exams/{examId}             → Xóa đề thi
 *
 * Endpoints cho học sinh:
 * - GET    /api/exams/pin/{pin}                          → Tìm đề thi theo mã PIN
 * - POST   /api/exams/{examId}/start                     → Bắt đầu làm bài (tạo submission)
 * - POST   /api/exams/submissions/{submissionId}/submit  → Nộp bài
 * - GET    /api/exams/{examId}/result                    → Xem kết quả bài thi đã nộp
 */
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Slf4j
public class ExamApiController {

    private final ExamService examService;
    private final UserRepository userRepository;

    // ======================================================================
    // Endpoints dành cho GIÁO VIÊN
    // ======================================================================

    /**
     * POST /api/exams
     * Tạo đề thi mới, hệ thống tự sinh mã PIN 5 chữ số
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExamDTO.Response>> createExam(
            @Valid @RequestBody ExamDTO.CreateRequest request) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        log.info("Giáo viên {} gọi API tạo đề thi: {}", currentUser.getId(), request.getTitle());
        ExamDTO.Response response = examService.createExam(currentUser.getId(), request, isAdmin);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * GET /api/exams
     * Lấy danh sách tất cả đề thi của giáo viên đang đăng nhập
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<ExamDTO.Response>>> getMyExams() {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        log.info("Giáo viên {} lấy danh sách đề thi", currentUser.getId());
        List<ExamDTO.Response> exams = examService.getExamsByTeacher(currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponseDTO.success(exams));
    }

    /**
     * GET /api/exams/{examId}/results
     * Xem kết quả bài làm của tất cả học sinh trong một đề thi
     */
    @GetMapping("/{examId}/results")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<ExamSubmissionDTO.Response>>> getExamResults(
            @PathVariable String examId) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        log.info("Giáo viên {} xem kết quả đề thi: {}", currentUser.getId(), examId);
        List<ExamSubmissionDTO.Response> results = examService.getExamResults(examId, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponseDTO.success(results));
    }

    /**
     * PUT /api/exams/{examId}/status
     * Mở hoặc đóng đề thi
     * Body: {"status": "ACTIVE"} hoặc {"status": "CLOSED"}
     */
    @PutMapping("/{examId}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExamDTO.Response>> updateExamStatus(
            @PathVariable String examId,
            @RequestBody Map<String, String> body) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        String newStatus = body.getOrDefault("status", "ACTIVE");
        log.info("Giáo viên {} thay đổi trạng thái đề thi {} → {}", currentUser.getId(), examId, newStatus);
        ExamDTO.Response response = examService.updateExamStatus(examId, currentUser.getId(), newStatus, isAdmin);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * DELETE /api/exams/{examId}
     * Xóa đề thi (chỉ xóa được khi chưa có bài làm nào)
     */
    @DeleteMapping("/{examId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> deleteExam(@PathVariable String examId) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        log.info("Giáo viên {} xóa đề thi: {}", currentUser.getId(), examId);
        examService.deleteExam(examId, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponseDTO.success("Đã xóa đề thi thành công"));
    }

    /**
     * GET /api/exams/{examId}
     * Lấy chi tiết một đề thi để chỉnh sửa
     * Chỉ giáo viên tạo hoặc admin mới được lấy
     */
    @GetMapping("/{examId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExamDTO.Response>> getExamById(@PathVariable String examId) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        log.info("Giáo viên {} lấy chi tiết đề thi: {}", currentUser.getId(), examId);
        ExamDTO.Response response = examService.getExamById(examId, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    // ======================================================================
    // Endpoints dành cho HỌC SINH
    // ======================================================================

    /**
     * GET /api/exams/pin/{pin}
     * Tìm đề thi theo mã PIN 5 chữ số
     * Xác thực học sinh có thuộc lớp được giao không
     * Trả về thông tin đề thi (ĐÃ ẨN đáp án đúng)
     */
    @GetMapping("/pin/{pin}")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExamDTO.Response>> getExamByPin(
            @PathVariable String pin) {
        String studentId = getCurrentUserId();
        log.info("Học sinh {} tra cứu đề thi với PIN: {}", studentId, pin);
        ExamDTO.Response response = examService.getExamByPin(pin, studentId);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * POST /api/exams/{examId}/start
     * Học sinh bắt đầu làm bài: tạo bài làm (ExamSubmission) với trạng thái IN_PROGRESS
     * Trả về submissionId để dùng khi nộp bài
     */
    @PostMapping("/{examId}/start")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> startExam(
            @PathVariable String examId) {
        // Lấy thông tin học sinh để lưu tên lên bảng kết quả
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User student = userRepository.findByEmail(auth.getName());
        if (student == null) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin học sinh");
        }

        String studentName = student.getFullName() != null ? student.getFullName() : student.getEmail();
        log.info("Học sinh {} ({}) bắt đầu làm đề thi: {}", student.getId(), studentName, examId);

        String submissionId = examService.startExam(examId, student.getId(), studentName);

        // Trả về submissionId để client dùng khi gọi API nộp bài
        Map<String, String> result = Map.of(
                "submissionId", submissionId,
                "message", "Bắt đầu làm bài thành công"
        );
        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }

    /**
     * POST /api/exams/submissions/{submissionId}/submit
     * Học sinh nộp bài: hệ thống tự động chấm điểm ngay
     * Trả về kết quả chi tiết bao gồm đáp án đúng và giải thích
     */
    @PostMapping("/submissions/{submissionId}/submit")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExamSubmissionDTO.Response>> submitExam(
            @PathVariable String submissionId,
            @RequestBody ExamSubmissionDTO.SubmitRequest request) {
        String studentId = getCurrentUserId();
        log.info("Học sinh {} nộp bài làm: {}", studentId, submissionId);
        ExamSubmissionDTO.Response result = examService.submitExam(submissionId, studentId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }

    /**
     * GET /api/exams/{examId}/result
     * Học sinh xem lại kết quả bài thi đã nộp (bao gồm đáp án và giải thích)
     */
    @GetMapping("/{examId}/result")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExamSubmissionDTO.Response>> getMyResult(
            @PathVariable String examId) {
        String studentId = getCurrentUserId();
        log.info("Học sinh {} xem kết quả đề thi: {}", studentId, examId);
        ExamSubmissionDTO.Response result = examService.getStudentResult(examId, studentId);
        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }

    // ======================================================================
    // Helper: Lấy ID của user đang đăng nhập
    // ======================================================================

    /**
     * Lấy ID (MongoDB ObjectId) của user đang đăng nhập thông qua SecurityContext
     * SecurityContext lưu email → tra cứu User → lấy ID
     */
    private String getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email);
        }
        return user;
    }
}
