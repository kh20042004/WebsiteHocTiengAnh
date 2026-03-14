package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * ========== LESSON ENTITY (MongoDB Document) ==========
 * Đại diện cho một bài học trong Unit
 * Ví dụ: Getting Started, Language, Reading, Speaking, Writing, Listening...
 * Collection name: lessons
 */
@Document(collection = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    // ========== ID ==========
    /**
     * ID tự động sinh bởi MongoDB (ObjectId)
     */
    @Id
    private String id;

    // ========== LIÊN KẾT ==========
    /**
     * ID của Unit cha chứa bài học này
     * Indexed để tìm kiếm nhanh theo Unit
     */
    @Indexed
    private String unitId;

    // ========== THÔNG TIN CƠ BẢN ==========
    /**
     * Tiêu đề bài học (ví dụ: "Getting Started", "Reading", "Speaking")
     */
    private String title;

    /**
     * Mô tả ngắn về bài học
     */
    private String description;

    /**
     * Loại bài học:
     * VOCABULARY   - Từ vựng
     * GRAMMAR      - Ngữ pháp
     * READING      - Đọc hiểu
     * LISTENING    - Nghe
     * SPEAKING     - Nói
     * WRITING      - Viết
     * GETTING_STARTED - Khởi động
     */
    private String type;

    // ========== NỘI DUNG ==========
    /**
     * Nội dung bài học (HTML hoặc text)
     * Lưu trữ nội dung bài học đầy đủ
     */
    private String content;

    /**
     * URL audio file (cho bài nghe)
     */
    private String audioUrl;

    /**
     * Danh sách từ vựng trong bài (chỉ dùng cho type = VOCABULARY)
     * Mỗi từ vựng được lưu dạng "word:meaning" hoặc có thể mở rộng
     */
    private List<VocabularyItem> vocabulary;

    // ========== CẤU HÌNH ==========
    /**
     * Số thứ tự bài học trong Unit
     */
    private Integer orderIndex;

    /**
     * Thời gian học ước tính (phút)
     */
    private Integer estimatedDurationMinutes;

    /**
     * Điểm XP nhận được khi hoàn thành bài học này
     */
    private Integer xpReward;

    // ========== TRẠNG THÁI ==========
    /**
     * Trạng thái: true = hiển thị, false = ẩn
     */
    private Boolean isActive;

    // ========== THỜI GIAN ==========
    /**
     * Thời điểm tạo (milliseconds)
     */
    private Long createdAt;

    /**
     * Thời điểm cập nhật gần nhất (milliseconds)
     */
    private Long updatedAt;

    /**
     * ID của giáo viên/admin tạo bài học này
     */
    private String createdBy;

    // ========== INNER CLASS: TỪ VỰNG ==========
    /**
     * Class lưu thông tin một từ vựng trong bài học
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VocabularyItem {

        /**
         * Từ tiếng Anh
         */
        private String word;

        /**
         * Phiên âm (IPA)
         */
        private String pronunciation;

        /**
         * Loại từ (noun, verb, adjective...)
         */
        private String partOfSpeech;

        /**
         * Nghĩa tiếng Việt
         */
        private String meaning;

        /**
         * Câu ví dụ tiếng Anh
         */
        private String exampleSentence;

        /**
         * Dịch câu ví dụ
         */
        private String exampleTranslation;

        /**
         * URL ảnh minh họa (tuỳ chọn)
         */
        private String imageUrl;
    }
}
