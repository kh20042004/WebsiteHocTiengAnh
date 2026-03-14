package com.english12smart.repository;

import com.english12smart.entity.Assignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends MongoRepository<Assignment, String> {

    /** Lấy bài tập theo giáo viên, sắp xếp mới nhất */
    List<Assignment> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    /** Lấy bài tập theo giáo viên và trạng thái */
    List<Assignment> findByTeacherIdAndStatusOrderByCreatedAtDesc(String teacherId, String status);

    /** Lấy bài tập theo lớp */
    List<Assignment> findByClassroomIdOrderByCreatedAtDesc(String classroomId);

    /** Đếm bài tập theo giáo viên */
    long countByTeacherId(String teacherId);

    /** Đếm bài tập theo giáo viên và trạng thái */
    long countByTeacherIdAndStatus(String teacherId, String status);

    /** Lấy bài tập theo nhiều classroomIds (cho student view) */
    List<Assignment> findByClassroomIdInOrderByCreatedAtDesc(java.util.Collection<String> classroomIds);
}
