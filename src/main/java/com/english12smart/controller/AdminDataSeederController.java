package com.english12smart.controller;

import com.english12smart.service.SampleDataSeederService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ========== ADMIN DATA SEEDER CONTROLLER ==========
 * API để tạo dữ liệu mẫu (bài tập + bài kiểm tra)
 * Chỉ admin mới được sử dụng
 */
@RestController
@RequestMapping("/api/admin/seeder")
@RequiredArgsConstructor
@Slf4j
public class AdminDataSeederController {

    private final SampleDataSeederService sampleDataSeederService;

    /**
     * POST /api/admin/seeder/seed-all
     * Tạo bài tập mẫu + bài kiểm tra cho tất cả các bài học
     * 
     * ⚠️ CHỈ ADMIN MỚI CÓ QUYỀN HỌC NHU CẦU
     */
    @PostMapping("/seed-all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> seedAllData() {
        log.info("🌱 Admin yêu cầu tạo dữ liệu mẫu cho tất cả lessons");

        try {
            sampleDataSeederService.seedExercisesAndExamsForAllLessons();
            
            return ResponseEntity.ok(new SeedResponse(
                    true,
                    "✅ Tạo dữ liệu mẫu thành công! Mỗi bài học đã có bài tập + bài kiểm tra mẫu."
            ));

        } catch (Exception e) {
            log.error("❌ Lỗi tạo dữ liệu: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new SeedResponse(
                    false,
                    "❌ Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Response object cho seeding
     */
    @Data
    @AllArgsConstructor
    static class SeedResponse {
        private boolean success;
        private String message;
    }
}
