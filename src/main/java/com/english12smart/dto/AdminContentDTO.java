package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminContentDTO {

    /**
     * Các loại nội dung trong hệ thống
     */
    public enum ContentType {
        UNIT,
        LESSON,
        EXERCISE,
        EXAM;

        public static ContentType from(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String normalized = raw.trim().toUpperCase();
            if (normalized.startsWith("ROLE_")) {
                normalized = normalized.substring(5);
            }
            try {
                return ContentType.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid content type: " + raw);
            }
        }
    }

    @Data
    @Builder
    public static class ContentFilter {
        private ContentType contentType;
        private String keyword;
        private String status;
        private Boolean isActive;
        private String createdBy;
        private int page;
        private int size;
        private String sortBy;
        private String sortOrder;

        public ContentFilter withPagination(int page, int size) {
            this.page = Math.max(0, page);
            this.size = Math.max(1, size);
            return this;
        }
    }

    @Data
    @Builder
    public static class ContentSummary {
        private String id;
        private ContentType contentType;
        private String title;
        private String subtitle;
        private String unitId;
        private String unitTitle;
        private String lessonId;
        private String lessonTitle;
        private Boolean isActive;
        private String status;
        private String createdBy;
        private Long createdAt;
        private Long updatedAt;
        private Map<String, Object> metadata;
    }

    @Data
    @Builder
    public static class ContentListResponse {
        private List<ContentSummary> items;
        private long total;
        private int page;
        private int size;
    }

    @Data
    @Builder
    public static class ContentStats {
        private Map<ContentType, Long> counts;
        private Map<String, Long> statusBreakdown;
        private long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusUpdateRequest {
        private Boolean isActive;
        private String status;
        private String note;
    }
}
