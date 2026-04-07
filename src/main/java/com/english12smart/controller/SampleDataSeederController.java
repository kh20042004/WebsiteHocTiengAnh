package com.english12smart.controller;

import com.english12smart.service.SampleDataSeederService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ========== SAMPLE DATA SEEDER CONTROLLER ==========
 * API endpoint để tạo dữ liệu mẫu
 * 
 * Chỉ dành cho development/testing
 * Trong production, cần bảo vệ với authentication/authorization
 */
@RestController
@RequestMapping("/api/admin/seed")
@RequiredArgsConstructor
@Slf4j
public class SampleDataSeederController {

    private final SampleDataSeederService seederService;

    /**
     * POST /api/admin/seed/exercises-and-exams
     * Tạo bài tập mẫu và bài kiểm tra cho tất cả bài học
     * 
     * @return Response message
     */
    @PostMapping("/exercises-and-exams")
    public ResponseEntity<String> seedExercisesAndExams() {
        log.info("🌱 Nhận yêu cầu tạo dữ liệu mẫu...");
        
        try {
            seederService.seedExercisesAndExamsForAllLessons();
            return ResponseEntity.ok("✅ Tạo dữ liệu mẫu thành công! Mỗi bài học đều có bài tập mẫu và bài kiểm tra.");
        } catch (Exception e) {
            log.error("❌ Lỗi tạo dữ liệu: {}", e.getMessage());
            return ResponseEntity.status(500).body("❌ Lỗi: " + e.getMessage());
        }
    }
}
