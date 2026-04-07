package com.english12smart.controller;

import com.english12smart.dto.AdminDTO;
import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.entity.User;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminApiController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDTO<AdminDTO.DashboardStats>> getDashboardStats() {
        AdminDTO.DashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy thống kê hệ thống thành công", stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponseDTO<AdminDTO.UserListResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword) {
        AdminDTO.UserListResponse users = adminService.getUsers(role, isActive, keyword);
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách người dùng thành công", users));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponseDTO<AdminDTO.UserSummary>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody AdminDTO.UpdateUserStatusRequest request) {
        String adminId = getCurrentUserId();
        AdminDTO.UserSummary user = adminService.updateUserStatus(userId, request.getIsActive(), adminId);
        return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật trạng thái người dùng thành công", user));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponseDTO<AdminDTO.UserSummary>> updateUserRole(
            @PathVariable String userId,
            @RequestBody AdminDTO.UpdateUserRoleRequest request) {
        String adminId = getCurrentUserId();
        AdminDTO.UserSummary user = adminService.updateUserRole(userId, request.getRole(), adminId);
        return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật quyền người dùng thành công", user));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable String userId) {
        String adminId = getCurrentUserId();
        adminService.deleteUser(userId, adminId);
        return ResponseEntity.ok(ApiResponseDTO.success("Xóa người dùng thành công", null));
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email);
        }
        log.debug("Admin thao tác: {} ({})", user.getEmail(), user.getId());
        return user.getId();
    }
}