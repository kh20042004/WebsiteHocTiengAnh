package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

/**
 * Entity để lưu lịch sử AI suggestions
 * Dùng để tracking và feedback từ giáo viên
 */
@Document(collection = "ai_suggestion_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AISuggestionLog {
    @Id
    private String id;

    // ID của giáo viên yêu cầu gợi ý
    @Field("teacher_id")
    private String teacherId;

    // Loại gợi ý: "exercise" hoặc "exam"
    @Field("suggestion_type")
    private String type;

    // Unit hoặc chủ đề
    @Field("unit")
    private String unit;

    // Prompt gửi đến AI
    @Field("prompt")
    private String prompt;

    // Response từ AI (JSON string)
    @Field("ai_response")
    private String aiResponse;

    // Số token sử dụng (tracking chi phí)
    @Field("tokens_used")
    private Integer tokensUsed;

    // Giáo viên có chấp nhận gợi ý này không?
    @Field("is_accepted")
    private Boolean isAccepted;

    // Giáo viên có lưu gợi ý này không?
    @Field("is_saved")
    private Boolean isSaved;

    // Feedback từ giáo viên (rating từ 1-5)
    @Field("feedback_rating")
    private Integer feedbackRating;

    // Ghi chú feedback
    @Field("feedback_note")
    private String feedbackNote;

    // Thời gian tạo
    @Field("created_at")
    private LocalDateTime createdAt;

    // Thời gian cập nhật feedback
    @Field("feedback_at")
    private LocalDateTime feedbackAt;

    // Status: "pending", "accepted", "rejected", "modified"
    @Field("status")
    private String status;

    // Công thức tính điểm (số phút làm bài)
    @Field("metadata")
    private String metadata;
}
