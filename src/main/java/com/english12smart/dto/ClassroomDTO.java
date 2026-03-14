package com.english12smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ClassroomDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "Tên lớp không được để trống")
        @Size(min = 2, max = 20, message = "Tên lớp phải từ 2-20 ký tự")
        private String name;

        @Size(max = 200, message = "Mô tả không quá 200 ký tự")
        private String description;

        @Pattern(regexp = "10|11|12", message = "Khối phải là 10, 11 hoặc 12")
        private String grade;

        private String schedule;

        @Pattern(regexp = "blue|emerald|purple|amber|rose", message = "Màu không hợp lệ")
        private String colorTheme;

        private Integer maxStudents;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
        private String description;
        private String grade;
        private String schedule;
        private String colorTheme;
        private Integer maxStudents;

        @Pattern(regexp = "ACTIVE|UPCOMING|COMPLETED", message = "Trạng thái không hợp lệ")
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String id;
        private String name;
        private String description;
        private String grade;
        private String teacherId;
        private String classCode;
        private String schedule;

        @Builder.Default
        private String colorTheme = "blue";

        @Builder.Default
        private String status = "ACTIVE";

        @Builder.Default
        private String statusDisplay = "Đang hoạt động";

        @Builder.Default
        private Integer maxStudents = 40;

        @Builder.Default
        private Integer studentCount = 0;

        @Builder.Default
        private Integer totalAssignments = 0;

        @Builder.Default
        private Integer ungradedAssignments = 0;

        @Builder.Default
        private String gradientClass = "from-blue-500 to-blue-600";

        @Builder.Default
        private String descriptionColorClass = "text-blue-100";

        private Long createdAt;
    }
}
