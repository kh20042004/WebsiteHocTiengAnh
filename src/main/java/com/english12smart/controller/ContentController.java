package com.english12smart.controller;

import com.english12smart.dto.ContentDTO;
import com.english12smart.entity.User;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.ContentService;
import com.english12smart.service.ProgressService;
import com.english12smart.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * ========== CONTENT MVC CONTROLLER ==========
 * Controller xử lý các trang web quản lý nội dung học tập
 *
 * Phân quyền:
 * - Teacher & Admin: Tạo/sửa/xoá Unit, Lesson, Exercise
 * - Student: Chỉ xem bài học
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ContentController {

    // ========== Dependencies ==========
    private final ContentService contentService;
    private final ProgressService progressService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // =============================================================
    //  TRANG QUẢN LÝ NỘI DUNG (Teacher / Admin)
    // =============================================================

    /**
     * GET /dashboard/teacher/content — Trang quản lý nội dung cho Teacher
     * Hiển thị danh sách tất cả Unit (kể cả ẩn)
     */
    @GetMapping("/dashboard/teacher/content")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public String teacherContent(Model model, HttpServletRequest request) {
        log.info("Giáo viên truy cập trang quản lý nội dung");

        // Lấy tất cả Unit (kể cả ẩn) để hiển thị cho teacher
        List<ContentDTO.UnitResponse> units = contentService.getAllUnits();
        model.addAttribute("units", units);

        // Thêm thông tin user vào model để hiển thị trên navbar
        addUserInfoToModel(model, request);

        return "teacher/content";
    }

    /**
     * GET /dashboard/teacher/content/units/{unitId} — Trang quản lý bài học trong Unit
     * Hiển thị danh sách Lesson của một Unit cụ thể
     */
    @GetMapping("/dashboard/teacher/content/units/{unitId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public String teacherUnitDetail(@PathVariable String unitId, Model model, HttpServletRequest request) {
        log.info("Giáo viên xem chi tiết Unit: {}", unitId);

        // Lấy thông tin Unit kèm bài học
        ContentDTO.UnitResponse unit = contentService.getUnitWithLessons(unitId);
        model.addAttribute("unit", unit);

        // Lấy TẤT CẢ bài học (kể cả ẩn) để teacher quản lý
        List<ContentDTO.LessonResponse> allLessons = contentService.getAllLessonsByUnit(unitId);
        model.addAttribute("lessons", allLessons);

        addUserInfoToModel(model, request);

        return "teacher/unit-detail";
    }

    /**
     * GET /dashboard/teacher/content/lessons/{lessonId} — Trang quản lý bài tập trong Lesson
     */
    @GetMapping("/dashboard/teacher/content/lessons/{lessonId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public String teacherLessonDetail(@PathVariable String lessonId, Model model, HttpServletRequest request) {
        log.info("Giáo viên xem chi tiết Lesson: {}", lessonId);

        // Lấy thông tin bài học
        ContentDTO.LessonResponse lesson = contentService.getLessonById(lessonId);
        model.addAttribute("lesson", lesson);

        // Lấy danh sách bài tập
        List<ContentDTO.ExerciseResponse> exercises = contentService.getExercisesByLesson(lessonId);
        model.addAttribute("exercises", exercises);

        addUserInfoToModel(model, request);

        return "teacher/lesson-detail";
    }

    /**
     * GET /dashboard/teacher/create-exercise — Trang tạo câu hỏi mới
     * Hiển thị form tạo câu hỏi với các loại khác nhau
     */
    @GetMapping("/dashboard/teacher/create-exercise")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public String createExercise(Model model, HttpServletRequest request) {
        log.info("🆕 Giáo viên tạo câu hỏi mới");

        // Lấy danh sách tất cả Lesson để hiển thị trong dropdown
        List<ContentDTO.LessonResponse> lessons = contentService.getAllLessons();
        model.addAttribute("lessons", lessons);

        // Thêm thông tin user
        addUserInfoToModel(model, request);

        return "teacher/create-exercise";
    }

    // =============================================================
    //  TRANG HỌC TẬP (Student)
    // =============================================================

    /**
     * GET /learn, /dashboard/student/learn — Trang danh sách các Unit để học sinh lựa chọn
     */
    @GetMapping({"/learn", "/dashboard/student/learn"})
    public String learnHome(Model model, HttpServletRequest request) {
        log.info("Học sinh truy cập trang học tập");

        // Chỉ lấy Unit đang active
        List<ContentDTO.UnitResponse> units = contentService.getAllActiveUnits();
        model.addAttribute("units", units);

        addUserInfoToModel(model, request);

        return "student/learn";
    }

    /**
     * GET /learn/units/{unitId}, /dashboard/student/learn/units/{unitId} — Trang danh sách bài học trong Unit
     */
    @GetMapping({"/learn/units/{unitId}", "/dashboard/student/learn/units/{unitId}"})
    public String learnUnit(@PathVariable String unitId, Model model, HttpServletRequest request) {
        log.info("Học sinh xem Unit: {}", unitId);

        // Lấy thông tin Unit kèm bài học active
        ContentDTO.UnitResponse unit = contentService.getUnitWithLessons(unitId);
        model.addAttribute("unit", unit);

        addUserInfoToModel(model, request);

        return "student/unit-detail";
    }

    /**
     * GET /learn/lessons/{lessonId}, /dashboard/student/learn/lessons/{lessonId} — Trang học bài học
     */
    @GetMapping({"/learn/lessons/{lessonId}", "/dashboard/student/learn/lessons/{lessonId}"})
    public String learnLesson(@PathVariable String lessonId, Model model, HttpServletRequest request) {
        log.info("Học sinh xem Lesson: {}", lessonId);

        // Lấy nội dung bài học
        ContentDTO.LessonResponse lesson = contentService.getLessonById(lessonId);
        model.addAttribute("lesson", lesson);

        // Lấy bài tập của bài học
        List<ContentDTO.ExerciseResponse> exercises = contentService.getExercisesByLesson(lessonId);
        model.addAttribute("exercises", exercises);

        // ========== TRACKING PROGRESS ==========
        // Lấy token từ cookie và ghi nhận xem bài học
        try {
            String token = extractTokenFromRequest(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                User user = userRepository.findByEmail(email);
                if (user != null) {
                    // Ghi nhận xem bài học
                    progressService.markLessonViewed(lessonId, user.getId());
                    
                    // Tính tiến độ và add vào model
                    var progress = progressService.calculateLessonProgress(lessonId, user.getId());
                    model.addAttribute("progress", progress);
                    log.info("Tiến độ bài học {}: {}%", lessonId, progress.getProgressPercent());
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi khi tracking progress: {}", e.getMessage());
        }

        addUserInfoToModel(model, request);

        return "student/lesson";
    }

    /**
     * GET /learn/exercises/{exerciseId}, /dashboard/student/learn/exercises/{exerciseId} — Trang làm bài tập
     */
    @GetMapping({"/learn/exercises/{exerciseId}", "/dashboard/student/learn/exercises/{exerciseId}"})
    public String doExercise(@PathVariable String exerciseId, Model model, HttpServletRequest request) {
        log.info("Học sinh làm bài tập: {}", exerciseId);

        // Lấy thông tin bài tập
        ContentDTO.ExerciseResponse exercise = contentService.getExerciseById(exerciseId);
        model.addAttribute("exercise", exercise);

        addUserInfoToModel(model, request);

        return "student/exercise";
    }

    // =============================================================
    //  HELPER: Thêm thông tin user vào Model để hiển thị trên navbar
    // =============================================================

    /**
     * Thêm thông tin user (từ JWT) vào Model
     * Dùng cho navbar hiển thị tên, avatar, role
     */
    private void addUserInfoToModel(Model model, HttpServletRequest request) {
        try {
            // Lấy JWT token từ cookie
            String token = extractTokenFromRequest(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                model.addAttribute("isAuthenticated", true);
                String email = jwtTokenProvider.getEmailFromToken(token);
                model.addAttribute("userRole", jwtTokenProvider.getRoleFromToken(token));
                
                // Lấy full name từ database nếu có, nếu không thì dùng email
                User user = userRepository.findByEmail(email);
                if (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) {
                    model.addAttribute("username", user.getFullName());
                } else {
                    model.addAttribute("username", email);
                }
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
    }

    /**
     * Lấy JWT token từ cookie hoặc Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // Thử từ Authorization header trước
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Thử từ cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
