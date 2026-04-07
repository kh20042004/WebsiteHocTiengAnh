package com.english12smart.service;

import com.english12smart.entity.*;
import com.english12smart.repository.ExerciseRepository;
import com.english12smart.repository.ExamRepository;
import com.english12smart.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ========== SAMPLE DATA SEEDER SERVICE ==========
 * Tạo dữ liệu mẫu: bài tập mẫu và bài kiểm tra cho mỗi bài học
 * Sử dụng khi cần setup dữ liệu ban đầu cho hệ thống
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SampleDataSeederService {

    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExamRepository examRepository;

    /**
     * Tạo bài tập mẫu và bài kiểm tra cho tất cả các bài học
     */
    public void seedExercisesAndExamsForAllLessons() {
        log.info("🌱 Bắt đầu tạo dữ liệu mẫu...");

        try {
            // Lấy tất cả lessons
            List<Lesson> allLessons = lessonRepository.findAll();
            log.info("📚 Tìm thấy {} bài học", allLessons.size());

            int exerciseCreated = 0;
            int examCreated = 0;

            for (Lesson lesson : allLessons) {
                // ========== KIỂM TRA EXERCISE ==========
                // Kiểm tra xem lesson này đã có exercise chưa
                var existingExercises = exerciseRepository.findByLessonIdOrderByOrderIndexAsc(lesson.getId());
                if (existingExercises.isEmpty()) {
                    createSampleExerciseForLesson(lesson);
                    exerciseCreated++;
                }

                // ========== KIỂM TRA EXAM ==========
                // Tạo exam mẫu (exam không liên kết trực tiếp với lesson trong DB)
                createSampleExamForLesson(lesson);
                examCreated++;
            }

            log.info("✅ Tạo xong! {} bài tập + {} bài kiểm tra", exerciseCreated, examCreated);

        } catch (Exception e) {
            log.error("❌ Lỗi tạo dữ liệu mẫu: {}", e.getMessage(), e);
        }
    }

    /**
     * Tạo bài tập mẫu cho một bài học cụ thể
     */
    private void createSampleExerciseForLesson(Lesson lesson) {
        try {
            // Tạo bài tập trắc nghiệm mẫu
            Exercise exercise = Exercise.builder()
                    .lessonId(lesson.getId())
                    .unitId(lesson.getUnitId())
                    .title("📝 Bài Tập Mẫu - " + lesson.getTitle())
                    .instruction("Chọn 1 đáp án đúng nhất cho mỗi câu hỏi. Hãy suy nghĩ cẩn thận trước khi chọn.")
                    .type("MULTIPLE_CHOICE")
                    .orderIndex(1)
                    .maxScore(100)
                    .xpReward(50)
                    .timeLimitMinutes(15)
                    .isActive(true)
                    .createdAt(System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .questions(generateSampleQuestions())
                    .build();

            exerciseRepository.save(exercise);
            log.info("✅ Tạo bài tập mẫu cho lesson: {}", lesson.getTitle());

        } catch (Exception e) {
            log.warn("⚠️ Lỗi tạo exercise: {}", e.getMessage());
        }
    }

    /**
     * Tạo bài kiểm tra mẫu cho một bài học cụ thể
     */
    private void createSampleExamForLesson(Lesson lesson) {
        try {
            // Tạo bài kiểm tra
            Exam exam = Exam.builder()
                    .title("📋 Bài Kiểm Tra - " + lesson.getTitle())
                    .description("Kiểm tra kiến thức về bài học: " + lesson.getTitle()
                            + "\nYêu cầu: Trả lời đúng ít nhất 70% câu hỏi")
                    .pinCode(generateRandomPinCode())
                    .status("ACTIVE")
                    .timeLimitMinutes(20)
                    .questions(generateSampleExamQuestions())
                    .createdAt(System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .build();

            examRepository.save(exam);
            log.info("✅ Tạo bài kiểm tra mẫu cho lesson: {} (PinCode: {})", 
                    lesson.getTitle(), exam.getPinCode());

        } catch (Exception e) {
            log.warn("⚠️ Lỗi tạo exam: {}", e.getMessage());
        }
    }

    /**
     * Tạo danh sách câu hỏi mẫu cho bài tập
     */
    private List<Exercise.Question> generateSampleQuestions() {
        return Arrays.asList(
                // Câu 1
                Exercise.Question.builder()
                        .questionIndex(1)
                        .questionText("Which sentence is grammatically correct?")
                        .options(Arrays.asList("She go to school", "She goes to school", "She going to school", "She gone to school"))
                        .correctAnswer("B")
                        .build(),

                // Câu 2
                Exercise.Question.builder()
                        .questionIndex(2)
                        .questionText("What is the synonym of 'happy'?")
                        .options(Arrays.asList("Sad", "Joyful", "Angry", "Tired"))
                        .correctAnswer("B")
                        .build(),

                // Câu 3
                Exercise.Question.builder()
                        .questionIndex(3)
                        .questionText("Choose the correct pronunciation of 'psychology':")
                        .options(Arrays.asList("pshi-ko-luh-jee", "sahy-kol-uh-jee", "psy-ko-luh-jee", "psi-ko-lo-gi"))
                        .correctAnswer("B")
                        .build(),

                // Câu 4
                Exercise.Question.builder()
                        .questionIndex(4)
                        .questionText("Fill in the blank: 'If I _____ you were coming, I would have waited.'")
                        .options(Arrays.asList("knew", "had known", "would know", "know"))
                        .correctAnswer("B")
                        .build(),

                // Câu 5
                Exercise.Question.builder()
                        .questionIndex(5)
                        .questionText("What does 'procrastinate' mean?")
                        .options(Arrays.asList(
                                "To do something quickly",
                                "To delay or postpone something",
                                "To plan ahead",
                                "To refuse to do something"
                        ))
                        .correctAnswer("B")
                        .build()
        );
    }

    /**
     * Tạo danh sách câu hỏi mẫu cho bài kiểm tra
     */
    private List<Exam.ExamQuestion> generateSampleExamQuestions() {
        return Arrays.asList(
                // Câu 1
                Exam.ExamQuestion.builder()
                        .questionIndex(1)
                        .questionText("What is the past tense of 'eat'?")
                        .options(Arrays.asList("Eat", "Ate", "Eaten", "Eating"))
                        .correctAnswer("B")
                        .score(10)
                        .build(),

                // Câu 2
                Exam.ExamQuestion.builder()
                        .questionIndex(2)
                        .questionText("Choose the sentence with correct word order:")
                        .options(Arrays.asList(
                                "I like very much this book",
                                "I like this book very much",
                                "Very much I like this book",
                                "I this book like very much"
                        ))
                        .correctAnswer("B")
                        .score(10)
                        .build(),

                // Câu 3
                Exam.ExamQuestion.builder()
                        .questionIndex(3)
                        .questionText("Which word is an adjective?")
                        .options(Arrays.asList("Quickly", "Beautiful", "Run", "Happily"))
                        .correctAnswer("B")
                        .score(10)
                        .build(),

                // Câu 4
                Exam.ExamQuestion.builder()
                        .questionIndex(4)
                        .questionText("What is the opposite of 'big'?")
                        .options(Arrays.asList("Large", "Small", "Large", "Huge"))
                        .correctAnswer("B")
                        .score(10)
                        .build(),

                // Câu 5
                Exam.ExamQuestion.builder()
                        .questionIndex(5)
                        .questionText("Complete the idiom: 'It's raining cats and _____'")
                        .options(Arrays.asList("Rabbits", "Dogs", "Birds", "Fish"))
                        .correctAnswer("B")
                        .score(10)
                        .build(),

                // Câu 6
                Exam.ExamQuestion.builder()
                        .questionIndex(6)
                        .questionText("Which is the correct plural of 'child'?")
                        .options(Arrays.asList("Childs", "Children", "Childes", "Childish"))
                        .correctAnswer("B")
                        .score(10)
                        .build(),

                // Câu 7
                Exam.ExamQuestion.builder()
                        .questionIndex(7)
                        .questionText("What time is it? _____ two o'clock.")
                        .options(Arrays.asList("This is", "It is", "There is", "That is"))
                        .correctAnswer("B")
                        .score(10)
                        .build(),

                // Câu 8
                Exam.ExamQuestion.builder()
                        .questionIndex(8)
                        .questionText("Choose the best response: 'How are you?' '_____'")
                        .options(Arrays.asList("I am fine, thank you", "I'm fine, thank you", "Fine, thanks", "All are correct"))
                        .correctAnswer("D")
                        .score(10)
                        .build(),

                // Câu 9
                Exam.ExamQuestion.builder()
                        .questionIndex(9)
                        .questionText("What is the present participle of 'go'?")
                        .options(Arrays.asList("Gone", "Going", "Goes", "Went"))
                        .correctAnswer("B")
                        .score(10)
                        .build(),

                // Câu 10
                Exam.ExamQuestion.builder()
                        .questionIndex(10)
                        .questionText("Which sentence has correct punctuation?")
                        .options(Arrays.asList(
                                "She said \"Hello, how are you\"",
                                "She said \"Hello, how are you?\"",
                                "She said Hello, how are you?",
                                "She said, \"Hello how are you?\""
                        ))
                        .correctAnswer("B")
                        .score(10)
                        .build()
        );
    }

    /**
     * Sinh mã PIN ngẫu nhiên (5 chữ số)
     */
    private String generateRandomPinCode() {
        Random random = new Random();
        int pinCode = 10000 + random.nextInt(90000); // Tạo số từ 10000 đến 99999
        return String.valueOf(pinCode);
    }
}
