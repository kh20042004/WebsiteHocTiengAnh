package com.english12smart.repository;

import com.english12smart.entity.Classroom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomRepository extends MongoRepository<Classroom, String> {

    /** Lấy tất cả lớp của một giáo viên, sắp xếp theo tên */
    List<Classroom> findByTeacherIdOrderByNameAsc(String teacherId);

    /** Lấy lớp theo giáo viên và trạng thái */
    List<Classroom> findByTeacherIdAndStatusOrderByNameAsc(String teacherId, String status);

    /** Kiểm tra tên lớp đã tồn tại trong giáo viên chưa */
    boolean existsByTeacherIdAndName(String teacherId, String name);

    /** Đếm số lớp của giáo viên */
    long countByTeacherId(String teacherId);

    /** Kiểm tra mã lớp đã tồn tại chưa */
    boolean existsByClassCode(String classCode);

    /** Tìm lớp theo mã lớp (cho học sinh tham gia) */
    java.util.Optional<Classroom> findByClassCode(String classCode);

    /** Tìm tất cả lớp mà học sinh đã tham gia */
    List<Classroom> findByStudentIdsContaining(String studentId);
}
