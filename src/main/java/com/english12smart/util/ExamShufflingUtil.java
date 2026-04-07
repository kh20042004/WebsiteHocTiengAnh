package com.english12smart.util;

import java.util.*;

/**
 * Utility class để xáo trộn câu hỏi và đáp án
 * Đảm bảo độ ngẫu nhiên cao để tránh gian lận
 */
public class ExamShufflingUtil {
    
    /**
     * Xáo trộn danh sách các items sử dụng Fisher-Yates algorithm
     */
    public static <T> List<T> shuffleList(List<T> list) {
        List<T> shuffled = new ArrayList<>(list);
        Random random = new Random(System.nanoTime()); // Sử dụng nano time để tăng randomness
        
        for (int i = shuffled.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            // Swap
            T temp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, temp);
        }
        
        return shuffled;
    }
    
    /**
     * Xáo trộn danh sách và giữ lại index của item gốc
     * Hữu ích khi cần biết đáp án đúng là option nào
     * 
     * @return Map chứa:
     *   - "shuffled": List các item đã xáo trộn
     *   - "indexMap": Map từ index mới -> index cũ
     */
    public static <T> Map<String, Object> shuffleWithIndexTracking(List<T> list) {
        List<T> shuffled = new ArrayList<>(list);
        Map<Integer, Integer> indexMap = new HashMap<>(); // newIndex -> oldIndex
        
        Random random = new Random(System.nanoTime());
        
        for (int i = 0; i < shuffled.size(); i++) {
            indexMap.put(i, i);
        }
        
        for (int i = shuffled.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            
            // Swap items
            T temp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, temp);
            
            // Swap index mapping
            Integer tempIndex = indexMap.get(i);
            indexMap.put(i, indexMap.get(j));
            indexMap.put(j, tempIndex);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("shuffled", shuffled);
        result.put("indexMap", indexMap);
        
        return result;
    }
    
    /**
     * Tạo một seed độc lập cho mỗi student-exam combination
     * Đảm bảo mỗi học sinh có thứ tự câu hỏi khác nhau
     */
    public static long generateShuffleSeed(String studentId, String examId, long timestamp) {
        // Kết hợp studentId, examId và timestamp để tạo seed duy nhất
        String combined = studentId + "|" + examId + "|" + timestamp;
        return combined.hashCode();
    }
    
    /**
     * Shuffle questions với seed cố định
     * Sử dụng khi cần reproducible shuffle (ví dụ: debug)
     */
    public static <T> List<T> shuffleWithSeed(List<T> list, long seed) {
        List<T> shuffled = new ArrayList<>(list);
        Random random = new Random(seed);
        
        for (int i = shuffled.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T temp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, temp);
        }
        
        return shuffled;
    }
    
    /**
     * Shuffle string arrays (dùng cho multiple choice options)
     */
    public static String[] shuffleStringArray(String[] array) {
        String[] shuffled = array.clone();
        Random random = new Random(System.nanoTime());
        
        for (int i = shuffled.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String temp = shuffled[i];
            shuffled[i] = shuffled[j];
            shuffled[j] = temp;
        }
        
        return shuffled;
    }
    
    /**
     * Tạo mapping từ đáp án gốc sang đáp án sau khi shuffle
     */
    public static Map<String, Integer> createAnswerMapping(String[] original, String[] shuffled) {
        Map<String, Integer> mapping = new HashMap<>();
        
        for (int i = 0; i < shuffled.length; i++) {
            String option = shuffled[i];
            // Tìm index của option này trong mảng gốc
            for (int j = 0; j < original.length; j++) {
                if (original[j].equals(option)) {
                    mapping.put(option, j);
                    break;
                }
            }
        }
        
        return mapping;
    }
}
