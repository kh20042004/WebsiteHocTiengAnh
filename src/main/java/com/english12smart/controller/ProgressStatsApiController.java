package com.english12smart.controller;

import com.english12smart.dto.ProgressStatsDTO;
import com.english12smart.service.ProgressStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * ========== PROGRESS STATS API ==========
 * API endpoints để lấy dữ liệu thống kê tiến độ
 */
@RestController
@RequestMapping("/api/progress-stats")
@RequiredArgsConstructor
@Slf4j
public class ProgressStatsApiController {

    private final ProgressStatsService progressStatsService;

    /**
     * GET /api/progress-stats/my-stats
     * Lấy thống kê tiến độ của học sinh hiện tại
     */
    @GetMapping("/my-stats")
    public ResponseEntity<ProgressStatsDTO> getMyProgressStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Lấy studentId từ email (thực tế nên query database để lấy ID)
            // Tạm thời sử dụng email làm key
            log.info("📊 Lấy progress stats cho user: {}", email);

            // FIXME: Lấy student ID thực sự từ database
            // Tạm thời return mock data, cần sửa lại
            ProgressStatsDTO stats = new ProgressStatsDTO();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy progress stats: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /api/progress-stats/student/{studentId}
     * Lấy thống kê tiến độ của một học sinh cụ thể (dành cho admin/teacher)
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ProgressStatsDTO> getStudentProgressStats(@PathVariable String studentId) {
        try {
            log.info("📊 Lấy progress stats cho học sinh: {}", studentId);
            ProgressStatsDTO stats = progressStatsService.getProgressStats(studentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy progress stats: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
