package com.english12smart.dto;

import com.english12smart.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ========== UNIT & LESSON DTOs ==========
 * Các DTO dùng cho API Unit, Lesson, Exercise
 */
public class ContentDTO {

    // =======================================================
    //  UNIT DTOs
    // =======================================================

    /**
     * Request tạo Unit mới
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnitCreateRequest {

        /** Tiêu đề Unit (bắt buộc) */
        private String title;

        /** Mô tả ngắn */
        private String description;

        /** Số thứ tự (1, 2, 3...) */
        private Integer orderIndex;

        /** Cấp độ: A1, A2, B1, B2 */
        private String level;

        /** URL ảnh thumbnail */
        private String thumbnailUrl;
    }

    /**
     * Request cập nhật Unit
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnitUpdateRequest {

        /** Tiêu đề mới */
        private String title;

        /** Mô tả mới */
        private String description;

        /** Cấp độ mới */
        private String level;

        /** URL ảnh thumbnail mới */
        private String thumbnailUrl;

        /** Trạng thái active */
        private Boolean isActive;
    }

    /**
     * Response trả về thông tin Unit
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnitResponse {

        /** ID Unit */
        private String id;

        /** Tiêu đề */
        private String title;

        /** Mô tả */
        private String description;

        /** Số thứ tự */
        private Integer orderIndex;

        /** Cấp độ */
        private String level;

        /** URL ảnh thumbnail */
        private String thumbnailUrl;

        /** Tổng số bài học */
        private Integer totalLessons;

        /** Trạng thái */
        private Boolean isActive;

        /** Thời điểm tạo */
        private Long createdAt;

        /** Danh sách bài học (tuỳ chọn, chỉ load khi cần) */
        private List<LessonResponse> lessons;
    }

    // =======================================================
    //  LESSON DTOs
    // =======================================================

    /**
     * Request tạo Lesson mới
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonCreateRequest {

        /** ID của Unit cha (bắt buộc) */
        private String unitId;

        /** Tiêu đề bài học */
        private String title;

        /** Mô tả */
        private String description;

        /**
         * Loại bài học:
         * VOCABULARY, GRAMMAR, READING, LISTENING, SPEAKING, WRITING, GETTING_STARTED
         */
        private String type;

        /** Nội dung bài học */
        private String content;

        /** URL audio (cho bài nghe) */
        private String audioUrl;

        /** Số thứ tự trong Unit */
        private Integer orderIndex;

        /** Thời gian ước tính (phút) */
        private Integer estimatedDurationMinutes;

        /** Điểm XP khi hoàn thành */
        private Integer xpReward;

        /** Danh sách từ vựng (chỉ dùng khi type = VOCABULARY) */
        private List<Lesson.VocabularyItem> vocabulary;
    }

    /**
     * Request cập nhật Lesson
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonUpdateRequest {

        /** Tiêu đề mới */
        private String title;

        /** Mô tả mới */
        private String description;

        /** Nội dung mới */
        private String content;

        /** URL audio mới */
        private String audioUrl;

        /** Thời gian ước tính mới */
        private Integer estimatedDurationMinutes;

        /** Điểm XP mới */
        private Integer xpReward;

        /** Danh sách từ vựng mới */
        private List<Lesson.VocabularyItem> vocabulary;

        /** Trạng thái active */
        private Boolean isActive;
    }

    /**
     * Response trả về thông tin Lesson
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonResponse {

        /** ID Lesson */
        private String id;

        /** ID Unit cha */
        private String unitId;

        /** Tiêu đề */
        private String title;

        /** Mô tả */
        private String description;

        /** Loại bài học */
        private String type;

        /** Nội dung */
        private String content;

        /** URL audio */
        private String audioUrl;

        /** Danh sách từ vựng */
        private List<Lesson.VocabularyItem> vocabulary;

        /** Số thứ tự */
        private Integer orderIndex;

        /** Thời gian ước tính */
        private Integer estimatedDurationMinutes;

        /** Điểm XP */
        private Integer xpReward;

        /** Trạng thái */
        private Boolean isActive;

        /** Thời điểm tạo */
        private Long createdAt;

        /** Số bài tập trong lesson này */
        private Long totalExercises;

        /** Tiến độ học (%) - từ ProgressService */
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
        private Integer progressPercent;
    }

    // =======================================================
    //  EXERCISE DTOs
    // =======================================================

    /**
     * Request tạo Exercise mới
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExerciseCreateRequest {

        /** ID của Lesson cha (bắt buộc) */
        private String lessonId;

        /** Tiêu đề bài tập */
        private String title;

        /** Hướng dẫn */
        private String instruction;

        /**
         * Loại bài tập:
         * MULTIPLE_CHOICE, FILL_IN_BLANK, TRUE_FALSE, MATCHING, ORDERING, SPEAKING_EXERCISE
         */
        private String type;

        /** Danh sách câu hỏi */
        private List<com.english12smart.entity.Exercise.Question> questions;

        /** Số thứ tự trong Lesson */
        private Integer orderIndex;

        /** Điểm tối đa */
        private Integer maxScore;

        /** Điểm XP khi hoàn thành */
        private Integer xpReward;

        /** Thời gian làm bài (phút), null = không giới hạn */
        private Integer timeLimitMinutes;

        // ========== SPEAKING EXERCISE FIELDS ==========
        /** Cụm từ/câu chuẩn để so sánh (dành cho SPEAKING_EXERCISE) */
        private String correctPhrase;

        /** Độ chính xác tối thiểu (0.0-1.0, null = mặc định 0.6) */
        private Double minAccuracy;

        /** Thời gian ghi âm tối đa (giây, null = không giới hạn) */
        private Integer recordingTimeoutSeconds;
    }

    /**
     * Response trả về thông tin Exercise
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExerciseResponse {

        /** ID Exercise */
        private String id;

        /** ID Lesson cha */
        private String lessonId;

        /** ID Unit */
        private String unitId;

        /** Tiêu đề */
        private String title;

        /** Hướng dẫn */
        private String instruction;

        /** Loại bài tập */
        private String type;

        /** Danh sách câu hỏi */
        private List<com.english12smart.entity.Exercise.Question> questions;

        /** Số thứ tự */
        private Integer orderIndex;

        /** Điểm tối đa */
        private Integer maxScore;

        /** Điểm XP */
        private Integer xpReward;

        /** Thời gian làm bài */
        private Integer timeLimitMinutes;

        /** Trạng thái */
        private Boolean isActive;

        /** Thời điểm tạo */
        private Long createdAt;

        // ========== SPEAKING EXERCISE FIELDS ==========
        /** Cụm từ/câu chuẩn để so sánh (dành cho SPEAKING_EXERCISE) */
        private String correctPhrase;

        /** Độ chính xác tối thiểu (0.0-1.0, null = mặc định 0.6) */
        private Double minAccuracy;

        /** Thời gian ghi âm tối đa (giây, null = không giới hạn) */
        private Integer recordingTimeoutSeconds;
    }
}
