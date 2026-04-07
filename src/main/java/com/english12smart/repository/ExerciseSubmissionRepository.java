package com.english12smart.repository;

import com.english12smart.entity.ExerciseSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ExerciseSubmissionRepository - Query bài nộp bài tập
 */
@Repository
public interface ExerciseSubmissionRepository extends MongoRepository<ExerciseSubmission, String> {

    /**
     * Lấy tất cả bài tập học sinh nộp theo lessonId
     */
    List<ExerciseSubmission> findByLessonIdAndStudentIdOrderBySubmittedAtDesc(String lessonId, String studentId);

    /**
     * Lấy tất cả bài tập học sinh nộp theo unitId
     */
    List<ExerciseSubmission> findByUnitIdAndStudentIdOrderBySubmittedAtDesc(String unitId, String studentId);

    /**
     * Lấy bài tập đã hoàn thành
     */
    List<ExerciseSubmission> findByLessonIdAndStudentIdAndStatusOrderBySubmittedAtDesc(String lessonId, String studentId, String status);

    /**
     * Đếm số bài tập hoàn thành trong bài học
     */
    Long countByLessonIdAndStudentIdAndStatus(String lessonId, String studentId, String status);

    /**
     * Đếm tổng số bài tập đã nộp
     */
    Long countByLessonIdAndStudentId(String lessonId, String studentId);

    /**
     * Lấy tất cả bài tập của học sinh (sorted by submitted time)
     */
    List<ExerciseSubmission> findByStudentIdOrderBySubmittedAtDesc(String studentId);
}
