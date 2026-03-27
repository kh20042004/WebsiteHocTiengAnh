package com.english12smart.repository;

import com.english12smart.entity.Exam;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy xuất dữ liệu đề thi từ MongoDB collection "exams"
 */
@Repository
public interface ExamRepository extends MongoRepository<Exam, String> {

    /**
     * Lấy danh sách đề thi của một giáo viên, sắp xếp mới nhất trước
     * Dùng để hiển thị trên trang quản lý đề thi của giáo viên
     */
    List<Exam> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    /**
     * Lấy danh sách đề thi của một lớp học, sắp xếp mới nhất trước
     * Dùng để giáo viên xem đề thi theo từng lớp
     */
    List<Exam> findByClassroomIdOrderByCreatedAtDesc(String classroomId);

    /**
     * Tìm đề thi theo mã PIN 5 số
     * Dùng khi học sinh nhập PIN để vào thi
     *
     * @param pinCode mã PIN 5 chữ số
     * @return Optional chứa Exam nếu tìm thấy, empty nếu không
     */
    Optional<Exam> findByPinCode(String pinCode);

    /**
     * Kiểm tra mã PIN đã tồn tại chưa
     * Dùng khi sinh mã PIN ngẫu nhiên để tránh trùng lặp
     */
    boolean existsByPinCode(String pinCode);

    /**
     * Đếm số đề thi của một giáo viên
     * Dùng cho thống kê trên dashboard
     */
    long countByTeacherId(String teacherId);

    /**
     * Đếm số đề thi đang mở của một giáo viên
     * Dùng cho thống kê số đề đang hoạt động
     */
    long countByTeacherIdAndStatus(String teacherId, String status);

    /**
     * Đếm số đề thi theo trạng thái (ACTIVE, CLOSED, DRAFT)
     */
    long countByStatus(String status);
}
