package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO cho bài làm thi (ExamSubmission)
 * Bao gồm: request nộp bài và response trả kết quả
 */
public class ExamSubmissionDTO {

    // ======================================================================
    // Request DTO: Học sinh nộp bài
    // ======================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmitRequest {

        /**
         * Bản đồ câu trả lời của học sinh
         * Key: questionIndex (số thứ tự câu hỏi, bắt đầu từ 0)
         * Value: câu trả lời học sinh chọn hoặc nhập vào
         */
        @Builder.Default
        private Map<Integer, String> answers = new HashMap<>();

        /** Thời gian thực tế làm bài tính bằng giây */
        private Integer timeTakenSeconds;
    }

    // ======================================================================
    // Response DTO: Kết quả sau khi chấm bài
    // ======================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        /** ID bài làm trong MongoDB */
        private String id;

        /** ID đề thi */
        private String examId;

        /** Tiêu đề đề thi */
        private String examTitle;

        /** ID học sinh */
        private String studentId;

        /** Tên học sinh */
        private String studentName;

        /** Điểm đạt được */
        @Builder.Default
        private Integer score = 0;

        /** Điểm tối đa của đề thi */
        @Builder.Default
        private Integer totalScore = 0;

        /** Tỷ lệ phần trăm điểm */
        @Builder.Default
        private Double percentage = 0.0;

        /** Text hiển thị điểm: "8/10" */
        private String scoreDisplay;

        /** Text hiển thị phần trăm: "80.0%" */
        private String percentageDisplay;

        /** Trạng thái bài làm: GRADED */
        private String status;

        /** Thời điểm nộp bài (epoch milliseconds) */
        private Long submittedAt;

        /** Text hiển thị ngày nộp: dd/MM/yyyy HH:mm */
        private String submittedAtDisplay;

        /** Thời gian làm bài tính bằng giây */
        private Integer timeTakenSeconds;

        /** Text hiển thị thời gian làm bài: "5m 30s" */
        private String timeTakenDisplay;

        /**
         * Câu trả lời của học sinh
         * Key: questionIndex, Value: câu trả lời
         */
        @Builder.Default
        private Map<Integer, String> answers = new HashMap<>();

        /**
         * Đáp án đúng cho từng câu (chỉ trả về sau khi chấm xong)
         * Key: questionIndex, Value: đáp án đúng
         */
        @Builder.Default
        private Map<Integer, String> correctAnswers = new HashMap<>();

        /**
         * Giải thích đáp án cho từng câu
         * Key: questionIndex, Value: giải thích
         */
        @Builder.Default
        private Map<Integer, String> explanations = new HashMap<>();

        /**
         * Kết quả từng câu: đúng (true) hay sai (false)
         * Key: questionIndex, Value: true = đúng, false = sai
         */
        @Builder.Default
        private Map<Integer, Boolean> questionResults = new HashMap<>();
    }
}
