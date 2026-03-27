package com.english12smart.repository;

import com.english12smart.entity.Exercise;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ========== EXERCISE REPOSITORY ==========
 * Tương tác với collection 'exercises' trong MongoDB
 */
@Repository
public interface ExerciseRepository extends MongoRepository<Exercise, String> {

    /**
     * Lấy tất cả bài tập active của một Lesson, sắp xếp theo thứ tự
     * @param lessonId - ID của Lesson
     */
    List<Exercise> findByLessonIdAndIsActiveTrueOrderByOrderIndexAsc(String lessonId);

    /**
     * Lấy tất cả bài tập của một Lesson (kể cả ẩn), dành cho admin/teacher
     * @param lessonId - ID của Lesson
     */
    List<Exercise> findByLessonIdOrderByOrderIndexAsc(String lessonId);

    /**
     * Lấy bài tập theo loại trong một Lesson
     * @param lessonId - ID của Lesson
     * @param type     - Loại bài tập
     */
    List<Exercise> findByLessonIdAndType(String lessonId, String type);

    /**
     * Đếm số bài tập của một Lesson
     * @param lessonId - ID của Lesson
     */
    long countByLessonId(String lessonId);

    /**
     * Xoá tất cả bài tập của một Lesson
     * @param lessonId - ID của Lesson bị xoá
     */
    void deleteByLessonId(String lessonId);

    /**
     * Xoá tất cả bài tập của một Unit
     * @param unitId - ID của Unit bị xoá
     */
    void deleteByUnitId(String unitId);

    /**
     * Đếm tổng số bài tập đang active
     */
    long countByIsActiveTrue();
}
