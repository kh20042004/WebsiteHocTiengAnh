package com.english12smart.service;

import com.english12smart.dto.AssignmentDTO;
import com.english12smart.entity.Assignment;
import com.english12smart.entity.Classroom;
import com.english12smart.exception.BadRequestException;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.AssignmentRepository;
import com.english12smart.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;

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
     * Tạo bài tập mới
     */
    public AssignmentDTO.Response createAssignment(AssignmentDTO.CreateRequest request, String teacherId) {
        // Kiểm tra lớp học tồn tại
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));

        // Kiểm tra giáo viên sở hữu lớp
        if (!classroom.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền giao bài cho lớp này");
        }

        long now = System.currentTimeMillis();
        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType() != null ? request.getType().toUpperCase() : "READING")
                .classroomId(classroom.getId())
                .classroomName(classroom.getName())
                .teacherId(teacherId)
                .assignedDate(now)
                .dueDate(request.getDueDate())
                .status("ACTIVE")
                .totalStudents(classroom.getStudentCount())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        log.info("Tạo bài tập mới: {} cho lớp {}", saved.getTitle(), classroom.getName());

        // Cập nhật số bài tập trong lớp
        classroom.setTotalAssignments(
                (classroom.getTotalAssignments() != null ? classroom.getTotalAssignments() : 0) + 1);
        classroomRepository.save(classroom);

        return toResponse(saved);
    }

    /**
     * Cập nhật bài tập
     */
    public AssignmentDTO.Response updateAssignment(String id, AssignmentDTO.UpdateRequest request, String teacherId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập"));

        if (!assignment.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền sửa bài tập này");
        }

        if (request.getTitle() != null) assignment.setTitle(request.getTitle());
        if (request.getDescription() != null) assignment.setDescription(request.getDescription());
        if (request.getType() != null) assignment.setType(request.getType().toUpperCase());
        if (request.getDueDate() != null) assignment.setDueDate(request.getDueDate());
        if (request.getStatus() != null) assignment.setStatus(request.getStatus());

        // Nếu đổi lớp
        if (request.getClassroomId() != null && !request.getClassroomId().equals(assignment.getClassroomId())) {
            Classroom newClassroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));
            assignment.setClassroomId(newClassroom.getId());
            assignment.setClassroomName(newClassroom.getName());
        }

        assignment.setUpdatedAt(System.currentTimeMillis());
        Assignment saved = assignmentRepository.save(assignment);
        log.info("Cập nhật bài tập: {}", saved.getTitle());
        return toResponse(saved);
    }

    /**
     * Xóa bài tập
     */
    public void deleteAssignment(String id, String teacherId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập"));

        if (!assignment.getTeacherId().equals(teacherId)) {
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
        return AssignmentDTO.Response.builder()
                .id(a.getId())
                .title(a.getTitle() != null ? a.getTitle() : "")
                .description(a.getDescription() != null ? a.getDescription() : "")
                .type(a.getType() != null ? a.getType() : "READING")
                .typeDisplay(a.getTypeDisplay())
                .typeBadgeClass(a.getTypeBadgeClass())
                .classroomId(a.getClassroomId())
                .classroomName(a.getClassroomName() != null ? a.getClassroomName() : "")
                .teacherId(a.getTeacherId())
                .assignedDate(a.getAssignedDate())
                .dueDate(a.getDueDate())
                .dueDateDisplay(formatDate(a.getDueDate()))
                .status(a.getStatus() != null ? a.getStatus() : "ACTIVE")
                .statusDisplay(a.getStatusDisplay())
                .statusBadgeClass(a.getStatusBadgeClass())
                .totalStudents(a.getTotalStudents() != null ? a.getTotalStudents() : 0)
                .submittedCount(a.getSubmittedCount() != null ? a.getSubmittedCount() : 0)
                .gradedCount(a.getGradedCount() != null ? a.getGradedCount() : 0)
                .pendingCount(a.getPendingCount())
                .averageScore(a.getAverageScore() != null ? a.getAverageScore() : 0.0)
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
