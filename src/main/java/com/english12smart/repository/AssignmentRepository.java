package com.english12smart.repository;

import com.english12smart.entity.Assignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ========== ASSIGNMENT REPOSITORY ==========
 * Repository để tương tác với Assignment collection trong MongoDB
 * 
 * Hỗ trợ:
 * - Tìm assignment theo teacher
 * - Tìm assignment theo classroom (single classroomId hoặc classroomIds array)
 * - Filter theo status
 * - Tìm assignment có chứa exercise nào đó
 */
@Repository
public interface AssignmentRepository extends MongoRepository<Assignment, String> {

    // ========== FIND BY TEACHER ==========
    /**
     * Lấy tất cả assignment của 1 teacher
     * Sắp xếp theo ngày tạo (mới nhất trước)
     * 
     * @param teacherId - Teacher ID
     * @return List assignments
     */
    List<Assignment> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    /**
     * Lấy assignment của 1 teacher theo trạng thái
     * 
     * @param teacherId - Teacher ID
     * @param status - ACTIVE, CLOSED, DRAFT
     * @return List assignments có status match
     */
    List<Assignment> findByTeacherIdAndStatusOrderByCreatedAtDesc(String teacherId, String status);

    // ========== FIND BY CLASSROOM (SINGLE) ==========
    /**
     * Lấy assignment giao cho 1 classroom (backward compatibility)
     * Dùng khi classroom chỉ sử dụng classroomId field (deprecated)
     * 
     * @param classroomId - Classroom ID
     * @return List assignments
     */
    List<Assignment> findByClassroomIdOrderByCreatedAtDesc(String classroomId);

    /**
     * Lấy assignment ACTIVE giao cho 1 classroom
     * 
     * @param classroomId - Classroom ID
     * @return List assignments với status = ACTIVE
     */
    List<Assignment> findByClassroomIdAndStatusOrderByCreatedAtDesc(String classroomId, String status);

    // ========== FIND BY CLASSROOM (MULTI-CLASS - NEW) ==========
    /**
     * Lấy assignment giao cho 1 classroom từ classroomIds array
     * Dùng khi classroom sử dụng classroomIds array (IMPROVED)
     * Query: classroomIds contains classroomId
     * 
     * @param classroomId - Classroom ID
     * @return List assignments có classroomIds contain classroomId
     */
    @Query("{ 'classroomIds': ?0 }")
    List<Assignment> findByClassroomIdsContaining(String classroomId);

    /**
     * Lấy assignment giao cho bất kỳ classroom nào trong list
     * Dùng để student xem assignments của các lớp họ tham gia
     * 
     * @param classroomIds - List classroom IDs
     * @return List assignments giao cho bất kỳ classroom nào
     */
    @Query("{ 'classroomIds': { $in: ?0 } }")
    List<Assignment> findByClassroomIdsIn(java.util.List<String> classroomIds);

    /**
     * Lấy active assignments giao cho bất kỳ classroom nào
     * 
     * @param classroomIds - List classroom IDs
     * @param status - Status (ACTIVE, CLOSED, DRAFT)
     * @return List assignments
     */
    @Query("{ 'classroomIds': { $in: ?0 }, 'status': ?1 }")
    List<Assignment> findByClassroomIdsInAndStatus(java.util.List<String> classroomIds, String status);

    /**
     * Lấy assignment giao cho nhiều classrooms (old method -backward compatibility)
     * 
     * @param classroomIds - List classroom IDs
     * @return List assignments
     */
    List<Assignment> findByClassroomIdInOrderByCreatedAtDesc(java.util.Collection<String> classroomIds);

    // ========== COUNT ==========
    /**
     * Đếm assignment của 1 teacher
     * 
     * @param teacherId - Teacher ID
     * @return Số lượng assignments
     */
    long countByTeacherId(String teacherId);

    /**
     * Đếm assignment của 1 teacher theo trạng thái
     * 
     * @param teacherId - Teacher ID
     * @param status - ACTIVE, CLOSED, DRAFT
     * @return Số lượng assignments
     */
    long countByTeacherIdAndStatus(String teacherId, String status);

    /**
     * Đếm assignment giao cho 1 classroom
     * 
     * @param classroomId - Classroom ID
     * @return Số lượng assignments
     */
    long countByClassroomId(String classroomId);

    // ========== FIND BY EXERCISE ==========
    /**
     * Tìm assignment chứa 1 exercise cụ thể
     * Dùng để check exercise có được dùng ở đâu
     * 
     * @param exerciseId - Exercise ID
     * @return List assignments containing exerciseId
     */
    @Query("{ 'exerciseIds': ?0 }")
    List<Assignment> findByExerciseId(String exerciseId);

    /**
     * Tìm assignment chứa bất kỳ exercise nào trong list
     * 
     * @param exerciseIds - List exercise IDs
     * @return List assignments
     */
    @Query("{ 'exerciseIds': { $in: ?0 } }")
    List<Assignment> findByExerciseIdIn(java.util.List<String> exerciseIds);

    // ========== DELETE ==========
    /**
     * Xóa tất cả assignment của 1 teacher
     * (Admin function)
     * 
     * @param teacherId - Teacher ID
     * @return Số assignments đã xóa
     */
    long deleteByTeacherId(String teacherId);
}
