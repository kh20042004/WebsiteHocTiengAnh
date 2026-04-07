package com.english12smart.service;

import com.english12smart.dto.ProgressStatsDTO;
import com.english12smart.entity.*;
import com.english12smart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ========== PROGRESS STATS SERVICE ==========
 * Tính toán thống kê tiến độ học tập từ dữ liệu thực tế
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressStatsService {

    private final LessonProgressRepository lessonProgressRepository;
    private final ExerciseSubmissionRepository exerciseSubmissionRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final UnitRepository unitRepository;

    /**
     * Lấy thống kê tiến độ chi tiết của học sinh
     */
    public ProgressStatsDTO getProgressStats(String studentId) {
        log.info("📊 Đang tính toán thống kê tiến độ cho học sinh: {}", studentId);

        ProgressStatsDTO stats = new ProgressStatsDTO();

        try {
            // 1. Weekly activity data - tính số phút học mỗi ngày trong 7 ngày qua
            stats.setWeeklyData(calculateWeeklyActivity(studentId));

            // 2. Skills breakdown - tính % hoàn thành mỗi kỹ năng
            stats.setSkillsData(calculateSkillsBreakdown(studentId));

            // 3. Recent activities - lấy 5 hoạt động gần nhất
            stats.setRecentActivities(getRecentActivities(studentId));

            // 4. Unit progress - tính % tiến độ mỗi unit
            stats.setUnitProgress(calculateUnitProgress(studentId));

            log.info("✅ Tính toán thống kê thành công");
        } catch (Exception e) {
            log.error("❌ Lỗi khi tính toán thống kê: {}", e.getMessage(), e);
        }

        return stats;
    }

    /**
     * Tính hoạt động trong 7 ngày qua (phút học mỗi ngày)
     */
    private List<Integer> calculateWeeklyActivity(String studentId) {
        List<Integer> weeklyMinutes = new ArrayList<>();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        for (int i = 6; i >= 0; i--) {
            ZonedDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            ZonedDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);

            long minTime = dayStart.toInstant().toEpochMilli();
            long maxTime = dayEnd.toInstant().toEpochMilli();

            // Lấy tất cả lesson progress được cập nhật trong ngày này
            var dayProgress = lessonProgressRepository.findByStudentIdOrderByLastUpdatedAtDesc(studentId)
                    .stream()
                    .filter(p -> p.getLastUpdatedAt() != null &&
                            p.getLastUpdatedAt() >= minTime && p.getLastUpdatedAt() <= maxTime)
                    .toList();

            int totalMinutes = dayProgress.stream()
                    .mapToInt(p -> p.getTotalStudyTimeMs() != null ? (int) (p.getTotalStudyTimeMs() / 60000) : 0)
                    .sum();

            weeklyMinutes.add(totalMinutes);
        }

        return weeklyMinutes;
    }

    /**
     * Tính % hoàn thành cho mỗi kỹ năng (Listening, Speaking, Reading, Writing, Grammar, Vocabulary)
     * Tạm thời trả về values cố định vì ExerciseSubmission không có skillType field
     */
    private List<Integer> calculateSkillsBreakdown(String studentId) {
        // Skills: [Listening, Speaking, Reading, Writing, Grammar, Vocabulary]
        // Tạm thời sử dụng dữ liệu từ ExamSubmission hoặc hardcode
        // TODO: Thêm skill tracking vào ExerciseSubmission để tính toán chính xác

        try {
            var examSubmissions = examSubmissionRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
            
            if (!examSubmissions.isEmpty()) {
                // Tính điểm trung bình từ exam submissions
                double avgScore = examSubmissions.stream()
                        .mapToDouble(e -> e.getScore() != null ? e.getScore() : 0)
                        .average()
                        .orElse(75);
                
                // Phân bố skill scores dựa trên exam average
                return Arrays.asList(
                    (int)(avgScore * 0.9),      // Listening
                    (int)(avgScore * 0.85),     // Speaking
                    (int)(avgScore * 0.95),     // Reading
                    (int)(avgScore * 0.8),      // Writing
                    (int)(avgScore * 0.9),      // Grammar
                    (int)(avgScore * 0.92)      // Vocabulary
                );
            }
        } catch (Exception e) {
            log.warn("⚠️  Lỗi tính skills breakdown: {}", e.getMessage());
        }

        // Default values nếu không có dữ liệu
        return Arrays.asList(75, 70, 80, 65, 75, 78);
    }

    /**
     * Lấy các hoạt động gần nhất (limit 5)
     */
    private List<ProgressStatsDTO.Activity> getRecentActivities(String studentId) {
        List<ProgressStatsDTO.Activity> activities = new ArrayList<>();

        try {
            // Lấy exam submissions gần đây (có thể dùng để biết hoạt động)
            var exams = examSubmissionRepository.findByStudentIdOrderBySubmittedAtDesc(studentId)
                    .stream().limit(5).toList();

            for (ExamSubmission exam : exams) {
                ProgressStatsDTO.Activity activity = new ProgressStatsDTO.Activity();
                activity.setType("exam");
                activity.setTitle("Hoàn thành bài kiểm tra");
                activity.setScore(exam.getScore() != null ? exam.getScore() : 0.0);
                activity.setMaxScore(100.0);
                activity.setTimeAgo(formatTimeAgo(exam.getSubmittedAt()));
                activities.add(activity);
            }

            // Nếu không đủ activities từ exams, lấy từ lesson progress
            if (activities.size() < 5) {
                var lessonProgress = lessonProgressRepository.findByStudentIdOrderByLastUpdatedAtDesc(studentId)
                        .stream().limit(5 - activities.size()).toList();

                for (LessonProgress lp : lessonProgress) {
                    ProgressStatsDTO.Activity activity = new ProgressStatsDTO.Activity();
                    activity.setType("lesson");
                    activity.setTitle("Học bài: Lesson");
                    activity.setScore(lp.getViewed() != null && lp.getViewed() ? 100.0 : 0.0);
                    activity.setMaxScore(100.0);
                    activity.setTimeAgo(formatTimeAgo(lp.getLastUpdatedAt()));
                    activities.add(activity);
                }
            }

        } catch (Exception e) {
            log.warn("⚠️  Lỗi lấy recent activities: {}", e.getMessage());
        }

        return activities;
    }

    /**
     * Tính % tiến độ của mỗi Unit
     */
    private List<ProgressStatsDTO.UnitProgressItem> calculateUnitProgress(String studentId) {
        List<ProgressStatsDTO.UnitProgressItem> unitProgress = new ArrayList<>();

        try {
            var allLessonProgress = lessonProgressRepository.findByStudentIdOrderByLastUpdatedAtDesc(studentId);

            // Group by unitId
            var unitMap = allLessonProgress.stream()
                    .collect(Collectors.groupingBy(LessonProgress::getUnitId));

            for (Map.Entry<String, List<LessonProgress>> entry : unitMap.entrySet()) {
                String unitId = entry.getKey();
                List<LessonProgress> progressList = entry.getValue();

                if (unitId != null) {
                    // Lấy unit info
                    var unitOpt = unitRepository.findById(unitId);
                    String unitName = unitOpt.map(Unit::getTitle).orElse("Unit " + unitId.substring(0, 4));

                    // Tính % hoàn thành
                    int completed = (int) progressList.stream().filter(p -> p.getViewed() != null && p.getViewed()).count();
                    int total = progressList.size();
                    int percentage = total > 0 ? (completed * 100) / total : 0;

                    ProgressStatsDTO.UnitProgressItem item = new ProgressStatsDTO.UnitProgressItem();
                    item.setUnitId(unitId);
                    item.setUnitName(unitName);
                    item.setPercentage(percentage);
                    item.setCompleted(completed);
                    item.setTotal(total);

                    unitProgress.add(item);
                }
            }
        } catch (Exception e) {
            log.warn("⚠️  Lỗi tính unit progress: {}", e.getMessage());
        }

        return unitProgress;
    }

    /**
     * Format timestamp thành "2 giờ trước", "5 phút trước", etc
     */
    private String formatTimeAgo(Long timestamp) {
        if (timestamp == null) return "N/A";

        long seconds = (System.currentTimeMillis() - timestamp) / 1000;
        if (seconds < 60) return "Vừa xong";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " phút trước";
        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";
        long days = hours / 24;
        if (days < 7) return days + " ngày trước";
        return "Lâu rồi";
    }
}
