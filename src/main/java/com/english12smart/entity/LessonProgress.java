package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * LessonProgress - Tracking tiến độ học sinh trên từng bài học
 * Ghi nhận: khi sinh viên xem bài học, làm bài tập, v.v.
 */
@Document(collection = "lesson_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgress {

    /** ID tracking */
    private String id;

    /** ID học sinh */
    private String studentId;

    /** ID bài học */
    private String lessonId;

    /** ID Unit */
    private String unitId;

    /** Đã xem bài học chưa (viewed = true khi sinh viên vào xem bài) */
    private Boolean viewed = false;

    /** Thời điểm xem lần đầu */
    private Long viewedAt;

    /** Tổng thời gian học (milliseconds) */
    private Long totalStudyTimeMs = 0L;

    /** Số lần xem */
    private Integer viewCount = 0;

    /** Số bài tập hoàn thành */
    private Integer completedExercises = 0;

    /** Số bài tập làm được */
    private Integer attemptedExercises = 0;

    /** Tổng điểm bài tập */
    private Integer totalScore = 0;

    /** Tổng điểm tối đa */
    private Integer maxScore = 0;

    /** Trạng thái: NOT_STARTED, IN_PROGRESS, COMPLETED */
    private String status = "NOT_STARTED";

    /** Tiến độ (%) */
    private Integer progressPercent = 0;

    /** Thời điểm lần cuối cập nhật */
    private Long lastUpdatedAt;

    /** Thời điểm hoàn thành */
    private Long completedAt;
}
