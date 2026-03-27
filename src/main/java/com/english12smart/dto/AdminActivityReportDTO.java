package com.english12smart.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AdminActivityReportDTO {

    private List<ActivityEntry> items;
    private long total;
    private int page;
    private int size;

    @Data
    @Builder
    public static class ActivityEntry {
        private String id;
        private String adminId;
        private String adminName;
        private String adminEmail;
        private String action;
        private String targetType;
        private String targetId;
        private Map<String, Object> metadata;
        private long createdAt;
    }

    @Data
    @Builder
    public static class ActionSummary {
        private String action;
        private long count;
    }
}
