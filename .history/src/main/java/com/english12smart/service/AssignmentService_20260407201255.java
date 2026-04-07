package com.english12smart.service;

import com.english12smart.dto.AssignmentDTO;
import com.english12smart.entity.Assignment;
import com.english12smart.entity.Classroom;
import com.english12smart.exception.BadRequestException;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.AssignmentRepository;
import com.english12smart.repository.AssignmentSubmissionRepository;
import com.english12smart.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;

    /**
     * Lấy tất cả bài tập của giáo viên
     */
    public List<AssignmentDTO.Response> getAssignmentsByTeacher(String teacherId) {
        return assignmentRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin chi tiết một bài tập
     */
    public AssignmentDTO.Response getAssignmentById(String assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập: " + assignmentId));
        return toResponse(assignment);
    }

    /**
     * Tạo bài tập mới (hỗ trợ multi-class)
     * 
     * Flow:
     * 1. Validate classroomIds array (bắt buộc)
     * 2. Validate teacher sở hữu all classrooms
     * 3. Create assignment với:
     *    - classroomIds: danh sách ID lớp
     *    - exerciseIds: danh sách ID bài tập
     *    - gradingMode: AUTO hoặc MANUAL
     *    - timeLimitMinutes: giới hạn thời gian (nếu có)
     * 4. Update statistics cho tất cả classrooms
     * 
     * @param request - CreateRequest với classroomIds[], exerciseIds[], gradingMode
     * @param teacherId - Teacher ID
     * @param adminOverride - Admin có thể bypass validation
     * @return AssignmentDTO.Response
     */
    public AssignmentDTO.Response createAssignment(AssignmentDTO.CreateRequest request, String teacherId,
            boolean adminOverride) {
        
        log.info("========== CREATE ASSIGNMENT ==========");
        log.info("Title: {}, Teacher: {}", request.getTitle(), teacherId);

        // ========== 1. Handle multi-class vs single-class ==========
        List<String> classroomIds = new java.util.ArrayList<>();
        String primaryClassroomId = null;
        String primaryClassroomName = null;

        // Nếu có classroomIds array (multi-class)
        if (request.getClassroomIds() != null && !request.getClassroomIds().isEmpty()) {
            classroomIds = request.getClassroomIds();
            log.info("Multi-class assignment: {} classrooms", classroomIds.size());
        } else {
            throw new BadRequestException("Vui lòng chọn ít nhất 1 lớp học");
        }

        // ========== 2. Validate & fetch classrooms ==========
        List<Classroom> classrooms = new java.util.ArrayList<>();
        int totalStudents = 0;

        for (String classroomId : classroomIds) {
            Classroom classroom = classroomRepository.findById(classroomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp: " + classroomId));

            // Validate teacher owns this classroom
            if (!adminOverride && !classroom.getTeacherId().equals(teacherId)) {
                throw new BadRequestException("Bạn không có quyền giao bài cho lớp: " + classroom.getName());
            }

            classrooms.add(classroom);
            Integer count = classroom.getStudentCount();
            totalStudents += (count != null ? count : 0);

            // Remember primary classroom (first one)
            if (primaryClassroomId == null) {
                primaryClassroomId = classroom.getId();
                primaryClassroomName = classroom.getName();
            }
        }

        log.info("Validated {} classrooms, total students: {}", classrooms.size(), totalStudents);

        // ========== 3. Create Assignment entity ==========
        long now = System.currentTimeMillis();
        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType() != null ? request.getType().toUpperCase() : "READING")
                // Backward compatibility: set classroomId for single-class assignments
                .classroomId(primaryClassroomId)
                .classroomName(primaryClassroomName)
                // New fields for multi-class
                .classroomIds(classroomIds)
                .exerciseIds(request.getExerciseIds() != null ? request.getExerciseIds() : new java.util.ArrayList<>())
                .gradingMode(request.getGradingMode() != null ? request.getGradingMode().toUpperCase() : "MANUAL")
                .teacherId(adminOverride ? classrooms.get(0).getTeacherId() : teacherId)
                .assignedDate(now)
                .dueDate(request.getDueDate())
                .status("ACTIVE")
                .totalStudents(totalStudents)
                .submittedCount(0)
                .gradedCount(0)
                .averageScore(0.0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        log.info("Assignment created: {} (ID: {})", saved.getTitle(), saved.getId());

        // ========== 4. Update statistics in all classrooms ==========
        for (Classroom classroom : classrooms) {
            classroom.setTotalAssignments(
                    (classroom.getTotalAssignments() != null ? classroom.getTotalAssignments() : 0) + 1);
            classroomRepository.save(classroom);
        }

        log.info("========== ASSIGNMENT CREATION SUCCESSFUL ==========");
        return toResponse(saved);
    }

    /**
     * Cập nhật bài tập
     */
    public AssignmentDTO.Response updateAssignment(String id, AssignmentDTO.UpdateRequest request, String teacherId,
            boolean adminOverride) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập"));

        if (!adminOverride && !assignment.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền sửa bài tập này");
        }

        if (request.getTitle() != null) assignment.setTitle(request.getTitle());
        if (request.getDescription() != null) assignment.setDescription(request.getDescription());
        if (request.getType() != null) assignment.setType(request.getType().toUpperCase());
        if (request.getDueDate() != null) assignment.setDueDate(request.getDueDate());
        if (request.getStatus() != null) assignment.setStatus(request.getStatus());

        // Nếu đổi lớp
        if (request.getClassroomIds() != null && !request.getClassroomIds().isEmpty()) {
            List<String> newClassroomNames = new ArrayList<>();
            for (String classroomId : request.getClassroomIds()) {
                Classroom classroom = classroomRepository.findById(classroomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học: " + classroomId));
                // Kiểm tra quyền
                if (!adminOverride && !classroom.getTeacherId().equals(teacherId)) {
                    throw new BadRequestException("Bạn không có quyền giao bài tập cho lớp này");
                }
                newClassroomNames.add(classroom.getName());
            }
            assignment.setClassroomIds(request.getClassroomIds());
            assignment.setClassroomNames(newClassroomNames);
        }

        assignment.setUpdatedAt(System.currentTimeMillis());
        Assignment saved = assignmentRepository.save(assignment);
        log.info("Cập nhật bài tập: {}", saved.getTitle());
        return toResponse(saved);
    }

    /**
     * Xóa bài tập
     */
    public void deleteAssignment(String id, String teacherId, boolean adminOverride) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập"));

        if (!adminOverride && !assignment.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền xóa bài tập này");
        }

        assignmentRepository.delete(assignment);
        log.info("Đã xóa bài tập: {}", assignment.getTitle());

        // Giảm số bài tập trong lớp
        classroomRepository.findById(assignment.getClassroomId()).ifPresent(classroom -> {
            int total = classroom.getTotalAssignments() != null ? classroom.getTotalAssignments() : 0;
            classroom.setTotalAssignments(Math.max(0, total - 1));
            classroomRepository.save(classroom);
        });
    }

    /**
     * Đếm thống kê
     */
    public long countByTeacher(String teacherId) {
        return assignmentRepository.countByTeacherId(teacherId);
    }

    public long countPendingByTeacher(String teacherId) {
        List<Assignment> assignments = assignmentRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
        return assignments.stream().mapToInt(Assignment::getPendingCount).sum();
    }

    // ---- Mapper ----
    private AssignmentDTO.Response toResponse(Assignment a) {
        // Tính toán submission statistics từ actual submission data
        long submittedSubmissions = assignmentSubmissionRepository.countByAssignmentIdAndStatusIn(
            a.getId(), List.of("SUBMITTED", "GRADED")
        );
        long gradedSubmissions = assignmentSubmissionRepository.countByAssignmentIdAndStatus(
            a.getId(), "GRADED"
        );
        long pendingSubmissions = Math.max(0, submittedSubmissions - gradedSubmissions);
        
        // Tính average score từ graded submissions
        double averageScore = 0.0;
        if (gradedSubmissions > 0) {
            List<Integer> scores = assignmentSubmissionRepository.findByAssignmentIdAndStatus(a.getId(), "GRADED")
                .stream()
                .map(s -> s.getScore() != null ? s.getScore() : 0)
                .collect(Collectors.toList());
            if (!scores.isEmpty()) {
                averageScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            }
        }
        
        return AssignmentDTO.Response.builder()
                .id(a.getId())
                .title(a.getTitle() != null ? a.getTitle() : "")
                .description(a.getDescription() != null ? a.getDescription() : "")
                .type(a.getType() != null ? a.getType() : "READING")
                .typeDisplay(a.getTypeDisplay())
                .typeBadgeClass(a.getTypeBadgeClass())
                .classroomId(a.getClassroomId())
                .classroomName(a.getClassroomName() != null ? a.getClassroomName() : "")
                // ===== Additional fields for multi-class & advanced features =====
                .classroomIds(a.getClassroomIds() != null ? a.getClassroomIds() : new java.util.ArrayList<>())
                .exerciseIds(a.getExerciseIds() != null ? a.getExerciseIds() : new java.util.ArrayList<>())
                .gradingMode(a.getGradingMode() != null ? a.getGradingMode() : "MANUAL")
                // ================================================================
                .teacherId(a.getTeacherId())
                .assignedDate(a.getAssignedDate())
                .dueDate(a.getDueDate())
                .dueDateDisplay(formatDate(a.getDueDate()))
                .status(a.getStatus() != null ? a.getStatus() : "ACTIVE")
                .statusDisplay(a.getStatusDisplay())
                .statusBadgeClass(a.getStatusBadgeClass())
                .totalStudents(a.getTotalStudents() != null ? a.getTotalStudents() : 0)
                .submittedCount((int) submittedSubmissions)
                .gradedCount((int) gradedSubmissions)
                .pendingCount((int) pendingSubmissions)
                .averageScore(averageScore)
                .createdAt(a.getCreatedAt())
                .build();
    }

    private String formatDate(Long millis) {
        if (millis == null || millis == 0) return "Không có hạn";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(new Date(millis));
        } catch (Exception e) {
            return "Không xác định";
        }
    }
}
