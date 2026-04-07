package com.english12smart.service;

import com.english12smart.dto.ExamDTO;
import com.english12smart.dto.ExamSubmissionDTO;
import com.english12smart.entity.Exam;
import com.english12smart.entity.Exam.ExamQuestion;
import com.english12smart.entity.ExamSubmission;
import com.english12smart.entity.Exercise;
import com.english12smart.entity.ExamAntiFraudLog;
import com.english12smart.exception.BadRequestException;
import com.english12smart.exception.ResourceNotFoundException;
import com.english12smart.repository.ClassroomRepository;
import com.english12smart.repository.ExamRepository;
import com.english12smart.repository.ExamSubmissionRepository;
import com.english12smart.repository.ExerciseRepository;
import com.english12smart.repository.ExamAntiFraudLogRepository;
import com.english12smart.util.ExamShufflingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service xử lý logic nghiệp vụ cho tính năng đề thi (Exam)
 * Gồm: tạo đề thi, sinh mã PIN, mở/đóng thi, học sinh nộp bài, chấm điểm tự động
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ClassroomRepository classroomRepository;
    private final ExerciseRepository exerciseRepository;

    /** Random dùng để sinh mã PIN ngẫu nhiên */
    private final Random random = new Random();

    // ======================================================================
    // Phương thức dành cho GIÁO VIÊN
    // ======================================================================

    /**
     * Tạo đề thi mới, sinh mã PIN ngẫu nhiên 5 chữ số
     *
     * @param teacherId ID giáo viên tạo đề
     * @param request   Thông tin đề thi từ form
     * @return ExamDTO.Response chứa đủ thông tin kể cả mã PIN mới sinh
     */
    public ExamDTO.Response createExam(String teacherId, ExamDTO.CreateRequest request, boolean adminOverride) {
        log.info("Giáo viên {} đang tạo đề thi mới: {}", teacherId, request.getTitle());

        // Kiểm tra lớp học tồn tại và giáo viên có quyền
        var classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + request.getClassroomId()));

        // Đảm bảo giáo viên chỉ tạo đề cho lớp của mình
        if (!adminOverride && !classroom.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền tạo đề thi cho lớp này");
        }

        // Xây dựng danh sách câu hỏi từ 2 nguồn:
        // 1. Import câu hỏi từ Exercise có sẵn trong ngân hàng
        // 2. Câu hỏi tự soạn do giáo viên nhập trực tiếp
        List<ExamQuestion> allQuestions = new ArrayList<>();

        // ---- Nguồn 1: Import từ Exercise bank ----
        if (request.getExerciseIds() != null && !request.getExerciseIds().isEmpty()) {
            log.info("Đang import câu hỏi từ {} exercise(s)", request.getExerciseIds().size());
            for (String exerciseId : request.getExerciseIds()) {
                Exercise exercise = exerciseRepository.findById(exerciseId).orElse(null);
                if (exercise == null || exercise.getQuestions() == null) {
                    log.warn("Bỏ qua exercise không tìm thấy: {}", exerciseId);
                    continue;
                }
                // Sao chép từng câu hỏi từ Exercise vào ExamQuestion
                // Dữ liệu được sao chép (denormalized), không chỉ lưu reference
                for (Exercise.Question q : exercise.getQuestions()) {
                    allQuestions.add(ExamQuestion.builder()
                            .questionIndex(allQuestions.size()) // đánh số thứ tự liên tiếp
                            .questionText(q.getQuestionText())
                            .type(exercise.getType())           // lấy loại từ Exercise cha
                            .options(q.getOptions() != null ? new ArrayList<>(q.getOptions()) : new ArrayList<>())
                            .correctAnswer(q.getCorrectAnswer())
                            .explanation(q.getExplanation())
                            .score(q.getScore() != null ? q.getScore() : 1)
                            .sourceExerciseId(exerciseId)       // ghi nguồn gốc câu hỏi
                            .build());
                }
            }
        }

        // ---- Nguồn 2: Câu hỏi tự soạn trực tiếp ----
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            log.info("Đang thêm {} câu hỏi tự soạn", request.getQuestions().size());
            for (ExamDTO.QuestionRequest qReq : request.getQuestions()) {
                allQuestions.add(ExamQuestion.builder()
                        .questionIndex(allQuestions.size()) // tiếp tục đánh số từ cuối import
                        .questionText(qReq.getQuestionText())
                        .type(qReq.getType())
                        .options(qReq.getOptions() != null ? new ArrayList<>(qReq.getOptions()) : new ArrayList<>())
                        .correctAnswer(qReq.getCorrectAnswer())
                        .explanation(qReq.getExplanation())
                        .score(qReq.getScore() != null ? qReq.getScore() : 1)
                        .sourceExerciseId(null) // câu hỏi tự soạn, không có nguồn gốc
                        .build());
            }
        }

        // Kiểm tra đề thi phải có ít nhất 1 câu hỏi
        if (allQuestions.isEmpty()) {
            throw new BadRequestException("Đề thi phải có ít nhất 1 câu hỏi");
        }

        // Sinh mã PIN 5 chữ số, đảm bảo duy nhất trong hệ thống
        String pinCode = generateUniquePin();
        log.info("Đã sinh mã PIN: {} cho đề thi: {}", pinCode, request.getTitle());

        // Đặt thời gian làm bài: nếu <= 0 thì coi như không giới hạn
        Integer timeLimitMinutes = request.getTimeLimitMinutes();
        if (timeLimitMinutes != null && timeLimitMinutes <= 0) {
            timeLimitMinutes = null;
        }

        long now = System.currentTimeMillis();

        // Tạo entity Exam và lưu vào database
        Exam exam = Exam.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .teacherId(adminOverride ? classroom.getTeacherId() : teacherId)
                .createdBy(teacherId)
                .classroomId(request.getClassroomId())
                .classroomName(classroom.getName())
                .pinCode(pinCode)
                .timeLimitMinutes(timeLimitMinutes)
                .status("ACTIVE") // mặc định mở ngay sau khi tạo
                .questions(allQuestions)
                .totalStudents(classroom.getStudentCount())
                .submittedCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        exam = examRepository.save(exam);
        log.info("Đã tạo đề thi ID: {} với {} câu hỏi, PIN: {}", exam.getId(), allQuestions.size(), pinCode);

        return toResponse(exam, false); // false = trả về đầy đủ (bao gồm đáp án) cho giáo viên
    }

    /**
     * Lấy danh sách tất cả đề thi của một giáo viên
     *
     * @param teacherId ID giáo viên
     * @return Danh sách đề thi, sắp xếp mới nhất trước
     */
        public List<ExamDTO.Response> getExamsByTeacher(String teacherId, boolean adminOverride) {
        log.info("Lấy danh sách đề thi của giáo viên: {}", teacherId);
        List<Exam> exams = adminOverride
            ? examRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(
                    b.getCreatedAt() != null ? b.getCreatedAt() : 0L,
                    a.getCreatedAt() != null ? a.getCreatedAt() : 0L))
                .collect(Collectors.toList())
            : examRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);

        return exams
                .stream()
                .map(exam -> toResponse(exam, false)) // giáo viên thấy đầy đủ thông tin
                .collect(Collectors.toList());
    }

    /**
     * Lấy kết quả tất cả bài làm của một đề thi (dành cho giáo viên xem)
     *
     * @param examId    ID đề thi
     * @param teacherId ID giáo viên (để kiểm tra quyền)
     * @return Danh sách kết quả, sắp xếp theo điểm cao nhất
     */
    public List<ExamSubmissionDTO.Response> getExamResults(String examId, String teacherId, boolean adminOverride) {
        log.info("Giáo viên {} đang xem kết quả đề thi: {}", teacherId, examId);

        // Kiểm tra đề thi tồn tại và thuộc giáo viên này
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + examId));

        if (!adminOverride && !exam.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền xem kết quả đề thi này");
        }

        // Lấy tất cả bài làm, sắp xếp theo điểm cao nhất
        return examSubmissionRepository.findByExamIdOrderByScoreDesc(examId)
                .stream()
                .map(sub -> toSubmissionResponse(sub, exam))
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin đề thi (chỉnh sửa)
     * Chỉ được cập nhật tiêu đề, mô tả, thời gian làm bài
     * Không được cập nhật câu hỏi khi đã có bài làm
     *
     * @param examId    ID đề thi
     * @param teacherId ID giáo viên (kiểm tra quyền)
     * @param request   Thông tin cập nhật
     * @return Đề thi đã cập nhật
     */
    public ExamDTO.Response updateExam(String examId, String teacherId, ExamDTO.CreateRequest request, boolean adminOverride) {
        log.info("Giáo viên {} cập nhật đề thi: {}", teacherId, examId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + examId));

        if (!adminOverride && !exam.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền cập nhật đề thi này");
        }

        // Cập nhật các thông tin cơ bản
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            exam.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            exam.setDescription(request.getDescription());
        }

        // Cập nhật thời gian làm bài
        Integer timeLimitMinutes = request.getTimeLimitMinutes();
        if (timeLimitMinutes != null && timeLimitMinutes <= 0) {
            timeLimitMinutes = null;
        }
        exam.setTimeLimitMinutes(timeLimitMinutes);

        // Cập nhật thời gian sửa đổi
        exam.setUpdatedAt(System.currentTimeMillis());

        // Nếu chưa có bài làm, cho phép cập nhật câu hỏi
        long submissionCount = examSubmissionRepository.countByExamId(examId);
        if (submissionCount == 0) {
            // Tái xây dựng danh sách câu hỏi từ request (giống như createExam)
            List<ExamQuestion> allQuestions = new ArrayList<>();

            if (request.getExerciseIds() != null && !request.getExerciseIds().isEmpty()) {
                log.info("Cập nhật câu hỏi import từ {} exercise(s)", request.getExerciseIds().size());
                for (String exerciseId : request.getExerciseIds()) {
                    Exercise exercise = exerciseRepository.findById(exerciseId).orElse(null);
                    if (exercise == null || exercise.getQuestions() == null) {
                        log.warn("Bỏ qua exercise không tìm thấy: {}", exerciseId);
                        continue;
                    }
                    for (Exercise.Question q : exercise.getQuestions()) {
                        allQuestions.add(ExamQuestion.builder()
                                .questionIndex(allQuestions.size())
                                .questionText(q.getQuestionText())
                                .type(exercise.getType())
                                .options(q.getOptions() != null ? new ArrayList<>(q.getOptions()) : new ArrayList<>())
                                .correctAnswer(q.getCorrectAnswer())
                                .explanation(q.getExplanation())
                                .score(q.getScore() != null ? q.getScore() : 1)
                                .sourceExerciseId(exerciseId)
                                .build());
                    }
                }
            }

            if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
                log.info("Cập nhật {} câu hỏi tự soạn", request.getQuestions().size());
                for (ExamDTO.QuestionRequest q : request.getQuestions()) {
                    allQuestions.add(ExamQuestion.builder()
                            .questionIndex(allQuestions.size())
                            .questionText(q.getQuestionText())
                            .type(q.getType() != null ? q.getType() : "MULTIPLE_CHOICE")
                            .options(q.getOptions() != null ? new ArrayList<>(q.getOptions()) : new ArrayList<>())
                            .correctAnswer(q.getCorrectAnswer())
                            .explanation(q.getExplanation())
                            .score(q.getScore() != null ? q.getScore() : 1)
                            .build());
                }
            }

            exam.setQuestions(allQuestions);
            log.info("Cập nhật {} câu hỏi", allQuestions.size());
        } else {
            log.warn("Đề thi {} đã có {} bài làm, không cập nhật câu hỏi", examId, submissionCount);
        }

        examRepository.save(exam);
        log.info("Đã cập nhật đề thi: {}", examId);
        return toResponse(exam, true);
    }

    /**
     * Cập nhật trạng thái đề thi (ACTIVE/CLOSED/DRAFT)
     *
     * @param examId    ID đề thi
     * @param teacherId ID giáo viên (kiểm tra quyền)
     * @param status    Trạng thái mới
     * @return Đề thi đã cập nhật
     */
    public ExamDTO.Response updateExamStatus(String examId, String teacherId, String status, boolean adminOverride) {
        log.info("Giáo viên {} cập nhật trạng thái đề thi {} → {}", teacherId, examId, status);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + examId));

        if (!adminOverride && !exam.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền cập nhật đề thi này");
        }

        // Kiểm tra giá trị trạng thái hợp lệ
        if (!List.of("ACTIVE", "CLOSED", "DRAFT").contains(status.toUpperCase())) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + status);
        }

        exam.setStatus(status.toUpperCase());
        exam.setUpdatedAt(System.currentTimeMillis());
        exam = examRepository.save(exam);

        return toResponse(exam, false);
    }

    /**
     * Xóa đề thi (chỉ xóa được khi không có bài làm nào)
     *
     * @param examId    ID đề thi
     * @param teacherId ID giáo viên (kiểm tra quyền)
     */
    public void deleteExam(String examId, String teacherId, boolean adminOverride) {
        log.info("Giáo viên {} đang xóa đề thi: {}", teacherId, examId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + examId));

        if (!adminOverride && !exam.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền xóa đề thi này");
        }

        // Không cho xóa nếu đã có học sinh nộp bài
        long submissionCount = examSubmissionRepository.countByExamId(examId);
        if (submissionCount > 0) {
            throw new BadRequestException("Không thể xóa đề thi đã có " + submissionCount + " bài làm. Hãy đóng đề thi thay vì xóa.");
        }

        examRepository.delete(exam);
        log.info("Đã xóa đề thi: {}", examId);
    }

    /**
     * Lấy chi tiết một đề thi để chỉnh sửa
     * Chỉ giáo viên tạo hoặc admin mới có quyền
     *
     * @param examId    ID đề thi
     * @param teacherId ID giáo viên (kiểm tra quyền)
     * @param adminOverride Có phải admin không
     * @return Thông tin đề thi (CÓ đáp án đúng và giải thích)
     */
    public ExamDTO.Response getExamById(String examId, String teacherId, boolean adminOverride) {
        log.info("Lấy chi tiết đề thi: {} (giáo viên: {})", examId, teacherId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + examId));

        if (!adminOverride && !exam.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Bạn không có quyền truy cập đề thi này");
        }

        // Trả về toàn bộ thông tin bao gồm đáp án đúng (showAnswers=true cho điều chỉnh)
        return toResponse(exam, true);
    }

    // ======================================================================
    // Phương thức dành cho HỌC SINH
    // ======================================================================

    /**
     * Tìm đề thi theo mã PIN và xác thực quyền truy cập của học sinh
     * Đáp án đúng (correctAnswer) được ẩn trước khi trả về cho học sinh
     *
     * @param pinCode   Mã PIN 5 chữ số học sinh nhập
     * @param studentId ID học sinh đang đăng nhập
     * @return Thông tin đề thi (KHÔNG có đáp án đúng)
     */
    public ExamDTO.Response getExamByPin(String pinCode, String studentId) {
        log.info("Học sinh {} đang tra cứu đề thi với PIN: {}", studentId, pinCode);

        // Tìm đề thi với mã PIN này
        Exam exam = examRepository.findByPinCode(pinCode)
                .orElseThrow(() -> new ResourceNotFoundException("Mã PIN không hợp lệ hoặc đề thi không tồn tại"));

        // Kiểm tra đề thi đang trong trạng thái mở
        if (!"ACTIVE".equals(exam.getStatus())) {
            throw new BadRequestException("Đề thi đã đóng, không thể tham gia");
        }

        // Kiểm tra học sinh có thuộc lớp được giao đề thi này không
        var classroom = classroomRepository.findById(exam.getClassroomId()).orElse(null);
        if (classroom == null || classroom.getStudentIds() == null
                || !classroom.getStudentIds().contains(studentId)) {
            throw new BadRequestException("Bạn không thuộc lớp được giao đề thi này");
        }

        // Kiểm tra học sinh đã nộp bài chưa
        if (examSubmissionRepository.existsByExamIdAndStudentId(exam.getId(), studentId)) {
            throw new BadRequestException("Bạn đã làm bài thi này rồi");
        }

        log.info("Học sinh {} hợp lệ, trả về đề thi: {}", studentId, exam.getTitle());
        // true = ẩn đáp án cho học sinh
        return toResponse(exam, true);
    }

    /**
     * Học sinh bắt đầu làm bài: tạo bài làm với trạng thái IN_PROGRESS
     *
     * @param examId      ID đề thi
     * @param studentId   ID học sinh
     * @param studentName Tên học sinh (để hiển thị trên bảng kết quả)
     * @return ID bài làm vừa tạo
     */
    public String startExam(String examId, String studentId, String studentName) {
        log.info("Học sinh {} bắt đầu làm bài thi: {}", studentId, examId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + examId));

        // Ngăn làm lại bài đã nộp
        if (examSubmissionRepository.existsByExamIdAndStudentId(examId, studentId)) {
            throw new BadRequestException("Bạn đã làm bài thi này rồi");
        }

        // Tạo bài làm mới, đánh dấu IN_PROGRESS
        ExamSubmission submission = ExamSubmission.builder()
                .examId(examId)
                .examTitle(exam.getTitle())
                .studentId(studentId)
                .studentName(studentName)
                .classroomId(exam.getClassroomId())
                .answers(new HashMap<>())
                .score(0)
                .totalScore(exam.getTotalScore())
                .percentage(0.0)
                .status("IN_PROGRESS")
                .startedAt(System.currentTimeMillis())
                .build();

        submission = examSubmissionRepository.save(submission);
        log.info("Đã tạo bài làm ID: {} cho học sinh {} - đề thi: {}", submission.getId(), studentId, examId);

        return submission.getId();
    }

    /**
     * Học sinh nộp bài: hệ thống tự động chấm điểm ngay lập tức
     *
     * @param submissionId ID bài làm
     * @param studentId    ID học sinh (kiểm tra quyền)
     * @param request      Câu trả lời và thời gian làm
     * @return Kết quả chi tiết sau khi chấm (có đáp án đúng và giải thích)
     */
    public ExamSubmissionDTO.Response submitExam(String submissionId, String studentId,
                                                  ExamSubmissionDTO.SubmitRequest request) {
        log.info("Học sinh {} đang nộp bài làm: {}", studentId, submissionId);

        // Lấy bài làm và kiểm tra quyền
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài làm với ID: " + submissionId));

        if (!submission.getStudentId().equals(studentId)) {
            throw new BadRequestException("Bạn không có quyền nộp bài làm này");
        }

        // Không cho nộp lại bài đã chấm
        if ("GRADED".equals(submission.getStatus()) || "SUBMITTED".equals(submission.getStatus())) {
            throw new BadRequestException("Bài làm này đã được nộp rồi");
        }

        // Lấy đề thi để có đáp án đúng để chấm
        Exam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi"));

        // ---- TỰ ĐỘNG CHẤM ĐIỂM ----
        Map<Integer, String> studentAnswers = request.getAnswers() != null ? request.getAnswers() : new HashMap<>();
        Map<Integer, String> correctAnswers = new HashMap<>();
        Map<Integer, String> explanations = new HashMap<>();
        Map<Integer, Boolean> questionResults = new HashMap<>();
        int totalScore = 0;
        int earnedScore = 0;

        for (ExamQuestion question : exam.getQuestions()) {
            int idx = question.getQuestionIndex();
            String correct = question.getCorrectAnswer();
            String studentAnswer = studentAnswers.getOrDefault(idx, "");
            int qScore = question.getScore() != null ? question.getScore() : 1;

            totalScore += qScore;
            correctAnswers.put(idx, correct);
            if (question.getExplanation() != null) {
                explanations.put(idx, question.getExplanation());
            }

            // So sánh không phân biệt chữ hoa/thường, bỏ khoảng trắng thừa
            boolean isCorrect = correct != null
                    && correct.trim().equalsIgnoreCase(studentAnswer.trim());
            questionResults.put(idx, isCorrect);

            if (isCorrect) {
                earnedScore += qScore;
            }
        }

        // Tính phần trăm điểm
        double percentage = totalScore > 0 ? (earnedScore * 100.0 / totalScore) : 0.0;

        // Cập nhật thông tin bài làm
        submission.setAnswers(studentAnswers);
        submission.setScore(earnedScore);
        submission.setTotalScore(totalScore);
        submission.setPercentage(percentage);
        submission.setStatus("GRADED");
        submission.setSubmittedAt(System.currentTimeMillis());
        submission.setTimeTakenSeconds(request.getTimeTakenSeconds());

        submission = examSubmissionRepository.save(submission);

        // Cập nhật số bài đã nộp trên đề thi
        exam.setSubmittedCount(exam.getSubmittedCount() + 1);
        exam.setUpdatedAt(System.currentTimeMillis());
        examRepository.save(exam);

        log.info("Học sinh {} nộp bài xong: {}/{} điểm ({:.1f}%)",
                studentId, earnedScore, totalScore, percentage);

        // Xây dựng response kết quả chi tiết (bao gồm đáp án đúng và giải thích)
        ExamSubmissionDTO.Response response = toSubmissionResponse(submission, exam);
        response.setCorrectAnswers(correctAnswers);
        response.setExplanations(explanations);
        response.setQuestionResults(questionResults);
        return response;
    }

    /**
     * Lấy lịch sử thi của học sinh (các bài thi đã làm)
     *
     * @param studentId ID học sinh
     * @return Danh sách kết quả thi, mới nhất trước
     */
    public List<ExamSubmissionDTO.Response> getStudentExamHistory(String studentId) {
        log.info("Lấy lịch sử thi của học sinh: {}", studentId);
        return examSubmissionRepository.findByStudentIdOrderBySubmittedAtDesc(studentId)
                .stream()
                .map(sub -> {
                    // Lấy exam để có thêm thông tin nếu cần, bỏ qua nếu không tìm thấy
                    Exam exam = examRepository.findById(sub.getExamId()).orElse(null);
                    return toSubmissionResponse(sub, exam);
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy kết quả chi tiết một bài thi của học sinh (sau khi đã nộp)
     *
     * @param examId    ID đề thi
     * @param studentId ID học sinh
     * @return Kết quả chi tiết bao gồm đáp án đúng và giải thích
     */
    public ExamSubmissionDTO.Response getStudentResult(String examId, String studentId) {
        log.info("Học sinh {} xem kết quả đề thi: {}", studentId, examId);

        ExamSubmission submission = examSubmissionRepository
                .findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài làm của bạn cho đề thi này"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi"));

        // Xây dựng lại correctAnswers và explanations từ đề thi
        Map<Integer, String> correctAnswers = new HashMap<>();
        Map<Integer, String> explanations = new HashMap<>();
        Map<Integer, Boolean> questionResults = new HashMap<>();

        for (ExamQuestion q : exam.getQuestions()) {
            int idx = q.getQuestionIndex();
            correctAnswers.put(idx, q.getCorrectAnswer());
            if (q.getExplanation() != null) {
                explanations.put(idx, q.getExplanation());
            }
            // So sánh câu trả lời học sinh với đáp án đúng
            String studentAnswer = submission.getAnswers().getOrDefault(idx, "");
            boolean isCorrect = q.getCorrectAnswer() != null
                    && q.getCorrectAnswer().trim().equalsIgnoreCase(studentAnswer.trim());
            questionResults.put(idx, isCorrect);
        }

        ExamSubmissionDTO.Response response = toSubmissionResponse(submission, exam);
        response.setCorrectAnswers(correctAnswers);
        response.setExplanations(explanations);
        response.setQuestionResults(questionResults);
        return response;
    }

    // ======================================================================
    // Phương thức private hỗ trợ
    // ======================================================================

    /**
     * Sinh mã PIN 5 chữ số ngẫu nhiên và đảm bảo không trùng với đề thi nào khác.
     * Định dạng: 5 chữ số, thêm số 0 đầu nếu cần (VD: "00123")
     * Thử tối đa 10 lần, ném lỗi nếu không thành công
     */
    private String generateUniquePin() {
        int maxAttempts = 10;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            // Sinh số ngẫu nhiên từ 10000 đến 99999 (đảm bảo đủ 5 chữ số)
            int pinNumber = 10000 + random.nextInt(90000);
            String pin = String.valueOf(pinNumber);

            if (!examRepository.existsByPinCode(pin)) {
                log.debug("Sinh PIN thành công sau {} lần thử: {}", attempt, pin);
                return pin;
            }
            log.debug("PIN {} đã tồn tại, thử lại lần {}", pin, attempt);
        }
        throw new RuntimeException("Không thể sinh mã PIN duy nhất sau " + maxAttempts + " lần thử. Vui lòng thử lại.");
    }

    /**
     * Helper: Chuyển Exam entity → ExamDTO.Response để trả về client
     *
     * @param exam       Exam entity từ database
     * @param hideAnswer true = ẩn đáp án đúng (dành cho học sinh đang thi)
     *                   false = giữ nguyên đáp án (dành cho giáo viên)
     */
    private ExamDTO.Response toResponse(Exam exam, boolean hideAnswer) {
        // Chuyển danh sách câu hỏi, có tuỳ chọn ẩn đáp án
        List<ExamDTO.QuestionResponse> questionResponses = new ArrayList<>();
        if (exam.getQuestions() != null) {
            for (ExamQuestion q : exam.getQuestions()) {
                questionResponses.add(ExamDTO.QuestionResponse.builder()
                        .questionIndex(q.getQuestionIndex())
                        .questionText(q.getQuestionText())
                        .type(q.getType())
                        .options(q.getOptions() != null ? new ArrayList<>(q.getOptions()) : new ArrayList<>())
                        // Ẩn đáp án đúng khi trả về cho học sinh đang thi
                        .correctAnswer(hideAnswer ? null : q.getCorrectAnswer())
                        // Ẩn giải thích khi đang thi (chỉ hiện sau khi nộp bài)
                        .explanation(hideAnswer ? null : q.getExplanation())
                        .score(q.getScore())
                        .sourceExerciseId(q.getSourceExerciseId())
                        .build());
            }
        }

        return ExamDTO.Response.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .pinCode(exam.getPinCode())
                .classroomId(exam.getClassroomId())
                .classroomName(exam.getClassroomName())
                .teacherId(exam.getTeacherId())
                .timeLimitMinutes(exam.getTimeLimitMinutes())
                .timeLimitDisplay(exam.getTimeLimitDisplay())
                .status(exam.getStatus())
                .statusDisplay(exam.getStatusDisplay())
                .statusBadgeClass(exam.getStatusBadgeClass())
                .totalStudents(exam.getTotalStudents())
                .submittedCount(exam.getSubmittedCount())
                .totalScore(exam.getTotalScore())
                .questionCount(exam.getQuestionCount())
                .questions(questionResponses)
                .createdAt(exam.getCreatedAt())
                .createdAtDisplay(formatDate(exam.getCreatedAt()))
                .build();
    }

    /**
     * Helper: Chuyển ExamSubmission entity → ExamSubmissionDTO.Response
     *
     * @param submission Bài làm từ database
     * @param exam       Đề thi tương ứng (có thể null nếu đề đã bị xóa)
     */
    private ExamSubmissionDTO.Response toSubmissionResponse(ExamSubmission submission, Exam exam) {
        return ExamSubmissionDTO.Response.builder()
                .id(submission.getId())
                .examId(submission.getExamId())
                .examTitle(submission.getExamTitle())
                .studentId(submission.getStudentId())
                .studentName(submission.getStudentName())
                .score(submission.getScore())
                .totalScore(submission.getTotalScore())
                .percentage(submission.getPercentage())
                .scoreDisplay(submission.getScoreDisplay())
                .percentageDisplay(submission.getPercentageDisplay())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .submittedAtDisplay(formatDateTime(submission.getSubmittedAt()))
                .timeTakenSeconds(submission.getTimeTakenSeconds())
                .timeTakenDisplay(submission.getTimeTakenDisplay())
                .answers(submission.getAnswers() != null ? new HashMap<>(submission.getAnswers()) : new HashMap<>())
                // correctAnswers và explanations sẽ được gán riêng khi cần
                .correctAnswers(new HashMap<>())
                .explanations(new HashMap<>())
                .questionResults(new HashMap<>())
                .build();
    }

    /**
     * Định dạng timestamp sang chuỗi ngày: dd/MM/yyyy
     */
    private String formatDate(Long millis) {
        if (millis == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date(millis));
    }

    /**
     * Định dạng timestamp sang chuỗi ngày giờ: dd/MM/yyyy HH:mm
     */
    private String formatDateTime(Long millis) {
        if (millis == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(millis));
    }
}
