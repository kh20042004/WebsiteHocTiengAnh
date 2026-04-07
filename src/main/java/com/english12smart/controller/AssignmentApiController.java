package com.english12smart.controller;

import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.dto.AssignmentDTO;
import com.english12smart.entity.User;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentApiController {

    private final AssignmentService assignmentService;
    private final UserRepository userRepository;

    /** POST /api/assignments - Tạo bài tập mới */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<AssignmentDTO.Response>> createAssignment(
            @Valid @RequestBody AssignmentDTO.CreateRequest request) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        return ResponseEntity.ok(
            ApiResponseDTO.success(assignmentService.createAssignment(request, currentUser.getId(), isAdmin)));
    }

    /** GET /api/assignments/{id} - Lấy thông tin bài tập */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AssignmentDTO.Response>> getAssignment(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponseDTO.success(assignmentService.getAssignmentById(id)));
    }

    /** PUT /api/assignments/{id} - Cập nhật bài tập */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<AssignmentDTO.Response>> updateAssignment(
            @PathVariable String id,
            @Valid @RequestBody AssignmentDTO.UpdateRequest request) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        return ResponseEntity.ok(ApiResponseDTO.success(
            assignmentService.updateAssignment(id, request, currentUser.getId(), isAdmin)));
    }

    /** DELETE /api/assignments/{id} - Xóa bài tập */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> deleteAssignment(@PathVariable String id) {
        User currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        assignmentService.deleteAssignment(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponseDTO.success("Xóa bài tập thành công"));
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
