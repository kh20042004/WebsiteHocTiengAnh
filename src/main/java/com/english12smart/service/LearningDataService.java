package com.english12smart.service;

import com.english12smart.dto.LearningDataOverviewDTO;
import com.english12smart.entity.ExerciseSubmission;
import com.english12smart.entity.LessonProgress;
import com.english12smart.entity.User;
import com.english12smart.repository.ExerciseSubmissionRepository;
import com.english12smart.repository.LessonProgressRepository;
import com.english12smart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningDataService {

    private final LessonProgressRepository lessonProgressRepository;
    private final ExerciseSubmissionRepository exerciseSubmissionRepository;
    private final UserRepository userRepository;

    public LearningDataOverviewDTO getOverview() {
        List<LessonProgress> progressRecords = lessonProgressRepository.findAll();
        List<ExerciseSubmission> submissions = exerciseSubmissionRepository.findAll();

        long lessonProgressRecords = progressRecords.size();
        Set<String> trackedStudents = progressRecords.stream()
            .map(LessonProgress::getStudentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        long completedLessons = progressRecords.stream()
            .map(LessonProgress::getStatus)
            .filter(status -> "COMPLETED".equalsIgnoreCase(status))
            .count();
        double averageLessonProgress = progressRecords.stream()
            .map(LessonProgress::getProgressPercent)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        long totalStudyTimeMs = progressRecords.stream()
            .map(LessonProgress::getTotalStudyTimeMs)
            .filter(Objects::nonNull)
            .mapToLong(Long::longValue)
            .sum();

        long totalExerciseSubmissions = submissions.size();
        long completedExerciseSubmissions = submissions.stream()
            .map(ExerciseSubmission::getStatus)
            .filter(status -> "COMPLETED".equalsIgnoreCase(status))
            .count();
        double averageExerciseScorePercent = submissions.stream()
            .filter(sub -> sub.getScore() != null && sub.getMaxScore() != null && sub.getMaxScore() > 0)
            .mapToDouble(sub -> (double) sub.getScore() / sub.getMaxScore() * 100)
            .average()
            .orElse(0.0);

        List<LearningDataOverviewDTO.TopStudent> topStudents = buildTopStudents(progressRecords);

        return LearningDataOverviewDTO.builder()
            .lessonProgressRecords(lessonProgressRecords)
            .trackedStudents(trackedStudents.size())
            .completedLessons(completedLessons)
            .averageLessonProgress(roundOneDecimal(averageLessonProgress))
            .totalExerciseSubmissions(totalExerciseSubmissions)
            .completedExerciseSubmissions(completedExerciseSubmissions)
            .averageExerciseScorePercent(roundOneDecimal(averageExerciseScorePercent))
            .totalStudyTimeMs(totalStudyTimeMs)
            .topStudents(topStudents)
            .build();
    }

    private List<LearningDataOverviewDTO.TopStudent> buildTopStudents(List<LessonProgress> progressRecords) {
        Map<String, List<LessonProgress>> groupedByStudent = progressRecords.stream()
            .filter(record -> record.getStudentId() != null && record.getProgressPercent() != null)
            .collect(Collectors.groupingBy(LessonProgress::getStudentId));

        List<StudentProgressMetric> metrics = groupedByStudent.entrySet().stream()
            .map(entry -> new StudentProgressMetric(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparingDouble(StudentProgressMetric::averageProgress).reversed())
            .limit(3)
            .collect(Collectors.toList());

        if (metrics.isEmpty()) {
            return List.of();
        }

        List<String> topStudentIds = metrics.stream()
            .map(StudentProgressMetric::studentId)
            .collect(Collectors.toList());
        Map<String, User> studentMap = userRepository.findByIdIn(topStudentIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        List<LearningDataOverviewDTO.TopStudent> topStudents = new ArrayList<>();
        for (StudentProgressMetric metric : metrics) {
            User user = studentMap.get(metric.studentId());
            String name = user != null ? user.getFullName() : null;
            topStudents.add(LearningDataOverviewDTO.TopStudent.builder()
                .studentId(metric.studentId())
                .fullName(name)
                .averageProgress(roundOneDecimal(metric.averageProgress()))
                .build());
        }

        return topStudents;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record StudentProgressMetric(String studentId, List<LessonProgress> records) {
        double averageProgress() {
            return records.stream()
                .map(LessonProgress::getProgressPercent)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        }
    }
}
