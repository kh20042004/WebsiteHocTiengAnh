package com.english12smart.service;

import com.english12smart.dto.ProgressDTO;
import com.english12smart.entity.ExerciseSubmission;
import com.english12smart.entity.Lesson;
import com.english12smart.repository.ExerciseRepository;
import com.english12smart.repository.ExerciseSubmissionRepository;
import com.english12smart.repository.LessonProgressRepository;
import com.english12smart.repository.LessonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ProgressService - Tính toán tiến độ học tập
 * 
 * Công thức tính tiến độ bài học:
 * Tiến độ = (Bước hoàn thành / Tổng bước) × 100
 * 
 * Với:
 * - 1 bước = xem bài học
 * - 1 bước = hoàn thành mỗi bài tập (score >= 50% max score)
 * 
 * Công thức: Tiến độ (%) = (viewed + completedExercises) / (1 + totalExercises) × 100
 */
@Slf4j
@Service
public class ProgressService {

    private final ExerciseSubmissionRepository submissionRepo;
    private final LessonProgressRepository progressRepo;
    private final LessonRepository lessonRepo;
    private final ExerciseRepository exerciseRepo;
    private final com.english12smart.repository.UserRepository userRepository;

    public ProgressService(ExerciseSubmissionRepository submissionRepo,
                          LessonProgressRepository progressRepo,
                          LessonRepository lessonRepo,
                          ExerciseRepository exerciseRepo,
                          com.english12smart.repository.UserRepository userRepository) {
        this.submissionRepo = submissionRepo;
        this.progressRepo = progressRepo;
        this.lessonRepo = lessonRepo;
        this.exerciseRepo = exerciseRepo;
        this.userRepository = userRepository;
    }

    /**
     * Tính tiến độ bài học của học sinh
     * Công thức: Tiến độ (%) = (viewed + completedExercises) / (1 + totalExercises) × 100
     * 
     * @param lessonId - ID bài học
     * @param studentId - ID học sinh
     * @return LessonProgress
     */
    public ProgressDTO.LessonProgress calculateLessonProgress(String lessonId, String studentId) {
        log.info("Tính tiến độ bài học {} cho học sinh {}", lessonId, studentId);

        // Lấy thông tin bài học
        Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
        if (lesson == null) {
            log.warn("Không tìm thấy lesson {}", lessonId);
            return ProgressDTO.LessonProgress.builder()
                    .lessonId(lessonId)
                    .progressPercent(0)
                    .build();
        }

        // Lấy hoặc tạo LessonProgress record
        com.english12smart.entity.LessonProgress progress = progressRepo.findByLessonIdAndStudentId(lessonId, studentId)
                .orElse(com.english12smart.entity.LessonProgress.builder()
                        .studentId(studentId)
                        .lessonId(lessonId)
                        .unitId(lesson.getUnitId())
                        .viewed(false)
                        .completedExercises(0)
                        .totalScore(0)
                        .maxScore(0)
                        .status("NOT_STARTED")
                        .progressPercent(0)
                        .build());

        // Đếm tổng số bài tập trong bài học từ Exercise repository
        long totalExercisesCount = exerciseRepo.countByLessonId(lessonId);
        int totalExercises = (int) totalExercisesCount;

        // Lấy tất cả bài tập học sinh nộp
        List<ExerciseSubmission> submissions = submissionRepo
                .findByLessonIdAndStudentIdOrderBySubmittedAtDesc(lessonId, studentId);

        // Đếm bài tập hoàn thành (score >= 50%)
        int completedCount = 0;
        int totalScore = 0;
        int maxTotalScore = 0;

        for (ExerciseSubmission sub : submissions) {
            int score = sub.getScore() != null ? sub.getScore() : 0;
            int maxScore = sub.getMaxScore() != null ? sub.getMaxScore() : 100;

            totalScore += score;
            maxTotalScore += maxScore;

            // Hoàn thành = status COMPLETED & score >= 50%
            if ("COMPLETED".equals(sub.getStatus()) && (maxScore > 0 && score >= maxScore * 0.5)) {
                completedCount++;
            }
        }

        // Cập nhật progress record
        progress.setCompletedExercises(completedCount);
        progress.setAttemptedExercises(submissions.size());
        progress.setTotalScore(totalScore);
        progress.setMaxScore(maxTotalScore);
        progress.setLastUpdatedAt(System.currentTimeMillis());

        // === TÍNH TIẾN ĐỘ ===
        // Tiến độ (%) = (viewed + completedExercises) / (1 + totalExercises) × 100
        // viewed = 1 (nếu viewed=true) hoặc 0 (nếu viewed=false)
        int viewedPoints = progress.getViewed() != null && progress.getViewed() ? 1 : 0;
        int totalSteps = 1 + totalExercises;  // 1 = xem bài + số bài tập
        int completedSteps = viewedPoints + completedCount;
        int progressPercent = totalExercises == 0 ? (viewedPoints > 0 ? 100 : 0) : (completedSteps * 100 / totalSteps);

        progress.setProgressPercent(progressPercent);

        // Cập nhật status
        if (progressPercent == 0) {
            progress.setStatus("NOT_STARTED");
        } else if (progressPercent == 100) {
            progress.setStatus("COMPLETED");
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(System.currentTimeMillis());
            }
        } else {
            progress.setStatus("IN_PROGRESS");
        }

        // Lưu lại
        progressRepo.save(progress);

        return ProgressDTO.LessonProgress.builder()
                .lessonId(lessonId)
                .lessonTitle(lesson.getTitle())
                .totalExercises(totalExercises)
                .completedExercises(completedCount)
                .progressPercent(progressPercent)
                .totalScore(totalScore)
                .maxTotalScore(maxTotalScore)
                .totalXP(0)
                .build();
    }

    /**
     * Lấy tiến độ (%) của bài học
     * @param lessonId - ID bài học
     * @param studentId - ID học sinh
     * @return Phần trăm (0-100)
     */
    public int getLessonProgressPercent(String lessonId, String studentId) {
        return calculateLessonProgress(lessonId, studentId).getProgressPercent();
    }

    /**
     * ===== TRACKING VIEWING =====
     * Ghi nhận khi học sinh bắt đầu xem bài học
     * @param lessonId - ID bài học
     * @param studentId - ID học sinh
     */
    public void markLessonViewed(String lessonId, String studentId) {
        log.info("Ghi nhận xem bài học {} cho học sinh {}", lessonId, studentId);

        Optional<com.english12smart.entity.LessonProgress> existing = progressRepo.findByLessonIdAndStudentId(lessonId, studentId);
        com.english12smart.entity.LessonProgress progress;

        if (existing.isPresent()) {
            progress = existing.get();
            if (!progress.getViewed()) {
                // Lần đầu xem
                progress.setViewed(true);
                progress.setViewedAt(System.currentTimeMillis());
                progress.setStatus("IN_PROGRESS");
            } else {
                // Xem lại
                progress.setViewCount((progress.getViewCount() != null ? progress.getViewCount() : 0) + 1);
            }
        } else {
            // Tạo record mới
            Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
            if (lesson == null) {
                log.warn("Không tìm thấy lesson {}", lessonId);
                return;
            }

            progress = com.english12smart.entity.LessonProgress.builder()
                    .studentId(studentId)
                    .lessonId(lessonId)
                    .unitId(lesson.getUnitId())
                    .viewed(true)
                    .viewedAt(System.currentTimeMillis())
                    .viewCount(1)
                    .status("IN_PROGRESS")
                    .progressPercent(0)
                    .build();
        }

        progress.setLastUpdatedAt(System.currentTimeMillis());
        progressRepo.save(progress);

        // Tính lại tiến độ
        calculateLessonProgress(lessonId, studentId);
    }

    /**
     * Ghi nhận bài tập hoàn thành
     * @param exerciseId - ID bài tập
     * @param studentId - ID học sinh
     * @param score - Điểm số đạt được
     * @param maxScore - Điểm tối đa
     */
    public void recordExerciseSubmission(String exerciseId, String studentId, 
                                       int score, int maxScore) {
        log.info("Ghi nhận bài tập {} cho học sinh {} - Điểm: {}/{}", 
                exerciseId, studentId, score, maxScore);

        // Tìm exercise để lấy lessonId, unitId
        var exercise = exerciseRepo.findById(exerciseId).orElse(null);
        if (exercise == null) {
            log.warn("Không tìm thấy exercise {}", exerciseId);
            return;
        }

        String lessonId = exercise.getLessonId();
        String unitId = exercise.getUnitId();

        // Tạo submission
        ExerciseSubmission submission = ExerciseSubmission.builder()
                .studentId(studentId)
                .exerciseId(exerciseId)
                .lessonId(lessonId)
                .unitId(unitId)
                .score(score)
                .maxScore(maxScore)
                .status("COMPLETED")
                .submittedAt(System.currentTimeMillis())
                .createdAt(System.currentTimeMillis())
                .build();

        submissionRepo.save(submission);
        log.info("Đã ghi nhận submission {}", submission.getId());

        // Cộng điểm XP cho User
        var user = userRepository.findById(studentId).orElse(null);
        if (user != null) {
            long totalXP = user.getTotalXP() != null ? user.getTotalXP() : 0L;
            int xpEarned = maxScore > 0 ? (score * 10 / maxScore) : 0; // Thưởng tối đa 10 XP cho mỗi bài tập
            if (xpEarned > 0) {
                user.setTotalXP(totalXP + xpEarned);
                
                // Cập nhật level nếu đủ điểm
                long newXP = user.getTotalXP();
                if (newXP >= 7000) user.setLevel("Advanced");
                else if (newXP >= 3500) user.setLevel("Upper-Intermediate");
                else if (newXP >= 1500) user.setLevel("Intermediate");
                else if (newXP >= 500) user.setLevel("Pre-Intermediate");
                else user.setLevel("Elementary");
                
                userRepository.save(user);
                log.info("Đã cộng {} XP cho học sinh {}", xpEarned, studentId);
            }
        }

        // Tính lại tiến độ bài học
        calculateLessonProgress(lessonId, studentId);
    }
}
