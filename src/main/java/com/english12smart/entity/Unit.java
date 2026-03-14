package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ========== UNIT ENTITY (MongoDB Document) ==========
 * Đại diện cho một Unit (chương) trong sách giáo khoa lớp 12
 * Ví dụ: Unit 1 - Life Stories, Unit 2 - Urbanisation...
 * Collection name: units
 */
@Document(collection = "units")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    // ========== ID ==========
    /**
     * ID tự động sinh bởi MongoDB (ObjectId)
     */
    @Id
    private String id;

    // ========== THÔNG TIN CƠ BẢN ==========
    /**
     * Tiêu đề Unit (ví dụ: "Life Stories")
     */
    private String title;

    /**
     * Mô tả ngắn về nội dung Unit
     */
    private String description;

    /**
     * Số thứ tự Unit (1-10 theo SGK lớp 12)
     */
    @Indexed
    private Integer orderIndex;

    /**
     * Cấp độ khó (A1, A2, B1, B2)
     * Theo chuẩn CEFR châu Âu
     */
    private String level;

    /**
     * URL ảnh thumbnail của Unit
     */
    private String thumbnailUrl;

    // ========== THỐNG KÊ ==========
    /**
     * Tổng số bài học trong Unit này
     * Được cập nhật tự động khi thêm/xoá bài học
     */
    private Integer totalLessons;

    // ========== TRẠNG THÁI ==========
    /**
     * Trạng thái: true = đang hiển thị, false = ẩn
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
     * ID của người tạo Unit (thường là ADMIN)
     */
    private String createdBy;
}
