package com.english12smart.repository;

import com.english12smart.entity.ExamAntiFraudLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamAntiFraudLogRepository extends MongoRepository<ExamAntiFraudLog, String> {
    
    // Lấy tất cả log gian lận của một submission
    List<ExamAntiFraudLog> findBySubmissionId(String submissionId);
    
    // Lấy tất cả log gian lận của một học sinh trong một kỳ thi
    List<ExamAntiFraudLog> findByExamIdAndStudentId(String examId, String studentId);
    
    // Lấy tất cả log gian lận của một đề thi
    List<ExamAntiFraudLog> findByExamId(String examId);
    
    // Lấy log chưa xem
    List<ExamAntiFraudLog> findByReviewedFalseAndExamId(String examId);
    
    // Lấy log trong khoảng thời gian
    List<ExamAntiFraudLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Lấy log theo severity
    List<ExamAntiFraudLog> findBySeverity(String severity);
    
    // Đếm số hành động nghi ngờ của một submission
    long countBySubmissionId(String submissionId);
    
    // Đếm số lần một loại fraud xảy ra
    long countBySubmissionIdAndFraudType(String submissionId, String fraudType);
}
