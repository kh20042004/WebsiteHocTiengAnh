package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho Lesson Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCreateDTO {

    private String title;
    private String description;
    private String type; // READING, LISTENING, GRAMMAR, VOCABULARY, LANGUAGE_FOCUS, WRITING, SPEAKING
    private String content;
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
    private Integer estimatedDurationMinutes;
    private Integer orderIndex;
    private Long unitId;
}

/**
 * DTO cho Lesson Update
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class LessonUpdateDTO {

    private String title;
    private String description;
    private String content;
    private String level;
    private Integer estimatedDurationMinutes;
    private Integer orderIndex;
    private String status; // DRAFT, PUBLISHED, ARCHIVED
}

/**
 * DTO cho Lesson Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class LessonDTO {

    private Long id;
    private Long unitId;
    private String title;
    private String description;
    private String type;
    private String content;
    private String thumbnailUrl;
    private String audioUrl;
    private String videoUrl;
    private String pdfUrl;
    private Integer orderIndex;
    private String level;
    private Integer estimatedDurationMinutes;
    private String status;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
