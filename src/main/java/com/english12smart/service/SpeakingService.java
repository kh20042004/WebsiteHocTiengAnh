package com.english12smart.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ========== SPEAKING SERVICE ==========
 * Xử lý logic cho bài tập nói (SPEAKING_EXERCISE)
 * - Tính độ chính xác giữa thật và chuẩn
 * - Sinh feedback tự động
 * - Upload audio lên Cloudinary
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpeakingService {

    private final Cloudinary cloudinary;

    // ========== MAIN METHODS ==========

    /**
     * Tính độ chính xác (%) giữa text người dùng và chuẩn
     * 
     * @param userTranscript Text do học sinh nói (Web Speech API trích xuất)
     * @param correctPhrase  Cụm từ chuẩn từ Exercise
     * @return Độ chính xác từ 0.0 đến 100.0 (%)
     */
    public Double calculateAccuracy(String userTranscript, String correctPhrase) {
        if (userTranscript == null || correctPhrase == null) {
            return 0.0;
        }

        // Normalize text: lowercase, trim whitespace, xóa punctuation
        String normalizedUser = normalizeText(userTranscript);
        String normalizedCorrect = normalizeText(correctPhrase);

        log.debug("User normalized: '{}' | Correct normalized: '{}'", normalizedUser, normalizedCorrect);

        // Nếu hoàn toàn giống nhau → 100%
        if (normalizedUser.equals(normalizedCorrect)) {
            return 100.0;
        }

        // Phương pháp 1: Levenshtein Distance (edit distance)
        double distance = calculateLevenshteinDistance(normalizedUser, normalizedCorrect);
        double maxLength = Math.max(normalizedUser.length(), normalizedCorrect.length());
        
        if (maxLength == 0) {
            return 100.0;
        }

        double similarity = ((maxLength - distance) / maxLength) * 100;
        
        // Phương pháp 2: Word-level similarity (nếu có sự khác biệt lớn)
        double wordSimilarity = calculateWordSimilarity(normalizedUser, normalizedCorrect);

        // Lấy giá trị cao hơn giữa 2 phương pháp
        double finalScore = Math.max(similarity, wordSimilarity);

        log.debug("Levenshtein similarity: {}, Word similarity: {}, Final: {}%", 
            similarity, wordSimilarity, finalScore);

        return Math.min(100.0, Math.max(0.0, finalScore));
    }

    /**
     * Sinh feedback tự động dựa trên độ chính xác
     * So sánh từng từ giữa userText và correctText
     * 
     * @param userTranscript       Text người dùng nói
     * @param correctPhrase        Cụm từ chuẩn
     * @param accuracy             Độ chính xác (%)
     * @param minAccuracy          Độ chính xác tối thiểu yêu cầu
     * @return Feedback string
     */
    public String generateFeedback(String userTranscript, String correctPhrase, 
                                  Double accuracy, Double minAccuracy) {
        if (userTranscript == null || userTranscript.trim().isEmpty()) {
            return "Bạn chưa nói gì. Vui lòng thử lại!";
        }

        if (accuracy == null) {
            accuracy = 0.0;
        }

        if (minAccuracy == null) {
            minAccuracy = 60.0;
        }

        StringBuilder feedback = new StringBuilder();

        // Feedback dựa trên accuracy score
        if (accuracy >= 95) {
            feedback.append("🎉 Tuyệt vời! Phát âm của bạn rất đúng. ");
        } else if (accuracy >= minAccuracy) {
            feedback.append("✅ Tốt! Bạn đã đạt yêu cầu. ");
        } else {
            feedback.append("⚠️ Chưa đủ tốt. Vui lòng thử lại. ");
        }

        // So sánh từng từ
        String[] userWords = normalizeText(userTranscript).split("\\s+");
        String[] correctWords = normalizeText(correctPhrase).split("\\s+");

        Set<String> missingWords = new HashSet<>(Arrays.asList(correctWords));
        for (String word : userWords) {
            missingWords.remove(word);
        }

        if (!missingWords.isEmpty()) {
            String missing = missingWords.stream()
                .limit(3) // Chỉ show 3 từ đầu tiên
                .collect(Collectors.joining(", "));
            feedback.append("Bạn thiếu từ(s): ").append(missing);
            
            if (missingWords.size() > 3) {
                feedback.append(" và ").append(missingWords.size() - 3).append(" từ khác");
            }
        }

        log.debug("Generated feedback: {}", feedback.toString());
        return feedback.toString();
    }

    /**
     * Upload audio file lên Cloudinary
     * 
     * @param file MultipartFile chứa audio
     * @return URL của audio trên Cloudinary
     */
    public String uploadAudioToCloudinary(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Audio file không được để trống");
        }

        try {
            // Upload file to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "speaking-exercises",
                            "resource_type", "auto",
                            "quality", "auto"
                    )
            );

            String audioUrl = (String) uploadResult.get("url");
            log.info("Audio uploaded to Cloudinary: {}", audioUrl);
            return audioUrl;

        } catch (IOException e) {
            log.error("Failed to upload audio to Cloudinary", e);
            throw new RuntimeException("Không thể upload audio: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error uploading to Cloudinary", e);
            throw new RuntimeException("Lỗi server khi upload: " + e.getMessage());
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Normalize text: lowercase, trim, remove punctuation except spaces
     * "Hello, my name is John!" → "hello my name is john"
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text
            .toLowerCase()                          // Chuyển thành chữ thường
            .trim()                                 // Xóa khoảng trắng đầu cuối
            .replaceAll("[!?.,;:'\"-]", "")        // Xóa dấu câu
            .replaceAll("\\s+", " ");              // Xóa khoảng trắng thừa
    }

    /**
     * Tính Levenshtein Distance (Edit Distance)
     * Số lần thao tác cần thiết để biến một string thành string khác
     * (insert, delete, replace)
     */
    private double calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                        Math.min(dp[i - 1][j], dp[i][j - 1]),
                        dp[i - 1][j - 1]
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Tính độ tương tự giữa 2 text dựa trên từ (word-level)
     * "Hello world" vs "Hello there world" → 66% (2/3 từ khớp)
     */
    private double calculateWordSimilarity(String text1, String text2) {
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");

        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size() * 100;
    }

    /**
     * Kiểm tra xem đạt yêu cầu độ chính xác tối thiểu hay không
     */
    public boolean isPassed(Double accuracy, Double minAccuracy) {
        if (accuracy == null || minAccuracy == null) {
            return false;
        }
        return accuracy >= minAccuracy;
    }
}
