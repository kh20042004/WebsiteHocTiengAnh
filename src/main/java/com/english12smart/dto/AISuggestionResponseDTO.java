package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response gợi ý AI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AISuggestionResponseDTO {
    // Status: "success" hoặc "error"
    private String status;

    // Message chi tiết
    private String message;

    // Danh sách gợi ý (có thể là Exercise hoặc Exam)
    private List<Object> suggestions;

    // ID của log gợi ý (để giáo viên có thể save/reject later)
    private String suggestionLogId;

    // Số token sử dụng (để tracking chi phí)
    private Integer tokensUsed;

    // Thời gian tạo
    private LocalDateTime createdAt;

    // Ghi chú cho giáo viên
    private String notes;
}
