package com.english12smart.util;

import com.english12smart.entity.Lesson;
import com.english12smart.entity.Lesson.VocabularyItem;
import com.english12smart.entity.Unit;
import com.english12smart.repository.LessonRepository;
import com.english12smart.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Unit23Initializer implements CommandLineRunner {

    private final UnitRepository unitRepository;
    private final LessonRepository lessonRepository;

    @Override
    public void run(String... args) throws Exception {
        List<Unit> existingUnits = unitRepository.findAll();
        
        boolean unit2Exists = existingUnits.stream()
                .anyMatch(u -> "A Multicultural World".equals(u.getTitle()) && u.getOrderIndex() == 2);
        if (!unit2Exists) {
            createUnit2();
        } else {
            System.out.println("ℹ️ Unit 2: A Multicultural World đã tồn tại trong hệ thống");
        }
        
        boolean unit3Exists = existingUnits.stream()
                .anyMatch(u -> "Green Living".equals(u.getTitle()) && u.getOrderIndex() == 3);
        if (!unit3Exists) {
            createUnit3();
        } else {
            System.out.println("ℹ️ Unit 3: Green Living đã tồn tại trong hệ thống");
        }
    }

    private void createUnit2() {
        Unit unit2 = Unit.builder()
                .title("A Multicultural World")
                .description("Học về sự đa dạng văn hóa trên thế giới, tôn trọng và và tìm hiểu các nền văn hóa khác nhau. Sử dụng từ vựng liên quan đến văn hóa và phong tục.")
                .orderIndex(2)
                .level("B1")
                .thumbnailUrl("https://res.cloudinary.com/dykzfyb4t/image/upload/v1645000000/english-12-smart/unit2-multicultural.jpg")
                .isActive(true)
                .createdBy("admin")
                .build();

        Unit savedUnit = unitRepository.save(unit2);
        System.out.println("📚 Tạo Unit 2: A Multicultural World | ID: " + savedUnit.getId());

        // Tiêu biểu 3 bài học cho Unit 2
        Lesson l1 = Lesson.builder()
                .unitId(savedUnit.getId())
                .title("Getting Started - Cultural Diversity")
                .description("Mở đầu chủ đề Đa dạng văn hóa. Các nền văn hóa trên toàn thế giới.")
                .type("GETTING_STARTED")
                .content("<div class='lesson-content'><h2>Chào mừng đến với Unit 2</h2><p>Trái đất của chúng ta có vô số nền văn hóa khác biệt, cùng khám phá và học tôn trọng chúng.</p></div>")
                .orderIndex(1)
                .estimatedDurationMinutes(15)
                .xpReward(10)
                .isActive(true)
                .createdBy("admin")
                .build();
        lessonRepository.save(l1);

        Lesson l2 = Lesson.builder()
                .unitId(savedUnit.getId())
                .title("Vocabulary - Văn hóa & Phong tục")
                .description("Từ vựng về giao lưu văn hóa.")
                .type("VOCABULARY")
                .content("<div class='vocabulary-lesson'><h2>Các từ vựng phổ biến</h2></div>")
                .orderIndex(2)
                .estimatedDurationMinutes(30)
                .xpReward(15)
                .isActive(true)
                .createdBy("admin")
                .vocabulary(Arrays.asList(
                        Lesson.VocabularyItem.builder().word("diversity").pronunciation("/daɪˈvɜːrsəti/").meaning("sự đa dạng").partOfSpeech("noun").exampleSentence("Cultural diversity makes our world colorful.").build(),
                        Lesson.VocabularyItem.builder().word("custom").pronunciation("/ˈkʌstəm/").meaning("phong tục").partOfSpeech("noun").exampleSentence("It is a local custom to bow when greeting.").build(),
                        Lesson.VocabularyItem.builder().word("identity").pronunciation("/aɪˈdentəti/").meaning("bản sắc").partOfSpeech("noun").exampleSentence("Language is an important part of national identity.").build()
                ))
                .build();
        lessonRepository.save(l2);

        Lesson l3 = Lesson.builder()
                .unitId(savedUnit.getId())
                .title("Language Focus - Mạo từ (Articles)")
                .description("Học về mạo từ (a/an/the) và mạo từ rỗng.")
                .type("GRAMMAR")
                .content("<div class='grammar-lesson'><h2>Mạo từ: A, An, The</h2><p>Sử dụng <b>The</b> khi nói về các quốc gia có trạng ngữ số nhiều như The USA, The Philippines...</p></div>")
                .orderIndex(3)
                .estimatedDurationMinutes(40)
                .xpReward(20)
                .isActive(true)
                .createdBy("admin")
                .build();
        lessonRepository.save(l3);

        savedUnit.setTotalLessons(3);
        unitRepository.save(savedUnit);
    }

    private void createUnit3() {
        Unit unit3 = Unit.builder()
                .title("Green Living")
                .description("Phong cách sống xanh, bảo vệ môi trường, giảm thiểu lượng khí thải và nâng cao ý thức về môi trường.")
                .orderIndex(3)
                .level("B1")
                .thumbnailUrl("https://res.cloudinary.com/dykzfyb4t/image/upload/v1645000000/english-12-smart/unit3-green-living.jpg")
                .isActive(true)
                .createdBy("admin")
                .build();

        Unit savedUnit = unitRepository.save(unit3);
        System.out.println("📚 Tạo Unit 3: Green Living | ID: " + savedUnit.getId());

        Lesson l1 = Lesson.builder()
                .unitId(savedUnit.getId())
                .title("Getting Started - Eco-friendly Lifestyle")
                .description("Giới thiệu phong cách sống thân thiện với môi trường.")
                .type("GETTING_STARTED")
                .content("<div class='lesson-content'><h2>Hướng tới lối sống xanh</h2><p>Bạn đã làm gì hôm nay để giúp bảo vệ trái đất?</p></div>")
                .orderIndex(1)
                .estimatedDurationMinutes(15)
                .xpReward(10)
                .isActive(true)
                .createdBy("admin")
                .build();
        lessonRepository.save(l1);

        Lesson l2 = Lesson.builder()
                .unitId(savedUnit.getId())
                .title("Vocabulary - Môi trường & Tái chế")
                .description("Từ vựng chủ đề môi trường.")
                .type("VOCABULARY")
                .content("<div class='vocabulary-lesson'><h2>Môi trường xung quanh ta</h2></div>")
                .orderIndex(2)
                .estimatedDurationMinutes(30)
                .xpReward(15)
                .isActive(true)
                .createdBy("admin")
                .vocabulary(Arrays.asList(
                        Lesson.VocabularyItem.builder().word("eco-friendly").pronunciation("/ˌiːkəʊ ˈfrendli/").meaning("thân thiện với môi trường").partOfSpeech("adj").exampleSentence("We should use eco-friendly products.").build(),
                        Lesson.VocabularyItem.builder().word("sustainable").pronunciation("/səˈsteɪnəbl/").meaning("bền vững").partOfSpeech("adj").exampleSentence("Sustainable development is our target.").build(),
                        Lesson.VocabularyItem.builder().word("carbon footprint").pronunciation("/ˌkɑːbən ˈfʊtprɪnt/").meaning("lượng khí thải carbon").partOfSpeech("noun").exampleSentence("Cycling helps reduce your carbon footprint.").build()
                ))
                .build();
        lessonRepository.save(l2);

        savedUnit.setTotalLessons(2);
        unitRepository.save(savedUnit);
    }
}
