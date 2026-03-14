package com.english12smart.util;

import lombok.extern.slf4j.Slf4j;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * ========== DATE/TIME UTILITY ==========
 * Các hàm tiện ích xử lý date và time
 */
@Slf4j
public class DateTimeUtils {
    
    /**
     * Format date thành string
     * 
     * @param date - Date object
     * @param pattern - Format pattern (e.g., "yyyy-MM-dd HH:mm:ss")
     * @return Formatted string
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(
                    date.toInstant(), 
                    ZoneId.systemDefault()
            );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return localDateTime.format(formatter);
        } catch (Exception e) {
            log.error("Error formatting date: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse string thành date
     * 
     * @param dateString - Date string
     * @param pattern - Format pattern
     * @return Date object
     */
    public static Date parse(String dateString, String pattern) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            log.error("Error parsing date: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get current timestamp (milliseconds)
     * 
     * @return Current timestamp
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * Get current date
     * 
     * @return Today's date
     */
    public static Date getCurrentDate() {
        return new Date();
    }
    
    /**
     * Thêm ngày vào date
     * 
     * @param date - Original date
     * @param days - Số ngày cần thêm
     * @return New date
     */
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(
                date.toInstant(), 
                ZoneId.systemDefault()
        );
        localDateTime = localDateTime.plusDays(days);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Thêm giờ vào date
     * 
     * @param date - Original date
     * @param hours - Số giờ cần thêm
     * @return New date
     */
    public static Date addHours(Date date, int hours) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(
                date.toInstant(), 
                ZoneId.systemDefault()
        );
        localDateTime = localDateTime.plusHours(hours);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Tính số ngày giữa hai date
     * 
     * @param from - Start date
     * @param to - End date
     * @return Số ngày
     */
    public static long daysBetween(Date from, Date to) {
        if (from == null || to == null) {
            return 0;
        }
        LocalDate fromDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(fromDate, toDate).getDays();
    }
    
    /**
     * Check nếu date là trong quá khứ
     * 
     * @param date - Date cần check
     * @return true nếu trong quá khứ
     */
    public static boolean isPastDate(Date date) {
        if (date == null) {
            return false;
        }
        return date.before(new Date());
    }
    
    /**
     * Check nếu date là trong tương lai
     * 
     * @param date - Date cần check
     * @return true nếu trong tương lai
     */
    public static boolean isFutureDate(Date date) {
        if (date == null) {
            return false;
        }
        return date.after(new Date());
    }
    
    /**
     * Format date thành Vietnamese format
     * 
     * @param date - Date object
     * @return Formatted string (dd/MM/yyyy)
     */
    public static String formatVietnamese(Date date) {
        return format(date, "dd/MM/yyyy");
    }
    
    /**
     * Format datetime thành Vietnamese format
     * 
     * @param date - Date object
     * @return Formatted string (dd/MM/yyyy HH:mm:ss)
     */
    public static String formatVietnameseDateTime(Date date) {
        return format(date, "dd/MM/yyyy HH:mm:ss");
    }
}