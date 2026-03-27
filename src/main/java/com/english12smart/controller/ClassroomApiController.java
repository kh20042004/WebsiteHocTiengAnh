package com.english12smart.controller;

import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.dto.ClassroomDTO;
import com.english12smart.entity.User;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API cho quản lý Lớp học
 */
@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
@Slf4j
public class ClassroomApiController {

    private final ClassroomService classroomService;
    private final UserRepository userRepository;

    /** GET /api/classrooms - Lấy danh sách lớp của giáo viên hiện tại */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<ClassroomDTO.Response>>> getMyClassrooms(
            @RequestParam(required = false) String status) {
        String teacherId = getCurrentUserId();
        List<ClassroomDTO.Response> classrooms = (status != null && !status.equals("all"))
                ? classroomService.getClassroomsByTeacherAndStatus(teacherId, status.toUpperCase())
                : classroomService.getClassroomsByTeacher(teacherId);
        return ResponseEntity.ok(ApiResponseDTO.success(classrooms));
    }

    /** GET /api/classrooms/{id} - Chi tiết lớp học */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ClassroomDTO.Response>> getClassroom(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponseDTO.success(classroomService.getClassroomById(id)));
    }

    /** POST /api/classrooms - Tạo lớp học mới */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ClassroomDTO.Response>> createClassroom(
            @Valid @RequestBody ClassroomDTO.CreateRequest request) {
        String teacherId = getCurrentUserId();
        ClassroomDTO.Response created = classroomService.createClassroom(request, teacherId);
        return ResponseEntity.ok(ApiResponseDTO.created(created));
    }

    /** PUT /api/classrooms/{id} - Cập nhật lớp học */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ClassroomDTO.Response>> updateClassroom(
            @PathVariable String id,
            @Valid @RequestBody ClassroomDTO.UpdateRequest request) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        return ResponseEntity.ok(ApiResponseDTO.success(
            classroomService.updateClassroom(id, request, currentUser.getId(), isAdmin)));
    }

    /** DELETE /api/classrooms/{id} - Xóa lớp học */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> deleteClassroom(@PathVariable String id) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        classroomService.deleteClassroom(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponseDTO.success("Xóa lớp học thành công"));
    }

    /** POST /api/classrooms/join - Học sinh tham gia lớp bằng mã lớp */
    @PostMapping("/join")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ClassroomDTO.Response>> joinClassroom(
            @RequestBody java.util.Map<String, String> body) {
        String classCode = body.get("classCode");
        if (classCode == null || classCode.isBlank()) {
            throw new com.english12smart.exception.BadRequestException("Vui lòng nhập mã lớp");
        }
        String studentId = getCurrentUserId();
        ClassroomDTO.Response result = classroomService.joinClassroom(classCode.trim().toUpperCase(), studentId);
        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }

    /** GET /api/classrooms/my - Lấy danh sách lớp của học sinh */
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<ClassroomDTO.Response>>> getMyStudentClassrooms() {
        String studentId = getCurrentUserId();
        List<ClassroomDTO.Response> classrooms = classroomService.getClassroomsByStudent(studentId);
        return ResponseEntity.ok(ApiResponseDTO.success(classrooms));
    }

    // ---- Helper ----

    private String getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new com.english12smart.exception.ResourceNotFoundException("Không tìm thấy người dùng");
        }
        return user;
    }
}
