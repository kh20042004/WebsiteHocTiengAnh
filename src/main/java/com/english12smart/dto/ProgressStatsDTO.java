package com.english12smart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ========== PROGRESS STATS DTO ==========
 * Dữ liệu thống kê tiến độ học tập để gửi lên frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressStatsDTO {

    @JsonProperty("weeklyData")
    private List<Integer> weeklyData; // [45, 60, 30, 75, 90, 50, 65] = phút mỗi ngày

    @JsonProperty("skillsData")
    private List<Integer> skillsData; // [85, 75, 90, 70, 80, 88] = % Listening, Speaking, Reading, Writing, Grammar, Vocabulary

    @JsonProperty("recentActivities")
    private List<Activity> recentActivities;

    @JsonProperty("unitProgress")
    private List<UnitProgressItem> unitProgress;

    /**
     * Hoạt động gần đây
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String type; // "exercise", "exam", "lesson"
        private String title;
        private Double score;
        private Double maxScore;
        private String timeAgo;
        private String icon; // icon type
    }

    /**
     * Tiến độ Unit
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitProgressItem {
        private String unitId;
        private String unitName;
        private Integer percentage; // 0-100
        private Integer completed; // số unit đã hoàn thành
        private Integer total; // tổng số unit
    }
}
