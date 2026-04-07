package com.english12smart.controller;

import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.dto.ExamDTO;
import com.english12smart.dto.ExamSubmissionDTO;
import com.english12smart.entity.User;
import com.english12smart.entity.Exam;
import com.english12smart.entity.ExamSubmission;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.exception.BadRequestException;
import com.english12smart.repository.UserRepository;
import com.english12smart.repository.ExamRepository;
import com.english12smart.repository.ExamSubmissionRepository;
import com.english12smart.service.ExamService;
import com.english12smart.util.ExamShufflingUtil;
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
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;

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
     * PUT /api/exams/{examId}
     * Cập nhật thông tin đề thi (chỉnh sửa)
     * Chỉ được cập nhật khi chưa có bài làm nào
     */
    @PutMapping("/{examId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExamDTO.Response>> updateExam(
            @PathVariable String examId,
            @Valid @RequestBody ExamDTO.CreateRequest request) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        log.info("Giáo viên {} cập nhật đề thi: {}", currentUser.getId(), examId);
        ExamDTO.Response response = examService.updateExam(examId, currentUser.getId(), request, isAdmin);
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
     * GET /api/exams/submissions/{submissionId}/questions
     * Lấy danh sách câu hỏi đã xáo trộn cho một submission
     * Dùng để hiển thị trong trang làm bài khi cần refresh
     */
    @GetMapping("/submissions/{submissionId}/questions")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getShuffledQuestions(
            @PathVariable String submissionId) {
        String studentId = getCurrentUserId();
        log.info("Lấy câu hỏi xáo trộn cho submission: {}", submissionId);
        
        // Lấy submission
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài làm"));
        
        // Kiểm tra quyền: chỉ học sinh làm bài mới có thể xem
        if (!submission.getStudentId().equals(studentId)) {
            throw new BadRequestException("Bạn không có quyền xem bài làm này");
        }
        
        // Lấy exam
        Exam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi"));
        
        // Xáo trộn lại câu hỏi theo shuffle seed
        List<Exam.ExamQuestion> questions = exam.getQuestions();
        List<Exam.ExamQuestion> shuffledQuestions = ExamShufflingUtil.shuffleWithSeed(questions, submission.getShuffleSeed());
        
        // Xáo trộn options
        for (Exam.ExamQuestion question : shuffledQuestions) {
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                question.setOptions(ExamShufflingUtil.shuffleList(question.getOptions()));
            }
        }
        
        Map<String, Object> result = Map.of(
                "questions", shuffledQuestions,
                "totalScore", exam.getTotalScore(),
                "timeLimitMinutes", exam.getTimeLimitMinutes() != null ? exam.getTimeLimitMinutes() : 0
        );
        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }

    /**
     * POST /api/exams/submissions/{submissionId}/log-fraud
     * Log hoạt động nghi ngờ gian lận (dùng từ frontend)
     */
    @PostMapping("/submissions/{submissionId}/log-fraud")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> logFraudEvent(
            @PathVariable String submissionId,
            @RequestBody Map<String, String> request) {
        try {
            String fraudType = request.get("fraudType"); // TAB_CHANGE, COPY_ATTEMPT, etc.
            String details = request.get("details");
            
            ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài làm"));
            
            // Tăng counter phù hợp
            switch (fraudType) {
                case "TAB_CHANGE":
                    submission.setTabChangeCount((submission.getTabChangeCount() != null ? submission.getTabChangeCount() : 0) + 1);
                    break;
                case "COPY_ATTEMPT":
                    submission.setCopyAttempts((submission.getCopyAttempts() != null ? submission.getCopyAttempts() : 0) + 1);
                    break;
                case "PASTE_ATTEMPT":
                    submission.setPasteAttempts((submission.getPasteAttempts() != null ? submission.getPasteAttempts() : 0) + 1);
                    break;
                case "RIGHT_CLICK":
                    submission.setRightClickAttempts((submission.getRightClickAttempts() != null ? submission.getRightClickAttempts() : 0) + 1);
                    break;
                case "DEV_TOOLS":
                    submission.setDevToolsAttempts((submission.getDevToolsAttempts() != null ? submission.getDevToolsAttempts() : 0) + 1);
                    break;
                case "FULLSCREEN_EXIT":
                    submission.setFullscreenExitCount((submission.getFullscreenExitCount() != null ? submission.getFullscreenExitCount() : 0) + 1);
                    break;
            }
            
            submission.setFraudLogCount((submission.getFraudLogCount() != null ? submission.getFraudLogCount() : 0) + 1);
            examSubmissionRepository.save(submission);
            
            log.warn("Ghi nhật ký gian lận - Submission: {}, Type: {}, Details: {}", submissionId, fraudType, details);
            
            return ResponseEntity.ok(ApiResponseDTO.success("Đã ghi nhận hoạt động"));
        } catch (Exception e) {
            log.error("Lỗi ghi nhật ký gian lận: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.success("Không thể ghi nhật ký"));
        }
    }

    /**
     * POST /api/exams/submissions/{submissionId}/log-fraud-batch
     * Log multiple fraud events in batch (dùng từ frontend batch logging)
     * Hiệu quả hơn so với gửi từng event một
     */
    @PostMapping("/submissions/{submissionId}/log-fraud-batch")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> logFraudEventBatch(
            @PathVariable String submissionId,
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> fraudEvents = (List<Map<String, String>>) request.get("fraudEvents");
            
            if (fraudEvents == null || fraudEvents.isEmpty()) {
                return ResponseEntity.ok(ApiResponseDTO.success("Không có sự kiện để ghi"));
            }

            ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài làm"));

            int totalEvents = 0;

            // Xử lý từng event trong batch
            for (Map<String, String> event : fraudEvents) {
                String fraudType = event.get("fraudType");
                String details = event.get("details");

                if (fraudType == null) {
                    continue;
                }

                // Tăng counter phù hợp
                switch (fraudType) {
                    case "TAB_CHANGE":
                        submission.setTabChangeCount((submission.getTabChangeCount() != null ? submission.getTabChangeCount() : 0) + 1);
                        break;
                    case "COPY_ATTEMPT":
                        submission.setCopyAttempts((submission.getCopyAttempts() != null ? submission.getCopyAttempts() : 0) + 1);
                        break;
                    case "PASTE_ATTEMPT":
                        submission.setPasteAttempts((submission.getPasteAttempts() != null ? submission.getPasteAttempts() : 0) + 1);
                        break;
                    case "CUT_ATTEMPT":
                        submission.setPasteAttempts((submission.getPasteAttempts() != null ? submission.getPasteAttempts() : 0) + 1);
                        break;
                    case "RIGHT_CLICK":
                        submission.setRightClickAttempts((submission.getRightClickAttempts() != null ? submission.getRightClickAttempts() : 0) + 1);
                        break;
                    case "DEV_TOOLS":
                        submission.setDevToolsAttempts((submission.getDevToolsAttempts() != null ? submission.getDevToolsAttempts() : 0) + 1);
                        break;
                    case "FULLSCREEN_EXIT":
                        submission.setFullscreenExitCount((submission.getFullscreenExitCount() != null ? submission.getFullscreenExitCount() : 0) + 1);
                        break;
                    case "DROP_ATTEMPT":
                    case "VIEW_SOURCE":
                        // Các loại khác
                        break;
                }

                totalEvents++;
                log.warn("Ghi nhật ký gian lận - Submission: {}, Type: {}, Details: {}", submissionId, fraudType, details);
            }

            submission.setFraudLogCount((submission.getFraudLogCount() != null ? submission.getFraudLogCount() : 0) + totalEvents);
            examSubmissionRepository.save(submission);

            log.warn("Xử lý batch {} sự kiện gian lận cho submission: {}", totalEvents, submissionId);

            return ResponseEntity.ok(ApiResponseDTO.success(String.format("Đã ghi nhận %d hoạt động", totalEvents)));
        } catch (Exception e) {
            log.error("Lỗi ghi nhật ký batch: {}", e.getMessage());
            return ResponseEntity.status(500).body(ApiResponseDTO.error(500, "Lỗi ghi nhật ký batch"));
        }
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

    /**
     * DELETE /api/exams/submissions/{submissionId}/reset
     * Xóa bài làm để học sinh làm lại
     */
    @DeleteMapping("/submissions/{submissionId}/reset")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> resetSubmission(@PathVariable String submissionId) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        examService.resetStudentSubmission(submissionId, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponseDTO.success("Đã xóa bài làm, học sinh có thể thi lại"));
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
