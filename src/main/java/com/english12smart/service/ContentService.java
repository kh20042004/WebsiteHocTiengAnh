package com.english12smart.service;

import com.english12smart.dto.ContentDTO;
import com.english12smart.entity.Exercise;
import com.english12smart.entity.Lesson;
import com.english12smart.entity.Unit;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.ExerciseRepository;
import com.english12smart.repository.LessonRepository;
import com.english12smart.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ========== CONTENT SERVICE ==========
 * Service xử lý toàn bộ logic liên quan đến nội dung học tập:
 * - Unit (chương): CRUD
 * - Lesson (bài học): CRUD
 * - Exercise (bài tập): CRUD
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    // ========== Dependencies ==========
    private final UnitRepository unitRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;

    // =============================================================
    //  UNIT - Quản lý chương
    // =============================================================

    /**
     * Lấy tất cả Unit đang active (dành cho học sinh)
     * @return Danh sách Unit đã sắp xếp theo orderIndex
     */
    public List<ContentDTO.UnitResponse> getAllActiveUnits() {
        log.info("Lấy danh sách tất cả Unit active");

        return unitRepository.findByIsActiveTrueOrderByOrderIndexAsc()
                .stream()
                .map(this::convertToUnitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả Unit (kể cả ẩn) — dành cho admin/teacher
     * @return Danh sách tất cả Unit
     */
    public List<ContentDTO.UnitResponse> getAllUnits() {
        log.info("Lấy danh sách tất cả Unit (admin)");

        return unitRepository.findAllByOrderByOrderIndexAsc()
                .stream()
                .map(this::convertToUnitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một Unit kèm danh sách bài học
     * @param unitId - ID của Unit
     * @return UnitResponse có chứa lessons
     */
    public ContentDTO.UnitResponse getUnitWithLessons(String unitId) {
        log.info("Lấy chi tiết Unit: {}", unitId);

        // Tìm Unit trong DB
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Unit với ID: " + unitId));

        // Lấy danh sách bài học active
        List<ContentDTO.LessonResponse> lessons = lessonRepository
                .findByUnitIdAndIsActiveTrueOrderByOrderIndexAsc(unitId)
                .stream()
                .map(this::convertToLessonResponse)
                .collect(Collectors.toList());

        // Build response
        ContentDTO.UnitResponse response = convertToUnitResponse(unit);
        response.setLessons(lessons);
        return response;
    }

    /**
     * Tạo Unit mới
     * @param request   - Thông tin Unit cần tạo
     * @param createdBy - ID của người tạo
     * @return UnitResponse
     */
    public ContentDTO.UnitResponse createUnit(ContentDTO.UnitCreateRequest request, String createdBy) {
        log.info("Tạo Unit mới: {} bởi {}", request.getTitle(), createdBy);

        Long now = System.currentTimeMillis();

        // Tạo entity từ request
        Unit unit = Unit.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .level(request.getLevel() != null ? request.getLevel() : "B1")
                .thumbnailUrl(request.getThumbnailUrl())
                .totalLessons(0)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(createdBy)
                .build();

        Unit saved = unitRepository.save(unit);
        log.info("Đã tạo Unit thành công: {}", saved.getId());

        return convertToUnitResponse(saved);
    }

    /**
     * Cập nhật Unit
     * @param unitId  - ID Unit cần cập nhật
     * @param request - Thông tin mới
     * @return UnitResponse đã cập nhật
     */
    public ContentDTO.UnitResponse updateUnit(String unitId, ContentDTO.UnitUpdateRequest request) {
        log.info("Cập nhật Unit: {}", unitId);

        // Tìm Unit trong DB
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Unit với ID: " + unitId));

        // Cập nhật từng field nếu có giá trị mới
        if (request.getTitle() != null) unit.setTitle(request.getTitle());
        if (request.getDescription() != null) unit.setDescription(request.getDescription());
        if (request.getLevel() != null) unit.setLevel(request.getLevel());
        if (request.getThumbnailUrl() != null) unit.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getIsActive() != null) unit.setIsActive(request.getIsActive());

        unit.setUpdatedAt(System.currentTimeMillis());

        Unit saved = unitRepository.save(unit);
        log.info("Đã cập nhật Unit: {}", saved.getId());

        return convertToUnitResponse(saved);
    }

    /**
     * Xoá Unit và tất cả bài học, bài tập bên trong
     * @param unitId - ID Unit cần xoá
     */
    public void deleteUnit(String unitId) {
        log.info("Xoá Unit: {}", unitId);

        // Kiểm tra Unit tồn tại
        if (!unitRepository.existsById(unitId)) {
            throw new ResourceNotFoundException("Không tìm thấy Unit với ID: " + unitId);
        }

        // Xoá tất cả bài tập trong Unit
        exerciseRepository.deleteByUnitId(unitId);

        // Xoá tất cả bài học trong Unit
        lessonRepository.deleteByUnitId(unitId);

        // Xoá Unit
        unitRepository.deleteById(unitId);
        log.info("Đã xoá Unit {} cùng toàn bộ nội dung bên trong", unitId);
    }

    // =============================================================
    //  LESSON - Quản lý bài học
    // =============================================================

    /**
     * Lấy tất cả bài học (kể cả ẩn) trong một Unit — dành cho teacher/admin quản lý
     * @param unitId - ID của Unit
     * @return Danh sách LessonResponse
     */
    public List<ContentDTO.LessonResponse> getAllLessonsByUnit(String unitId) {
        log.info("Lấy tất cả bài học của Unit (admin/teacher): {}", unitId);

        return lessonRepository.findByUnitIdOrderByOrderIndexAsc(unitId)
                .stream()
                .map(this::convertToLessonResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả bài học active trong một Unit (dành cho học sinh)
     * @param unitId - ID của Unit
     * @return Danh sách LessonResponse
     */
    public List<ContentDTO.LessonResponse> getLessonsByUnit(String unitId) {
        log.info("Lấy bài học của Unit: {}", unitId);

        // Kiểm tra Unit tồn tại
        if (!unitRepository.existsById(unitId)) {
            throw new ResourceNotFoundException("Không tìm thấy Unit với ID: " + unitId);
        }

        return lessonRepository.findByUnitIdAndIsActiveTrueOrderByOrderIndexAsc(unitId)
                .stream()
                .map(this::convertToLessonResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy TẤT CẢ bài học active (bao gồm cả thông tin Unit)
     * Dùng cho dropdown trong form tạo bài tập
     * @return Danh sách LessonResponse từ tất cả Units
     */
    public List<ContentDTO.LessonResponse> getAllLessons() {
        log.info("📚 Lấy danh sách tất cả bài học");

        return lessonRepository.findByIsActiveTrueOrderByOrderIndexAsc()
                .stream()
                .map(this::convertToLessonResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một bài học
     * @param lessonId - ID của Lesson
     * @return LessonResponse
     */
    public ContentDTO.LessonResponse getLessonById(String lessonId) {
        log.info("Lấy chi tiết Lesson: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        return convertToLessonResponse(lesson);
    }

    /**
     * Tạo bài học mới
     * @param request   - Thông tin bài học
     * @param createdBy - ID người tạo
     * @return LessonResponse
     */
    public ContentDTO.LessonResponse createLesson(ContentDTO.LessonCreateRequest request, String createdBy) {
        log.info("Tạo bài học mới: {} trong Unit: {}", request.getTitle(), request.getUnitId());

        // Kiểm tra Unit tồn tại
        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy Unit với ID: " + request.getUnitId()));

        Long now = System.currentTimeMillis();

        // Tạo Lesson entity
        Lesson lesson = Lesson.builder()
                .unitId(request.getUnitId())
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .content(request.getContent())
                .audioUrl(request.getAudioUrl())
                .vocabulary(request.getVocabulary())
                .orderIndex(request.getOrderIndex())
                .estimatedDurationMinutes(
                        request.getEstimatedDurationMinutes() != null ? request.getEstimatedDurationMinutes() : 30)
                .xpReward(request.getXpReward() != null ? request.getXpReward() : 10)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(createdBy)
                .build();

        Lesson saved = lessonRepository.save(lesson);

        // Cập nhật tổng số bài học trong Unit
        unit.setTotalLessons((int) lessonRepository.countByUnitIdAndIsActiveTrue(unit.getId()));
        unit.setUpdatedAt(now);
        unitRepository.save(unit);

        log.info("Đã tạo bài học thành công: {}", saved.getId());
        return convertToLessonResponse(saved);
    }

    /**
     * Cập nhật bài học
     * @param lessonId - ID bài học cần cập nhật
     * @param request  - Thông tin mới
     * @return LessonResponse đã cập nhật
     */
    public ContentDTO.LessonResponse updateLesson(String lessonId, ContentDTO.LessonUpdateRequest request) {
        log.info("Cập nhật bài học: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        // Cập nhật từng field nếu có giá trị mới
        if (request.getTitle() != null) lesson.setTitle(request.getTitle());
        if (request.getDescription() != null) lesson.setDescription(request.getDescription());
        if (request.getContent() != null) lesson.setContent(request.getContent());
        if (request.getAudioUrl() != null) lesson.setAudioUrl(request.getAudioUrl());
        if (request.getEstimatedDurationMinutes() != null)
            lesson.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        if (request.getXpReward() != null) lesson.setXpReward(request.getXpReward());
        if (request.getVocabulary() != null) lesson.setVocabulary(request.getVocabulary());
        if (request.getIsActive() != null) lesson.setIsActive(request.getIsActive());

        lesson.setUpdatedAt(System.currentTimeMillis());

        Lesson saved = lessonRepository.save(lesson);
        log.info("Đã cập nhật bài học: {}", saved.getId());

        return convertToLessonResponse(saved);
    }

    /**
     * Xoá bài học và toàn bộ bài tập bên trong
     * @param lessonId - ID bài học cần xoá
     */
    public void deleteLesson(String lessonId) {
        log.info("Xoá bài học: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        // Xoá tất cả bài tập trong Lesson
        exerciseRepository.deleteByLessonId(lessonId);

        // Xoá Lesson
        lessonRepository.deleteById(lessonId);

        // Cập nhật lại tổng bài học của Unit
        unitRepository.findById(lesson.getUnitId()).ifPresent(unit -> {
            unit.setTotalLessons((int) lessonRepository.countByUnitIdAndIsActiveTrue(unit.getId()));
            unit.setUpdatedAt(System.currentTimeMillis());
            unitRepository.save(unit);
        });

        log.info("Đã xoá bài học: {}", lessonId);
    }

    // =============================================================
    //  EXERCISE - Quản lý bài tập
    // =============================================================

    /**
     * Lấy tất cả bài tập active của một bài học
     * @param lessonId - ID của Lesson
     * @return Danh sách ExerciseResponse
     */
    public List<ContentDTO.ExerciseResponse> getExercisesByLesson(String lessonId) {
        log.info("Lấy bài tập của Lesson: {}", lessonId);

        return exerciseRepository.findByLessonIdAndIsActiveTrueOrderByOrderIndexAsc(lessonId)
                .stream()
                .map(this::convertToExerciseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một bài tập
     * @param exerciseId - ID của Exercise
     * @return ExerciseResponse
     */
    public ContentDTO.ExerciseResponse getExerciseById(String exerciseId) {
        log.info("Lấy chi tiết Exercise: {}", exerciseId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập với ID: " + exerciseId));

        return convertToExerciseResponse(exercise);
    }

    /**
     * Lấy multiple exercises by IDs (dùng cho student làm bài)
     */
    public List<ContentDTO.ExerciseResponse> getExercisesByIds(List<String> exerciseIds) {
        log.info("Lấy {} bài tập by IDs", exerciseIds.size());

        return exerciseRepository.findAllById(exerciseIds)
                .stream()
                .map(this::convertToExerciseResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Tạo bài tập mới
     * @param request   - Thông tin bài tập
     * @param createdBy - ID người tạo
     * @return ExerciseResponse
     */
    public ContentDTO.ExerciseResponse createExercise(ContentDTO.ExerciseCreateRequest request, String createdBy) {
        log.info("Tạo bài tập mới trong Lesson: {}", request.getLessonId());

        // Kiểm tra Lesson tồn tại và lấy unitId
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy bài học với ID: " + request.getLessonId()));

        Long now = System.currentTimeMillis();

        // Tạo Exercise entity
        Exercise exercise = Exercise.builder()
                .lessonId(request.getLessonId())
                .unitId(lesson.getUnitId())
                .title(request.getTitle())
                .instruction(request.getInstruction())
                .type(request.getType())
                .questions(request.getQuestions())
                .orderIndex(request.getOrderIndex())
                .maxScore(request.getMaxScore() != null ? request.getMaxScore() : 10)
                .xpReward(request.getXpReward() != null ? request.getXpReward() : 5)
                .timeLimitMinutes(request.getTimeLimitMinutes())
                // SPEAKING_EXERCISE fields
                .correctPhrase(request.getCorrectPhrase())
                .minAccuracy(request.getMinAccuracy())
                .recordingTimeoutSeconds(request.getRecordingTimeoutSeconds())
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(createdBy)
                .build();

        Exercise saved = exerciseRepository.save(exercise);
        log.info("Đã tạo bài tập thành công: {}", saved.getId());

        return convertToExerciseResponse(saved);
    }

    /**
     * Xoá bài tập
     * @param exerciseId - ID bài tập cần xoá
     */
    public void deleteExercise(String exerciseId) {
        log.info("Xoá bài tập: {}", exerciseId);

        if (!exerciseRepository.existsById(exerciseId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài tập với ID: " + exerciseId);
        }

        exerciseRepository.deleteById(exerciseId);
        log.info("Đã xoá bài tập: {}", exerciseId);
    }

    // =============================================================
    //  HELPER METHODS - Chuyển đổi Entity → DTO
    // =============================================================

    /**
     * Chuyển Unit entity thành UnitResponse DTO
     */
    private ContentDTO.UnitResponse convertToUnitResponse(Unit unit) {
        return ContentDTO.UnitResponse.builder()
                .id(unit.getId())
                .title(unit.getTitle())
                .description(unit.getDescription())
                .orderIndex(unit.getOrderIndex())
                .level(unit.getLevel())
                .thumbnailUrl(unit.getThumbnailUrl())
                .totalLessons(unit.getTotalLessons() != null ? unit.getTotalLessons() : 0)
                .isActive(unit.getIsActive())
                .createdAt(unit.getCreatedAt())
                .build();
    }

    /**
     * Chuyển Lesson entity thành LessonResponse DTO
     */
    private ContentDTO.LessonResponse convertToLessonResponse(Lesson lesson) {
        // Đếm số bài tập trong lesson này
        long totalExercises = exerciseRepository.countByLessonId(lesson.getId());
        String normalizedAudioText = resolveAudioTextForListening(lesson);

        return ContentDTO.LessonResponse.builder()
                .id(lesson.getId())
                .unitId(lesson.getUnitId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .type(lesson.getType())
                .content(lesson.getContent())
                .audioUrl(lesson.getAudioUrl())
                .audioText(normalizedAudioText)
                .vocabulary(lesson.getVocabulary())
                .orderIndex(lesson.getOrderIndex())
                .estimatedDurationMinutes(lesson.getEstimatedDurationMinutes())
                .xpReward(lesson.getXpReward())
                .isActive(lesson.getIsActive())
                .createdAt(lesson.getCreatedAt())
                .totalExercises(totalExercises)
                .build();
    }

    private String resolveAudioTextForListening(Lesson lesson) {
        if (lesson.getAudioText() != null && !lesson.getAudioText().trim().isEmpty()) {
            return lesson.getAudioText().trim();
        }
        // Chỉ dùng text đã lưu khi tạo audio từ modal để đảm bảo so sánh đúng nguồn.
        return "";
    }

    /**
     * Chuyển Exercise entity thành ExerciseResponse DTO
     */
    private ContentDTO.ExerciseResponse convertToExerciseResponse(Exercise exercise) {
        return ContentDTO.ExerciseResponse.builder()
                .id(exercise.getId())
                .lessonId(exercise.getLessonId())
                .unitId(exercise.getUnitId())
                .title(exercise.getTitle())
                .instruction(exercise.getInstruction())
                .type(exercise.getType())
                .questions(exercise.getQuestions())
                .orderIndex(exercise.getOrderIndex())
                .maxScore(exercise.getMaxScore())
                .xpReward(exercise.getXpReward())
                .timeLimitMinutes(exercise.getTimeLimitMinutes())
                .isActive(exercise.getIsActive())
                .createdAt(exercise.getCreatedAt())
                // SPEAKING_EXERCISE fields
                .correctPhrase(exercise.getCorrectPhrase())
                .minAccuracy(exercise.getMinAccuracy())
                .recordingTimeoutSeconds(exercise.getRecordingTimeoutSeconds())
                .build();
    }
}
