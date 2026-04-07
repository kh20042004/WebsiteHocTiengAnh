package com.english12smart.repository;

import com.english12smart.entity.AssignmentSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * ========== ASSIGNMENT SUBMISSION REPOSITORY ==========
 * Repository để tương tác với AssignmentSubmission collection trong MongoDB
 * 
 * Query methods để tìm submissions của:
 * - 1 assignment (danh sách tất cả students)
 * - 1 student cho 1 assignment
 * - Tất cả submissions chưa chấm
 * - Tất cả submissions của 1 student
 */
@Repository
public interface AssignmentSubmissionRepository extends MongoRepository<AssignmentSubmission, String> {

    // ========== FIND BY ASSIGNMENT ==========
    /**
     * Tìm tất cả submissions cho 1 assignment
     * Dùng để giáo viên xem danh sách bài nộp
     * 
     * @param assignmentId - Assignment ID
     * @return List các submissions (có thể rỗng)
     */
    List<AssignmentSubmission> findByAssignmentId(String assignmentId);

    // ========== FIND BY STUDENT & ASSIGNMENT ==========
    /**
     * Tìm submission của 1 student cho 1 assignment
     * 
     * @param assignmentId - Assignment ID
     * @param studentId - Student ID
     * @return Submission nếu tìm thấy, Optional.empty() nếu không
     */
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(String assignmentId, String studentId);

    // ========== FIND BY STUDENT ==========
    /**
     * Tìm tất cả submissions của 1 student
     * Dùng để student xem lịch sử submission
     * 
     * @param studentId - Student ID
     * @return List submissions
     */
    List<AssignmentSubmission> findByStudentId(String studentId);

    /**
     * Tìm submissions của 1 student cho 1 classroom
     * 
     * @param studentId - Student ID
     * @param classroomId - Classroom ID
     * @return List submissions
     */
    List<AssignmentSubmission> findByStudentIdAndClassroomId(String studentId, String classroomId);

    // ========== FIND BY STATUS ==========
    /**
     * Tìm tất cả submissions chưa chấm của 1 assignment
     * Dùng để giáo viên xem danh sách bài cần chấm
     * 
     * @param assignmentId - Assignment ID
     * @return List submissions với status = "SUBMITTED" hoặc "IN_PROGRESS"
     */
    @Query("{ 'assignmentId': ?0, 'status': { $in: ['SUBMITTED', 'IN_PROGRESS'] } }")
    List<AssignmentSubmission> findUngradedByAssignmentId(String assignmentId);

    /**
     * Tìm submissions đã chấm của 1 assignment
     * 
     * @param assignmentId - Assignment ID
     * @return List submissions có score != null
     */
    @Query("{ 'assignmentId': ?0, 'score': { $ne: null } }")
    List<AssignmentSubmission> findGradedByAssignmentId(String assignmentId);

    // ========== COUNT ==========
    /**
     * Đếm số submissions cho 1 assignment
     * 
     * @param assignmentId - Assignment ID
     * @return Số lượng submissions
     */
    long countByAssignmentId(String assignmentId);

    /**
     * Đếm số submissions đã submit cho 1 assignment
     * 
     * @param assignmentId - Assignment ID
     * @return Số lượng submissions với status != "NOT_STARTED"
     */
    @Query("{ 'assignmentId': ?0, 'status': { $ne: 'NOT_STARTED' } }")
    long countSubmittedByAssignmentId(String assignmentId);

    /**
     * Đếm số submissions đã chấm cho 1 assignment
     * 
     * @param assignmentId - Assignment ID
     * @return Số lượng submissions có score != null
     */
    @Query("{ 'assignmentId': ?0, 'score': { $ne: null } }")
    long countGradedByAssignmentId(String assignmentId);

    // ========== FIND BY CLASSROOM ==========
    /**
     * Tìm tất cả submissions của 1 classroom
     * 
     * @param classroomId - Classroom ID
     * @return List submissions
     */
    List<AssignmentSubmission> findByClassroomId(String classroomId);

    /**
     * Tìm tất cả submissions chưa chấm của 1 classroom
     * 
     * @param classroomId - Classroom ID
     * @return List submissions chờ chấm
     */
    @Query("{ 'classroomId': ?0, 'score': null }")
    List<AssignmentSubmission> findUngradedByClassroomId(String classroomId);

    // ========== FIND BY TEACHER ==========
    /**
     * Tìm tất cả submissions được giao bởi 1 teacher
     * 
     * @param teacherId - Teacher ID
     * @return List submissions
     */
    List<AssignmentSubmission> findByTeacherId(String teacherId);

    /**
     * Tìm tất cả submissions cần chấm của 1 teacher
     * 
     * @param teacherId - Teacher ID
     * @return List submissions với score = null
     */
    @Query("{ 'teacherId': ?0, 'score': null }")
    List<AssignmentSubmission> findUngradedByTeacherId(String teacherId);

    // ========== LATE SUBMISSIONS ==========
    /**
     * Tìm submissions nộp quá hạn cho 1 assignment
     * 
     * @param assignmentId - Assignment ID
     * @return List submissions với isLate = true
     */
    @Query("{ 'assignmentId': ?0, 'isLate': true }")
    List<AssignmentSubmission> findLateSubmissionsByAssignmentId(String assignmentId);

    // ========== DELETE ==========
    /**
     * Xóa tất cả submissions của 1 assignment
     * (Khi giáo viên xóa assignment)
     * 
     * @param assignmentId - Assignment ID
     * @return Số submissions đã xóa
     */
    long deleteByAssignmentId(String assignmentId);
}
