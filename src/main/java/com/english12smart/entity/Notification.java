package com.english12smart.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ========== NOTIFICATION ENTITY ==========
 * Quản lý thông báo cho người dùng
 */
@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    /**
     * ID của người nhận thông báo
     */
    @Indexed
    private String userId;

    /**
     * Tiêu đề thông báo
     */
    private String title;

    /**
     * Nội dung chi tiết
     */
    private String message;

    /**
     * Loại thông báo: SYSTEM, ASSIGNMENT, EXAM, CLASSROOM, etc.
     */
    private String type;

    /**
     * Đường dẫn điều hướng khi người dùng click vào thông báo (tùy chọn)
     */
    private String targetUrl;

    /**
     * Trạng thái đọc: true = đã đọc, false = chưa đọc
     */
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Thời gian tạo thông báo (milliseconds)
     */
    private Long createdAt;
}
