package com.english12smart.service;

import com.english12smart.dto.AssignmentSubmissionDTO;
import com.english12smart.entity.Assignment;
import com.english12smart.entity.AssignmentSubmission;
import com.english12smart.entity.User;
import com.english12smart.exception.BadRequestException;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.AssignmentRepository;
import com.english12smart.repository.AssignmentSubmissionRepository;
import com.english12smart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ========== ASSIGNMENT SUBMISSION SERVICE ==========
 * Service xử lý logic về submission & grading
 * 
 * Chức năng:
 * 1. Submit assignment (học sinh nộp bài)
 * 2. Grade submission (giáo viên chấm)
 * 3. Tính toán statistics
 * 4. Check deadline, time limit
 * 
 * Flow:
 * 1. Student start assignment → tạo submission với status NOT_STARTED
 * 2. Student submit → update submission + answers
 * 3. Check deadline → set isLate flag nếu quá hạn
 * 4. Auto-grade nếu mode = AUTO
 * 5. Teacher grade nếu mode = MANUAL
 * 6. Update assignment statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentSubmissionService {

    // ========== DEPENDENCIES ==========
    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    // ========== SUBMIT ASSIGNMENT - Học sinh nộp bài ==========
    /**
     * Học sinh nộp assignment
     * 
     * Flow:
     * 1. Tìm assignment
     * 2. Tìm hoặc tạo submission cho student
     * 3. Validate deadline & time limit
     * 4. Lưu submitted answers
     * 5. Set submitted date
     * 6. Check quá hạn
     * 7. Auto-grade nếu cần
     * 8. Cập nhật assignment statistics
     * 
     * @param assignmentId - Assignment ID
     * @param studentId - Student ID nộp bài
     * @param request - Submitted answers
     * @return SubmitResponse
     * @throws ResourceNotFoundException - Assignment không tồn tại
     * @throws BadRequestException - Deadline đã qua, hoặc không có quyền
     */
    @Transactional
    public AssignmentSubmissionDTO.SubmitResponse submitAssignment(
            String assignmentId,
            String studentId,
            AssignmentSubmissionDTO.SubmitRequest request) {

        log.info("========== SUBMIT ASSIGNMENT ==========");
        log.info("AssignmentId: {}, StudentId: {}", assignmentId, studentId);

        // ========== 1. Tìm assignment ==========
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài tập không tồn tại"));

        // ========== 2. Tìm user (student) ==========
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Học sinh không tồn tại"));

        // ========== 3. Tìm hoặc tạo submission ==========
        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseGet(() -> createNewSubmission(assignment, student));

        // ========== 4. Validate deadline ==========
        long now = System.currentTimeMillis();
        if (assignment.getDueDate() != null && now > assignment.getDueDate()) {
            log.warn("Submission quá hạn. Assignment deadline: {}, Current time: {}",
                    assignment.getDueDate(), now);
            // Vẫn cho submit nhưng đánh dấu isLate = true
        }

        // ========== 5. Update submission content ==========
        submission.setSubmittedAnswers(request.getSubmittedAnswers());
        submission.setSubmittedAt(now);
        submission.setTimeUsedSeconds(request.getTimeUsedSeconds());

        // ========== 6. Check deadline & set isLate flag ==========
        if (assignment.getDueDate() != null && now > assignment.getDueDate()) {
            submission.setIsLate(true);
            long lateMillis = now - assignment.getDueDate();
            submission.setLateMinutes((int) (lateMillis / (1000 * 60)));
            log.info("Quá hạn {} phút", submission.getLateMinutes());
        } else {
            submission.setIsLate(false);
            submission.setLateMinutes(null);
        }

        // ========== 7. Set status = SUBMITTED ==========
        submission.setStatus("SUBMITTED");
        submission.setUpdatedAt(now);

        // ========== 8. Auto-grade nếu Assignment.gradingMode = AUTO ==========
        if ("AUTO".equals(assignment.getGradingMode())) {
            log.info("Mode AUTO, thực hiện auto-grade");
            // TODO: Implement auto-grading logic
            // Tính điểm dựa trên answer key từ exercises
            // submission.setAutoScore(...);
            // submission.setScore(...);
            // submission.setStatus("GRADED");
        }

        // ========== 9. Lưu submission ==========
        AssignmentSubmission saved = submissionRepository.save(submission);
        log.info("Submission saved với status: {}", saved.getStatus());

        // ========== 10. Update assignment statistics (Commented out for now to prevent 500 errors) ==========
        // TODO: Fix updateAssignmentStatistics method
        // updateAssignmentStatistics(assignment);
        log.info("Skipping assignment statistics update");

        // ========== 11. Return response ==========
        log.info("========== SUBMIT ASSIGNMENT SUCCESSFUL ==========");
        return AssignmentSubmissionDTO.SubmitResponse.builder()
                .submissionId(saved.getId())
                .assignmentId(assignmentId)
                .studentId(studentId)
                .status(saved.getStatus())
                .submittedAt(saved.getSubmittedAt())
                .score(saved.getScore())
                .feedback(saved.getFeedback())
                .message("Nộp bài thành công")
                .isLate(saved.getIsLate())
                .lateMinutes(saved.getLateMinutes())
                .build();
    }

    // ========== GRADE SUBMISSION - Giáo viên chấm bài ==========
    /**
     * Giáo viên chấm 1 bài submission
     * 
     * Flow:
     * 1. Tìm submission
     * 2. Tìm assignment
     * 3. Validate teacher có quyền chấm không
     * 4. Update score & feedback
     * 5. Set gradedAt & gradedByTeacherId
     * 6. Set status = GRADED
     * 7. Update assignment statistics
     * 
     * @param submissionId - Submission ID cần chấm
     * @param teacherId - Teacher ID chấm
     * @param request - Score & feedback
     * @return GradeResponse
     */
    @Transactional
    public AssignmentSubmissionDTO.GradeResponse gradeSubmission(
            String submissionId,
            String teacherId,
            AssignmentSubmissionDTO.GradeRequest request) {

        log.info("========== GRADE SUBMISSION ==========");
        log.info("SubmissionId: {}, TeacherId: {}", submissionId, teacherId);

        // ========== 1. Tìm submission ==========
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài nộp không tồn tại"));

        // ========== 2. Tìm assignment ==========
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Bài tập không tồn tại"));

        // ========== 3. Validate teacher có quyền ==========
        if (!assignment.getTeacherId().equals(teacherId)) {
            log.warn("Teacher {} không có quyền chấm bài tập của teacher {}",
                    teacherId, assignment.getTeacherId());
            throw new BadRequestException("Bạn không có quyền chấm bài này");
        }

        // ========== 4. Validate score ==========
        if (request.getScore() == null || request.getScore() < 0 || request.getScore() > 10) {
            throw new BadRequestException("Điểm phải nằm trong khoảng 0-10");
        }

        // ========== 5. Update score & feedback ==========
        long now = System.currentTimeMillis();
        submission.setManualScore(request.getScore().intValue());
        submission.setScore(request.getScore()); // Set final score
        submission.setFeedback(request.getFeedback());
        submission.setGradedAt(now);
        submission.setGradedByTeacherId(teacherId);
        submission.setStatus("GRADED");
        submission.setUpdatedAt(now);

        // ========== 6. Lưu submission ==========
        AssignmentSubmission saved = submissionRepository.save(submission);
        log.info("Submission chấm với điểm: {}", saved.getScore());

        // ========== 7. Update assignment statistics (Commented out for now to prevent 500 errors) ==========
        // TODO: Fix updateAssignmentStatistics method
        // updateAssignmentStatistics(assignment);
        log.info("Skipping assignment statistics update");

        // ========== 8. Lấy teacher name ==========
        User teacher = userRepository.findById(teacherId).orElse(null);
        String teacherName = teacher != null ? teacher.getFullName() : "Unknown";

        // ========== 9. Return response ==========
        log.info("========== GRADE SUBMISSION SUCCESSFUL ==========");
        return AssignmentSubmissionDTO.GradeResponse.builder()
                .submissionId(saved.getId())
                .assignmentId(assignment.getId())
                .studentId(saved.getStudentId())
                .studentName(saved.getStudentName())
                .score(saved.getScore())
                .feedback(saved.getFeedback())
                .gradedAt(saved.getGradedAt())
                .message("Chấm bài thành công")
                .build();
    }

    // ========== GET SUBMISSIONS - Lấy danh sách bài nộp ==========
    /**
     * Lấy danh sách tất cả submissions của 1 assignment
     * Dùng để giáo viên xem tất cả bài nộp để chấm
     * 
     * @param assignmentId - Assignment ID
     * @param teacherId - Teacher ID (để validate quyền)
     * @return List submissions
     */
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionDTO.SubmissionListItemDTO> getSubmissionsByAssignment(
            String assignmentId,
            String teacherId) {

        log.info("Lấy submissions cho assignment: {}", assignmentId);

        // ========== Validate inputs ==========
        if (teacherId == null || teacherId.isEmpty()) {
            throw new BadRequestException("Teacher ID không hợp lệ");
        }

        // ========== Validate assignment & teacher ==========
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài tập không tồn tại"));

        if (assignment.getTeacherId() == null || !assignment.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền xem bài nộp này");
        }

        // ========== Lấy submissions ==========
        List<AssignmentSubmission> submissions = submissionRepository
                .findByAssignmentIdOrderBySubmittedAtDesc(assignmentId);

        // ========== Convert to DTO ==========
        return submissions.stream()
                .map(this::toSubmissionListItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết 1 submission
     * 
     * @param submissionId - Submission ID
     * @return SubmissionResponse
     */
    @Transactional(readOnly = true)
    public AssignmentSubmissionDTO.SubmissionResponse getSubmission(String submissionId) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài nộp không tồn tại"));

        return toSubmissionDTO(submission);
    }

    // ========== GET MY SUBMISSION - Student xem kết quả bài nộp của mình ==========
    /**
     * Lấy submission của student hiện tại cho 1 assignment
     * Dùng để student xem kết quả bài của mình
     * 
     * @param assignmentId - Assignment ID
     * @param studentId - Student ID (từ JWT token)
     * @return SubmissionResponse
     */
    @Transactional(readOnly = true)
    public AssignmentSubmissionDTO.SubmissionResponse getMySubmission(
            String assignmentId,
            String studentId) {
        
        log.info("========== GET MY SUBMISSION ==========");
        log.info("Assignment: {}, Student: {}", assignmentId, studentId);

        // ========== Tìm submission của student trong assignment này ==========
        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bạn chưa nộp bài tập này hoặc bài tập không tồn tại"));

        // ========== Convert to DTO và trả về ==========
        log.info("Found submission: {}", submission.getId());
        return toSubmissionDTO(submission);
    }

    // ========== HELPER METHODS ==========
    /**
     * Tạo submission mới cho 1 student khi submitting lần đầu
     */
    private AssignmentSubmission createNewSubmission(Assignment assignment, User student) {
        long now = System.currentTimeMillis();
        return AssignmentSubmission.builder()
                .assignmentId(assignment.getId())
                .studentId(student.getId())
                .studentName(student.getFullName())
                .studentEmail(student.getEmail())
                .classroomId(assignment.getClassroomId()) // Dùng single classroom nếu có
                .teacherId(assignment.getTeacherId())
                .status("NOT_STARTED")
                .startedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Cập nhật statistics của assignment
     * Calculate: submittedCount, pendingCount, gradedCount, averageScore
     */
    private void updateAssignmentStatistics(Assignment assignment) {
        long submittedCount = submissionRepository.countSubmittedByAssignmentId(assignment.getId());
        long gradedCount = submissionRepository.countGradedByAssignmentId(assignment.getId());

        // ========== Tính average score ==========
        List<AssignmentSubmission> gradedSubmissions = submissionRepository
                .findGradedByAssignmentId(assignment.getId());

        double totalScore = gradedSubmissions.stream()
                .mapToDouble(s -> s.getScore() != null ? s.getScore() : 0.0)
                .sum();

        double averageScore = !gradedSubmissions.isEmpty()
                ? totalScore / gradedSubmissions.size()
                : 0.0;

        // ========== Update assignment ==========
        assignment.setSubmittedCount((int) submittedCount);
        assignment.setGradedCount((int) gradedCount);
        assignment.setPendingCount((int) (submittedCount - gradedCount));
        assignment.setAverageScore(averageScore);
        assignment.setTotalScore(totalScore);
        assignment.setUpdatedAt(System.currentTimeMillis());

        assignmentRepository.save(assignment);
        log.debug("Assignment statistics updated: submitted={}, graded={}, avg={}",
                submittedCount, gradedCount, averageScore);
    }

    /**
     * Convert AssignmentSubmission to SubmissionListItemDTO
     */
    private AssignmentSubmissionDTO.SubmissionListItemDTO toSubmissionListItemDTO(
            AssignmentSubmission submission) {
        return AssignmentSubmissionDTO.SubmissionListItemDTO.builder()
                .submissionId(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .studentName(submission.getStudentName())
                .studentEmail(submission.getStudentEmail())
                .status(submission.getStatus())
                .statusDisplay(submission.getStatusDisplay())
                .statusBadgeClass(submission.getStatusBadgeClass())
                .submittedAt(submission.getSubmittedAt())
                .submittedAtDisplay(formatDate(submission.getSubmittedAt()))
                .isLate(submission.getIsLate())
                .lateMinutes(submission.getLateMinutes())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .gradedAt(submission.getGradedAt())
                .build();
    }

    /**
     * Convert AssignmentSubmission to SubmissionDTO
     */
    private AssignmentSubmissionDTO.SubmissionResponse toSubmissionDTO(
            AssignmentSubmission submission) {
        return AssignmentSubmissionDTO.SubmissionResponse.builder()
                .submissionId(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .studentName(submission.getStudentName())
                .studentEmail(submission.getStudentEmail())
                .classroomId(submission.getClassroomId())
                .submittedAnswers(submission.getSubmittedAnswers())
                .status(submission.getStatus())
                .statusDisplay(submission.getStatusDisplay())
                .statusBadgeClass(submission.getStatusBadgeClass())
                .startedAt(submission.getStartedAt())
                .submittedAt(submission.getSubmittedAt())
                .submittedAtDisplay(formatDate(submission.getSubmittedAt()))
                .timeLimitMinutes(submission.getTimeLimitMinutes())
                .timeUsedSeconds(submission.getTimeUsedSeconds())
                .isLate(submission.getIsLate())
                .lateMinutes(submission.getLateMinutes())
                .score(submission.getScore())
                .autoScore(submission.getAutoScore())
                .manualScore(submission.getManualScore())
                .feedback(submission.getFeedback())
                .gradedAt(submission.getGradedAt())
                .gradedByTeacherName(getUserNameById(submission.getGradedByTeacherId()))
                .gradedAtDisplay(formatDate(submission.getGradedAt()))
                .build();
    }

    /**
     * Helper: Format date
     */
    private String formatDate(Long millis) {
        if (millis == null || millis == 0) return null;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(new Date(millis));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper: Lấy user name
     */
    private String getUserNameById(String userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(User::getFullName)
                .orElse(null);
    }
}
