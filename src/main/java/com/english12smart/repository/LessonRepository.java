package com.english12smart.repository;

import com.english12smart.entity.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ========== LESSON REPOSITORY ==========
 * Tương tác với collection 'lessons' trong MongoDB
 */
@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {

    /**
     * Lấy tất cả bài học active của một Unit, sắp xếp theo thứ tự
     * @param unitId - ID của Unit
     */
    List<Lesson> findByUnitIdAndIsActiveTrueOrderByOrderIndexAsc(String unitId);

    /**
     * Lấy tất cả bài học của một Unit (kể cả ẩn), dành cho admin/teacher
     * @param unitId - ID của Unit
     */
    List<Lesson> findByUnitIdOrderByOrderIndexAsc(String unitId);

    /**
     * Lấy bài học theo loại trong một Unit
     * @param unitId - ID của Unit
     * @param type   - Loại bài học (VOCABULARY, GRAMMAR, READING...)
     */
    List<Lesson> findByUnitIdAndTypeAndIsActiveTrueOrderByOrderIndexAsc(String unitId, String type);

    /**
     * Đếm số bài học active trong một Unit
     * @param unitId - ID của Unit
     */
    long countByUnitIdAndIsActiveTrue(String unitId);

    /**
     * Đếm tổng số bài học đang active
     */
    long countByIsActiveTrue();

    /**
     * Lấy tất cả bài học đang active (không phân biệt Unit)
     * Dùng cho dropdown trong form tạo bài tập
     */
    List<Lesson> findByIsActiveTrueOrderByOrderIndexAsc();

    // ========== AUDIO GENERATION QUERIES ==========

    /**
     * Lấy tất cả bài học có status audio là PENDING
     * (Dùng để cleanup failed jobs)
     */
    List<Lesson> findByAudioStatusAndAudioGeneratedAtBefore(String status, LocalDateTime dateTime);

    /**
     * Lấy bài học theo audio status
     */
    List<Lesson> findByAudioStatus(String audioStatus);

    /**
     * Kiểm tra xem lesson có audio không
     */
    boolean existsByIdAndAudioUrlNotNull(String id);

    /**
     * Xoá tất cả bài học của một Unit (khi xoá Unit)
     * @param unitId - ID của Unit bị xoá
     */
    void deleteByUnitId(String unitId);

    /**
     * Tìm bài học của một giáo viên/admin
     * @param createdBy - ID người tạo
     */
    List<Lesson> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}
