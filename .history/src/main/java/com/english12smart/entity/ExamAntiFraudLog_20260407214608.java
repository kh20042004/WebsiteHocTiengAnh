package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

/**
 * Ghi nhận các hành động nghi ngờ gian lận trong kỳ thi
 */
@Document(collection = "exam_anti_fraud_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAntiFraudLog {
    
    @Id
    private String id;
    
    @Indexed
    private String submissionId;
    
    @Indexed
    private String examId;
    
    @Indexed
    private String studentId;
    
    @Indexed
    private String studentName;
    
    /**
     * Loại hành động nghi ngờ
     * TAB_CHANGE: Rời bỏ tab bài thi
     * COPY_ATTEMPT: Cố gắng copy
     * PASTE_ATTEMPT: Cố gắng dán
     * RIGHT_CLICK: Click chuột phải
     * DEV_TOOLS: Mở DevTools
     * FULLSCREEN_EXIT: Thoát chế độ fullscreen
     * SUSPICIOUS_SPEED: Hoàn thành quá nhanh
     * UNUSUAL_ACTIVITY: Hoạt động bất thường khác
     */
    private String fraudType;
    
    // Số lần lặp lại hành động này
    private Integer count;
    
    // Dấu thời gian khi phát hiện
    private LocalDateTime detectedAt;
    
    // Mô tả chi tiết
    private String details;
    
    // User-agent thiết bị
    private String userAgent;
    
    // IP address
    private String ipAddress;
    
    // Độ nghiêm trọng: LOW, MEDIUM, HIGH
    private String severity;
    
    // Đã xem hay chưa
    private Boolean reviewed;
    
    // Ghi chú của giáo viên
    @Builder.Default
    private String teacherNote = "";
    
    // Timestamp tạo bản ghi
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
