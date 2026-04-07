package com.english12smart.controller;

import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.entity.ExamAntiFraudLog;
import com.english12smart.entity.ExamSubmission;
import com.english12smart.entity.Exam;
import com.english12smart.entity.User;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.exception.BadRequestException;
import com.english12smart.repository.ExamRepository;
import com.english12smart.repository.ExamSubmissionRepository;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.ExamAntiFraudLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API Controller cho Fraud Analysis (Phân Tích Gian Lận)
 * Base URL: /api/exams
 * 
 * Dành cho GIÁO VIÊN xem fraud logs của học sinh
 */
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Slf4j
public class FraudAnalysisController {

    private final ExamAntiFraudLogService fraudLogService;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final UserRepository userRepository;

    // ======================================================================
    // Endpoint 1: Lấy danh sách fraud logs của một submission
    // ======================================================================

    /**
     * GET /api/exams/{examId}/submissions/{submissionId}/fraud-logs
     * 
     * Lấy tất cả fraud events của một bài làm
     * Chỉ giáo viên mới có quyền xem
     */
    @GetMapping("/{examId}/submissions/{submissionId}/fraud-logs")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getFraudLogs(
            @PathVariable String examId,
            @PathVariable String submissionId) {
        
        log.info("Lấy fraud logs - Exam: {}, Submission: {}", examId, submissionId);

        try {
            // Kiểm tra exam tồn tại
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi"));

            // Kiểm tra quyền: giáo viên phải tạo exam này
            User teacher = getCurrentUser();
            if (!exam.getTeacherId().equals(teacher.getId())) {
                throw new BadRequestException("Bạn không có quyền xem fraud logs của đề thi này");
            }

            // Kiểm tra submission tồn tại và thuộc exam này
            ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài làm"));

            if (!submission.getExamId().equals(examId)) {
                throw new BadRequestException("Bài làm không thuộc đề thi này");
            }

            // Lấy fraud logs
            List<ExamAntiFraudLog> fraudLogs = fraudLogService.getFraudLogsBySubmission(submissionId);

            // Sắp xếp theo thời gian (mới nhất trước)
            fraudLogs.sort((a, b) -> b.getDetectedAt().compareTo(a.getDetectedAt()));

            Map<String, Object> response = new HashMap<>();
            response.put("studentName", submission.getStudentName());
            response.put("submissionId", submissionId);
            response.put("fraudLogCount", fraudLogs.size());
            response.put("trustScore", submission.getTrustScore());
            response.put("fraudLogs", fraudLogs);

            return ResponseEntity.ok(ApiResponseDTO.success(response));

        } catch (Exception e) {
            log.error("Lỗi lấy fraud logs: {}", e.getMessage());
            throw e;
        }
    }

    // ======================================================================
    // Endpoint 2: Lấy fraud summary của submission
    // ======================================================================

    /**
     * GET /api/exams/{examId}/submissions/{submissionId}/fraud-summary
     * 
     * Lấy tóm tắt chi tiết gian lận (thống kê + rủi ro)
     */
    @GetMapping("/{examId}/submissions/{submissionId}/fraud-summary")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getFraudSummary(
            @PathVariable String examId,
            @PathVariable String submissionId) {
        
        log.info("Lấy fraud summary - Exam: {}, Submission: {}", examId, submissionId);

        try {
            // Kiểm tra exam tồn tại
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi"));

            // Kiểm tra quyền
            User teacher = getCurrentUser();
            if (!exam.getTeacherId().equals(teacher.getId())) {
                throw new BadRequestException("Bạn không có quyền xem fraud summary");
            }

            // Phân tích fraud
            Map<String, Object> analysis = fraudLogService.analyzeSubmissionFraud(submissionId);

            return ResponseEntity.ok(ApiResponseDTO.success(analysis));

        } catch (Exception e) {
            log.error("Lỗi lấy fraud summary: {}", e.getMessage());
            throw e;
        }
    }

    // ======================================================================
    // Endpoint 3: Dashboard gian lận của một đề thi
    // ======================================================================

    /**
     * GET /api/exams/{examId}/fraud-dashboard
     * 
     * Lấy dashboard hiển thị tất cả students nghi ngờ gian lận
     * Giáo viên dùng để review nhanh
     */
    @GetMapping("/{examId}/fraud-dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getFraudDashboard(
            @PathVariable String examId) {
        
        log.info("Lấy fraud dashboard - Exam: {}", examId);

        try {
            // Kiểm tra exam tồn tại
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi"));

            // Kiểm tra quyền
            User teacher = getCurrentUser();
            if (!exam.getTeacherId().equals(teacher.getId())) {
                throw new BadRequestException("Bạn không có quyền xem fraud dashboard");
            }

            // Lấy fraud summary
            Map<String, Object> fraudSummary = fraudLogService.getFraudSummary(examId);

            // Thêm thông tin exam
            Map<String, Object> response = new HashMap<>(fraudSummary);
            response.put("examTitle", exam.getTitle());
            response.put("examId", examId);

            return ResponseEntity.ok(ApiResponseDTO.success(response));

        } catch (Exception e) {
            log.error("Lỗi lấy fraud dashboard: {}", e.getMessage());
            throw e;
        }
    }

    // ======================================================================
    // Endpoint 4: Mark fraud log as reviewed
    // ======================================================================

    /**
     * PUT /api/exams/{examId}/fraud-logs/{fraudLogId}/mark-reviewed
     * 
     * Giáo viên đánh dấu đã xem fraud log này
     */
    @PutMapping("/{examId}/fraud-logs/{fraudLogId}/mark-reviewed")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> markAsReviewed(
            @PathVariable String examId,
            @PathVariable String fraudLogId,
            @RequestBody(required = false) Map<String, String> request) {
        
        try {
            String note = request != null ? request.get("note") : "";
            
            if (note != null && !note.isEmpty()) {
                fraudLogService.addTeacherNote(fraudLogId, note);
            } else {
                fraudLogService.markFraudLogAsReviewed(fraudLogId);
            }

            return ResponseEntity.ok(ApiResponseDTO.success("Đã đánh dấu xem"));

        } catch (Exception e) {
            log.error("Lỗi mark as reviewed: {}", e.getMessage());
            throw new BadRequestException("Không thể cập nhật");
        }
    }

    // ======================================================================
    // Endpoint 5: Lấy unreviewed fraud logs
    // ======================================================================

    /**
     * GET /api/exams/{examId}/fraud-logs/unreviewed
     * 
     * Lấy danh sách fraud logs chưa xem
     */
    @GetMapping("/{examId}/fraud-logs/unreviewed")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getUnreviewedFraudLogs(
            @PathVariable String examId) {
        
        try {
            // Kiểm tra exam tồn tại
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi"));

            // Kiểm tra quyền
            User teacher = getCurrentUser();
            if (!exam.getTeacherId().equals(teacher.getId())) {
                throw new BadRequestException("Bạn không có quyền xem fraud logs");
            }

            // Lấy unreviewed logs
            List<ExamAntiFraudLog> unreviewedLogs = fraudLogService.getUnreviewedFraudLogs(examId);

            // Sắp xếp theo severity và thời gian
            unreviewedLogs.sort((a, b) -> {
                int severityOrder = "HIGH".equals(b.getSeverity()) ? 1 : 
                                  "MEDIUM".equals(b.getSeverity()) ? 0 : -1;
                if (severityOrder != 0) return severityOrder;
                return b.getDetectedAt().compareTo(a.getDetectedAt());
            });

            Map<String, Object> response = new HashMap<>();
            response.put("count", unreviewedLogs.size());
            response.put("fraudLogs", unreviewedLogs);

            return ResponseEntity.ok(ApiResponseDTO.success(response));

        } catch (Exception e) {
            log.error("Lỗi lấy unreviewed fraud logs: {}", e.getMessage());
            throw e;
        }
    }

    // ======================================================================
    // Helper method
    // ======================================================================

    /**
     * Lấy user đang đăng nhập
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName());
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy user");
        }
        return user;
    }
}
