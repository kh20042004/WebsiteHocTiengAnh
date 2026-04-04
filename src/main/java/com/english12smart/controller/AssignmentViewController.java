package com.english12smart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ========== ASSIGNMENT VIEW CONTROLLER ==========
 * Controller để phục vụ các HTML page cho assignment (Không phải REST API)
 * 
 * Endpoints:
 * - GET /assignment/{assignmentId}/do - Student làm bài tập
 * - GET /assignment/{assignmentId}/grade - Teacher chấm bài tập
 * - GET /assignment/{assignmentId}/result - Student xem kết quả bài tập
 * 
 * Notes:
 * - Tất cả endpoints đều require authentication (JWT token)
 * - Authorization checks sẽ được làm ở API endpoints (POST /api/assignment/xxx)
 * - HTML pages chỉ để phục vụ form UI + JavaScript
 */
@Controller
@RequestMapping("/assignment")
@RequiredArgsConstructor
@Slf4j
public class AssignmentViewController {

    // ========== STUDENT DO ASSIGNMENT - GET /assignment/{assignmentId}/do ==========
    /**
     * Phục vụ trang HTML để student làm bài tập
     * 
     * Endpoint: GET /assignment/{assignmentId}/do
     * Auth: Require JWT token (STUDENT role)
     * 
     * View: templates/student/assignment-do.html
     * 
     * Flow:
     * 1. Student click "Làm bài" trên trang danh sách bài tập
     * 2. GET /assignment/{assignmentId}/do
     * 3. Server return HTML page +JavaScrip
     * 4. JavaScript lấy assignmentId từ URL
     * 5. JavaScript fetch GET /api/assignment/{assignmentId} để lấy thông tin
     * 6. JavaScript render các exercise từ API
     * 7. Student nhập câu trả lời
     * 8. JavaScript auto-save draft mỗi 30 giây vào localStorage
     * 9. Student click "Nộp bài"
     * 10. JavaScript hiển thị confirm modal
     * 11. JavaScript POST /api/assignment/{assignmentId}/submit
     * 12. API validate, lưu submission, update statistics
     * 13. JavaScript redirect đến /assignment/{assignmentId}/result
     * 
     * Features:
     * - Auto-save draft (localStorage)
     * - Countdown timer (nếu có time limit)
     * - Multiple exercise types: MULTIPLE_CHOICE, TRUE_FALSE, FILL_IN_BLANK, SHORT_ANSWER
     * - Deadline validation
     * - Late submission detection
     * 
     * @param assignmentId - Assignment ID
     * @return HTML page - student/assignment-do.html
     */
    @GetMapping("/{assignmentId}/do")
    public String showAssignmentPage(@PathVariable String assignmentId) {
        log.info("========== ASSIGNMENT DO PAGE LOAD ==========");
        log.info("Assignment ID: {}", assignmentId);
        log.info("Student accessing assignment page");

        // ========== Return view name ==========
        // Spring sẽ tìm file: src/main/resources/templates/student/assignment-do.html
        return "student/assignment-do";
    }

    // ========== TEACHER GRADE ASSIGNMENT - GET /assignment/{assignmentId}/grade ==========
    /**
     * Phục vụ trang HTML để teacher chấm bài tập
     * 
     * Endpoint: GET /assignment/{assignmentId}/grade
     * Auth: Require JWT token (TEACHER role)
     * 
     * View: templates/teacher/assignment-grade.html
     * 
     * Flow:
     * 1. Teacher click "Chấm bài" trên trang danh sách assignment
     * 2. GET /assignment/{assignmentId}/grade
     * 3. Server return HTML page
     * 4. JavaScript lấy assignmentId từ URL
     * 5. JavaScript fetch GET /api/assignment/{assignmentId} để lấy thông tin
     * 6. JavaScript fetch GET /api/assignment/{assignmentId}/submissions để lấy danh sách nộp
     * 7. JavaScript render danh sách submissions (có filter, sort)
     * 8. Teacher click 1 submission trong danh sách
     * 9. JavaScript fetch GET /api/assignment/submission/{submissionId} để lấy chi tiết
     * 10. JavaScript render answers, form chấm điểm
     * 11. Teacher nhập điểm (0-10) + feedback
     * 12. Teacher click "Chấm bài"
     * 13. JavaScript hiển thị confirm modal
     * 14. JavaScript PUT /api/assignment/{assignmentId}/submission/{submissionId}/grade
     * 15. API validate permissions, update submission, update statistics
     * 16. JavaScript reload danh sách submissions
     * 17. Highlight next pending submission
     * 
     * Features:
     * - Two-column layout (submissions list + grading form)
     * - Filter: all, pending, graded, late
     * - Sort: newest, oldest, pending first
     * - Auto-move to next pending submission
     * - Statistics: total, pending, graded, average
     * - Student info display
     * - Submitted answers preview
     * - Feedback textarea with char count
     * 
     * @param assignmentId - Assignment ID
     * @return HTML page - teacher/assignment-grade.html
     */
    @GetMapping("/{assignmentId}/grade")
    public String showGradingPage(@PathVariable String assignmentId) {
        log.info("========== ASSIGNMENT GRADE PAGE LOAD ==========");
        log.info("Assignment ID: {}", assignmentId);
        log.info("Teacher accessing grading page");

        // ========== Return view name ==========
        // Spring sẽ tìm file: src/main/resources/templates/teacher/assignment-grade.html
        return "teacher/assignment-grade";
    }

    // ========== STUDENT VIEW RESULT - GET /assignment/{assignmentId}/result ==========
    /**
     * Phục vụ trang HTML để student xem kết quả bài tập
     * 
     * Endpoint: GET /assignment/{assignmentId}/result
     * Auth: Require JWT token (STUDENT role)
     * 
     * View: templates/student/assignment-result.html
     * 
     * Flow:
     * 1. Student submit assignment thành công
     * 2. JavaScript auto-redirect đến /assignment/{assignmentId}/result
     * 3. hoặc Student click "Xem kết quả" từ trang danh sách
     * 4. Server return HTML page
     * 5. JavaScript lấy assignmentId từ URL
     * 6. JavaScript fetch GET /api/assignment/{assignmentId} để lấy thông tin assignment
     * 7. JavaScript fetch GET /api/assignment/{assignmentId}/my-submission để lấy chi tiết nộp
     * 8. JavaScript fetch GET /api/exercise?ids=... để lấy thông tin exercise
     * 9. JavaScript render thông tin assignment
     * 10. JavaScript render thông tin nộp (time, status, late indicator)
     * 11. JavaScript render câu trả lời đã nộp
     * 12. Nếu đã chấm: hiển thị điểm + feedback từ teacher
     * 13. Nếu chưa chấm: hiển thị "Chờ teacher chấm"
     * 14. Student có thể download kết quả (PDF hoặc JSON)
     * 
     * Features:
     * - Success banner (confirmation submission)
     * - Assignment info display
     * - Stats: answered count, time used, status
     * - Late submission indicator
     * - Submitted answers display
     * - Teacher feedback (if graded)
     * - Download options (PDF, JSON)
     * - Link to other assignments (similar)
     * 
     * @param assignmentId - Assignment ID
     * @return HTML page - student/assignment-result.html
     */
    @GetMapping("/{assignmentId}/result")
    public String showResultPage(@PathVariable String assignmentId) {
        log.info("========== ASSIGNMENT RESULT PAGE LOAD ==========");
        log.info("Assignment ID: {}", assignmentId);
        log.info("Student accessing result page");

        // ========== Return view name ==========
        // Spring sẽ tìm file: src/main/resources/templates/student/assignment-result.html
        return "student/assignment-result";
    }

    // ========== NOTES ==========
    /*
     * STUDENT SUBMISSION FLOW:
     * 
     * 1. Student visit /assignment/{assignmentId}/do
     * 2. HTML + JavaScript loaded
     * 3. JavaScript fetch /api/assignment/{assignmentId}
     * 4. JavaScript fetch /api/exercise?ids=...
     * 5. JavaScript render exercises with various input types
     * 6. JavaScript set up timer (if time limit exists)
     * 7. JavaScript auto-save to localStorage every 30 seconds
     * 8. Student answers questions
     * 9. Student click "Nộp bài"
     * 10. Confirm modal shown
     * 11. JavaScript POST /api/assignment/{assignmentId}/submit
     * 12. API:
     *     - Validate deadline
     *     - Create/update AssignmentSubmission
     *     - Detect late submission
     *     - Auto-grade if gradingMode = AUTO
     *     - Update Assignment statistics
     * 13. Redirect to /assignment/{assignmentId}/result
     * 14. Result page shows:
     *     - Success message
     *     - Submitted answers
     *     - Time used, late indicator
     *     - (If graded) Score + feedback
     * 
     * TEACHER GRADING FLOW:
     * 
     * 1. Teacher visit /assignment/{assignmentId}/grade
     * 2. HTML + JavaScript loaded
     * 3. JavaScript fetch /api/assignment/{assignmentId}
     * 4. JavaScript fetch /api/assignment/{assignmentId}/submissions
     * 5. JavaScript render submissions list
     * 6. Stats shown: total, pending, graded, average
     * 7. Teacher click on a submission
     * 8. JavaScript fetch /api/assignment/submission/{submissionId}
     * 9. Grading form shown with:
     *     - Student info
     *     - Submission status, time used, late indicator
     *     - Answers preview
     *     - Score input (0-10)
     *     - Feedback textarea
     * 10. Teacher enter score + feedback
     * 11. Teacher click "Chấm bài"
     * 12. Confirm modal shown
     * 13. JavaScript PUT /api/assignment/{assignmentId}/submission/{submissionId}/grade
     * 14. API:
     *     - Validate teacher is assignment creator (or admin)
     *     - Update submission (score, feedback, gradedAt, gradedByTeacherId)
     *     - Update submission status to GRADED
     *     - Recalculate Assignment statistics
     * 15. Refresh submissions list
     * 16. Auto-move to next pending submission
     * 
     * SECURITY NOTES:
     * - Student can only submit their own assignment
     * - Student can't edit after submitting
     * - Teacher can only grade their own assignments (or admin override)
     * - Deadline can be enforced by checking timestamp
     * - Late submission tracked but still accepted (teacher policy)
     * - Time limit enforced by timer on client (backend also validates)
     */
}
