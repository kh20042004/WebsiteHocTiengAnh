package com.english12smart.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LearningDataOverviewDTO {

    private long lessonProgressRecords;
    private long trackedStudents;
    private long completedLessons;
    private double averageLessonProgress;
    private long totalExerciseSubmissions;
    private long completedExerciseSubmissions;
    private double averageExerciseScorePercent;
    private long totalStudyTimeMs;
    private List<TopStudent> topStudents;

    @Data
    @Builder
    public static class TopStudent {
        private String studentId;
        private String fullName;
        private double averageProgress;
    }
}
