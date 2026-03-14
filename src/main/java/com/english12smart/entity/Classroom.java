package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity lớp học của giáo viên
 */
@Document(collection = "classrooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {

    @Id
    private String id;

    /** Tên lớp, VD: 12A1 */
    @Indexed
    private String name;

    /** Mô tả, VD: English Advanced */
    private String description;

    /** Khối học: "12", "11", "10" */
    private String grade;

    /** Giáo viên sở hữu lớp */
    @Indexed
    private String teacherId;

    /** Mã lớp cho học sinh tham gia (6 ký tự, VD: AB12CD) */
    @Indexed
    private String classCode;

    /** Lịch học, VD: "T2, T4, T6 • 7:00 - 8:30" */
    private String schedule;

    /** Màu chủ đề: blue | emerald | purple | amber | rose */
    @Builder.Default
    private String colorTheme = "blue";

    /** Trạng thái: ACTIVE | UPCOMING | COMPLETED */
    @Builder.Default
    private String status = "ACTIVE";

    /** Sĩ số tối đa */
    @Builder.Default
    private Integer maxStudents = 40;

    /** Danh sách ID học sinh đã tham gia */
    @Builder.Default
    private List<String> studentIds = new ArrayList<>();

    /** Số bài tập đã tạo */
    @Builder.Default
    private Integer totalAssignments = 0;

    /** Số bài tập chưa chấm */
    @Builder.Default
    private Integer ungradedAssignments = 0;

    private Long createdAt;
    private Long updatedAt;

    /** Số học sinh hiện tại */
    public int getStudentCount() {
        return studentIds == null ? 0 : studentIds.size();
    }

    /** Màu Tailwind CSS gradient tương ứng với colorTheme */
    public String getGradientClass() {
        return switch (colorTheme == null ? "blue" : colorTheme) {
            case "emerald" -> "from-emerald-500 to-emerald-600";
            case "purple"  -> "from-purple-500 to-purple-600";
            case "amber"   -> "from-amber-500 to-amber-600";
            case "rose"    -> "from-rose-500 to-rose-600";
            default        -> "from-blue-500 to-blue-600";
        };
    }

    /** Text color cho badge mô tả */
    public String getDescriptionColorClass() {
        return switch (colorTheme == null ? "blue" : colorTheme) {
            case "emerald" -> "text-emerald-100";
            case "purple"  -> "text-purple-100";
            case "amber"   -> "text-amber-100";
            case "rose"    -> "text-rose-100";
            default        -> "text-blue-100";
        };
    }

    /** Display text cho status */
    public String getStatusDisplay() {
        return switch (status == null ? "ACTIVE" : status) {
            case "UPCOMING"   -> "Sắp bắt đầu";
            case "COMPLETED"  -> "Đã kết thúc";
            default           -> "Đang hoạt động";
        };
    }
}
