package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class AdminClassroomDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomFilter {
        private String teacherId;
        private String teacherKeyword;
        private String grade;
        private String status;
        private String keyword;
        private int page;
        private int size;
        private String sortBy;
        private String sortOrder;

        public ClassroomFilter withPagination(int page, int size) {
            this.page = Math.max(0, page);
            this.size = Math.max(1, size);
            return this;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomSummary {
        private String id;
        private String name;
        private String description;
        private String grade;
        private String status;
        private String classCode;
        private String schedule;
        private Integer maxStudents;
        private Integer studentCount;
        private String teacherId;
        private String teacherName;
        private String teacherEmail;
        private Long createdAt;
        private Long updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomListResponse {
        private List<ClassroomSummary> items;
        private long total;
        private int page;
        private int size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomStats {
        private long totalClassrooms;
        private Map<String, Long> statusCounts;
        private long totalStudents;
        private double averageStudents;
        private long totalTeachers;
        private long activeTeachers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomStatusUpdateRequest {
        private String status;
    }
}
