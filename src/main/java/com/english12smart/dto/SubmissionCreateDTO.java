package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho Submission Request (học sinh nộp bài)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionCreateDTO {

    private Long exerciseId;
    private Long assignmentId;
    private String answers; // JSON hoặc text
    // File audio sẽ được upload riêng qua endpoint /api/media/upload/audio
}

/**
 * DTO cho Submission Grading (giáo viên chấm)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SubmissionGradeDTO {

    private Double score;
    private String feedback;
    private String gradingDetails; // JSON: {"accuracy": 0.85, "fluency": 0.80, ...}
}

/**
 * DTO cho Submission Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SubmissionDTO {

    private Long id;
    private Long exerciseId;
    private Long assignmentId;
    private Long studentId;
    private String studentName;
    private String answers;
    private String audioUrl;
    private String transcript;
    private Double score;
    private String feedback;
    private String gradingDetails;
    private String status;
    private Long gradedById;
    private String gradedByName;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private Integer attemptNumber;
    private Boolean isLate;
}
