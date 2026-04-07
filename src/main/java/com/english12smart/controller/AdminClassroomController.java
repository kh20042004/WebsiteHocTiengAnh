package com.english12smart.controller;

import com.english12smart.dto.AdminActivityReportDTO;
import com.english12smart.dto.AdminClassroomDTO;
import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.entity.AdminActivityLog;
import com.english12smart.entity.User;
import com.english12smart.exception.BadRequestException;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.UserRepository;
import com.english12smart.service.AdminActivityLogService;
import com.english12smart.service.AdminClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/classrooms")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminClassroomController {

    private final AdminClassroomService adminClassroomService;
    private final AdminActivityLogService activityLogService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<AdminClassroomDTO.ClassroomListResponse>> listClassrooms(
            @RequestParam(required = false) String teacherId,
            @RequestParam(required = false) String teacherKeyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {

        AdminClassroomDTO.ClassroomFilter filter = AdminClassroomDTO.ClassroomFilter.builder()
                .teacherId(teacherId)
                .teacherKeyword(teacherKeyword)
                .status(status)
                .grade(grade)
                .keyword(keyword)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .build()
                .withPagination(page != null ? page : 0, size != null ? size : 10);

        AdminClassroomDTO.ClassroomListResponse response = adminClassroomService.listClassrooms(filter);
        return ResponseEntity.ok(ApiResponseDTO.success("Danh sách lớp học", response));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDTO<AdminClassroomDTO.ClassroomStats>> getStats() {
        AdminClassroomDTO.ClassroomStats stats = adminClassroomService.getClassroomStats();
        return ResponseEntity.ok(ApiResponseDTO.success("Thống kê lớp học", stats));
    }

    @PutMapping("/{classroomId}/status")
    public ResponseEntity<ApiResponseDTO<AdminClassroomDTO.ClassroomSummary>> updateStatus(
            @PathVariable String classroomId,
            @RequestBody AdminClassroomDTO.ClassroomStatusUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Body request là bắt buộc");
        }
        AdminClassroomDTO.ClassroomSummary summary = adminClassroomService
                .updateClassroomStatus(classroomId, request, getCurrentAdminId());
        return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật trạng thái lớp học thành công", summary));
    }

    @DeleteMapping("/{classroomId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteClassroom(@PathVariable String classroomId) {
        adminClassroomService.deleteClassroom(classroomId, getCurrentAdminId());
        return ResponseEntity.ok(ApiResponseDTO.success("Xóa lớp học thành công", null));
    }

        @GetMapping("/activities")
        public ResponseEntity<ApiResponseDTO<AdminActivityReportDTO>> listActivities(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        int pageIndex = Math.max(page != null ? page : 0, 0);
        int pageSize = Math.max(size != null ? size : 10, 1);
        var pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        var logsPage = activityLogService.getRecentActivities(pageable);
        List<AdminActivityReportDTO.ActivityEntry> entries = integrateAdminDetails(logsPage.getContent()).stream()
            .map(entry -> {
                AdminActivityLog log = entry.log();
                return AdminActivityReportDTO.ActivityEntry.builder()
                    .id(log.getId())
                    .adminId(log.getAdminId())
                    .adminName(entry.adminName())
                    .adminEmail(entry.adminEmail())
                    .action(log.getAction())
                    .targetType(log.getTargetType())
                    .targetId(log.getTargetId())
                    .metadata(log.getMetadata() == null ? Map.of() : log.getMetadata())
                    .createdAt(log.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        AdminActivityReportDTO response = AdminActivityReportDTO.builder()
            .items(entries)
            .page(logsPage.getNumber())
            .size(logsPage.getSize())
            .total(logsPage.getTotalElements())
            .build();
        return ResponseEntity.ok(ApiResponseDTO.success("Danh sách hoạt động admin", response));
        }

        @GetMapping("/activities/summary")
        public ResponseEntity<ApiResponseDTO<List<AdminActivityReportDTO.ActionSummary>>> getActivitySummary(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        int lookbackDays = Math.max(days != null ? days : 7, 1);
        Duration window = Duration.ofDays(lookbackDays);
        List<AdminActivityReportDTO.ActionSummary> summary = activityLogService.summarizeRecentActions(window)
            .stream()
            .map(count -> AdminActivityReportDTO.ActionSummary.builder()
                .action(count.action())
                .count(count.count())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseDTO.success("Tóm tắt hoạt động admin", summary));
        }

    private List<AdminLogWithUser> integrateAdminDetails(List<AdminActivityLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return List.of();
        }
        Map<String, User> adminMap = findAdminsByIds(logs);
        return logs.stream()
            .map(log -> {
                User admin = adminMap.get(log.getAdminId());
                return new AdminLogWithUser(
                    log,
                    admin != null ? admin.getFullName() : null,
                    admin != null ? admin.getEmail() : null
                );
            })
            .collect(Collectors.toList());
    }

    private Map<String, User> findAdminsByIds(List<AdminActivityLog> logs) {
        Set<String> adminIds = logs.stream()
            .map(AdminActivityLog::getAdminId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (adminIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findByIdIn(new ArrayList<>(adminIds)).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private record AdminLogWithUser(AdminActivityLog log, String adminName, String adminEmail) {
    }

    private String getCurrentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email);
        }
        log.debug("Admin thao tác lớp học: {} ({})", user.getEmail(), user.getId());
        return user.getId();
    }
}
