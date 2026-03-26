package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ProgressDTO - Tiến độ học tập
 * Tính tiến độ dựa trên bài tập hoàn thành
 */
public class ProgressDTO {

    /**
     * Tiến độ bài học
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonProgress {

        /** ID bài học */
        private String lessonId;

        /** Tên bài học */
        private String lessonTitle;

        /** Tổng số bài tập */
        private Integer totalExercises;

        /** Số bài tập hoàn thành (điểm >= 50% max score) */
        private Integer completedExercises;

        /**
         * Tiến độ (%): (Số bài hoàn thành / Tổng số bài) × 100
         * Cộng vào phần đã hoàn thành được
         */
        private Integer progressPercent;

        /** Tổng điểm */
        private Integer totalScore;

        /** Tổng điểm tối đa */
        private Integer maxTotalScore;

        /** Tổng XP kiếm được */
        private Integer totalXP;
    }

    /**
     * Tiến độ Unit
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnitProgress {

        /** ID Unit */
        private String unitId;

        /** Tên Unit */
        private String unitTitle;

        /** Tổng số bài học */
        private Integer totalLessons;

        /** Số bài học hoàn thành */
        private Integer completedLessons;

        /** Tiến độ trung bình (%) */
        private Integer averageProgress;

        /** Tổng XP kiếm được */
        private Integer totalXP;
    }

    /**
     * Response bài tập nộp
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExerciseSubmissionResponse {

        /** ID */
        private String id;

        /** ID bài tập */
        private String exerciseId;

        /** Điểm số */
        private Integer score;

        /** Điểm tối đa */
        private Integer maxScore;

        /** Phần trăm điểm */
        private Integer scorePercent;

        /** Trạng thái */
        private String status;

        /** Thời gian nộp */
        private Long submittedAt;
    }
}
