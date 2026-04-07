package com.english12smart.service;

import com.english12smart.entity.ExamAntiFraudLog;
import com.english12smart.entity.ExamSubmission;
import com.english12smart.repository.ExamAntiFraudLogRepository;
import com.english12smart.repository.ExamSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý fraud logs (nhật ký gian lận)
 * Lưu, phân tích, và tính toán Trust Score
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExamAntiFraudLogService {

    private final ExamAntiFraudLogRepository fraudLogRepository;
    private final ExamSubmissionRepository examSubmissionRepository;

    // ======================================================================
    // 1. Lưu Fraud Log
    // ======================================================================

    /**
     * Lưu fraud event từ client
     */
    public void saveFraudLog(String submissionId, String fraudType, String details, String ipAddress, String userAgent) {
        try {
            // Lấy submission để lấy info
            ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                    .orElse(null);

            if (submission == null) {
                log.warn("Không tìm thấy submission để lưu fraud log: {}", submissionId);
                return;
            }

            // Xác định mức độ nghiêm trọng
            String severity = determineSeverity(fraudType);

            // Tạo fraud log
            ExamAntiFraudLog fraudLog = ExamAntiFraudLog.builder()
                    .submissionId(submissionId)
                    .examId(submission.getExamId())
                    .studentId(submission.getStudentId())
                    .studentName(submission.getStudentName())
                    .fraudType(fraudType)
                    .details(details)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .count(1)
                    .severity(severity)
                    .detectedAt(LocalDateTime.now())
                    .reviewed(false)
                    .build();

            fraudLogRepository.save(fraudLog);
            log.info("Ghi fraud log: {} - {}", fraudType, submissionId);

            // Cập nhật counters trong submission
            updateSubmissionFraudCounters(submission, fraudType);

            // Recalculate trust score
            recalculateTrustScore(submission);

        } catch (Exception e) {
            log.error("Lỗi khi lưu fraud log: {}", e.getMessage(), e);
        }
    }

    // ======================================================================
    // 2. Lấy Fraud Logs của Submission
    // ======================================================================

    /**
     * Lấy tất cả fraud logs của một submission
     */
    public List<ExamAntiFraudLog> getFraudLogsBySubmission(String submissionId) {
        return fraudLogRepository.findBySubmissionId(submissionId);
    }

    /**
     * Lấy fraud logs trong khoảng thời gian
     */
    public List<ExamAntiFraudLog> getFraudLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return fraudLogRepository.findByCreatedAtBetween(startTime, endTime);
    }

    /**
     * Lấy fraud logs theo severity
     */
    public List<ExamAntiFraudLog> getFraudLogsBySeverity(String severity) {
        return fraudLogRepository.findBySeverity(severity);
    }

    /**
     * Lấy fraud logs chưa xem
     */
    public List<ExamAntiFraudLog> getUnreviewedFraudLogs(String examId) {
        return fraudLogRepository.findByReviewedFalseAndExamId(examId);
    }

    // ======================================================================
    // 3. Phân Tích Fraud
    // ======================================================================

    /**
     * Phân tích chi tiết gian lận của một submission
     * Trả về report tổng hợp
     */
    public Map<String, Object> analyzeSubmissionFraud(String submissionId) {
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElse(null);

        if (submission == null) {
            return Map.of("error", "Submission not found");
        }

        List<ExamAntiFraudLog> fraudLogs = getFraudLogsBySubmission(submissionId);

        // Đếm số lần mỗi loại fraud
        Map<String, Long> fraudTypeCounts = fraudLogs.stream()
                .collect(Collectors.groupingBy(
                        ExamAntiFraudLog::getFraudType,
                        Collectors.counting()
                ));

        // Xác định mức độ rủi ro
        String riskLevel = determineRiskLevel(submission);

        // Tính thời gian làm bài
        String timeTaken = submission.getTimeTakenDisplay();

        // Tốc độ hoàn thành (câu/phút)
        double completionSpeed = calculateCompletionSpeed(submission);

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("submissionId", submissionId);
        analysis.put("studentId", submission.getStudentId());
        analysis.put("studentName", submission.getStudentName());
        analysis.put("fraudLogCount", fraudLogs.size());
        analysis.put("fraudTypeCounts", fraudTypeCounts);
        analysis.put("trustScore", submission.getTrustScore());
        analysis.put("riskLevel", riskLevel);
        analysis.put("timeTaken", timeTaken);
        analysis.put("completionSpeed", String.format("%.2f câu/phút", completionSpeed));
        analysis.put("tabChangeCount", submission.getTabChangeCount());
        analysis.put("copyAttempts", submission.getCopyAttempts());
        analysis.put("pasteAttempts", submission.getPasteAttempts());
        analysis.put("rightClickAttempts", submission.getRightClickAttempts());
        analysis.put("devToolsAttempts", submission.getDevToolsAttempts());
        analysis.put("fullscreenExitCount", submission.getFullscreenExitCount());
        analysis.put("suspiciousSpeed", submission.getSuspiciousSpeed());
        analysis.put("score", submission.getScoreDisplay());
        analysis.put("percentage", submission.getPercentageDisplay());

        return analysis;
    }

    /**
     * Lấy fraud summary cho dashboard
     */
    public Map<String, Object> getFraudSummary(String examId) {
        // Lấy tất cả submissions của exam này
        List<ExamSubmission> submissions = examSubmissionRepository.findByExamId(examId);

        // Phân tích từng submission
        List<Map<String, Object>> suspiciousSubmissions = submissions.stream()
                .filter(sub -> sub.getTrustScore() < 80) // Chỉ lấy những cái nghi ngờ
                .map(sub -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("submissionId", sub.getId());
                    summary.put("studentName", sub.getStudentName());
                    summary.put("studentId", sub.getStudentId());
                    summary.put("trustScore", sub.getTrustScore());
                    summary.put("violationCount", sub.getFraudLogCount());
                    summary.put("riskLevel", determineRiskLevel(sub));
                    summary.put("score", sub.getScoreDisplay());
                    summary.put("tabChanges", sub.getTabChangeCount());
                    summary.put("copyAttempts", sub.getCopyAttempts());
                    summary.put("devTools", sub.getDevToolsAttempts());
                    return summary;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("trustScore"), (Integer) a.get("trustScore")))
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("examId", examId);
        summary.put("totalSubmissions", submissions.size());
        summary.put("suspiciousSubmissions", suspiciousSubmissions);
        summary.put("highRiskCount", suspiciousSubmissions.stream()
                .filter(s -> "HIGH".equals(s.get("riskLevel"))).count());
        summary.put("mediumRiskCount", suspiciousSubmissions.stream()
                .filter(s -> "MEDIUM".equals(s.get("riskLevel"))).count());
        summary.put("lowRiskCount", suspiciousSubmissions.stream()
                .filter(s -> "LOW".equals(s.get("riskLevel"))).count());

        return summary;
    }

    // ======================================================================
    // 4. Tính Trust Score
    // ======================================================================

    /**
     * Tính Trust Score (0-100)
     * - 100: Hoàn toàn tin cậy
     * - 80-99: Hầu như tin cậy
     * - 60-79: Bình thường
     * - 40-59: Nghi ngờ
     * - 0-39: Rất nghi ngờ
     */
    public Integer calculateTrustScore(ExamSubmission submission) {
        int score = 100;

        // Tab change: -2 điểm mỗi lần
        int tabChanges = submission.getTabChangeCount() != null ? submission.getTabChangeCount() : 0;
        score -= Math.min(tabChanges * 2, 20);

        // Copy attempt: -3 điểm mỗi lần
        int copyAttempts = submission.getCopyAttempts() != null ? submission.getCopyAttempts() : 0;
        score -= Math.min(copyAttempts * 3, 25);

        // Paste attempt: -3 điểm mỗi lần
        int pasteAttempts = submission.getPasteAttempts() != null ? submission.getPasteAttempts() : 0;
        score -= Math.min(pasteAttempts * 3, 25);

        // Right click: -1 điểm mỗi lần
        int rightClickAttempts = submission.getRightClickAttempts() != null ? submission.getRightClickAttempts() : 0;
        score -= Math.min(rightClickAttempts, 10);

        // DevTools: -5 điểm mỗi lần
        int devToolsAttempts = submission.getDevToolsAttempts() != null ? submission.getDevToolsAttempts() : 0;
        score -= Math.min(devToolsAttempts * 5, 30);

        // Fullscreen exit: -1 điểm mỗi lần
        int fullscreenExits = submission.getFullscreenExitCount() != null ? submission.getFullscreenExitCount() : 0;
        score -= Math.min(fullscreenExits, 10);

        // Suspicious speed: -20 điểm
        if (Boolean.TRUE.equals(submission.getSuspiciousSpeed())) {
            score -= 20;
        }

        // Ensure score is between 0-100
        score = Math.max(0, Math.min(100, score));

        // Cập nhật vào submission
        submission.setTrustScore(score);
        examSubmissionRepository.save(submission);

        return score;
    }

    /**
     * Recalculate trust score (helper method)
     */
    private void recalculateTrustScore(ExamSubmission submission) {
        calculateTrustScore(submission);
    }

    // ======================================================================
    // 5. Helper Methods
    // ======================================================================

    /**
     * Xác định mức độ nghiêm trọng của fraud type
     */
    private String determineSeverity(String fraudType) {
        return switch (fraudType) {
            case "DEV_TOOLS", "COPY_ATTEMPT", "PASTE_ATTEMPT", "CUT_ATTEMPT" -> "HIGH";
            case "TAB_CHANGE", "RIGHT_CLICK", "FULLSCREEN_EXIT" -> "MEDIUM";
            case "EXAM_STARTED", "UNUSUAL_ACTIVITY" -> "LOW";
            default -> "LOW";
        };
    }

    /**
     * Xác định mức độ rủi ro (HIGH/MEDIUM/LOW)
     */
    private String determineRiskLevel(ExamSubmission submission) {
        int trustScore = submission.getTrustScore() != null ? submission.getTrustScore() : 100;

        if (trustScore >= 80) {
            return "LOW";
        } else if (trustScore >= 60) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    /**
     * Tính tốc độ hoàn thành (câu hỏi/phút)
     */
    private double calculateCompletionSpeed(ExamSubmission submission) {
        Integer timeTaken = submission.getTimeTakenSeconds();
        if (timeTaken == null || timeTaken <= 0) {
            return 0;
        }

        // Lấy số câu hỏi từ totalScore (giả định 1 câu = 1 điểm)
        int questionCount = submission.getTotalScore() != null ? submission.getTotalScore() : 1;
        double minutes = timeTaken / 60.0;

        return questionCount / minutes;
    }

    /**
     * Cập nhật fraud counters trong submission
     */
    private void updateSubmissionFraudCounters(ExamSubmission submission, String fraudType) {
        switch (fraudType) {
            case "TAB_CHANGE" -> submission.setTabChangeCount(
                    (submission.getTabChangeCount() != null ? submission.getTabChangeCount() : 0) + 1);
            case "COPY_ATTEMPT" -> submission.setCopyAttempts(
                    (submission.getCopyAttempts() != null ? submission.getCopyAttempts() : 0) + 1);
            case "PASTE_ATTEMPT" -> submission.setPasteAttempts(
                    (submission.getPasteAttempts() != null ? submission.getPasteAttempts() : 0) + 1);
            case "RIGHT_CLICK" -> submission.setRightClickAttempts(
                    (submission.getRightClickAttempts() != null ? submission.getRightClickAttempts() : 0) + 1);
            case "DEV_TOOLS" -> submission.setDevToolsAttempts(
                    (submission.getDevToolsAttempts() != null ? submission.getDevToolsAttempts() : 0) + 1);
            case "FULLSCREEN_EXIT" -> submission.setFullscreenExitCount(
                    (submission.getFullscreenExitCount() != null ? submission.getFullscreenExitCount() : 0) + 1);
        }

        submission.setFraudLogCount((submission.getFraudLogCount() != null ? submission.getFraudLogCount() : 0) + 1);
        examSubmissionRepository.save(submission);
    }

    /**
     * Mark fraud log as reviewed
     */
    public void markFraudLogAsReviewed(String fraudLogId) {
        ExamAntiFraudLog fraudLog = fraudLogRepository.findById(fraudLogId).orElse(null);
        if (fraudLog != null) {
            fraudLog.setReviewed(true);
            fraudLogRepository.save(fraudLog);
        }
    }

    /**
     * Add teacher note to fraud log
     */
    public void addTeacherNote(String fraudLogId, String note) {
        ExamAntiFraudLog fraudLog = fraudLogRepository.findById(fraudLogId).orElse(null);
        if (fraudLog != null) {
            fraudLog.setTeacherNote(note);
            fraudLog.setReviewed(true);
            fraudLogRepository.save(fraudLog);
        }
    }
}
