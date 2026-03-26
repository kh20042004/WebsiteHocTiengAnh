package com.english12smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO cho tính năng đề thi (Exam)
 * Bao gồm: request tạo đề, response trả về, và các inner class câu hỏi
 */
public class ExamDTO {

    // ======================================================================
    // Request DTO: Tạo đề thi mới
    // ======================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        /** Tiêu đề đề thi, bắt buộc nhập */
        @NotBlank(message = "Tiêu đề đề thi không được để trống")
        private String title;

        /** Mô tả / hướng dẫn làm bài */
        private String description;

        /** ID lớp học được giao đề, bắt buộc chọn */
        @NotBlank(message = "Vui lòng chọn lớp học")
        private String classroomId;

        /**
         * Thời gian làm bài tính bằng phút
         * null hoặc 0 = không giới hạn thời gian
         */
        @Min(value = 1, message = "Thời gian làm bài phải ít nhất 1 phút")
        private Integer timeLimitMinutes;

        /**
         * Danh sách câu hỏi tùy chỉnh do giáo viên tự soạn
         * Được ghép cùng với câu hỏi import từ Exercise bank
         */
        @Builder.Default
        private List<QuestionRequest> questions = new ArrayList<>();

        /**
         * Danh sách ID của Exercise có sẵn để import câu hỏi
         * Câu hỏi từ Exercise sẽ được sao chép vào đề thi
         */
        @Builder.Default
        private List<String> exerciseIds = new ArrayList<>();
    }

    // ======================================================================
    // Request DTO: Câu hỏi gửi lên khi tạo đề
    // ======================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionRequest {

        /** Nội dung câu hỏi */
        @NotBlank(message = "Nội dung câu hỏi không được để trống")
        private String questionText;

        /**
         * Loại câu hỏi: MULTIPLE_CHOICE | TRUE_FALSE | FILL_IN_BLANK
         */
        private String type;

        /** Các lựa chọn trả lời (dùng cho MULTIPLE_CHOICE và TRUE_FALSE) */
        @Builder.Default
        private List<String> options = new ArrayList<>();

        /** Đáp án đúng */
        @NotBlank(message = "Đáp án đúng không được để trống")
        private String correctAnswer;

        /** Giải thích đáp án (tùy chọn) */
        private String explanation;

        /** Điểm của câu này, mặc định là 1 */
        @Builder.Default
        private Integer score = 1;

        /** ID Exercise nguồn nếu import từ ngân hàng bài tập */
        private String sourceExerciseId;
    }

    // ======================================================================
    // Response DTO: Trả về thông tin đề thi
    // ======================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        /** ID MongoDB của đề thi */
        private String id;

        /** Tiêu đề đề thi */
        private String title;

        /** Mô tả / hướng dẫn làm bài */
        private String description;

        /** Mã PIN 5 chữ số học sinh dùng để vào thi */
        private String pinCode;

        /** ID lớp học */
        private String classroomId;

        /** Tên lớp học */
        private String classroomName;

        /** ID giáo viên tạo đề */
        private String teacherId;

        /** Thời gian làm bài (phút), null = không giới hạn */
        private Integer timeLimitMinutes;

        /** Text hiển thị thời gian làm bài */
        private String timeLimitDisplay;

        /** Trạng thái: DRAFT | ACTIVE | CLOSED */
        private String status;

        /** Text hiển thị trạng thái bằng tiếng Việt */
        private String statusDisplay;

        /** CSS class cho badge trạng thái */
        private String statusBadgeClass;

        /** Tổng số học sinh trong lớp */
        @Builder.Default
        private Integer totalStudents = 0;

        /** Số bài đã nộp */
        @Builder.Default
        private Integer submittedCount = 0;

        /** Tổng điểm tối đa của đề thi */
        @Builder.Default
        private Integer totalScore = 0;

        /** Số lượng câu hỏi */
        @Builder.Default
        private Integer questionCount = 0;

        /**
         * Danh sách câu hỏi.
         * Khi trả về cho học sinh, correctAnswer sẽ bị ẩn.
         * Khi trả về cho giáo viên, có đủ thông tin.
         */
        @Builder.Default
        private List<QuestionResponse> questions = new ArrayList<>();

        /** Thời điểm tạo (epoch milliseconds) */
        private Long createdAt;

        /** Text hiển thị ngày tạo: dd/MM/yyyy */
        private String createdAtDisplay;
    }

    // ======================================================================
    // Response DTO: Câu hỏi trả về trong Response
    // ======================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResponse {

        /** Số thứ tự câu hỏi (bắt đầu từ 0) */
        private Integer questionIndex;

        /** Nội dung câu hỏi */
        private String questionText;

        /** Loại câu hỏi */
        private String type;

        /** Các lựa chọn trả lời */
        @Builder.Default
        private List<String> options = new ArrayList<>();

        /**
         * Đáp án đúng.
         * Sẽ bị đặt thành null khi trả về cho học sinh đang thi.
         * Chỉ hiện sau khi học sinh đã nộp bài.
         */
        private String correctAnswer;

        /** Giải thích đáp án */
        private String explanation;

        /** Điểm của câu này */
        @Builder.Default
        private Integer score = 1;

        /** ID Exercise gốc (nếu import từ ngân hàng) */
        private String sourceExerciseId;
    }
}
