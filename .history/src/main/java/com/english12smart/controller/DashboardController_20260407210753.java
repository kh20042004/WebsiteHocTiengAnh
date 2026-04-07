package com.english12smart.controller;

import com.english12smart.dto.AssignmentDTO;
import com.english12smart.dto.ClassroomDTO;
import com.english12smart.dto.ContentDTO;
import com.english12smart.dto.ExamSubmissionDTO;
import com.english12smart.entity.Assignment;
import com.english12smart.entity.Classroom;
import com.english12smart.entity.Exam;
import com.english12smart.entity.ExamSubmission;
import com.english12smart.entity.User;
import com.english12smart.repository.AssignmentRepository;
import com.english12smart.repository.AssignmentSubmissionRepository;
import com.english12smart.repository.ClassroomRepository;
import com.english12smart.repository.ExamRepository;
import com.english12smart.repository.ExamSubmissionRepository;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.AssignmentService;
import com.english12smart.service.ClassroomService;
import com.english12smart.service.ContentService;
import com.english12smart.service.ExamService;
import com.english12smart.service.ProgressService;
import com.english12smart.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ========== DASHBOARD CONTROLLER ==========
 * Controller xử lý routing đến dashboard dựa trên role của user
 * 
 * Flow:
 * 1. User đăng nhập thành công → Redirect đến /dashboard
 * 2. DashboardController kiểm tra role
 * 3. Redirect đến dashboard tương ứng:
 * - STUDENT → /student/dashboard
 * - TEACHER → /teacher/dashboard
 * - ADMIN → /admin/dashboard
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final JwtTokenProvider jwtTokenProvider;
    private final ClassroomService classroomService;
    private final AssignmentService assignmentService;
    private final ContentService contentService;
    private final ExamService examService;
    private final ProgressService progressService;
    private final ClassroomRepository classroomRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final UserRepository userRepository;

    /**
     * Dashboard chính - Redirect dựa trên role
     * GET /dashboard
     * 
     * Luồng hoạt động:
     * 1. Kiểm tra user đã đăng nhập chưa
     * 2. Lấy role của user từ SecurityContext
     * 3. Redirect đến dashboard tương ứng:
     * - ADMIN → /admin/dashboard
     * - TEACHER → /teacher/dashboard
     * - STUDENT → /student/dashboard
     * 
     * @param request - HTTP request
     * @return redirect URL dựa trên role
     */
    @GetMapping
    public String dashboard(HttpServletRequest request) {
        log.info("Có yêu cầu truy cập dashboard");

        // Lấy thông tin authentication từ SecurityContext
        // SecurityContext chứa thông tin user đã đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem user đã đăng nhập chưa
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User chưa đăng nhập, chuyển hướng về trang login");
            return "redirect:/auth/login";
        }

        // Lấy role từ authorities (quyền hạn)
        // Mỗi user có 1 role: ROLE_STUDENT, ROLE_TEACHER, hoặc ROLE_ADMIN
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_STUDENT"); // Mặc định là STUDENT nếu không tìm thấy role

        log.info("Role của user: {}", role);

        // Sau khi đăng nhập, redirect về trang chủ
        // Navbar sẽ tự động thay đổi hiển thị user menu dropdown
        log.info("Chuyển hướng về trang chủ với user menu");
        return "redirect:/";
    }

    /**
     * Dashboard dành cho Học sinh
     * GET /dashboard/student
     * 
     * Hiển thị:
     * - Danh sách tất cả Unit (chương)
     * - Bài học trong mỗi Unit kèm tiến độ
     * - Bài tập gần đây cần làm
     * - Thống kê học tập tổng quát
     * 
     * Luồng hoạt động:
     * 1. Lấy thông tin user đang đăng nhập từ SecurityContext
     * 2. Lấy dữ liệu từ database:
     *    - Tất cả Unit active (sắp xếp theo thứ tự)
     *    - Từng Unit lấy danh sách Lesson
     *    - Danh sách Assignment gần đây (theo lớp học của student)
     * 3. Tính toán thống kê:
     *    - Tổng bài học
     *    - Số bài đã hoàn thành
     *    - Số bài đang học
     * 4. Truyền ra view để hiển thị
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return student dashboard template (student/dashboard.html)
     */
    @GetMapping("/student")
    public String studentDashboard(Model model) {
        log.info("========== Đang tải Student Dashboard ==========");

        try {
            // ========== 1. Lấy thông tin user đang đăng nhập ==========
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("User chưa đăng nhập, chuyển hướng về login");
                return "redirect:/auth/login";
            }

            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email);
            
            if (currentUser == null) {
                log.error("Không tìm thấy user với email: {}", email);
                return "redirect:/auth/login";
            }

            log.info("User đang đăng nhập: {} ({})", currentUser.getFullName(), currentUser.getId());

            // Lấy studentId cho các bước tiếp theo
            String studentId = currentUser.getId();

            // ========== 2. Lấy danh sách Unit active (chương) ==========
            log.info("Đang lấy danh sách Unit...");
            List<ContentDTO.UnitResponse> allUnits = new ArrayList<>();
            
            try {
                // Lấy tất cả Unit
                List<ContentDTO.UnitResponse> unitList = contentService.getAllActiveUnits();
                
                // Load Lessons cho mỗi Unit
                for (ContentDTO.UnitResponse unit : unitList) {
                    ContentDTO.UnitResponse unitWithLessons = contentService.getUnitWithLessons(unit.getId());
                    
                    // ========== CALCULATE PROGRESS cho mỗi lesson ==========
                    if (unitWithLessons.getLessons() != null) {
                        for (ContentDTO.LessonResponse lesson : unitWithLessons.getLessons()) {
                            try {
                                int progressPercent = progressService.getLessonProgressPercent(lesson.getId(), studentId);
                                lesson.setProgressPercent(progressPercent);
                                log.debug("Tiến độ bài học {}: {}%", lesson.getTitle(), progressPercent);
                            } catch (Exception e) {
                                log.warn("Lỗi tính tiến độ bài học {}: {}", lesson.getId(), e.getMessage());
                                lesson.setProgressPercent(0);  // Default 0% nếu có lỗi
                            }
                        }
                    }
                    
                    allUnits.add(unitWithLessons);
                }
                
                log.info("Đã load {} units", allUnits.size());
            } catch (Exception e) {
                log.error("Lỗi khi load units: {}", e.getMessage());
                // Nếu có lỗi, vẫn tiếp tục bằng list rỗng
            }
            
            // Tính thống kê:
            // - Tổng bài học = tổng của tất cả lessons trong tất cả units
            // - Bài đã hoàn thành: từ tracking data (tạm thời để cứng = 60% tương đối)
            // - Bài đang học: từ tracking data
            int totalLessons = 0;
            for (ContentDTO.UnitResponse unit : allUnits) {
                if (unit.getLessons() != null) {
                    totalLessons += unit.getLessons().size();
                }
            }
            
            int completedLessons = (int) (totalLessons * 0.6); // Giả định 60% bài đã hoàn thành
            int ongoingLessons = (int) (totalLessons * 0.3);   // Giả định 30% bài đang học
            
            log.info("Tổng bài học: {}, Đã hoàn thành: {}, Đang học: {}", 
                totalLessons, completedLessons, ongoingLessons);

            // ========== 3. Lấy danh sách lớp học của student ==========
            log.info("Đang lấy danh sách lớp học của học sinh...");
            
            // Tìm tất cả classroom có chứa studentId này
            List<Classroom> studentClassrooms = classroomRepository.findByStudentIdsContaining(studentId);
            
            log.info("Học sinh tham gia {} lớp học", studentClassrooms.size());

            // ========== 4. Lấy bài tập gần đây của các lớp học ==========
            List<String> classroomIds = studentClassrooms.stream()
                    .map(Classroom::getId)
                    .collect(Collectors.toList());
            
            List<Assignment> recentAssignments = new ArrayList<>();
            if (!classroomIds.isEmpty()) {
                log.info("Đang lấy bài tập từ các lớp...");
                recentAssignments = assignmentRepository.findByClassroomIdInOrderByCreatedAtDesc(
                        classroomIds,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
                        .stream()
                        .limit(5) // Chỉ lấy 5 bài tập gần nhất
                        .collect(Collectors.toList());
                log.info("Có {} bài tập gần đây", recentAssignments.size());
            } else {
                log.info("Học sinh chưa tham gia lớp nào");
            }

            // ========== 5. Truyền dữ liệu ra view ==========
            // Thông tin user
            model.addAttribute("username", currentUser.getFullName() != null ? 
                currentUser.getFullName() : currentUser.getEmail());
            model.addAttribute("userEmail", currentUser.getEmail());
            model.addAttribute("avatarUrl", currentUser.getAvatarUrl());
            model.addAttribute("role", "STUDENT");

            // Thống kê
            model.addAttribute("totalLessons", totalLessons);
            model.addAttribute("completedLessons", completedLessons);
            model.addAttribute("ongoingLessons", ongoingLessons);
            model.addAttribute("dayStreak", 7); // Hardcode tạm thời (có thể lấy từ tracking)
            
            // Danh sách Unit và Lesson
            model.addAttribute("units", allUnits);
            
            // Danh sách lớp học
            model.addAttribute("classrooms", studentClassrooms);
            model.addAttribute("classroomCount", studentClassrooms.size());
            
            // Bài tập gần đây
            model.addAttribute("recentAssignments", recentAssignments);
            model.addAttribute("assignmentCount", recentAssignments.size());

            log.info("========== Dashboard dữ liệu đã sẵn sàng ==========");

        } catch (Exception e) {
            log.error("Lỗi khi tải student dashboard: {}", e.getMessage(), e);
            // Thiết lập dữ liệu mặc định nếu có lỗi
            model.addAttribute("username", "Học sinh");
            model.addAttribute("role", "STUDENT");
            model.addAttribute("totalLessons", 0);
            model.addAttribute("completedLessons", 0);
            model.addAttribute("ongoingLessons", 0);
            model.addAttribute("dayStreak", 0);
            model.addAttribute("units", new ArrayList<>());
            model.addAttribute("classrooms", new ArrayList<>());
            model.addAttribute("recentAssignments", new ArrayList<>());
        }

        return "student/dashboard";
    }

    /**
     * Dashboard dành cho Giáo viên
     * GET /teacher/dashboard
     * 
     * Hiển thị:
     * - Danh sách lớp học
     * - Học sinh trong lớp
     * - Quản lý bài tập
     * - Thống kê kết quả học tập
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return teacher dashboard template
     */
    @GetMapping("/teacher")
    public String teacherDashboard(Model model) {
        log.info("Đang tải teacher dashboard");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user != null) {
                model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getEmail());
                model.addAttribute("role", "TEACHER");

                String teacherId = user.getId();

                // Lấy danh sách lớp (DTO)
                var classrooms = classroomService.getClassroomsByTeacher(teacherId);
                model.addAttribute("classrooms", classrooms);
                model.addAttribute("totalClasses", classrooms.size());

                // Đếm tổng học sinh (từ entity, lấy studentIds)
                var classroomEntities = classroomRepository.findByTeacherIdOrderByNameAsc(teacherId);
                java.util.Set<String> studentIdSet = new java.util.HashSet<>();
                for (var cls : classroomEntities) {
                    if (cls.getStudentIds() != null) {
                        studentIdSet.addAll(cls.getStudentIds());
                    }
                }
                model.addAttribute("totalStudents", studentIdSet.size());

                // Lấy bài tập
                List<AssignmentDTO.Response> assignments = assignmentService.getAssignmentsByTeacher(teacherId);
                model.addAttribute("totalAssignments", assignments.size());

                // Pending count
                long pendingCount = assignments.stream()
                        .mapToInt(a -> a.getPendingCount() != null ? a.getPendingCount() : 0).sum();
                model.addAttribute("pendingCount", pendingCount);

                // Bài tập gần đây (tối đa 5)
                var recentAssignments = assignments.stream()
                        .limit(5)
                        .collect(java.util.stream.Collectors.toList());
                model.addAttribute("recentAssignments", recentAssignments);
            } else {
                setDefaultDashboard(model);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải teacher dashboard: {}", e.getMessage(), e);
            setDefaultDashboard(model);
        }

        return "teacher/dashboard";
    }

    private void setDefaultDashboard(Model model) {
        model.addAttribute("username", "Teacher");
        model.addAttribute("role", "TEACHER");
        model.addAttribute("classrooms", List.of());
        model.addAttribute("totalClasses", 0);
        model.addAttribute("totalStudents", 0);
        model.addAttribute("totalAssignments", 0);
        model.addAttribute("pendingCount", 0);
        model.addAttribute("recentAssignments", List.of());
    }

    /**
     * Dashboard dành cho Admin
     * GET /admin/dashboard
     * 
     * Hiển thị:
     * - Quản lý người dùng
     * - Thống kê hệ thống
     * - Cấu hình ứng dụng
     * - Logs và monitoring
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return admin dashboard template
     */
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        log.info("Đang tải admin dashboard");

        // Lấy thông tin user đang đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Truyền dữ liệu ra view
        model.addAttribute("username", username);
        model.addAttribute("role", "ADMIN");

        return "admin/dashboard";
    }

    /**
     * Trang tiến độ học tập của học sinh
     * GET /dashboard/student/progress
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return progress page template
     */
    @GetMapping("/student/progress")
    public String studentProgress(Model model) {
        log.info("Đang tải student progress page");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user != null) {
                String displayName = user.getFullName() != null ? user.getFullName() : user.getEmail();
                model.addAttribute("username", displayName);
                model.addAttribute("role", "STUDENT");

                // Learning stats từ User entity
                long totalXP = user.getTotalXP() != null ? user.getTotalXP() : 0L;
                int currentStreak = user.getCurrentStreak() != null ? user.getCurrentStreak() : 0;
                int longestStreak = user.getLongestStreak() != null ? user.getLongestStreak() : 0;
                int totalMinutes = user.getTotalLearningMinutes() != null ? user.getTotalLearningMinutes() : 0;
                String level = user.getLevel() != null ? user.getLevel() : "Beginner";

                model.addAttribute("totalXP", totalXP);
                model.addAttribute("currentStreak", currentStreak);
                model.addAttribute("longestStreak", longestStreak);
                model.addAttribute("totalMinutes", totalMinutes);
                model.addAttribute("totalHours", String.format("%.1f", totalMinutes / 60.0));
                model.addAttribute("level", level);

                // Level config
                long nextLevelXP;
                long currentLevelXP;
                String nextLevel;
                if (totalXP < 500) {
                    currentLevelXP = 0; nextLevelXP = 500; nextLevel = "Elementary";
                } else if (totalXP < 1500) {
                    currentLevelXP = 500; nextLevelXP = 1500; nextLevel = "Pre-Intermediate";
                } else if (totalXP < 3500) {
                    currentLevelXP = 1500; nextLevelXP = 3500; nextLevel = "Intermediate";
                } else if (totalXP < 7000) {
                    currentLevelXP = 3500; nextLevelXP = 7000; nextLevel = "Upper-Intermediate";
                } else {
                    currentLevelXP = 7000; nextLevelXP = 10000; nextLevel = "Advanced";
                }
                long range = nextLevelXP - currentLevelXP;
                long progress = Math.min(totalXP - currentLevelXP, range);
                int xpPercent = range > 0 ? (int) (progress * 100 / range) : 100;
                model.addAttribute("nextLevel", nextLevel);
                model.addAttribute("nextLevelXP", nextLevelXP);
                model.addAttribute("xpPercent", xpPercent);

                // Classes
                String studentId = user.getId();
                var myClasses = classroomRepository.findByStudentIdsContaining(studentId);
                model.addAttribute("totalClasses", myClasses.size());

                // Assignments
                if (!myClasses.isEmpty()) {
                    java.util.List<String> classroomIds = myClasses.stream()
                            .map(c -> c.getId()).collect(java.util.stream.Collectors.toList());
                    List<Assignment> assignments = assignmentRepository.findByClassroomIdInOrderByCreatedAtDesc(
                            classroomIds,
                            Sort.by(Sort.Direction.DESC, "createdAt")
                    );
                    model.addAttribute("totalAssignments", assignments.size());
                    long completedAssignments = assignments.stream()
                            .filter(a -> "COMPLETED".equals(a.getStatus()) || "GRADED".equals(a.getStatus()))
                            .count();
                    model.addAttribute("completedAssignments", completedAssignments);
                    int completionRate = assignments.size() > 0
                            ? (int) (completedAssignments * 100 / assignments.size()) : 0;
                    model.addAttribute("completionRate", completionRate);
                } else {
                    model.addAttribute("totalAssignments", 0);
                    model.addAttribute("completedAssignments", 0);
                    model.addAttribute("completionRate", 0);
                }

                // Member since
                if (user.getCreatedAt() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/yyyy");
                    model.addAttribute("memberSince", sdf.format(new java.util.Date(user.getCreatedAt())));
                } else {
                    model.addAttribute("memberSince", "N/A");
                }
            } else {
                setDefaultStudentProgress(model);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải student progress: {}", e.getMessage(), e);
            setDefaultStudentProgress(model);
        }

        return "student/progress";
    }

    private void setDefaultStudentProgress(Model model) {
        model.addAttribute("username", "Student");
        model.addAttribute("role", "STUDENT");
        model.addAttribute("totalXP", 0L);
        model.addAttribute("currentStreak", 0);
        model.addAttribute("longestStreak", 0);
        model.addAttribute("totalMinutes", 0);
        model.addAttribute("totalHours", "0.0");
        model.addAttribute("level", "Beginner");
        model.addAttribute("nextLevel", "Elementary");
        model.addAttribute("nextLevelXP", 500L);
        model.addAttribute("xpPercent", 0);
        model.addAttribute("totalClasses", 0);
        model.addAttribute("totalAssignments", 0);
        model.addAttribute("completedAssignments", 0L);
        model.addAttribute("completionRate", 0);
        model.addAttribute("memberSince", "N/A");
    }

    /**
     * Trang lớp học của học sinh
     * GET /dashboard/student/classes
     */
    @GetMapping("/student/classes")
    public String studentClasses(Model model) {
        log.info("Đang tải student classes page");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user != null) {
                model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getEmail());
                model.addAttribute("role", "STUDENT");

                String studentId = user.getId();
                java.util.List<ClassroomDTO.Response> myClasses = classroomService.getClassroomsByStudent(studentId);
                model.addAttribute("myClasses", myClasses);
                model.addAttribute("totalClasses", myClasses.size());
            } else {
                model.addAttribute("username", "Student");
                model.addAttribute("role", "STUDENT");
                model.addAttribute("myClasses", List.of());
                model.addAttribute("totalClasses", 0);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải student classes: {}", e.getMessage(), e);
            model.addAttribute("username", "Student");
            model.addAttribute("role", "STUDENT");
            model.addAttribute("myClasses", List.of());
            model.addAttribute("totalClasses", 0);
        }

        return "student/classes";
    }

    /**
     * Trang bài tập của học sinh
     * GET /dashboard/student/exercises
     *
     * @param model - Model để truyền dữ liệu ra view
     * @return exercises page template
     */
    @GetMapping("/student/exercises")
    public String studentExercises(Model model) {
        log.info("Đang tải student exercises page");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user != null) {
                model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getEmail());
                model.addAttribute("role", "STUDENT");

                String studentId = user.getId();

                // Tìm tất cả lớp mà học sinh tham gia
                var myClassrooms = classroomRepository.findByStudentIdsContaining(studentId);
                model.addAttribute("myClassrooms", myClassrooms);

                // Tạo map classroomId -> classroomName
                java.util.Map<String, String> classNameMap = new java.util.HashMap<>();
                java.util.List<String> classroomIds = new java.util.ArrayList<>();
                for (var cls : myClassrooms) {
                    classNameMap.put(cls.getId(), cls.getName());
                    classroomIds.add(cls.getId());
                }

                // Lấy bài tập từ tất cả các lớp
                java.util.List<java.util.Map<String, Object>> exerciseList = new java.util.ArrayList<>();
                if (!classroomIds.isEmpty()) {
                    List<Assignment> assignments = assignmentRepository
                            .findByClassroomIdInOrderByCreatedAtDesc(
                                    classroomIds,
                                    Sort.by(Sort.Direction.DESC, "createdAt")
                            );
                    for (Assignment a : assignments) {
                        java.util.Map<String, Object> info = new java.util.LinkedHashMap<>();
                        info.put("id", a.getId());
                        info.put("title", a.getTitle());
                        info.put("description", a.getDescription());
                        info.put("type", a.getType());
                        info.put("typeDisplay", a.getTypeDisplay());
                        info.put("classroomId", a.getClassroomId());
                        info.put("classroomName", classNameMap.getOrDefault(a.getClassroomId(), ""));
                        
                        // ========== CHECK STUDENT SUBMISSION STATUS ==========
                        // Instead of using assignment status, check if student submitted
                        var submission = assignmentSubmissionRepository.findByAssignmentIdAndStudentId(a.getId(), studentId);
                        String studentSubmissionStatus = "NOT_SUBMITTED";
                        if (submission.isPresent()) {
                            studentSubmissionStatus = submission.get().getStatus(); // SUBMITTED, GRADED, etc.
                        }
                        info.put("status", studentSubmissionStatus);
                        
                        info.put("dueDate", a.getDueDate());
                        // Format due date
                        if (a.getDueDate() != null) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                            info.put("dueDateDisplay", sdf.format(new java.util.Date(a.getDueDate())));
                            // Check if overdue
                            info.put("isOverdue", a.getDueDate() < System.currentTimeMillis());
                        } else {
                            info.put("dueDateDisplay", "Không giới hạn");
                            info.put("isOverdue", false);
                        }
                        // Icon và color theo type
                        String icon = "solar:document-text-bold";
                        String colorClass = "bg-blue-50 text-blue-600";
                        if ("LISTENING".equalsIgnoreCase(a.getType())) {
                            icon = "solar:headphones-round-sound-bold"; colorClass = "bg-amber-50 text-amber-600";
                        } else if ("SPEAKING".equalsIgnoreCase(a.getType())) {
                            icon = "solar:microphone-2-bold"; colorClass = "bg-purple-50 text-purple-600";
                        } else if ("WRITING".equalsIgnoreCase(a.getType())) {
                            icon = "solar:pen-new-square-bold"; colorClass = "bg-blue-50 text-blue-600";
                        } else if ("READING".equalsIgnoreCase(a.getType())) {
                            icon = "solar:document-text-bold"; colorClass = "bg-red-50 text-red-600";
                        } else if ("GRAMMAR".equalsIgnoreCase(a.getType())) {
                            icon = "solar:pen-new-square-bold"; colorClass = "bg-emerald-50 text-emerald-600";
                        } else if ("VOCABULARY".equalsIgnoreCase(a.getType())) {
                            icon = "solar:book-bookmark-bold"; colorClass = "bg-indigo-50 text-indigo-600";
                        }
                        info.put("icon", icon);
                        info.put("colorClass", colorClass);
                        exerciseList.add(info);
                    }
                }

                model.addAttribute("exercises", exerciseList);
                model.addAttribute("totalExercises", exerciseList.size());

                // Thống kê - based on student submission status
                long pendingExercises = exerciseList.stream()
                        .filter(e -> "NOT_SUBMITTED".equals(e.get("status")))
                        .count();
                long completedExercises = exerciseList.stream()
                        .filter(e -> "GRADED".equals(e.get("status")) || "SUBMITTED".equals(e.get("status")))
                        .count();
                long overdueExercises = exerciseList.stream()
                        .filter(e -> Boolean.TRUE.equals(e.get("isOverdue")) && "NOT_SUBMITTED".equals(e.get("status")))
                        .count();
                model.addAttribute("pendingExercises", pendingExercises);
                model.addAttribute("completedExercises", completedExercises);
                model.addAttribute("overdueExercises", overdueExercises);
            } else {
                setDefaultStudentExercises(model);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải student exercises: {}", e.getMessage(), e);
            setDefaultStudentExercises(model);
        }

        return "student/exercises";
    }

    private void setDefaultStudentExercises(Model model) {
        model.addAttribute("username", "Student");
        model.addAttribute("role", "STUDENT");
        model.addAttribute("exercises", List.of());
        model.addAttribute("myClassrooms", List.of());
        model.addAttribute("totalExercises", 0);
        model.addAttribute("pendingExercises", 0);
        model.addAttribute("completedExercises", 0);
        model.addAttribute("overdueExercises", 0);
    }

    /**
     * Trang tạo bài tập mới của giáo viên
     * GET /dashboard/teacher/create-assignment
     *
     * Cung cấp dữ liệu cho form:
     * - Danh sách Unit để giáo viên chọn nội dung bài tập
     * - Danh sách lớp học của giáo viên để chọn lớp giao bài
     *
     * @param model - Model để truyền dữ liệu ra view
     * @return trang tạo bài tập (teacher/create-assignment.html)
     */
    @GetMapping("/teacher/create-assignment")
    public String teacherCreateAssignment(Model model) {
        log.info("Đang tải trang tạo bài tập");

        try {
            // Lấy thông tin giáo viên đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            // Thiết lập thông tin cơ bản của user vào model
            String username = (user != null && user.getFullName() != null)
                    ? user.getFullName() : email;
            model.addAttribute("username", username);
            model.addAttribute("role", "TEACHER");

            // Lấy danh sách Unit active để đổ vào dropdown chọn unit
            List<com.english12smart.dto.ContentDTO.UnitResponse> units = contentService.getAllActiveUnits();
            model.addAttribute("units", units);

            // Lấy danh sách lớp học của giáo viên để đổ vào danh sách chọn lớp giao bài
            String teacherId = (user != null) ? user.getId() : email;
            List<ClassroomDTO.Response> classes = classroomService.getClassroomsByTeacher(teacherId);
            model.addAttribute("classes", classes);

            log.info("Tải trang tạo bài tập thành công: {} units, {} lớp", units.size(), classes.size());

        } catch (Exception e) {
            log.error("Lỗi khi tải trang tạo bài tập: {}", e.getMessage(), e);
            // Đặt dữ liệu mặc định để tránh lỗi render template
            model.addAttribute("username", "Teacher");
            model.addAttribute("role", "TEACHER");
            model.addAttribute("units", List.of());
            model.addAttribute("classes", List.of());
        }

        return "teacher/create-assignment";
    }

    /**
     * Trang quản lý lớp học của giáo viên
     * GET /dashboard/teacher/classes
     *
     * @param model - Model để truyền dữ liệu ra view
     * @return teacher classes page template
     */
    @GetMapping("/teacher/classes")
    public String teacherClasses(Model model) {
        log.info("Đang tải teacher classes page");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Lấy thông tin user để có ID
            User user = userRepository.findByEmail(email);
            String teacherId = (user != null) ? user.getId() : email;
            String username = (user != null && user.getFullName() != null && !user.getFullName().isEmpty())
                    ? user.getFullName() : email;

            // Lấy danh sách lớp học
            List<ClassroomDTO.Response> classrooms = classroomService.getClassroomsByTeacher(teacherId);

            model.addAttribute("username", username);
            model.addAttribute("role", "TEACHER");
            model.addAttribute("classrooms", classrooms != null ? classrooms : List.of());
            model.addAttribute("totalClasses", classrooms != null ? classrooms.size() : 0);
            model.addAttribute("activeClasses",
                    classrooms != null
                            ? classrooms.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count()
                            : 0);

            return "teacher/classes";
        } catch (Exception e) {
            log.error("Lỗi khi tải teacher classes page: ", e);
            model.addAttribute("username", "Teacher");
            model.addAttribute("role", "TEACHER");
            model.addAttribute("classrooms", List.of());
            model.addAttribute("totalClasses", 0);
            model.addAttribute("activeClasses", 0);
            return "teacher/classes";
        }
    }

    /**
     * Trang quản lý học sinh của giáo viên
     * GET /dashboard/teacher/students
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return teacher students page template
     */
    @GetMapping("/teacher/students")
    public String teacherStudents(Model model) {
        log.info("Đang tải teacher students page");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user != null) {
                model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getEmail());
                model.addAttribute("role", "TEACHER");

                String teacherId = user.getId();

                // Lấy tất cả lớp của giáo viên
                var classrooms = classroomService.getClassroomsByTeacher(teacherId);
                model.addAttribute("classrooms", classrooms);

                // Thu thập tất cả studentIds từ các lớp (cùng với tên lớp)
                java.util.Map<String, String> studentClassMap = new java.util.LinkedHashMap<>();
                var classroomEntities = classroomRepository.findByTeacherIdOrderByNameAsc(teacherId);
                for (var cls : classroomEntities) {
                    if (cls.getStudentIds() != null) {
                        for (String sid : cls.getStudentIds()) {
                            studentClassMap.put(sid, cls.getName());
                        }
                    }
                }

                // Lấy thông tin user của các học sinh
                java.util.List<java.util.Map<String, Object>> studentList = new java.util.ArrayList<>();
                if (!studentClassMap.isEmpty()) {
                    var students = userRepository.findByIdIn(new java.util.ArrayList<>(studentClassMap.keySet()));
                    for (var s : students) {
                        java.util.Map<String, Object> info = new java.util.LinkedHashMap<>();
                        info.put("id", s.getId());
                        info.put("fullName", s.getFullName() != null ? s.getFullName() : s.getEmail());
                        info.put("email", s.getEmail());
                        info.put("avatarUrl", s.getAvatarUrl());
                        info.put("classroomName", studentClassMap.getOrDefault(s.getId(), ""));
                        info.put("isActive", s.getIsActive() != null ? s.getIsActive() : true);
                        info.put("totalXP", s.getTotalXP() != null ? s.getTotalXP() : 0);
                        info.put("level", s.getLevel() != null ? s.getLevel() : "Beginner");
                        info.put("currentStreak", s.getCurrentStreak() != null ? s.getCurrentStreak() : 0);
                        info.put("totalLearningMinutes", s.getTotalLearningMinutes() != null ? s.getTotalLearningMinutes() : 0);
                        // Initials cho avatar
                        String fn = s.getFullName() != null ? s.getFullName() : s.getEmail();
                        String[] parts = fn.split("\\s+");
                        String initials = parts.length >= 2
                                ? ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase()
                                : fn.substring(0, Math.min(2, fn.length())).toUpperCase();
                        info.put("initials", initials);
                        studentList.add(info);
                    }
                }

                model.addAttribute("students", studentList);
                model.addAttribute("totalStudents", studentList.size());
                long activeStudents = studentList.stream()
                        .filter(s -> Boolean.TRUE.equals(s.get("isActive"))).count();
                model.addAttribute("activeStudents", activeStudents);
            } else {
                model.addAttribute("username", "Teacher");
                model.addAttribute("role", "TEACHER");
                model.addAttribute("classrooms", List.of());
                model.addAttribute("students", List.of());
                model.addAttribute("totalStudents", 0);
                model.addAttribute("activeStudents", 0);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải students page: {}", e.getMessage(), e);
            model.addAttribute("username", "Teacher");
            model.addAttribute("role", "TEACHER");
            model.addAttribute("classrooms", List.of());
            model.addAttribute("students", List.of());
            model.addAttribute("totalStudents", 0);
            model.addAttribute("activeStudents", 0);
        }

        return "teacher/students";
    }

    /**
     * Trang quản lý bài tập của giáo viên
     * GET /dashboard/teacher/assignments
     * 
     * @param model - Model để truyền dữ liệu ra view
     * @return teacher assignments page template
     */
    @GetMapping("/teacher/assignments")
    public String teacherAssignments(Model model) {
        log.info("Đang tải teacher assignments page");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user != null) {
                model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getEmail());
                model.addAttribute("role", "TEACHER");

                String teacherId = user.getId();

                // Lấy danh sách bài tập
                List<AssignmentDTO.Response> assignments = assignmentService.getAssignmentsByTeacher(teacherId);
                model.addAttribute("assignments", assignments);

                // Lấy danh sách lớp học cho dropdown
                var classrooms = classroomService.getClassroomsByTeacher(teacherId);
                model.addAttribute("classrooms", classrooms);

                // Thống kê
                long totalAssignments = assignments.size();
                long pendingCount = assignments.stream()
                        .mapToInt(a -> a.getPendingCount() != null ? a.getPendingCount() : 0).sum();
                long gradedCount = assignments.stream()
                        .mapToInt(a -> a.getGradedCount() != null ? a.getGradedCount() : 0).sum();
                double avgScore = assignments.stream()
                        .filter(a -> a.getGradedCount() != null && a.getGradedCount() > 0)
                        .mapToDouble(a -> a.getAverageScore() != null ? a.getAverageScore() : 0)
                        .average()
                        .orElse(0.0);

                model.addAttribute("totalAssignments", totalAssignments);
                model.addAttribute("pendingCount", pendingCount);
                model.addAttribute("gradedCount", gradedCount);
                model.addAttribute("avgScore", String.format("%.1f", avgScore));
            } else {
                model.addAttribute("username", "Teacher");
                model.addAttribute("role", "TEACHER");
                model.addAttribute("assignments", List.of());
                model.addAttribute("classrooms", List.of());
                model.addAttribute("totalAssignments", 0);
                model.addAttribute("pendingCount", 0);
                model.addAttribute("gradedCount", 0);
                model.addAttribute("avgScore", "0.0");
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải assignments page: {}", e.getMessage(), e);
            model.addAttribute("username", "Teacher");
            model.addAttribute("role", "TEACHER");
            model.addAttribute("assignments", List.of());
            model.addAttribute("classrooms", List.of());
            model.addAttribute("totalAssignments", 0);
            model.addAttribute("pendingCount", 0);
            model.addAttribute("gradedCount", 0);
            model.addAttribute("avgScore", "0.0");
        }

        return "teacher/assignments";
    }

    // ======================================================================
    // Các route mới: Tính năng đề thi (Exam)
    // ======================================================================

    /**
     * Trang danh sách đề thi của giáo viên
     * GET /dashboard/teacher/exams
     *
     * Hiển thị tất cả đề thi giáo viên đã tạo với thống kê tổng quát
     *
     * @param model - Model để truyền dữ liệu ra view
     * @return trang danh sách đề thi (teacher/exams.html)
     */
    @GetMapping("/teacher/exams")
    public String teacherExams(Model model) {
        log.info("Đang tải trang danh sách đề thi của giáo viên");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user != null) {
                model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getEmail());
                model.addAttribute("role", "TEACHER");

                String teacherId = user.getId();

                // Lấy tất cả đề thi của giáo viên, sắp xếp mới nhất trước
                List<com.english12smart.entity.Exam> exams = examRepository
                        .findByTeacherIdOrderByCreatedAtDesc(teacherId);
                model.addAttribute("exams", exams);

                // Thống kê cho cards phía trên
                long totalExams = exams.size();
                long activeExams = exams.stream()
                        .filter(e -> "ACTIVE".equals(e.getStatus())).count();
                long totalSubmissions = exams.stream()
                        .mapToLong(e -> e.getSubmittedCount() != null ? e.getSubmittedCount() : 0)
                        .sum();

                model.addAttribute("totalExams", totalExams);
                model.addAttribute("activeExams", activeExams);
                model.addAttribute("totalSubmissions", totalSubmissions);
            } else {
                setDefaultTeacherExams(model);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải trang đề thi: {}", e.getMessage(), e);
            setDefaultTeacherExams(model);
        }

        return "teacher/exams";
    }

    /**
     * Trang tạo đề thi mới của giáo viên
     * GET /dashboard/teacher/create-exam
     *
     * Cung cấp dữ liệu cho form:
     * - Danh sách lớp học để chọn lớp giao đề
     * - Danh sách Unit để giáo viên duyệt ngân hàng Exercise
     *
     * @param model - Model để truyền dữ liệu ra view
     * @return trang tạo đề thi (teacher/create-exam.html)
     */
    @GetMapping("/teacher/create-exam")
    public String teacherCreateExam(Model model) {
        log.info("Đang tải trang tạo đề thi mới");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            String username = (user != null && user.getFullName() != null)
                    ? user.getFullName() : email;
            model.addAttribute("username", username);
            model.addAttribute("role", "TEACHER");

            // Lấy danh sách lớp học của giáo viên để đổ vào dropdown
            String teacherId = (user != null) ? user.getId() : email;
            List<ClassroomDTO.Response> classes = classroomService.getClassroomsByTeacher(teacherId);
            model.addAttribute("classes", classes);

            // Lấy danh sách Unit active để giáo viên duyệt ngân hàng câu hỏi
            List<ContentDTO.UnitResponse> units = contentService.getAllActiveUnits();
            model.addAttribute("units", units);

            log.info("Tải trang tạo đề thi thành công: {} lớp, {} units", classes.size(), units.size());

        } catch (Exception e) {
            log.error("Lỗi khi tải trang tạo đề thi: {}", e.getMessage(), e);
            // Đặt dữ liệu mặc định khi có lỗi
            model.addAttribute("username", "Teacher");
            model.addAttribute("role", "TEACHER");
            model.addAttribute("classes", List.of());
            model.addAttribute("units", List.of());
        }

        return "teacher/create-exam";
    }

    /**
     * Trang nhập mã PIN và lịch sử thi của học sinh
     * GET /dashboard/student/exams
     *
     * Hiển thị ô nhập PIN và danh sách các bài thi đã làm
     *
     * @param model - Model để truyền dữ liệu ra view
     * @return trang đề thi học sinh (student/exams.html)
     */
    @GetMapping("/student/exams")
    public String studentExams(Model model) {
        log.info("Đang tải trang đề thi của học sinh");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user != null) {
                model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getEmail());
                model.addAttribute("role", "STUDENT");

                // Lấy lịch sử thi của học sinh (các bài đã nộp)
                List<ExamSubmission> history = examSubmissionRepository
                        .findByStudentIdOrderBySubmittedAtDesc(user.getId());
                model.addAttribute("examHistory", history);
                model.addAttribute("totalExams", history.size());
            } else {
                model.addAttribute("username", "Student");
                model.addAttribute("role", "STUDENT");
                model.addAttribute("examHistory", List.of());
                model.addAttribute("totalExams", 0);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải trang đề thi học sinh: {}", e.getMessage(), e);
            model.addAttribute("username", "Student");
            model.addAttribute("role", "STUDENT");
            model.addAttribute("examHistory", List.of());
            model.addAttribute("totalExams", 0);
        }

        return "student/exams";
    }

    /**
     * Trang làm bài thi của học sinh
     * GET /dashboard/student/exam/{examId}
     *
     * Xác thực học sinh có quyền vào thi, rồi load đề thi (ĐÃ ẨN đáp án)
     *
     * @param examId ID đề thi
     * @param model  - Model để truyền dữ liệu ra view
     * @return trang làm bài (student/exam-taking.html) hoặc redirect nếu không hợp lệ
     */
    @GetMapping("/student/exam/{examId}")
    public String studentExamTaking(@PathVariable String examId, Model model) {
        log.info("Đang tải trang làm bài thi, examId: {}", examId);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return "redirect:/auth/login";
            }

            model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getEmail());
            model.addAttribute("role", "STUDENT");

            // Kiểm tra học sinh đã nộp bài chưa (SUBMITTED hoặc GRADED) → redirect sang trang kết quả
            // Nếu là IN_PROGRESS thì cho phép tiếp tục làm bài
            Optional<ExamSubmission> existingSubmission = examSubmissionRepository.findByExamIdAndStudentId(examId, user.getId());
            if (existingSubmission.isPresent()) {
                String status = existingSubmission.get().getStatus();
                if ("SUBMITTED".equals(status) || "GRADED".equals(status)) {
                    log.info("Học sinh {} đã nộp bài thi {}, chuyển sang trang kết quả", user.getId(), examId);
                    return "redirect:/dashboard/student/exam/" + examId + "/result";
                }
                // Nếu IN_PROGRESS thì tiếp tục làm bài
                log.info("Học sinh {} tiếp tục làm bài IN_PROGRESS thi {}", user.getId(), examId);
            }

            // Lấy đề thi và ẨN đáp án đúng trước khi truyền vào view
            Exam exam = examRepository.findById(examId).orElse(null);
            if (exam == null) {
                log.warn("Không tìm thấy đề thi: {}", examId);
                return "redirect:/dashboard/student/exams";
            }

            // Kiểm tra đề thi đang mở
            if (!"ACTIVE".equals(exam.getStatus())) {
                log.warn("Đề thi {} đã đóng, học sinh {} không thể vào thi", examId, user.getId());
                return "redirect:/dashboard/student/exams";
            }

            // Truyền đề thi vào model (KHÔNG có correctAnswer)
            model.addAttribute("exam", exam);
            model.addAttribute("studentId", user.getId());
            model.addAttribute("studentName", user.getFullName() != null ? user.getFullName() : user.getEmail());

        } catch (Exception e) {
            log.error("Lỗi khi tải trang làm bài: {}", e.getMessage(), e);
            return "redirect:/dashboard/student/exams";
        }

        return "student/exam-taking";
    }

    /**
     * Trang xem kết quả bài thi của học sinh
     * GET /dashboard/student/exam/{examId}/result
     *
     * Hiển thị điểm số, phần trăm, đáp án đúng và giải thích từng câu
     *
     * @param examId ID đề thi
     * @param model  - Model để truyền dữ liệu ra view
     * @return trang kết quả (student/exam-result.html) hoặc redirect nếu chưa nộp
     */
    @GetMapping("/student/exam/{examId}/result")
    public String studentExamResult(@PathVariable String examId, Model model) {
        log.info("Đang tải trang kết quả bài thi, examId: {}", examId);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return "redirect:/auth/login";
            }

            model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getEmail());
            model.addAttribute("role", "STUDENT");

            // Lấy kết quả bài làm, bao gồm đáp án đúng và giải thích
            ExamSubmissionDTO.Response result = examService.getStudentResult(examId, user.getId());
            model.addAttribute("result", result);

            // Lấy thông tin đề thi để hiển thị chi tiết từng câu hỏi
            Exam exam = examRepository.findById(examId).orElse(null);
            model.addAttribute("exam", exam);

        } catch (Exception e) {
            log.error("Lỗi khi tải trang kết quả: {}", e.getMessage(), e);
            // Nếu chưa có bài làm → redirect về trang đề thi
            return "redirect:/dashboard/student/exams";
        }

        return "student/exam-result";
    }

    /** Dữ liệu mặc định khi có lỗi load trang đề thi giáo viên */
    private void setDefaultTeacherExams(Model model) {
        model.addAttribute("username", "Teacher");
        model.addAttribute("role", "TEACHER");
        model.addAttribute("exams", List.of());
        model.addAttribute("totalExams", 0L);
        model.addAttribute("activeExams", 0L);
        model.addAttribute("totalSubmissions", 0L);
    }
}
