package com.english12smart.service;

import com.english12smart.dto.ClassroomDTO;
import com.english12smart.entity.Classroom;
import com.english12smart.exception.BadRequestException;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    /**
     * Lấy danh sách lớp học của một giáo viên
     */
    public List<ClassroomDTO.Response> getClassroomsByTeacher(String teacherId) {
        List<Classroom> classrooms = classroomRepository.findByTeacherIdOrderByNameAsc(teacherId);

        // Auto-generate classCode cho các lớp chưa có (dữ liệu cũ)
        for (Classroom c : classrooms) {
            if (c.getClassCode() == null || c.getClassCode().isBlank()) {
                c.setClassCode(generateUniqueClassCode());
                classroomRepository.save(c);
                log.info("Đã tạo mã lớp {} cho lớp {}", c.getClassCode(), c.getName());
            }
        }

        return classrooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách lớp theo giáo viên và trạng thái
     */
    public List<ClassroomDTO.Response> getClassroomsByTeacherAndStatus(String teacherId, String status) {
        return classroomRepository.findByTeacherIdAndStatusOrderByNameAsc(teacherId, status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một lớp học
     */
    public ClassroomDTO.Response getClassroomById(String classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + classroomId));
        return toResponse(classroom);
    }

    /**
     * Tạo lớp học mới
     */
    public ClassroomDTO.Response createClassroom(ClassroomDTO.CreateRequest request, String teacherId) {
        // Kiểm tra trùng tên trong lớp của giáo viên này
        if (classroomRepository.existsByTeacherIdAndName(teacherId, request.getName())) {
            throw new BadRequestException("Bạn đã có lớp học tên '" + request.getName() + "'");
        }

        long now = System.currentTimeMillis();
        String classCode = generateUniqueClassCode();
        Classroom classroom = Classroom.builder()
                .name(request.getName())
                .description(request.getDescription())
                .grade(request.getGrade() != null ? request.getGrade() : "12")
                .teacherId(teacherId)
                .classCode(classCode)
                .schedule(request.getSchedule())
                .colorTheme(request.getColorTheme() != null ? request.getColorTheme() : "blue")
                .status("ACTIVE")
                .maxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 40)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Classroom saved = classroomRepository.save(classroom);
        log.info("Tạo lớp học mới: {} bởi giáo viên {}", saved.getName(), teacherId);
        return toResponse(saved);
    }

    /**
     * Cập nhật thông tin lớp học
     */
    public ClassroomDTO.Response updateClassroom(String classroomId, ClassroomDTO.UpdateRequest request, String teacherId,
            boolean adminOverride) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + classroomId));

        if (!adminOverride && !classroom.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền sửa lớp học này");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            classroom.setName(request.getName());
        }
        if (request.getDescription() != null) {
            classroom.setDescription(request.getDescription());
        }
        if (request.getGrade() != null) {
            classroom.setGrade(request.getGrade());
        }
        if (request.getSchedule() != null) {
            classroom.setSchedule(request.getSchedule());
        }
        if (request.getColorTheme() != null) {
            classroom.setColorTheme(request.getColorTheme());
        }
        if (request.getMaxStudents() != null) {
            classroom.setMaxStudents(request.getMaxStudents());
        }
        if (request.getStatus() != null) {
            classroom.setStatus(request.getStatus());
        }
        classroom.setUpdatedAt(System.currentTimeMillis());

        Classroom saved = classroomRepository.save(classroom);
        log.info("Cập nhật lớp học: {}", classroomId);
        return toResponse(saved);
    }

    /**
     * Xóa lớp học
     */
    public void deleteClassroom(String classroomId, String teacherId, boolean adminOverride) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + classroomId));

        if (!adminOverride && !classroom.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền xóa lớp học này");
        }

        classroomRepository.deleteById(classroomId);
        log.info("Xóa lớp học: {}", classroomId);
    }

    /**
     * Học sinh tham gia lớp học bằng mã lớp
     */
    public ClassroomDTO.Response joinClassroom(String classCode, String studentId) {
        Classroom classroom = classroomRepository.findByClassCode(classCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với mã: " + classCode));

        // Kiểm tra trạng thái lớp
        if (!"ACTIVE".equals(classroom.getStatus())) {
            throw new BadRequestException("Lớp học này hiện không nhận thêm học sinh");
        }

        // Kiểm tra đã tham gia chưa
        if (classroom.getStudentIds() != null && classroom.getStudentIds().contains(studentId)) {
            throw new BadRequestException("Bạn đã tham gia lớp này rồi");
        }

        // Kiểm tra sĩ số
        int currentSize = classroom.getStudentIds() != null ? classroom.getStudentIds().size() : 0;
        if (currentSize >= classroom.getMaxStudents()) {
            throw new BadRequestException("Lớp đã đầy (" + classroom.getMaxStudents() + " học sinh)");
        }

        // Thêm student vào lớp
        if (classroom.getStudentIds() == null) {
            classroom.setStudentIds(new java.util.ArrayList<>());
        }
        classroom.getStudentIds().add(studentId);
        classroom.setUpdatedAt(System.currentTimeMillis());
        Classroom saved = classroomRepository.save(classroom);
        log.info("Học sinh {} đã tham gia lớp {}", studentId, classroom.getName());
        return toResponse(saved);
    }

    /**
     * Lấy danh sách lớp của 1 học sinh
     */
    public List<ClassroomDTO.Response> getClassroomsByStudent(String studentId) {
        List<Classroom> classrooms = classroomRepository.findByStudentIdsContaining(studentId);
        return classrooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Đếm số lớp học của giáo viên
     */
    public long countByTeacher(String teacherId) {
        return classroomRepository.countByTeacherId(teacherId);
    }

    // ---- Helper ----

    private ClassroomDTO.Response toResponse(Classroom c) {
        return ClassroomDTO.Response.builder()
                .id(c.getId())
                .name(c.getName() != null ? c.getName() : "Lớp học")
                .description(c.getDescription())
                .grade(c.getGrade())
                .teacherId(c.getTeacherId())
                .classCode(c.getClassCode())
                .schedule(c.getSchedule())
                .colorTheme(c.getColorTheme() != null ? c.getColorTheme() : "blue")
                .status(c.getStatus() != null ? c.getStatus() : "ACTIVE")
                .statusDisplay(c.getStatusDisplay() != null ? c.getStatusDisplay() : "Đang hoạt động")
                .maxStudents(c.getMaxStudents() != null ? c.getMaxStudents() : 40)
                .studentCount(c.getStudentCount())
                .totalAssignments(c.getTotalAssignments() != null ? c.getTotalAssignments() : 0)
                .ungradedAssignments(c.getUngradedAssignments() != null ? c.getUngradedAssignments() : 0)
                .gradientClass(c.getGradientClass() != null ? c.getGradientClass() : "from-blue-500 to-blue-600")
                .descriptionColorClass(c.getDescriptionColorClass() != null ? c.getDescriptionColorClass() : "text-blue-100")
                .createdAt(c.getCreatedAt())
                .build();
    }

    /**
     * Tạo mã lớp học ngẫu nhiên 6 ký tự (chữ hoa + số), đảm bảo unique
     */
    private String generateUniqueClassCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Bỏ I,O,0,1 để tránh nhầm lẫn
        Random random = new Random();
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            String code = sb.toString();
            if (!classroomRepository.existsByClassCode(code)) {
                return code;
            }
        }
        // Fallback: dùng timestamp để đảm bảo unique
        return Long.toString(System.currentTimeMillis() % 1_000_000, 36).toUpperCase();
    }
}
