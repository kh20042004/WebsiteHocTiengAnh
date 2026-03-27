package com.english12smart.service;

import com.english12smart.dto.AdminClassroomDTO;
import com.english12smart.entity.Classroom;
import com.english12smart.entity.User;
import com.english12smart.exception.BadRequestException;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.ClassroomRepository;
import com.english12smart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final AdminActivityLogService activityLogService;
    private final MeterRegistry meterRegistry;

    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "UPCOMING", "COMPLETED");
    private static final String GRADE_PATTERN = "^(10|11|12)$";

    public AdminClassroomDTO.ClassroomListResponse listClassrooms(AdminClassroomDTO.ClassroomFilter filter) {
        try {
            validateFilter(filter);
            int page = Math.max(filter.getPage(), 0);
            int size = filter.getSize() > 0 ? filter.getSize() : 10;

            List<Classroom> classrooms = classroomRepository.findAll();
            Map<String, User> teacherMap = loadTeacherMap(classrooms);
            String keyword = normalize(filter.getKeyword());
            String teacherKeyword = normalize(filter.getTeacherKeyword());

            List<Classroom> filtered = classrooms.stream()
                    .filter(c -> matchesFilters(c, teacherMap, filter, keyword, teacherKeyword))
                    .sorted(classroomComparator(filter))
                    .collect(Collectors.toList());

            int total = filtered.size();
            int from = Math.min(page * size, total);
            int to = Math.min(from + size, total);
            List<Classroom> pageItems = from >= to ? List.of() : filtered.subList(from, to);

            List<AdminClassroomDTO.ClassroomSummary> summaries = pageItems.stream()
                    .map(c -> toSummary(c, teacherMap.get(c.getTeacherId())))
                    .collect(Collectors.toList());

            AdminClassroomDTO.ClassroomListResponse response = AdminClassroomDTO.ClassroomListResponse.builder()
                    .items(summaries)
                    .total(total)
                    .page(page)
                    .size(size)
                    .build();
            meterRegistry.counter("admin.classrooms.list", "result", "success").increment();
            return response;
        } catch (RuntimeException ex) {
            meterRegistry.counter("admin.classrooms.list", "result", "error").increment();
            throw ex;
        }
    }

    public AdminClassroomDTO.ClassroomStats getClassroomStats() {
        List<Classroom> classrooms = classroomRepository.findAll();
        long totalClassrooms = classrooms.size();
        long totalStudents = classrooms.stream()
                .mapToInt(Classroom::getStudentCount)
                .sum();
        double averageStudents = totalClassrooms == 0 ? 0.0 : (double) totalStudents / totalClassrooms;

        Map<String, Long> statusCounts = classrooms.stream()
                .map(c -> Optional.ofNullable(c.getStatus()).map(String::toUpperCase).orElse("UNKNOWN"))
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

        long totalTeachers = userRepository.countByRole("TEACHER");
        long activeTeachers = userRepository.countByRoleAndIsActiveTrue("TEACHER");

        return AdminClassroomDTO.ClassroomStats.builder()
                .totalClassrooms(totalClassrooms)
                .statusCounts(statusCounts)
                .totalStudents(totalStudents)
                .averageStudents(averageStudents)
                .totalTeachers(totalTeachers)
                .activeTeachers(activeTeachers)
                .build();
    }

    public AdminClassroomDTO.ClassroomSummary updateClassroomStatus(String classroomId,
                                                                    AdminClassroomDTO.ClassroomStatusUpdateRequest request,
                                                                    String adminId) {
        try {
            String normalizedStatus = normalizeStatus(request.getStatus());
            if (normalizedStatus == null) {
                throw new BadRequestException("Trạng thái là bắt buộc");
            }
            Classroom classroom = classroomRepository.findById(classroomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học: " + classroomId));
            String previousStatus = classroom.getStatus();
            classroom.setStatus(normalizedStatus);
            classroom.setUpdatedAt(System.currentTimeMillis());
            Classroom saved = classroomRepository.save(classroom);
            log.info("Admin cập nhật trạng thái lớp {} -> {}", classroomId, normalizedStatus);
            activityLogService.recordStatusChange(adminId, classroomId, classroom.getName(), previousStatus, normalizedStatus);
            AdminClassroomDTO.ClassroomSummary summary = toSummary(saved, findTeacher(saved.getTeacherId()));
            meterRegistry.counter("admin.classrooms.status.update", "result", "success").increment();
            return summary;
        } catch (RuntimeException ex) {
            meterRegistry.counter("admin.classrooms.status.update", "result", "error").increment();
            throw ex;
        }
    }

    private Map<String, User> loadTeacherMap(List<Classroom> classrooms) {
        Set<String> teacherIds = classrooms.stream()
                .map(Classroom::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (teacherIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findByIdIn(new ArrayList<>(teacherIds)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private boolean matchesFilters(Classroom classroom, Map<String, User> teacherMap,
                                   AdminClassroomDTO.ClassroomFilter filter,
                                   String keyword, String teacherKeyword) {
        if (filter.getStatus() != null && !isValidStatus(filter.getStatus())) {
            return false;
        }
        if (filter.getTeacherId() != null && !filter.getTeacherId().equals(classroom.getTeacherId())) {
            return false;
        }
        if (filter.getStatus() != null && !filter.getStatus().equalsIgnoreCase(classroom.getStatus())) {
            return false;
        }

        if (filter.getGrade() != null && !filter.getGrade().equalsIgnoreCase(classroom.getGrade())) {
            return false;
        }
        if (filter.getGrade() != null && !filter.getGrade().equalsIgnoreCase(classroom.getGrade())) {
            return false;
        }
        if (keyword != null && !matchesKeyword(classroom, keyword)) {
            return false;
        }
        if (teacherKeyword != null && !matchesTeacherKeyword(classroom, teacherMap, teacherKeyword)) {
            return false;
        }
        return true;
    }

    private boolean matchesKeyword(Classroom classroom, String keyword) {
        String normalizedRoom = (classroom.getName() == null ? "" : classroom.getName()).toLowerCase(Locale.ROOT);
        String normalizedDescription = (classroom.getDescription() == null ? "" : classroom.getDescription()).toLowerCase(Locale.ROOT);
        String normalizedCode = (classroom.getClassCode() == null ? "" : classroom.getClassCode()).toLowerCase(Locale.ROOT);
        String normalizedSchedule = (classroom.getSchedule() == null ? "" : classroom.getSchedule()).toLowerCase(Locale.ROOT);
        return normalizedRoom.contains(keyword) || normalizedDescription.contains(keyword)
                || normalizedCode.contains(keyword) || normalizedSchedule.contains(keyword);
    }

    private boolean matchesTeacherKeyword(Classroom classroom, Map<String, User> teacherMap, String teacherKeyword) {
        User teacher = teacherMap.get(classroom.getTeacherId());
        if (teacher == null) {
            return false;
        }
        String normalizedName = (teacher.getFullName() == null ? "" : teacher.getFullName()).toLowerCase(Locale.ROOT);
        String normalizedEmail = (teacher.getEmail() == null ? "" : teacher.getEmail()).toLowerCase(Locale.ROOT);
        return normalizedName.contains(teacherKeyword) || normalizedEmail.contains(teacherKeyword);
    }

    private Comparator<Classroom> classroomComparator(AdminClassroomDTO.ClassroomFilter filter) {
        Comparator<Classroom> comparator = Comparator.comparing(c -> Optional.ofNullable(c.getCreatedAt()).orElse(0L));
        if ("name".equalsIgnoreCase(filter.getSortBy())) {
            comparator = Comparator.comparing(c -> Optional.ofNullable(c.getName()).orElse(""), String::compareToIgnoreCase);
        }
        if ("grade".equalsIgnoreCase(filter.getSortBy())) {
            comparator = Comparator.comparing(c -> Optional.ofNullable(c.getGrade()).orElse(""), String::compareToIgnoreCase);
        }
        if ("status".equalsIgnoreCase(filter.getSortBy())) {
            comparator = Comparator.comparing(c -> Optional.ofNullable(c.getStatus()).orElse(""), String::compareToIgnoreCase);
        }
        if ("asc".equalsIgnoreCase(filter.getSortOrder())) {
            return comparator;
        }
        return comparator.reversed();
    }

    private AdminClassroomDTO.ClassroomSummary toSummary(Classroom classroom, User teacher) {
        return AdminClassroomDTO.ClassroomSummary.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .description(classroom.getDescription())
                .grade(classroom.getGrade())
                .status(classroom.getStatus())
                .classCode(classroom.getClassCode())
                .schedule(classroom.getSchedule())
                .maxStudents(classroom.getMaxStudents())
                .studentCount(classroom.getStudentCount())
                .teacherId(classroom.getTeacherId())
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .teacherEmail(teacher != null ? teacher.getEmail() : null)
                .createdAt(classroom.getCreatedAt())
                .updatedAt(classroom.getUpdatedAt())
                .build();
    }

    private User findTeacher(String teacherId) {
        if (teacherId == null) {
            return null;
        }
        return userRepository.findById(teacherId).orElse(null);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        String trimmed = status.trim().toUpperCase(Locale.ROOT);
        if (trimmed.isEmpty() || !VALID_STATUSES.contains(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private boolean isValidStatus(String status) {
        if (status == null) {
            return true;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return VALID_STATUSES.contains(normalized);
    }

    private void validateFilter(AdminClassroomDTO.ClassroomFilter filter) {
        if (filter == null) {
            return;
        }
        if (filter.getStatus() != null && !isValidStatus(filter.getStatus())) {
            throw new BadRequestException("Trạng thái lớp học không hợp lệ");
        }
        if (filter.getGrade() != null && !filter.getGrade().trim().matches(GRADE_PATTERN)) {
            throw new BadRequestException("Khối học chỉ hỗ trợ 10, 11 hoặc 12");
        }
        if (filter.getTeacherKeyword() != null && filter.getTeacherKeyword().length() > 64) {
            throw new BadRequestException("Từ khoá giáo viên tối đa 64 ký tự");
        }
    }
}
