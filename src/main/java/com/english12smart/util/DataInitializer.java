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

/**
 * DataInitializer - Khởi tạo dữ liệu học tập cho ứng dụng
 * 
 * Lớp này tự động chạy khi ứng dụng khởi động và tạo Unit 1: "Life Stories"
 * cùng với 7 bài học chi tiết theo chương trình sgk tiếng anh lớp 12
 * 
 * Cấu trúc Unit 1:
 * - Lesson 1: Getting Started (Khởi động)
 * - Lesson 2: Vocabulary (Từ vựng)
 * - Lesson 3: Language Focus (Ngữ pháp)
 * - Lesson 4: Reading (Đọc hiểu)
 * - Lesson 5: Listening (Nghe hiểu)
 * - Lesson 6: Speaking (Nói)
 * - Lesson 7: Writing (Viết)
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UnitRepository unitRepository;
    private final LessonRepository lessonRepository;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem Unit 1 đã tồn tại chưa bằng cách tìm unit có tiêu đề "Life Stories"
        List<Unit> existingUnits = unitRepository.findAll();
        boolean unit1Exists = existingUnits.stream()
                .anyMatch(u -> "Life Stories".equals(u.getTitle()) && u.getOrderIndex() == 1);
        
        if (!unit1Exists) {
            // Nếu Unit 1 chưa tồn tại, tiến hành tạo
            createUnit1();
            System.out.println("✅ Unit 1: Life Stories đã được tạo thành công!");
        } else {
            System.out.println("ℹ️ Unit 1: Life Stories đã tồn tại trong hệ thống");
        }
    }

    /**
     * Tạo Unit 1: Life Stories - Đơn vị toàn bộ
     * 
     * Thông tin Unit:
     * - Tên: Life Stories (Những câu chuyện sống)
     * - Mô tả: Học cách kể lại câu chuyện cuộc sống, sử dụng thì quá khứ
     *          để diễn tả các sự kiện và kinh nghiệm cá nhân
     * - Cấp độ: B1 (Trung cấp - theo tiêu chuẩn CEFR)
     * - Thứ tự: 1 (đơn vị đầu tiên)
     */
    private void createUnit1() {
        // Tạo Unit 1 với thông tin cơ bản
        Unit unit1 = Unit.builder()
                .title("Life Stories")                      // Tên đơn vị: Những câu chuyện sống
                .description("Học cách kể lại những câu chuyện cuộc sống, sử dụng thì quá khứ " +
                           "để diễn tả các sự kiện và kinh nghiệm. Nâng cao kỹ năng giao tiếp " +
                           "qua việc chia sẻ và lắng nghe các câu chuyện của người khác.")
                .orderIndex(1)                              // Đây là unit thứ 1
                .level("B1")                                // Cấp độ B1 (trung cấp)
                .thumbnailUrl("https://res.cloudinary.com/dykzfyb4t/image/upload/" +
                            "v1645000000/english-12-smart/unit1-life-stories.jpg") // Hình ảnh đại diện
                .isActive(true)                             // Kích hoạt unit này
                .createdBy("admin")                         // Tạo bởi quản trị viên
                .build();

        Unit savedUnit = unitRepository.save(unit1);
        System.out.println("📚 Tạo Unit 1: Life Stories | ID: " + savedUnit.getId());

        // Tạo các bài học cho Unit 1
        createLesson1_GettingStarted(savedUnit.getId());
        createLesson2_Vocabulary(savedUnit.getId());
        createLesson3_LanguageFocus(savedUnit.getId());
        createLesson4_Reading(savedUnit.getId());
        createLesson5_Listening(savedUnit.getId());
        createLesson6_Speaking(savedUnit.getId());
        createLesson7_Writing(savedUnit.getId());

        // Cập nhật số lượng bài học trong unit
        unit1.setTotalLessons(7);
        unitRepository.save(unit1);
    }

    /**
     * Bài học 1: Getting Started (Khởi động)
     * 
     * Mục đích:
     * - Tạo hứng thú cho học sinh về chủ đề "Những câu chuyện sống"
     * - Giới thiệu từ vựng cơ bản liên quan đến các giai đoạn cuộc sống
     * - Xây dựng bối cảnh và mục tiêu học tập
     * 
     * Nội dung:
     * - Hình ảnh về các giai đoạn cuộc sống con người
     * - Các câu hỏi khởi động để học sinh chia sẻ trải nghiệm
     * - Mục tiêu bài học
     */
    private void createLesson1_GettingStarted(String unitId) {
        Lesson lesson1 = Lesson.builder()
                .unitId(unitId)                             // Thuộc Unit 1
                .title("Getting Started - Khởi Động")       // Tên bài học
                .description("Gợi ý và khởi động về chủ đề những câu chuyện sống. " +
                           "Giới thiệu từ vựng cơ bản và kích thích sự tò mò của học sinh.")
                .type("GETTING_STARTED")                    // Loại bài: Khởi động
                .content("<div class='lesson-content'>" +
                       "<h2>Những Câu Chuyện Sống - Khởi Động</h2>" +
                       "<p>Mỗi người đều có những câu chuyện riêng để kể. Câu chuyện này có thể là về:</p>" +
                       "<ul>" +
                       "  <li>👶 <strong>Tuổi thơ</strong> - Những kỷ niệm từ khi còn nhỏ</li>" +
                       "  <li>📚 <strong>Giáo dục</strong> - Những trải nghiệm ở trường học</li>" +
                       "  <li>💼 <strong>Sự nghiệp</strong> - Những thách thức và thành tích trong công việc</li>" +
                       "  <li>❤️ <strong>Gia đình</strong> - Những mối quan hệ quý báu</li>" +
                       "  <li>✈️ <strong>Những cuộc phiêu lưu</strong> - Những chuyến đi và khám phá</li>" +
                       "</ul>" +
                       "<h3>Câu hỏi gợi mở:</h3>" +
                       "<p>1. Sự kiện nào trong cuộc sống bạn nhớ rõ nhất?</p>" +
                       "<p>2. Bạn từng nghe câu chuyện cuộc sống của ai? Đó là một trải nghiệm tuyệt vời?</p>" +
                       "<p>3. Kỹ năng nào giúp bạn kể lại một câu chuyện một cách hấp dẫn?</p>" +
                       "</div>")
                .orderIndex(1)                              // Bài thứ 1 trong unit
                .estimatedDurationMinutes(15)               // Thời gian: 15 phút
                .xpReward(5)                                // Điểm kinh nghiệm: 5
                .isActive(true)                             // Kích hoạt bài học này
                .createdBy("admin")
                .vocabulary(new ArrayList<>())              // Không có từ vựng chi tiết (chỉ khởi động)
                .build();

        lessonRepository.save(lesson1);
        System.out.println("  ✓ Lesson 1: Getting Started");
    }

    /**
     * Bài học 2: Vocabulary - Từ Vựng
     * 
     * Mục đích:
     * - Dạy từ vựng liên quan đến các sự kiện và giai đoạn cuộc sống
     * - Giới thiệu cách phát âm (IPA) đúng của các từ
     * - Cung cấp ví dụ câu sử dụng từ vựng trong ngữ cảnh thực tế
     * 
     * Từ vựng:
     * - 20 từ vựng chủ chốt về cuộc sống, sự kiện, cảm xúc
     * - Mỗi từ có: phát âm, định nghĩa, ví dụ câu
     */
    private void createLesson2_Vocabulary(String unitId) {
        // Tạo danh sách từ vựng với phát âm, định nghĩa và ví dụ
        // Sử dụng builder pattern để tạo VocabularyItem
        List<VocabularyItem> vocabulary = Arrays.asList(
            // Từ 1: birth - sự sinh ra, chào đời
            Lesson.VocabularyItem.builder()
                .word("birth")
                .pronunciation("/bɜːrθ/")
                .partOfSpeech("noun")
                .meaning("sự sinh ra, chào đời")
                .exampleSentence("She celebrated her birth on March 15, 1995.")
                .exampleTranslation("Cô ấy kỷ niệm ngày sinh nhật của mình vào ngày 15 tháng 3 năm 1995.")
                .build(),

            // Từ 2: childhood - tuổi thơ
            Lesson.VocabularyItem.builder()
                .word("childhood")
                .pronunciation("/ˈtʃaɪldhʊd/")
                .partOfSpeech("noun")
                .meaning("tuổi thơ, thời cách đây lâu")
                .exampleSentence("I have happy memories from my childhood in the countryside.")
                .exampleTranslation("Tôi có những ký ức vui vẻ từ thời thơ ấu ở vùng nông thôn.")
                .build(),

            // Từ 3: adolescence - tuổi thiếu niên
            Lesson.VocabularyItem.builder()
                .word("adolescence")
                .pronunciation("/ˌædəˈlesns/")
                .partOfSpeech("noun")
                .meaning("tuổi thiếu niên, giai đoạn từ 13-19 tuổi")
                .exampleSentence("Adolescence is a time of significant physical and emotional changes.")
                .exampleTranslation("Tuổi thiếu niên là thời kỳ thay đổi lớn về thể chất và cảm xúc.")
                .build(),

            // Từ 4: experience - trải nghiệm
            Lesson.VocabularyItem.builder()
                .word("experience")
                .pronunciation("/ɪkˈspɪriəns/")
                .partOfSpeech("noun/verb")
                .meaning("trải nghiệm, kinh nghiệm; trải qua, cảm nhận")
                .exampleSentence("She has gained valuable experience through her travels.")
                .exampleTranslation("Cô ấy đã có được kinh nghiệm quý báu thông qua những chuyến đi của mình.")
                .build(),

            // Từ 5: achievement - thành tựu
            Lesson.VocabularyItem.builder()
                .word("achievement")
                .pronunciation("/əˈtʃiːvmənt/")
                .partOfSpeech("noun")
                .meaning("thành tựu, kết quả đạt được")
                .exampleSentence("Graduating from university was his greatest achievement.")
                .exampleTranslation("Tốt nghiệp đại học là thành tựu lớn nhất của anh ấy.")
                .build(),

            // Từ 6: challenge - thách thức
            Lesson.VocabularyItem.builder()
                .word("challenge")
                .pronunciation("/ˈtʃælɪndʒ/")
                .partOfSpeech("noun")
                .meaning("thách thức, khó khăn")
                .exampleSentence("Learning a new language presents many challenges.")
                .exampleTranslation("Học một ngôn ngữ mới có nhiều thách thức.")
                .build(),

            // Từ 7: fortunate - may mắn
            Lesson.VocabularyItem.builder()
                .word("fortunate")
                .pronunciation("/ˈfɔːrtʃənət/")
                .partOfSpeech("adjective")
                .meaning("may mắn, tốt số")
                .exampleSentence("I was fortunate to meet her at the right time.")
                .exampleTranslation("Tôi may mắn gặp gỡ cô ấy vào đúng thời khắc.")
                .build(),

            // Từ 8: memorable - đáng nhớ
            Lesson.VocabularyItem.builder()
                .word("memorable")
                .pronunciation("/ˈmeməɑːbl/")
                .partOfSpeech("adjective")
                .meaning("đáng nhớ, ghi nhớ được")
                .exampleSentence("That was the most memorable day of my life.")
                .exampleTranslation("Đó là ngày đáng nhớ nhất trong cuộc sống của tôi.")
                .build(),

            // Từ 9: remarkably - một cách đáng chú ý
            Lesson.VocabularyItem.builder()
                .word("remarkably")
                .pronunciation("/rɪˈmɑːrkəbli/")
                .partOfSpeech("adverb")
                .meaning("một cách đáng chú ý, nhất là")
                .exampleSentence("She spoke remarkably well for someone learning the language.")
                .exampleTranslation("Cô ấy nói khá tốt đối với một người đang học ngôn ngữ.")
                .build(),

            // Từ 10: coincidence - sự trùng hợp
            Lesson.VocabularyItem.builder()
                .word("coincidence")
                .pronunciation("/kəʊˈɪnsɪdəns/")
                .partOfSpeech("noun")
                .meaning("sự trùng hợp,巧合")
                .exampleSentence("It was just a coincidence that we met at the train station.")
                .exampleTranslation("Đó chỉ là một sự trùng hợp ngẫu nhiên khi chúng ta gặp nhau ở ga tàu.")
                .build(),

            // Từ 11: accomplish - hoàn thành
            Lesson.VocabularyItem.builder()
                .word("accomplish")
                .pronunciation("/əˈkʌmplɪʃ/")
                .partOfSpeech("verb")
                .meaning("hoàn thành, đạt được")
                .exampleSentence("She accomplished her dream of becoming a doctor.")
                .exampleTranslation("Cô ấy đã hoàn thành giấc mơ trở thành bác sĩ.")
                .build(),

            // Từ 12: persevere - kiên trì
            Lesson.VocabularyItem.builder()
                .word("persevere")
                .pronunciation("/ˌpɜːrsɪˈvɪr/")
                .partOfSpeech("verb")
                .meaning("kiên trì, bền bỉ")
                .exampleSentence("He persevered through many difficulties to reach his goal.")
                .exampleTranslation("Anh ấy đã kiên trì vượt qua nhiều khó khăn để đạt được mục tiêu.")
                .build(),

            // Từ 13: overcome - vượt qua
            Lesson.VocabularyItem.builder()
                .word("overcome")
                .pronunciation("/ˌəʊvərˈkʌm/")
                .partOfSpeech("verb")
                .meaning("vượt qua, chiến thắng")
                .exampleSentence("With effort, I overcame my fear of public speaking.")
                .exampleTranslation("Nhờ nỗ lực, tôi đã vượt qua nỗi sợ nói trước công chúng.")
                .build(),

            // Từ 14: pivotal - then chốt
            Lesson.VocabularyItem.builder()
                .word("pivotal")
                .pronunciation("/ˈpɪvətl/")
                .partOfSpeech("adjective")
                .meaning("then chốt, quan trọng nhất")
                .exampleSentence("That meeting was pivotal in changing my career path.")
                .exampleTranslation("Cuộc họp đó là người quyết định trong việc thay đổi sự nghiệp của tôi.")
                .build(),

            // Từ 15: turning point - bước ngoặt
            Lesson.VocabularyItem.builder()
                .word("turning point")
                .pronunciation("/ˈtɜːrnɪŋ pɔɪnt/")
                .partOfSpeech("noun phrase")
                .meaning("bước ngoặt, điểm chuyển ngoặt")
                .exampleSentence("Moving to the city was a turning point in my life.")
                .exampleTranslation("Chuyển đến thành phố là một bước ngoặt trong cuộc sống của tôi.")
                .build(),

            // Từ 16: nostalgia - nỗi nhớ thương
            Lesson.VocabularyItem.builder()
                .word("nostalgia")
                .pronunciation("/nɑːˈstældʒə/")
                .partOfSpeech("noun")
                .meaning("lòng nhớ thương, nỗi hoài cổ")
                .exampleSentence("Looking at old photos filled me with nostalgia.")
                .exampleTranslation("Nhìn những bức ảnh cũ làm tôi cảm thấy nỗi nhớ thương.")
                .build(),

            // Từ 17: reflect - suy tư
            Lesson.VocabularyItem.builder()
                .word("reflect")
                .pronunciation("/rɪˈflekt/")
                .partOfSpeech("verb")
                .meaning("suy tư, tomy")
                .exampleSentence("I often reflect on my past experiences and what I've learned.")
                .exampleTranslation("Tôi thường suy tư về những trải nghiệm quá khứ và những gì tôi đã học.")
                .build(),

            // Từ 18: influence - ảnh hưởng
            Lesson.VocabularyItem.builder()
                .word("influence")
                .pronunciation("/ˈɪnfluəns/")
                .partOfSpeech("noun/verb")
                .meaning("ảnh hưởng, tác động; ảnh hưởng đến")
                .exampleSentence("My parents' influence shaped who I am today.")
                .exampleTranslation("Ảnh hưởng của cha mẹ tôi đã định hình nên con người thôi nay.")
                .build(),

            // Từ 19: resilience - sức bền
            Lesson.VocabularyItem.builder()
                .word("resilience")
                .pronunciation("/rɪˈzɪliəns/")
                .partOfSpeech("noun")
                .meaning("khả năng phục hồi, sức bền")
                .exampleSentence("Her resilience in the face of adversity was admirable.")
                .exampleTranslation("Sức bền của cô ấy trước những khó khăn là đáng ngưỡng mộ.")
                .build(),

            // Từ 20: regret - hối tiếc
            Lesson.VocabularyItem.builder()
                .word("regret")
                .pronunciation("/rɪˈɡret/")
                .partOfSpeech("noun/verb")
                .meaning("hối tiếc, nuối tiếc; hối hận")
                .exampleSentence("I don't regret any decision I've made in my life.")
                .exampleTranslation("Tôi không hối tiếc bất kỳ quyết định nào tôi đã đưa ra.")
                .build()
        );

        Lesson lesson2 = Lesson.builder()
                .unitId(unitId)
                .title("Vocabulary - Từ Vựng Chủ Đề")
                .description("Học 20 từ vựng chủ chốt liên quan đến những sự kiện cuộc sống. " +
                           "Mỗi từ đều có phát âm, định nghĩa, loại từ và ví dụ câu trong ngữ cảnh thực tế.")
                .type("VOCABULARY")
                .content("<div class='vocabulary-lesson'>" +
                       "<h2>Từ Vựng Unit 1</h2>" +
                       "<p>Dưới đây là 20 từ vựng quan trọng để bạn có thể kể lại những câu chuyện sống.</p>" +
                       "<h3>Hướng dẫn học tập:</h3>" +
                       "<ol>" +
                       "  <li>Phát âm: Nghe và lặp lại từng từ</li>" +
                       "  <li>Ý nghĩa: Đọc định nghĩa và dịch sang Tiếng Việt</li>" +
                       "  <li>Ví dụ: Đọc ví dụ câu và hiểu cách sử dụng từ</li>" +
                       "  <li>Luyện tập: Tạo câu riêng sử dụng các từ này</li>" +
                       "</ol>" +
                       "</div>")
                .orderIndex(2)
                .estimatedDurationMinutes(45)
                .xpReward(15)
                .isActive(true)
                .createdBy("admin")
                .vocabulary(vocabulary)
                .build();

        lessonRepository.save(lesson2);
        System.out.println("  ✓ Lesson 2: Vocabulary (20 từ vựng)");
    }

    /**
     * Bài học 3: Language Focus - Ngữ Pháp
     * 
     * Mục đích:
     * - Ôn tập và nâng cao kỹ năng sử dụng thì quá khứ
     * - Hướng dẫn cách kết nối ý trong câu chuyện dài
     * - Cung cấp các cấu trúc ngữ pháp để kể lại sự kiện một cách tự nhiên
     * 
     * Nội dung:
     * - Simple Past (Thì quá khứ đơn)
     * - Past Continuous (Thì quá khứ tiếp diễn)
     * - Past Perfect (Thì quá khứ hoàn thành)
     * - Linking words (Từ liên kết)
     */
    private void createLesson3_LanguageFocus(String unitId) {
        Lesson lesson3 = Lesson.builder()
                .unitId(unitId)
                .title("Language Focus - Ngữ Pháp Trọng Tâm")
                .description("Học ngữ pháp cơ bản để kể lại những câu chuyện quá khứ. " +
                           "Tập trung vào thì quá khứ đơn, quá khứ tiếp diễn, quá khứ hoàn thành " +
                           "và các từ nối để kết nối ý tưởng.")
                .type("GRAMMAR")
                .content("<div class='grammar-lesson'>" +
                       "<h2>Thì Quá Khứ & Kết Nối Ý Tưởng</h2>" +
                       "<h3>1. Simple Past (Thì Quá Khứ Đơn)</h3>" +
                       "<p><strong>Cấu trúc:</strong> S + V(ed/irregular) + O</p>" +
                       "<p><strong>Ví dụ:</strong> She moved to London in 2015.</p>" +
                       "<p><strong>Cách dùng:</strong> Diễn tả hành động hoàn thành trong quá khứ</p>" +
                       "<h3>2. Past Continuous (Thì Quá Khứ Tiếp Diễn)</h3>" +
                       "<p><strong>Cấu trúc:</strong> S + was/were + V(ing)</p>" +
                       "<p><strong>Ví dụ:</strong> When she arrived, I was reading a book.</p>" +
                       "<p><strong>Cách dùng:</strong> Hành động đang xảy ra bị gián đoạn bởi hành động khác</p>" +
                       "<h3>3. Past Perfect (Thì Quá Khứ Hoàn Thành)</h3>" +
                       "<p><strong>Cấu trúc:</strong> S + had + V(ed)</p>" +
                       "<p><strong>Ví dụ:</strong> Before she moved, she had visited many countries.</p>" +
                       "<p><strong>Cách dùng:</strong> Hành động hoàn thành trước một thời điểm trong quá khứ</p>" +
                       "<h3>4. Từ Nối (Linking Words)</h3>" +
                       "<ul>" +
                       "  <li><strong>Before/After:</strong> Trước khi / Sau khi</li>" +
                       "  <li><strong>As soon as:</strong> Ngay khi</li>" +
                       "  <li><strong>When/While:</strong> Khi / Trong khi</li>" +
                       "  <li><strong>Moreover/Furthermore:</strong> Hơn nữa</li>" +
                       "  <li><strong>However:</strong> Tuy nhiên</li>" +
                       "  <li><strong>In addition:</strong> Ngoài ra</li>" +
                       "</ul>" +
                       "</div>")
                .orderIndex(3)
                .estimatedDurationMinutes(50)
                .xpReward(20)
                .isActive(true)
                .createdBy("admin")
                .vocabulary(new ArrayList<>())
                .build();

        lessonRepository.save(lesson3);
        System.out.println("  ✓ Lesson 3: Language Focus (Ngữ pháp)");
    }

    /**
     * Bài học 4: Reading - Đọc Hiểu
     * 
     * Mục đích:
     * - Phát triển kỹ năng đọc hiểu
     * - Học cách nhận dạng thông tin chính và chi tiết
     * - Thực hành trả lời câu hỏi dựa trên văn bản
     * 
     * Nội dung:
     * - Bài đọc về một câu chuyện cuộc sống thực tế
     * - Các câu hỏi comprehension (hiểu biết)
     * - Từ vựng mới trong ngữ cảnh
     */
    private void createLesson4_Reading(String unitId) {
        Lesson lesson4 = Lesson.builder()
                .unitId(unitId)
                .title("Reading - Đọc Hiểu")
                .description("Đọc và hiểu các câu chuyện thực tế về cuộc sống con người. " +
                           "Trả lời các câu hỏi để kiểm tra mức độ hiểu bài và rèn luyện kỹ năng đọc.")
                .type("READING")
                .content("<div class='reading-lesson'>" +
                       "<h2>The Story of Steve Jobs</h2>" +
                       "<p class='reading-text'>" +
                       "Steve Jobs was born on February 24, 1955, in Los Angeles, California. " +
                       "His biological parents were graduate students, but they put him up for adoption. " +
                       "He was adopted by Paul and Clara Jobs, who raised him in Silicon Valley. " +
                       "<br/><br/>" +
                       "As a child, Steve was curious and creative. He took apart electronic devices " +
                       "to understand how they worked. In high school, he built his first electronic device " +
                       "with help from Steve Wozniak, who would later become his business partner. " +
                       "<br/><br/>" +
                       "In 1976, Jobs and Wozniak founded Apple Computer Company in Jobs' family garage. " +
                       "They worked tirelessly to create personal computers that were easy to use. " +
                       "Their dedication paid off, and Apple became one of the most successful companies in the world. " +
                       "<br/><br/>" +
                       "In 1997, Jobs faced a major challenge when Apple was struggling financially. " +
                       "However, he didn't give up. He worked on new products and technologies. " +
                       "By introducing the iMac, iPod, iPhone, and iPad, he transformed Apple and the technology industry. " +
                       "<br/><br/>" +
                       "Steve Jobs is remembered as a visionary and innovator. " +
                       "Despite facing personal challenges and health issues, he never stopped pursuing his passion for technology. " +
                       "His story teaches us the importance of perseverance and creativity in achieving our dreams." +
                       "</p>" +
                       "<h3>Comprehension Questions (Câu hỏi hiểu biết):</h3>" +
                       "<ol>" +
                       "  <li>When and where was Steve Jobs born?</li>" +
                       "  <li>Who raised Steve Jobs?</li>" +
                       "  <li>What was Steve's childhood like?</li>" +
                       "  <li>When did Jobs and Wozniak found Apple?</li>" +
                       "  <li>What challenge did Apple face in 1997?</li>" +
                       "  <li>Name three products that Jobs introduced to transform Apple.</li>" +
                       "  <li>What qualities made Steve Jobs successful?</li>" +
                       "</ol>" +
                       "</div>")
                .orderIndex(4)
                .estimatedDurationMinutes(45)
                .xpReward(20)
                .isActive(true)
                .createdBy("admin")
                .vocabulary(new ArrayList<>())
                .build();

        lessonRepository.save(lesson4);
        System.out.println("  ✓ Lesson 4: Reading (Comprehension)");
    }

    /**
     * Bài học 5: Listening - Nghe Hiểu
     * 
     * Mục đích:
     * - Phát triển kỹ năng nghe hiểu tiếng Anh
     * - Nhận biết chi tiết và thông tin chính từ lời nói
     * - Quen thuộc với các cách phát âm và intonation khác nhau
     * 
     * Nội dung:
     * - Liên kết đến audio về các câu chuyện cá nhân
     * - Bài tập điền từ còn thiếu
     * - Câu hỏi trắc nghiệm
     */
    private void createLesson5_Listening(String unitId) {
        Lesson lesson5 = Lesson.builder()
                .unitId(unitId)
                .title("Listening - Nghe Hiểu")
                .description("Nghe các câu chuyện từ người bản xứ tiếng Anh và trả lời câu hỏi. " +
                           "Rèn luyện kỹ năng nhận biết thông tin chính và chi tiết từ lời nói ghi âm.")
                .type("LISTENING")
                .audioUrl("https://res.cloudinary.com/dykzfyb4t/video/upload/" +
                        "v1645000000/english-12-smart/unit1-listening-exercise.mp3")
                .content("<div class='listening-lesson'>" +
                       "<h2>Listening Exercise - Bài Tập Nghe</h2>" +
                       "<p>Hãy nghe đoạn âm thanh 2 lần. Trong lần nghe thứ hai, trả lời các câu hỏi dưới đây:</p>" +
                       "<h3>Lần nghe 1: Tìm hiểu nội dung chung</h3>" +
                       "<p>Bạn sẽ nghe một người phụ nữ kể lại câu chuyện cuộc sống của mình.</p>" +
                       "<h3>Lần nghe 2: Trả lời câu hỏi chi tiết</h3>" +
                       "<ol>" +
                       "  <li>What is the speaker's name? (Tên của người nói là gì?)</li>" +
                       "  <li>Where was she born? (Cô ấy sinh ra ở đâu?)</li>" +
                       "  <li>What did she study at university? (Cô ấy học gì ở đại học?)</li>" +
                       "  <li>What was her first job? (Công việc đầu tiên của cô ấy là gì?)</li>" +
                       "  <li>What was the most important event in her life? (Sự kiện quan trọng nhất trong cuộc sống là gì?)</li>" +
                       "  <li>What advice does she give to young people? (Cô ấy cho lý do gì cho giới trẻ?)</li>" +
                       "</ol>" +
                       "<h3>Ghi chú để nghe (Notes):</h3>" +
                       "<p>Trong khi nghe, hãy ghi chú lại:</p>" +
                       "<ul>" +
                       "  <li>Các từ khóa (key words)</li>" +
                       "  <li>Các con số (numbers)</li>" +
                       "  <li>Các sự kiện quan trọng (important events)</li>" +
                       "</ul>" +
                       "</div>")
                .orderIndex(5)
                .estimatedDurationMinutes(40)
                .xpReward(20)
                .isActive(true)
                .createdBy("admin")
                .vocabulary(new ArrayList<>())
                .build();

        lessonRepository.save(lesson5);
        System.out.println("  ✓ Lesson 5: Listening (Nghe hiểu)");
    }

    /**
     * Bài học 6: Speaking - Nói
     * 
     * Mục đích:
     * - Phát triển kỹ năng nói tiếng Anh
     * - Luyện tập kể lại câu chuyện cuộc sống
     * - Phát triển tự tin khi nói trước công chúng
     * 
     * Nội dung:
     * - Hướng dẫn phát âm
     * - Bài tập luyện tập nói
     * - Các tình huống giao tiếp thực tế
     * - Đánh giá phát âm
     */
    private void createLesson6_Speaking(String unitId) {
        Lesson lesson6 = Lesson.builder()
                .unitId(unitId)
                .title("Speaking - Nói")
                .description("Luyện tập nói tiếng Anh để kể lại câu chuyện cuộc sống của bạn. " +
                           "Phát triển kỹ năng giao tiếp và tự tin khi nói tiếng Anh.")
                .type("SPEAKING")
                .content("<div class='speaking-lesson'>" +
                       "<h2>Speaking Exercise - Bài Tập Nói</h2>" +
                       "<h3>Phần 1: Phát âm Thanh Điệu (Pronunciation & Intonation)</h3>" +
                       "<p>Nghe và lặp lại:</p>" +
                       "<ul>" +
                       "  <li>'I was born in...' (Tôi sinh ra ở...)</li>" +
                       "  <li>'When I was a child...' (Khi tôi còn nhỏ...)</li>" +
                       "  <li>'One memorable experience was...' (Một trải nghiệm đáng nhớ là...)</li>" +
                       "  <li>'This challenged me to...' (Điều này thúc đẩy tôi...)</li>" +
                       "</ul>" +
                       "<h3>Phần 2: Bài Tập Luyện Tập (Practice Exercises)</h3>" +
                       "<p><strong>Task 1: Thông tin cá nhân (Personal Information)</strong></p>" +
                       "<p>Trả lời các câu hỏi sau:</p>" +
                       "<ol>" +
                       "  <li>Tell me about where you were born. (Kể cho tôi nghe về nơi bạn sinh ra)</li>" +
                       "  <li>Describe an important event in your childhood. (Mô tả một sự kiện quan trọng trong tuổi thơ bạn)</li>" +
                       "  <li>What was your biggest achievement? (Thành tựu lớn nhất của bạn là gì?)</li>" +
                       "</ol>" +
                       "<p><strong>Task 2: Kể Câu Chuyện (Story Telling)</strong></p>" +
                       "<p>Kể một câu chuyện từ cuộc sống của bạn (2-3 phút):</p>" +
                       "<ul>" +
                       "  <li>Giới thiệu chủ đề (Introduction)</li>" +
                       "  <li>Kể chi tiết sự kiện (Details)</li>" +
                       "  <li>Kết luận và bài học rút ra (Conclusion & Lesson)</li>" +
                       "</ul>" +
                       "<h3>Phần 3: Đánh Giá (Assessment Tips)</h3>" +
                       "<p>Tự đánh giá bản thân hoặc nhờ giáo viên đánh giá:</p>" +
                       "<ul>" +
                       "  <li>✓ Phát âm: Âm thanh rõ ràng, chính xác?</li>" +
                       "  <li>✓ Ngữ pháp: Sử dụng thì quá khứ đúng?</li>" +
                       "  <li>✓ Từ vựng: Dùng các từ phù hợp?</li>" +
                       "  <li>✓ Luồng nói: Tốc độ và nhịp điệu tự nhiên?</li>" +
                       "  <li>✓ Tự tin: Nói với sự tự tin?</li>" +
                       "</ul>" +
                       "</div>")
                .orderIndex(6)
                .estimatedDurationMinutes(50)
                .xpReward(25)
                .isActive(true)
                .createdBy("admin")
                .vocabulary(new ArrayList<>())
                .build();

        lessonRepository.save(lesson6);
        System.out.println("  ✓ Lesson 6: Speaking (Nói)");
    }

    /**
     * Bài học 7: Writing - Viết
     * 
     * Mục đích:
     * - Phát triển kỹ năng viết tiếng Anh
     * - Viết được một đoạn văn kể lại câu chuyện sống
     * - Sử dụng ngữ pháp và từ vựng chính xác
     * - Sắp xếp ý tưởng một cách hợp lý
     * 
     * Nội dung:
     * - Outline (Dàn bài)
     * - Hướng dẫn viết từng phần
     * - Ví dụ mẫu
     * - Bài tập viết
     * - Hướng dẫn kiểm tra lại (Proofreading)
     */
    private void createLesson7_Writing(String unitId) {
        Lesson lesson7 = Lesson.builder()
                .unitId(unitId)
                .title("Writing - Viết")
                .description("Viết một đoạn văn hoặc bài essay kể lại câu chuyện cuộc sống. " +
                           "Học cấu trúc bài viết, sắp xếp ý tưởng, và kiểm tra lại công việc của bạn.")
                .type("WRITING")
                .content("<div class='writing-lesson'>" +
                       "<h2>Writing Exercise - Bài Tập Viết</h2>" +
                       "<h3>Phần 1: Cấu Trúc Bài Viết (Structure)</h3>" +
                       "<p><strong>Paragraph Structure (Cấu trúc Đoạn văn):</strong></p>" +
                       "<ol>" +
                       "  <li><strong>Introduction (Mở bài):</strong> Giới thiệu sự kiện hoặc câu chuyện</li>" +
                       "  <li><strong>Body (Thân bài):</strong> Mô tả chi tiết sự kiện, cảm xúc, và kết quả</li>" +
                       "  <li><strong>Conclusion (Kết bài):</strong> Kết luận và bài học rút ra</li>" +
                       "</ol>" +
                       "<h3>Phần 2: Từ Nối Hữu Ích (Useful Linking Words)</h3>" +
                       "<ul>" +
                       "  <li>First/Firstly: Trước tiên</li>" +
                       "  <li>Then/After that: Sau đó</li>" +
                       "  <li>When/While: Khi / Trong khi</li>" +
                       "  <li>Before/After: Trước / Sau</li>" +
                       "  <li>As soon as: Ngay khi</li>" +
                       "  <li>Because/Because of: Vì / Bởi vì</li>" +
                       "  <li>As a result: Do đó</li>" +
                       "  <li>Finally/In conclusion: Cuối cùng / Kết luận</li>" +
                       "</ul>" +
                       "<h3>Phần 3: Bài Mẫu (Model Essay)</h3>" +
                       "<p class='model-essay'>" +
                       "<strong>A Turning Point in My Life</strong><br/>" +
                       "One memorable experience changes my life forever. When I was fifteen, I decided to join " +
                       "the debate club at my school. At first, I was nervous because I had never spoken in front " +
                       "of a large audience before. However, my teacher encouraged me to try, and I did.<br/><br/>" +
                       "Joining the debate club taught me not only about public speaking but also about confidence. " +
                       "I learned to organize my thoughts clearly and express my opinions in English. After six months " +
                       "of practice, I won a local debate competition. This victory was the result of perseverance and hard work.<br/><br/>" +
                       "Looking back, I realize that this experience shaped who I am today. It gave me the confidence " +
                       "to pursue my dreams and helped me understand the importance of stepping outside my comfort zone. " +
                       "I am grateful for this turning point in my life." +
                       "</p>" +
                       "<h3>Phần 4: Bài Tập (Writing Task)</h3>" +
                       "<p><strong>Task: Write about an important event in your life (3-4 paragraphs)</strong></p>" +
                       "<p>Viết về một sự kiện quan trọng trong cuộc sống bạn (3-4 đoạn văn):</p>" +
                       "<ul>" +
                       "  <li>Chọn một sự kiện có ý nghĩa với bạn</li>" +
                       "  <li>Viết dàn bài trước (outline)</li>" +
                       "  <li>Viết bản nháp (draft)</li>" +
                       "  <li>Kiểm tra lại (proofread)</li>" +
                       "</ul>" +
                       "<h3>Phần 5: Hướng Dẫn Kiểm Tra (Proofreading Checklist)</h3>" +
                       "<ul>" +
                       "  <li>☐ Có giới thiệu rõ ràng về sự kiện?</li>" +
                       "  <li>☐ Có mô tả chi tiết đủ?</li>" +
                       "  <li>☐ Ngữ pháp có chính xác (thì quá khứ)?</li>" +
                       "  <li>☐ Từ vựng phù hợp và đa dạng?</li>" +
                       "  <li>☐ Có sử dụng từ nối để kết nối ý?</li>" +
                       "  <li>☐ Có kết bài mạnh mẽ với bài học rút ra?</li>" +
                       "  <li>☐ Có lỗi chính tả?</li>" +
                       "  <li>☐ Đúng từ, dấu câu?</li>" +
                       "</ul>" +
                       "</div>")
                .orderIndex(7)
                .estimatedDurationMinutes(60)
                .xpReward(25)
                .isActive(true)
                .createdBy("admin")
                .vocabulary(new ArrayList<>())
                .build();

        lessonRepository.save(lesson7);
        System.out.println("  ✓ Lesson 7: Writing (Viết)");
    }
}
