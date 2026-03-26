package com.english12smart.repository;

import com.english12smart.entity.LessonProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LessonProgressRepository - Query tiến độ bài học
 */
@Repository
public interface LessonProgressRepository extends MongoRepository<LessonProgress, String> {

    /**
     * Lấy tiến độ của học sinh cho bài học cụ thể
     */
    Optional<LessonProgress> findByLessonIdAndStudentId(String lessonId, String studentId);

    /**
     * Lấy tất cả tiến độ bài học của học sinh trong Unit
     */
    List<LessonProgress> findByUnitIdAndStudentIdOrderByLastUpdatedAtDesc(String unitId, String studentId);

    /**
     * Lấy tất cả tiến độ bài học của học sinh
     */
    List<LessonProgress> findByStudentIdOrderByLastUpdatedAtDesc(String studentId);

    /**
     * Đếm số bài học học sinh đã hoàn thành
     */
    Long countByUnitIdAndStudentIdAndStatus(String unitId, String studentId, String status);
}
