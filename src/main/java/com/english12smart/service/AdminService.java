package com.english12smart.service;

import com.english12smart.constant.UserRole;
import com.english12smart.dto.AdminDTO;
import com.english12smart.entity.User;
import com.english12smart.exception.BadRequestException;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.AssignmentRepository;
import com.english12smart.repository.ClassroomRepository;
import com.english12smart.repository.ExamRepository;
import com.english12smart.repository.ExamSubmissionRepository;
import com.english12smart.repository.UnitRepository;
import com.english12smart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final UnitRepository unitRepository;
    private final ClassroomRepository classroomRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;

    public AdminDTO.UserListResponse getUsers(String role, Boolean isActive, String keyword) {
        String normalizedRole = normalizeRole(role);
        String normalizedKeyword = keyword != null ? keyword.trim().toLowerCase(Locale.ROOT) : null;

        List<AdminDTO.UserSummary> users = userRepository.findAll().stream()
                .filter(user -> normalizedRole == null || normalizedRole.equalsIgnoreCase(user.getRole()))
                .filter(user -> isActive == null || isActive.equals(Boolean.TRUE.equals(user.getIsActive())))
                .filter(user -> {
                    if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
                        return true;
                    }
                    String email = user.getEmail() != null ? user.getEmail().toLowerCase(Locale.ROOT) : "";
                    String fullName = user.getFullName() != null ? user.getFullName().toLowerCase(Locale.ROOT) : "";
                    return email.contains(normalizedKeyword) || fullName.contains(normalizedKeyword);
                })
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Long::compareTo)).reversed())
                .map(this::toUserSummary)
                .collect(Collectors.toList());

        return AdminDTO.UserListResponse.builder()
                .total(users.size())
                .users(users)
                .build();
    }

    public AdminDTO.UserSummary updateUserStatus(String targetUserId, Boolean isActive, String currentAdminId) {
        if (isActive == null) {
            throw new BadRequestException("Trường isActive là bắt buộc");
        }
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + targetUserId));

        if (targetUserId.equals(currentAdminId) && !isActive) {
            throw new BadRequestException("Không thể tự khóa tài khoản admin hiện tại");
        }

        user.setIsActive(isActive);
        user.setUpdatedAt(System.currentTimeMillis());
        User saved = userRepository.save(user);
        log.info("Admin {} cập nhật trạng thái user {} -> {}", currentAdminId, targetUserId, isActive);
        return toUserSummary(saved);
    }

    public AdminDTO.UserSummary updateUserRole(String targetUserId, String role, String currentAdminId) {
        String normalizedRole = normalizeRole(role);
        if (normalizedRole == null) {
            throw new BadRequestException("Role không hợp lệ. Chỉ hỗ trợ: ADMIN, TEACHER, STUDENT");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + targetUserId));

        if (targetUserId.equals(currentAdminId) && !"ADMIN".equals(normalizedRole)) {
            throw new BadRequestException("Không thể tự hạ quyền admin hiện tại");
        }

        user.setRole(normalizedRole);
        user.setUpdatedAt(System.currentTimeMillis());
        User saved = userRepository.save(user);
        log.info("Admin {} cập nhật role user {} -> {}", currentAdminId, targetUserId, normalizedRole);
        return toUserSummary(saved);
    }

    public AdminDTO.DashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long adminUsers = userRepository.countByRole("ADMIN");
        long teacherUsers = userRepository.countByRole("TEACHER");
        long studentUsers = userRepository.countByRole("STUDENT");

        long sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
        long newUsersLast7Days = userRepository.countRecentUsers(sevenDaysAgo);

        AdminDTO.UserStats userStats = AdminDTO.UserStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(Math.max(0, totalUsers - activeUsers))
                .adminUsers(adminUsers)
                .teacherUsers(teacherUsers)
                .studentUsers(studentUsers)
                .newUsersLast7Days(newUsersLast7Days)
                .build();

        AdminDTO.ContentStats contentStats = AdminDTO.ContentStats.builder()
                .totalUnits(unitRepository.count())
                .activeUnits(unitRepository.countByIsActiveTrue())
                .totalClassrooms(classroomRepository.count())
                .totalAssignments(assignmentRepository.count())
                .totalExams(examRepository.count())
                .totalExamSubmissions(examSubmissionRepository.count())
                .build();

        return AdminDTO.DashboardStats.builder()
                .users(userStats)
                .content(contentStats)
                .build();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        try {
            UserRole.valueOf(normalized);
            return normalized;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private AdminDTO.UserSummary toUserSummary(User user) {
        return AdminDTO.UserSummary.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .totalXP(user.getTotalXP())
                .level(user.getLevel())
                .build();
    }
}