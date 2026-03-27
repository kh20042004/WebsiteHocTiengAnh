package com.english12smart.service;

import com.english12smart.dto.AdminContentDTO;
import com.english12smart.entity.Exam;
import com.english12smart.entity.Exercise;
import com.english12smart.entity.Lesson;
import com.english12smart.entity.Unit;
import com.english12smart.exception.BadRequestException;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.ExamRepository;
import com.english12smart.repository.ExerciseRepository;
import com.english12smart.repository.LessonRepository;
import com.english12smart.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminContentService {

    private final UnitRepository unitRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExamRepository examRepository;

    public AdminContentDTO.ContentListResponse listContent(AdminContentDTO.ContentFilter filter) {
        int page = Math.max(filter.getPage(), 0);
        int size = filter.getSize() > 0 ? filter.getSize() : 20;
        List<AdminContentDTO.ContentSummary> items = new ArrayList<>();
        Map<String, String> unitTitleCache = new LinkedHashMap<>();

        if (shouldInclude(filter, AdminContentDTO.ContentType.UNIT)) {
            items.addAll(unitRepository.findAllByOrderByOrderIndexAsc().stream()
                    .filter(unit -> matchesFilters(unit, filter))
                    .map(unit -> toUnitSummary(unit))
                    .collect(Collectors.toList()));
        }

        if (shouldInclude(filter, AdminContentDTO.ContentType.LESSON)) {
            items.addAll(lessonRepository.findAll().stream()
                    .filter(lesson -> matchesFilters(lesson, filter))
                    .map(lesson -> toLessonSummary(lesson, unitTitleCache))
                    .collect(Collectors.toList()));
        }

        if (shouldInclude(filter, AdminContentDTO.ContentType.EXERCISE)) {
            items.addAll(exerciseRepository.findAll().stream()
                    .filter(exercise -> matchesFilters(exercise, filter))
                    .map(exercise -> toExerciseSummary(exercise, unitTitleCache))
                    .collect(Collectors.toList()));
        }

        if (shouldInclude(filter, AdminContentDTO.ContentType.EXAM)) {
            items.addAll(examRepository.findAll().stream()
                    .filter(exam -> matchesFilters(exam, filter))
                    .map(exam -> toExamSummary(exam))
                    .collect(Collectors.toList()));
        }

        Comparator<AdminContentDTO.ContentSummary> comparator = comparator(filter);
        items.sort(comparator);

        int total = items.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<AdminContentDTO.ContentSummary> paged = from >= to ? List.of() : items.subList(from, to);

        return AdminContentDTO.ContentListResponse.builder()
                .items(paged)
                .total(total)
                .page(page)
                .size(size)
                .build();
    }

    public AdminContentDTO.ContentStats getContentStats() {
        long totalUnits = unitRepository.count();
        long totalLessons = lessonRepository.count();
        long totalExercises = exerciseRepository.count();
        long totalExams = examRepository.count();

        Map<AdminContentDTO.ContentType, Long> counts = Map.of(
                AdminContentDTO.ContentType.UNIT, totalUnits,
                AdminContentDTO.ContentType.LESSON, totalLessons,
                AdminContentDTO.ContentType.EXERCISE, totalExercises,
                AdminContentDTO.ContentType.EXAM, totalExams
        );

        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        statusBreakdown.put("UNIT_ACTIVE", unitRepository.countByIsActiveTrue());
        statusBreakdown.put("LESSON_ACTIVE", lessonRepository.countByIsActiveTrue());
        statusBreakdown.put("EXERCISE_ACTIVE", exerciseRepository.countByIsActiveTrue());
        statusBreakdown.put("EXAM_ACTIVE", examRepository.countByStatus("ACTIVE"));
        statusBreakdown.put("EXAM_DRAFT", examRepository.countByStatus("DRAFT"));
        statusBreakdown.put("EXAM_CLOSED", examRepository.countByStatus("CLOSED"));

        return AdminContentDTO.ContentStats.builder()
                .counts(counts)
                .statusBreakdown(statusBreakdown)
                .total(totalUnits + totalLessons + totalExercises + totalExams)
                .build();
    }

    public AdminContentDTO.ContentSummary updateContentStatus(AdminContentDTO.ContentType type, String id,
                                                              AdminContentDTO.StatusUpdateRequest request) {
        if (type == null) {
            throw new BadRequestException("contentType is required");
        }
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        return switch (type) {
            case UNIT -> updateUnitStatus(id, request);
            case LESSON -> updateLessonStatus(id, request);
            case EXERCISE -> updateExerciseStatus(id, request);
            case EXAM -> updateExamStatus(id, request);
        };
    }

    private AdminContentDTO.ContentSummary updateUnitStatus(String id, AdminContentDTO.StatusUpdateRequest request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + id));
        if (request.getIsActive() != null) {
            unit.setIsActive(request.getIsActive());
        }
        unit.setUpdatedAt(System.currentTimeMillis());
        return toUnitSummary(unitRepository.save(unit));
    }

    private AdminContentDTO.ContentSummary updateLessonStatus(String id, AdminContentDTO.StatusUpdateRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + id));
        if (request.getIsActive() != null) {
            lesson.setIsActive(request.getIsActive());
        }
        lesson.setUpdatedAt(System.currentTimeMillis());
        Map<String, String> unitCache = new LinkedHashMap<>();
        return toLessonSummary(lessonRepository.save(lesson), unitCache);
    }

    private AdminContentDTO.ContentSummary updateExerciseStatus(String id, AdminContentDTO.StatusUpdateRequest request) {
        var exerciseOptional = exerciseRepository.findById(id);
        if (exerciseOptional.isEmpty()) {
            throw new ResourceNotFoundException("Exercise not found: " + id);
        }
        var exercise = exerciseOptional.get();
        if (request.getIsActive() != null) {
            exercise.setIsActive(request.getIsActive());
        }
        exercise.setUpdatedAt(System.currentTimeMillis());
        Map<String, String> unitCache = new LinkedHashMap<>();
        return toExerciseSummary(exerciseRepository.save(exercise), unitCache);
    }

    private AdminContentDTO.ContentSummary updateExamStatus(String id, AdminContentDTO.StatusUpdateRequest request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            exam.setStatus(request.getStatus().trim().toUpperCase());
        } else if (request.getIsActive() != null) {
            exam.setStatus(request.getIsActive() ? "ACTIVE" : "CLOSED");
        }
        exam.setUpdatedAt(System.currentTimeMillis());
        return toExamSummary(examRepository.save(exam));
    }

    private boolean shouldInclude(AdminContentDTO.ContentFilter filter, AdminContentDTO.ContentType type) {
        return filter.getContentType() == null || filter.getContentType() == type;
    }

    private boolean matchesFilters(Unit unit, AdminContentDTO.ContentFilter filter) {
        if (filter.getKeyword() != null && !matchesKeyword(unit.getTitle(), unit.getDescription(), filter.getKeyword())) {
            return false;
        }
        if (filter.getCreatedBy() != null && !filter.getCreatedBy().equals(unit.getCreatedBy())) {
            return false;
        }
        if (filter.getIsActive() != null && !filter.getIsActive().equals(unit.getIsActive())) {
            return false;
        }
        return matchesStatus(filter.getStatus(), unit.getIsActive());
    }

    private boolean matchesFilters(Lesson lesson, AdminContentDTO.ContentFilter filter) {
        if (filter.getKeyword() != null && !matchesKeyword(lesson.getTitle(), lesson.getDescription(), filter.getKeyword())) {
            return false;
        }
        if (filter.getCreatedBy() != null && !filter.getCreatedBy().equals(lesson.getCreatedBy())) {
            return false;
        }
        if (filter.getIsActive() != null && !filter.getIsActive().equals(lesson.getIsActive())) {
            return false;
        }
        return matchesStatus(filter.getStatus(), lesson.getIsActive());
    }

    private boolean matchesFilters(Exercise exercise, AdminContentDTO.ContentFilter filter) {
        if (filter.getKeyword() != null && !matchesKeyword(exercise.getTitle(), exercise.getInstruction(), filter.getKeyword())) {
            return false;
        }
        if (filter.getCreatedBy() != null && !filter.getCreatedBy().equals(exercise.getCreatedBy())) {
            return false;
        }
        if (filter.getIsActive() != null && !filter.getIsActive().equals(exercise.getIsActive())) {
            return false;
        }
        return matchesStatus(filter.getStatus(), exercise.getIsActive());
    }

    private boolean matchesFilters(Exam exam, AdminContentDTO.ContentFilter filter) {
        if (filter.getKeyword() != null && !matchesKeyword(exam.getTitle(), exam.getDescription(), filter.getKeyword())) {
            return false;
        }
        if (filter.getCreatedBy() != null && !filter.getCreatedBy().equals(exam.getTeacherId())) {
            return false;
        }
        if (filter.getStatus() != null && !filter.getStatus().equalsIgnoreCase(exam.getStatus())) {
            return false;
        }
        if (filter.getIsActive() != null) {
            boolean active = "ACTIVE".equalsIgnoreCase(exam.getStatus());
            if (!filter.getIsActive().equals(active)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesKeyword(String title, String description, String keyword) {
        String haystack = (title == null ? "" : title) + " " + (description == null ? "" : description);
        return haystack.toLowerCase().contains(keyword.toLowerCase());
    }

    private boolean matchesStatus(String statusFilter, Boolean active) {
        if (statusFilter == null) {
            return true;
        }
        if ("ACTIVE".equalsIgnoreCase(statusFilter)) {
            return Boolean.TRUE.equals(active);
        }
        if ("HIDDEN".equalsIgnoreCase(statusFilter)) {
            return Boolean.FALSE.equals(active);
        }
        return true;
    }

    private Comparator<AdminContentDTO.ContentSummary> comparator(AdminContentDTO.ContentFilter filter) {
        Comparator<AdminContentDTO.ContentSummary> base = Comparator.comparing(s -> Optional.ofNullable(s.getCreatedAt()).orElse(0L));
        if ("updatedAt".equalsIgnoreCase(filter.getSortBy())) {
            base = Comparator.comparing(s -> Optional.ofNullable(s.getUpdatedAt()).orElse(0L));
        }
        if ("title".equalsIgnoreCase(filter.getSortBy())) {
            base = Comparator.comparing(s -> Optional.ofNullable(s.getTitle()).orElse(""), String::compareToIgnoreCase);
        }
        if ("desc".equalsIgnoreCase(filter.getSortOrder())) {
            base = base.reversed();
        }
        return base;
    }

    private AdminContentDTO.ContentSummary toUnitSummary(Unit unit) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("level", unit.getLevel());
        metadata.put("orderIndex", unit.getOrderIndex());
        metadata.put("totalLessons", unit.getTotalLessons());
        return AdminContentDTO.ContentSummary.builder()
                .id(unit.getId())
                .contentType(AdminContentDTO.ContentType.UNIT)
                .title(unit.getTitle())
                .subtitle(unit.getDescription())
                .isActive(unit.getIsActive())
                .status(Boolean.TRUE.equals(unit.getIsActive()) ? "ACTIVE" : "HIDDEN")
                .createdBy(unit.getCreatedBy())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .metadata(metadata)
                .build();
    }

    private AdminContentDTO.ContentSummary toLessonSummary(Lesson lesson, Map<String, String> unitCache) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("type", lesson.getType());
        metadata.put("orderIndex", lesson.getOrderIndex());
        metadata.put("estimatedDurationMinutes", lesson.getEstimatedDurationMinutes());
        metadata.put("xpReward", lesson.getXpReward());
        metadata.put("totalExercises", exerciseRepository.countByLessonId(lesson.getId()));
        String unitTitle = resolveUnitTitle(lesson.getUnitId(), unitCache);
        return AdminContentDTO.ContentSummary.builder()
                .id(lesson.getId())
                .contentType(AdminContentDTO.ContentType.LESSON)
                .title(lesson.getTitle())
                .subtitle(lesson.getDescription())
                .unitId(lesson.getUnitId())
                .unitTitle(unitTitle)
                .isActive(lesson.getIsActive())
                .status(Boolean.TRUE.equals(lesson.getIsActive()) ? "ACTIVE" : "HIDDEN")
                .createdBy(lesson.getCreatedBy())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .metadata(metadata)
                .build();
    }

    private AdminContentDTO.ContentSummary toExerciseSummary(Exercise exercise,
                                                               Map<String, String> unitCache) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("type", exercise.getType());
        metadata.put("orderIndex", exercise.getOrderIndex());
        metadata.put("maxScore", exercise.getMaxScore());
        metadata.put("xpReward", exercise.getXpReward());
        metadata.put("timeLimitMinutes", exercise.getTimeLimitMinutes());
        String unitTitle = resolveUnitTitle(exercise.getUnitId(), unitCache);
        return AdminContentDTO.ContentSummary.builder()
                .id(exercise.getId())
                .contentType(AdminContentDTO.ContentType.EXERCISE)
                .title(exercise.getTitle())
                .subtitle(exercise.getInstruction())
                .unitId(exercise.getUnitId())
                .unitTitle(unitTitle)
                .lessonId(exercise.getLessonId())
                .isActive(exercise.getIsActive())
                .status(Boolean.TRUE.equals(exercise.getIsActive()) ? "ACTIVE" : "HIDDEN")
                .createdBy(exercise.getCreatedBy())
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .metadata(metadata)
                .build();
    }

    private AdminContentDTO.ContentSummary toExamSummary(Exam exam) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("classroomName", exam.getClassroomName());
        metadata.put("status", exam.getStatus());
        metadata.put("pinCode", exam.getPinCode());
        metadata.put("questionCount", exam.getQuestionCount());
        metadata.put("totalScore", exam.getTotalScore());
        metadata.put("submittedCount", exam.getSubmittedCount());
        metadata.put("timeLimitMinutes", exam.getTimeLimitMinutes());
        return AdminContentDTO.ContentSummary.builder()
                .id(exam.getId())
                .contentType(AdminContentDTO.ContentType.EXAM)
                .title(exam.getTitle())
                .subtitle(exam.getDescription())
                .unitId(exam.getClassroomId())
                .lessonId(exam.getTeacherId())
                .isActive("ACTIVE".equalsIgnoreCase(exam.getStatus()))
                .status(exam.getStatus())
                .createdBy(exam.getTeacherId())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .metadata(metadata)
                .build();
    }

    private String resolveUnitTitle(String unitId, Map<String, String> cache) {
        if (unitId == null) {
            return null;
        }
        return cache.computeIfAbsent(unitId, id -> unitRepository.findById(id)
                .map(Unit::getTitle)
                .orElse(null));
    }
}
