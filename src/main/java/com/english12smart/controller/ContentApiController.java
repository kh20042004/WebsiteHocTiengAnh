package com.english12smart.controller;

import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.dto.ContentDTO;
import com.english12smart.service.ContentService;
import com.english12smart.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * ========== CONTENT API CONTROLLER ==========
 * REST API để quản lý nội dung học tập: Unit, Lesson, Exercise
 *
 * Phân quyền:
 * - GET (đọc): Mọi user đã đăng nhập
 * - POST/PUT/DELETE (ghi): Chỉ TEACHER và ADMIN
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContentApiController {

    // ========== Dependencies ==========
    private final ContentService contentService;
    private final JwtTokenProvider jwtTokenProvider;

    // =============================================================
    //  UNIT APIs
    // =============================================================

    /**
     * GET /api/units — Lấy danh sách tất cả Unit đang active (học sinh xem)
     */
    @GetMapping("/units")
    public ResponseEntity<ApiResponseDTO<List<ContentDTO.UnitResponse>>> getAllUnits() {
        log.info("API: Lấy danh sách Unit active");

        List<ContentDTO.UnitResponse> units = contentService.getAllActiveUnits();
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách chương thành công", units));
    }

    /**
     * GET /api/units/all — Lấy TẤT CẢ Unit kể cả ẩn (teacher/admin quản lý)
     */
    @GetMapping("/units/all")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<ContentDTO.UnitResponse>>> getAllUnitsForAdmin() {
        log.info("API: Lấy tất cả Unit (admin/teacher)");

        List<ContentDTO.UnitResponse> units = contentService.getAllUnits();
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách chương thành công", units));
    }

    /**
     * GET /api/units/{unitId} — Lấy chi tiết Unit kèm danh sách bài học
     */
    @GetMapping("/units/{unitId}")
    public ResponseEntity<ApiResponseDTO<ContentDTO.UnitResponse>> getUnitDetail(
            @PathVariable String unitId) {
        log.info("API: Lấy chi tiết Unit: {}", unitId);

        ContentDTO.UnitResponse unit = contentService.getUnitWithLessons(unitId);
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy thông tin chương thành công", unit));
    }

    /**
     * POST /api/units — Tạo Unit mới (TEACHER / ADMIN)
     */
    @PostMapping("/units")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ContentDTO.UnitResponse>> createUnit(
            @RequestBody ContentDTO.UnitCreateRequest request,
            HttpServletRequest httpRequest) {
        log.info("API: Tạo Unit mới: {}", request.getTitle());

        // Lấy userId từ JWT token để lưu người tạo
        String userId = extractUserIdFromRequest(httpRequest);

        ContentDTO.UnitResponse unit = contentService.createUnit(request, userId);
        return ResponseEntity.status(201)
                .body(ApiResponseDTO.created(unit));
    }

    /**
     * PUT /api/units/{unitId} — Cập nhật Unit (TEACHER / ADMIN)
     */
    @PutMapping("/units/{unitId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ContentDTO.UnitResponse>> updateUnit(
            @PathVariable String unitId,
            @RequestBody ContentDTO.UnitUpdateRequest request) {
        log.info("API: Cập nhật Unit: {}", unitId);

        ContentDTO.UnitResponse unit = contentService.updateUnit(unitId, request);
        return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật chương thành công", unit));
    }

    /**
     * DELETE /api/units/{unitId} — Xoá Unit và toàn bộ nội dung (TEACHER / ADMIN)
     */
    @DeleteMapping("/units/{unitId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUnit(@PathVariable String unitId) {
        log.info("API: Xoá Unit: {}", unitId);

        contentService.deleteUnit(unitId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>success("Xoá chương thành công", null));
    }

    // =============================================================
    //  LESSON APIs
    // =============================================================

    /**
     * GET /api/units/{unitId}/lessons — Lấy danh sách bài học trong Unit
     */
    @GetMapping("/units/{unitId}/lessons")
    public ResponseEntity<ApiResponseDTO<List<ContentDTO.LessonResponse>>> getLessonsByUnit(
            @PathVariable String unitId) {
        log.info("API: Lấy bài học của Unit: {}", unitId);

        List<ContentDTO.LessonResponse> lessons = contentService.getLessonsByUnit(unitId);
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách bài học thành công", lessons));
    }

    /**
     * GET /api/lessons/{lessonId} — Lấy chi tiết một bài học
     */
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponseDTO<ContentDTO.LessonResponse>> getLessonDetail(
            @PathVariable String lessonId) {
        log.info("API: Lấy chi tiết Lesson: {}", lessonId);

        ContentDTO.LessonResponse lesson = contentService.getLessonById(lessonId);
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy thông tin bài học thành công", lesson));
    }

    /**
     * POST /api/lessons — Tạo bài học mới (TEACHER hoặc ADMIN)
     */
    @PostMapping("/lessons")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ContentDTO.LessonResponse>> createLesson(
            @RequestBody ContentDTO.LessonCreateRequest request,
            HttpServletRequest httpRequest) {
        log.info("API: Tạo bài học mới: {}", request.getTitle());

        String userId = extractUserIdFromRequest(httpRequest);

        ContentDTO.LessonResponse lesson = contentService.createLesson(request, userId);
        return ResponseEntity.status(201)
                .body(ApiResponseDTO.created(lesson));
    }

    /**
     * PUT /api/lessons/{lessonId} — Cập nhật bài học (TEACHER hoặc ADMIN)
     */
    @PutMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ContentDTO.LessonResponse>> updateLesson(
            @PathVariable String lessonId,
            @RequestBody ContentDTO.LessonUpdateRequest request) {
        log.info("API: Cập nhật Lesson: {}", lessonId);

        ContentDTO.LessonResponse lesson = contentService.updateLesson(lessonId, request);
        return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật bài học thành công", lesson));
    }

    /**
     * DELETE /api/lessons/{lessonId} — Xoá bài học (TEACHER hoặc ADMIN)
     */
    @DeleteMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteLesson(@PathVariable String lessonId) {
        log.info("API: Xoá Lesson: {}", lessonId);

        contentService.deleteLesson(lessonId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>success("Xoá bài học thành công", null));
    }

    // =============================================================
    //  EXERCISE APIs
    // =============================================================

    /**
     * GET /api/lessons/{lessonId}/exercises — Lấy bài tập của một bài học
     */
    @GetMapping("/lessons/{lessonId}/exercises")
    public ResponseEntity<ApiResponseDTO<List<ContentDTO.ExerciseResponse>>> getExercisesByLesson(
            @PathVariable String lessonId) {
        log.info("API: Lấy bài tập của Lesson: {}", lessonId);

        List<ContentDTO.ExerciseResponse> exercises = contentService.getExercisesByLesson(lessonId);
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách bài tập thành công", exercises));
    }

    /**
     * GET /api/units/{unitId}/exercises — Lấy TẤT CẢ bài tập trong Unit (qua các Lesson)
     */
    @GetMapping("/units/{unitId}/exercises")
    public ResponseEntity<ApiResponseDTO<List<ContentDTO.ExerciseResponse>>> getExercisesByUnit(
            @PathVariable String unitId) {
        log.info("API: Lấy tất cả bài tập của Unit: {}", unitId);

        // Lấy tất cả lesson trong unit, rồi lấy hết exercises
        List<ContentDTO.LessonResponse> lessons = contentService.getLessonsByUnit(unitId);
        List<ContentDTO.ExerciseResponse> allExercises = new java.util.ArrayList<>();

        for (ContentDTO.LessonResponse lesson : lessons) {
            allExercises.addAll(contentService.getExercisesByLesson(lesson.getId()));
        }

        return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách bài tập thành công", allExercises));
    }

    /**
     * GET /api/exercises/{exerciseId} — Lấy chi tiết một bài tập
     */
    @GetMapping("/exercises/{exerciseId}")
    public ResponseEntity<ApiResponseDTO<ContentDTO.ExerciseResponse>> getExerciseDetail(
            @PathVariable String exerciseId) {
        log.info("API: Lấy chi tiết Exercise: {}", exerciseId);

        ContentDTO.ExerciseResponse exercise = contentService.getExerciseById(exerciseId);
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy thông tin bài tập thành công", exercise));
    }

    /**
     * POST /api/exercises — Tạo bài tập mới (TEACHER hoặc ADMIN)
     */
    @PostMapping("/exercises")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ContentDTO.ExerciseResponse>> createExercise(
            @RequestBody ContentDTO.ExerciseCreateRequest request,
            HttpServletRequest httpRequest) {
        log.info("API: Tạo bài tập mới cho Lesson: {}", request.getLessonId());

        String userId = extractUserIdFromRequest(httpRequest);

        ContentDTO.ExerciseResponse exercise = contentService.createExercise(request, userId);
        return ResponseEntity.status(201)
                .body(ApiResponseDTO.created(exercise));
    }

    /**
     * DELETE /api/exercises/{exerciseId} — Xoá bài tập (TEACHER hoặc ADMIN)
     */
    @DeleteMapping("/exercises/{exerciseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteExercise(@PathVariable String exerciseId) {
        log.info("API: Xoá Exercise: {}", exerciseId);

        contentService.deleteExercise(exerciseId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>success("Xoá bài tập thành công", null));
    }
    /**
     * GET /api/exercises?ids=id1,id2,id3 — Lấy multiple exercises by IDs (dùng cho student làm bài)
     */
    @GetMapping("/exercises")
    public ResponseEntity<ApiResponseDTO<List<ContentDTO.ExerciseResponse>>> getExercisesByIds(
            @RequestParam String ids) {
        log.info("API: Lấy exercises by IDs: {}", ids);

        List<String> exerciseIds = java.util.Arrays.asList(ids.split(","));
        List<ContentDTO.ExerciseResponse> exercises = contentService.getExercisesByIds(exerciseIds);

        return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách bài tập thành công", exercises));
    }
    // =============================================================
    //  HELPER: Lấy userId từ JWT trong request
    // =============================================================

    /**
     * Lấy userId từ JWT token trong header Authorization hoặc cookie
     * @param request - HttpServletRequest
     * @return userId hoặc "unknown" nếu không tìm thấy
     */
    private String extractUserIdFromRequest(HttpServletRequest request) {
        try {
            // Thử lấy từ Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtTokenProvider.getUserIdFromToken(token);
            }

            // Thử lấy từ cookie
            if (request.getCookies() != null) {
                for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                    if ("token".equals(cookie.getName())) {
                        return jwtTokenProvider.getUserIdFromToken(cookie.getValue());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Không thể lấy userId từ token: {}", e.getMessage());
        }
        return "unknown";
    }
}
