package com.english12smart.repository;

import com.english12smart.entity.AISuggestionLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho AI Suggestion Log
 */
@Repository
public interface AISuggestionLogRepository extends MongoRepository<AISuggestionLog, String> {

    // Lấy tất cả gợi ý của một giáo viên
    List<AISuggestionLog> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    // Lấy gợi ý theo loại (exercise hoặc exam)
    List<AISuggestionLog> findByTeacherIdAndType(String teacherId, String type);

    // Lấy gợi ý đã chấp nhận
    List<AISuggestionLog> findByTeacherIdAndIsAcceptedTrue(String teacherId);

    // Lấy gợi ý đã lưu
    List<AISuggestionLog> findByTeacherIdAndIsSavedTrue(String teacherId);

    // Lấy gợi ý theo unit
    List<AISuggestionLog> findByTeacherIdAndUnit(String teacherId, String unit);

    // Lấy gợi ý trong khoảng thời gian
    @Query("{ 'teacher_id': ?0, 'created_at': { $gte: ?1, $lte: ?2 } }")
    List<AISuggestionLog> findByTeacherIdAndDateRange(String teacherId, LocalDateTime startDate, LocalDateTime endDate);

    // Đếm số gợi ý của giáo viên theo status
    Long countByTeacherIdAndStatus(String teacherId, String status);

    // Tính tổng token đã sử dụng trong tháng
    @Query("{ 'teacher_id': ?0, 'created_at': { $gte: ?1, $lte: ?2 } }")
    List<AISuggestionLog> findByTeacherIdInDateRange(String teacherId, LocalDateTime startDate, LocalDateTime endDate);

    // Lấy gợi ý theo rating
    List<AISuggestionLog> findByTeacherIdAndFeedbackRating(String teacherId, Integer rating);
}
