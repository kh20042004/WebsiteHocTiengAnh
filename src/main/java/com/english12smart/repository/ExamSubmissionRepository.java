package com.english12smart.repository;

import com.english12smart.entity.ExamSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy xuất dữ liệu bài làm thi từ MongoDB collection "exam_submissions"
 */
@Repository
public interface ExamSubmissionRepository extends MongoRepository<ExamSubmission, String> {

    /**
     * Tìm bài làm theo đề thi và học sinh
     * Dùng để kiểm tra học sinh đã nộp bài chưa và lấy kết quả
     */
    Optional<ExamSubmission> findByExamIdAndStudentId(String examId, String studentId);

    /**
     * Lấy tất cả bài làm của một đề thi, sắp xếp theo điểm cao nhất
     * Dùng để giáo viên xem bảng xếp hạng kết quả đề thi
     */
    List<ExamSubmission> findByExamIdOrderByScoreDesc(String examId);

    /**
     * Lấy lịch sử thi của một học sinh, sắp xếp mới nhất trước
     * Dùng để học sinh xem lại các bài thi đã làm
     */
    List<ExamSubmission> findByStudentIdOrderBySubmittedAtDesc(String studentId);

    /**
     * Đếm số bài nộp của một đề thi
     * Dùng để cập nhật submittedCount trên Exam entity
     */
    long countByExamId(String examId);

    /**
     * Kiểm tra học sinh đã có bài làm cho đề thi này chưa
     * Dùng để ngăn học sinh vào làm lại bài đã nộp
     */
    boolean existsByExamIdAndStudentId(String examId, String studentId);
}
