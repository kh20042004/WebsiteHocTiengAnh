package com.english12smart.service.impl;

import com.english12smart.dto.*;
import com.english12smart.entity.AISuggestionLog;
import com.english12smart.repository.AISuggestionLogRepository;
import com.english12smart.service.AISuggestionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service Implementation cho AI Suggestion sử dụng Google Gemini API
 * Tích hợp Google Generative AI thông qua HTTP REST API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AISuggestionServiceImpl implements AISuggestionService {

    private final AISuggestionLogRepository suggestionLogRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-pro}")
    private String modelName;

    // Google Gemini API endpoint
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    /**
     * Lấy ID giáo viên từ SecurityContext
     */
    private String getCurrentTeacherId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Gợi ý tạo bài tập từ AI
     */
    @Override
    public AISuggestionResponseDTO suggestExercises(AISuggestionRequestDTO request) {
        try {
            log.info("Đang gợi ý bài tập cho unit: {}", request.getUnit());

            // Tạo prompt cho AI
            String prompt = buildExercisePrompt(request);

            // Gọi Gemini API
            String response = callGeminiAPI(prompt);

            // Parse response thành JSON
            List<ExerciseSuggestionDTO> exercises;
            
            // Kiểm tra nếu là mock data (API quota exceeded)
            if (isMockResponse(response)) {
                log.info("📚 Using mock exercises data (API quota exceeded)");
                exercises = generateMockExercises();
                response = "Mock data - API quota exceeded";
            } else {
                exercises = parseExercisesResponse(response);
            }

            // Lưu log
            String suggestionLogId = saveSuggestionLog(
                    request.getUnit(),
                    "exercise",
                    prompt,
                    response,
                    response.length() / 4
            );

            log.info("Gợi ý bài tập thành công - ID: {}", suggestionLogId);

            return AISuggestionResponseDTO.builder()
                    .status("success")
                    .message("Gợi ý bài tập thành công")
                    .suggestions(new ArrayList<>(exercises))
                    .suggestionLogId(suggestionLogId)
                    .tokensUsed(response.length() / 4)
                    .createdAt(LocalDateTime.now())
                    .notes("Có " + exercises.size() + " bài tập được gợi ý")
                    .build();

        } catch (Exception e) {
            log.error("Lỗi gợi ý bài tập: ", e);
            return buildErrorResponse("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Gợi ý tạo bài kiểm tra từ AI
     */
    @Override
    public AISuggestionResponseDTO suggestExam(AISuggestionRequestDTO request) {
        try {
            log.info("Đang gợi ý bài kiểm tra cho unit: {}", request.getUnit());

            // Tạo prompt cho AI
            String prompt = buildExamPrompt(request);

            // Gọi Gemini API
            String response = callGeminiAPI(prompt);

            // Parse response thành JSON
            ExamSuggestionDTO exam;
            
            // Kiểm tra nếu là mock data (API quota exceeded)
            if (isMockResponse(response)) {
                log.info("📚 Using mock exam data (API quota exceeded)");
                exam = generateMockExam();
                response = "Mock data - API quota exceeded";
            } else {
                exam = parseExamResponse(response);
            }

            // Lưu log
            String suggestionLogId = saveSuggestionLog(
                    request.getUnit(),
                    "exam",
                    prompt,
                    response,
                    response.length() / 4
            );

            log.info("Gợi ý bài kiểm tra thành công - ID: {}", suggestionLogId);

            return AISuggestionResponseDTO.builder()
                    .status("success")
                    .message("Gợi ý bài kiểm tra thành công")
                    .suggestions(Arrays.asList(exam))
                    .suggestionLogId(suggestionLogId)
                    .tokensUsed(response.length() / 4)
                    .createdAt(LocalDateTime.now())
                    .notes("Bài kiểm tra gồm " + (exam.getSections() != null ? exam.getSections().size() : 0) + " phần")
                    .build();

        } catch (Exception e) {
            log.error("Lỗi gợi ý bài kiểm tra: ", e);
            return buildErrorResponse("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Cải thiện nội dung bài tập
     */
    @Override
    public AISuggestionResponseDTO improveContent(AISuggestionRequestDTO request) {
        try {
            log.info("Đang cải thiện nội dung");

            String prompt = buildImprovePrompt(request);
            String response = callGeminiAPI(prompt);

            String suggestionLogId = saveSuggestionLog(
                    "improve",
                    "improve",
                    prompt,
                    response,
                    response.length() / 4
            );

            return AISuggestionResponseDTO.builder()
                    .status("success")
                    .message("Cải thiện nội dung thành công")
                    .suggestions(Arrays.asList(response))
                    .suggestionLogId(suggestionLogId)
                    .tokensUsed(response.length() / 4)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Lỗi cải thiện nội dung: ", e);
            return buildErrorResponse("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Tạo giải thích cho một khái niệm
     */
    @Override
    @Cacheable(value = "explanations", key = "#concept")
    public AISuggestionResponseDTO generateExplanation(String concept) {
        try {
            log.info("Đang tạo giải thích cho: {}", concept);

            String prompt = String.format(
                    "Hãy giải thích chi tiết khái niệm tiếng Anh '%s' cho học sinh lớp 12. " +
                    "Bao gồm: định nghĩa, ví dụ, cách sử dụng, và mẹo ghi nhớ. " +
                    "Trả lời bằng tiếng Việt.",
                    concept
            );

            String response = callGeminiAPI(prompt);

            return AISuggestionResponseDTO.builder()
                    .status("success")
                    .message("Giải thích được tạo thành công")
                    .suggestions(Arrays.asList(response))
                    .tokensUsed(response.length() / 4)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Lỗi tạo giải thích: ", e);
            return buildErrorResponse("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Gợi ý mức độ khó cho câu hỏi
     */
    @Override
    public String suggestDifficulty(String question) {
        try {
            String prompt = String.format(
                    "Đánh giá mức độ khó của câu hỏi tiếng Anh sau (A1, A2, B1, B2, C1):\n" +
                    "\"%s\"\n" +
                    "Chỉ trả lời một mức độ khó duy nhất (A1 hoặc A2 hoặc B1 hoặc B2 hoặc C1)",
                    question
            );

            String response = callGeminiAPI(prompt).trim();
            return response;
        } catch (Exception e) {
            log.error("Lỗi gợi ý mức độ khó: ", e);
            return "B1";
        }
    }

    /**
     * Lưu feedback từ giáo viên
     */
    @Override
    public void saveFeedback(String suggestionLogId, Integer rating, String note) {
        try {
            AISuggestionLog suggestionLog = suggestionLogRepository.findById(suggestionLogId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy gợi ý"));

            suggestionLog.setFeedbackRating(rating);
            suggestionLog.setFeedbackNote(note);
            suggestionLog.setFeedbackAt(LocalDateTime.now());
            suggestionLog.setStatus(rating >= 4 ? "accepted" : "rejected");

            suggestionLogRepository.save(suggestionLog);
            log.info("Feedback lưu thành công cho: {}", suggestionLogId);

        } catch (Exception e) {
            log.error("Lỗi lưu feedback: ", e);
            throw new RuntimeException("Không thể lưu feedback: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử gợi ý của giáo viên
     */
    @Override
    public List<Object> getSuggestionHistory(String teacherId) {
        List<AISuggestionLog> logs = suggestionLogRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
        return new ArrayList<>(logs);
    }

    /**
     * Xóa một gợi ý
     */
    @Override
    public void deleteSuggestion(String suggestionLogId) {
        try {
            suggestionLogRepository.deleteById(suggestionLogId);
            log.info("Gợi ý đã xóa: {}", suggestionLogId);
        } catch (Exception e) {
            log.error("Lỗi xóa gợi ý: ", e);
            throw new RuntimeException("Không thể xóa gợi ý: " + e.getMessage());
        }
    }

    // ================== HELPER METHODS ==================

    /**
     * Gọi Google Gemini API
     * Nếu API quota exceeded (429), trả về mock data
     */
    private String callGeminiAPI(String prompt) throws Exception {
        try {
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> partMap = new HashMap<>();
            partMap.put("text", prompt);
            requestBody.put("contents", Arrays.asList(
                    Map.of("parts", Arrays.asList(partMap))
            ));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            // Build URL with API key
            String url = GEMINI_API_URL.replace("{model}", modelName) + "?key=" + geminiApiKey;

            log.debug("Calling Gemini API: {}", url);

            // Make request
            String response = restTemplate.postForObject(url, entity, String.class);

            // Extract text from response
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText("");
                }
            }

            log.warn("Unexpected Gemini API response format");
            return response;

        } catch (HttpClientErrorException e) {
            // Nếu lỗi 429 (Too Many Requests) - quota exceeded
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("⚠️  API quota exceeded (429). Using mock data fallback.");
                return "{\"isMock\": true}"; // Return marker để biết là mock data
            }
            
            log.error("Lỗi gọi Gemini API (HTTP {}): {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Không thể kết nối đến AI: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Lỗi gọi Gemini API: ", e);
            throw new RuntimeException("Không thể kết nối đến AI: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo prompt cho việc gợi ý bài tập
     */
    private String buildExercisePrompt(AISuggestionRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tạo ").append(request.getQuantity() != null ? request.getQuantity() : 1).append(" bài tập tiếng Anh\n\n");
        sb.append("Chi tiết:\n");
        sb.append("- Unit/Chủ đề: ").append(request.getUnit()).append("\n");
        sb.append("- Mức độ: ").append(request.getSkillLevel()).append("\n");
        sb.append("- Loại bài: ").append(request.getExerciseType()).append("\n");

        if (request.getCustomPrompt() != null && !request.getCustomPrompt().isEmpty()) {
            sb.append("- Yêu cầu thêm: ").append(request.getCustomPrompt()).append("\n");
        }

        sb.append("\nFormat trả lời (JSON):\n");
        sb.append("[\n");
        sb.append("  {\n");
        sb.append("    \"question\": \"...\",\n");
        sb.append("    \"options\": [\"option1\", \"option2\", \"option3\", \"option4\"],\n");
        sb.append("    \"correctAnswerIndex\": 0,\n");
        sb.append("    \"explanation\": \"...\",\n");
        sb.append("    \"difficulty\": \"").append(request.getSkillLevel()).append("\",\n");
        sb.append("    \"type\": \"").append(request.getExerciseType() != null ? request.getExerciseType() : "multiple_choice").append("\"\n");
        sb.append("  }\n");
        sb.append("]\n\n");
        sb.append("Trả lời CHỈ định dạng JSON, không thêm văn bản khác");

        return sb.toString();
    }

    /**
     * Tạo prompt cho việc gợi ý bài kiểm tra
     */
    private String buildExamPrompt(AISuggestionRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tạo bài kiểm tra tiếng Anh\n\n");
        sb.append("Chi tiết:\n");
        sb.append("- Tiêu đề: ").append(request.getExamType()).append(" Exam\n");
        sb.append("- Unit/Chủ đề: ").append(request.getUnit()).append("\n");
        sb.append("- Thời gian: ").append(request.getDuration() != null ? request.getDuration() : 45).append(" phút\n");
        sb.append("- Số câu: ").append(request.getTotalQuestions() != null ? request.getTotalQuestions() : 20).append("\n");
        sb.append("- Mức độ: ").append(request.getSkillLevel()).append("\n");

        sb.append("\nFormat trả lời: JSON chứa tiêu đề và hướng dẫn\n");
        sb.append("Trả lời CHỈ định dạng JSON");

        return sb.toString();
    }

    /**
     * Tạo prompt cho việc cải thiện nội dung
     */
    private String buildImprovePrompt(AISuggestionRequestDTO request) {
        return String.format(
                "Xin vui lòng sửa lỗi và cải thiện bài tập tiếng Anh sau:\n\n" +
                "\"%s\"\n\n" +
                "Lưu ý:\n" +
                "- Kiểm tra ngữ pháp, spelling\n" +
                "- Làm rõ ý nghĩa\n" +
                "- Thêm các phương án trả lời khác (nếu là multiple choice)\n" +
                "- Giữ mức độ khó tương tự\n" +
                "- Trả lời bằng tiếng Việt\n\n" +
                "Ghi rõ những gì được cải thiện",
                request.getOriginalContent() != null ? request.getOriginalContent() : ""
        );
    }

    /**
     * Parse response từ AI thành danh sách ExerciseSuggestionDTO
     */
    private List<ExerciseSuggestionDTO> parseExercisesResponse(String response) {
        try {
            List<ExerciseSuggestionDTO> exercises = new ArrayList<>();

            // Loại bỏ ``` nếu có
            String cleanedResponse = response.replaceAll("```json", "").replaceAll("```", "").trim();

            // Parse JSON array
            ExerciseSuggestionDTO[] exercisesArray = objectMapper.readValue(
                    cleanedResponse,
                    ExerciseSuggestionDTO[].class
            );

            exercises.addAll(Arrays.asList(exercisesArray));
            return exercises;

        } catch (Exception e) {
            log.error("Lỗi parse exercise response: ", e);
            return Arrays.asList(
                    ExerciseSuggestionDTO.builder()
                            .question(response)
                            .difficulty("B1")
                            .type("text")
                            .build()
            );
        }
    }

    /**
     * Parse response từ AI thành ExamSuggestionDTO
     */
    private ExamSuggestionDTO parseExamResponse(String response) {
        try {
            String cleanedResponse = response.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(cleanedResponse, ExamSuggestionDTO.class);

        } catch (Exception e) {
            log.error("Lỗi parse exam response: ", e);
            return ExamSuggestionDTO.builder()
                    .title("Exam")
                    .description(response)
                    .sections(new ArrayList<>())
                    .build();
        }
    }

    /**
     * Lưu suggestion log vào database
     */
    private String saveSuggestionLog(String unit, String type, String prompt, String response, Integer tokensUsed) {
        AISuggestionLog log = AISuggestionLog.builder()
                .teacherId(getCurrentTeacherId())
                .type(type)
                .unit(unit)
                .prompt(prompt)
                .aiResponse(response)
                .tokensUsed(tokensUsed)
                .isAccepted(false)
                .isSaved(false)
                .createdAt(LocalDateTime.now())
                .status("pending")
                .build();

        AISuggestionLog savedLog = suggestionLogRepository.save(log);
        return savedLog.getId();
    }

    /**
     * Tạo response lỗi
     */
    private AISuggestionResponseDTO buildErrorResponse(String message) {
        return AISuggestionResponseDTO.builder()
                .status("error")
                .message(message)
                .suggestions(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ================== MOCK DATA GENERATORS (Fallback khi API quota exceeded) ==================

    /**
     * Generate mock exercises khi API quota exceeded
     */
    private List<ExerciseSuggestionDTO> generateMockExercises() {
        return Arrays.asList(
                ExerciseSuggestionDTO.builder()
                        .question("Choose the correct present perfect form: 'I _____ (not see) this movie before.'")
                        .options(Arrays.asList("haven't seen", "didn't see", "haven't saw", "don't see"))
                        .correctAnswerIndex(0)
                        .explanation("Dùng present perfect 'have/haven't + past participle' cho trải nghiệm chưa bao giờ xảy ra")
                        .difficulty("B1")
                        .type("multiple_choice")
                        .build(),
                ExerciseSuggestionDTO.builder()
                        .question("Complete: 'She _____ her studies for 3 years now.'")
                        .options(Arrays.asList("has been continuing", "has continued", "continues", "is continuing"))
                        .correctAnswerIndex(1)
                        .explanation("Present perfect dùng cho hành động bắt đầu trong quá khứ kéo dài đến hiện tại")
                        .difficulty("B1")
                        .type("multiple_choice")
                        .build(),
                ExerciseSuggestionDTO.builder()
                        .question("Fill in: 'How long _____ (you/work) here?'")
                        .options(Arrays.asList("have you worked", "do you work", "have you been working", "are you working"))
                        .correctAnswerIndex(0)
                        .explanation("Present perfect dùng để hỏi khoảng thời gian từ khi bắt đầu đến bây giờ")
                        .difficulty("A2")
                        .type("fill_blank")
                        .build()
        );
    }

    /**
     * Generate mock exam khi API quota exceeded
     */
    private ExamSuggestionDTO generateMockExam() {
        List<ExerciseSuggestionDTO> section1Questions = Arrays.asList(
                ExerciseSuggestionDTO.builder()
                        .question("The student _____ homework every day.")
                        .options(Arrays.asList("do", "does", "doing", "done"))
                        .correctAnswerIndex(1)
                        .difficulty("A2")
                        .type("multiple_choice")
                        .build(),
                ExerciseSuggestionDTO.builder()
                        .question("She _____ to school yesterday.")
                        .options(Arrays.asList("go", "goes", "went", "going"))
                        .correctAnswerIndex(2)
                        .difficulty("A2")
                        .type("multiple_choice")
                        .build()
        );

        List<ExamSuggestionDTO.ExamSectionDTO> sections = Arrays.asList(
                ExamSuggestionDTO.ExamSectionDTO.builder()
                        .name("Present Simple & Past Simple")
                        .description("Test các thì cơ bản")
                        .questionCount(2)
                        .pointPerQuestion(5.0)
                        .questions(section1Questions)
                        .build(),
                ExamSuggestionDTO.ExamSectionDTO.builder()
                        .name("Vocabulary")
                        .description("Test từ vựng Unit 1")
                        .questionCount(2)
                        .pointPerQuestion(5.0)
                        .questions(new ArrayList<>())
                        .build()
        );

        return ExamSuggestionDTO.builder()
                .title("Midterm Exam - Unit 1")
                .description("Test các thì cơ bản và từ vựng Unit 1")
                .duration(60)
                .difficulty("A2")
                .sections(sections)
                .instructions("Làm bài trong 60 phút. Không được dùng tài liệu.")
                .build();
    }

    /**
     * Parse mock data marker
     */
    private boolean isMockResponse(String response) {
        try {
            JsonNode node = objectMapper.readTree(response);
            return node.path("isMock").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    }
}

