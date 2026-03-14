package com.english12smart.util;

import lombok.extern.slf4j.Slf4j;
import java.util.*;

/**
 * ========== VALIDATION UTILITY ==========
 * Các hàm tiện ích validation dữ liệu
 */
@Slf4j
public class ValidationUtils {
    
    /**
     * Validate required field không để trống
     * 
     * @param value - Value cần validate
     * @param fieldName - Tên field (cho error message)
     * @return Error message nếu invalid, null nếu valid
     */
    public static String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " không được để trống";
        }
        return null;
    }
    
    /**
     * Validate string length
     * 
     * @param value - Value cần validate
     * @param minLength - Min length
     * @param maxLength - Max length
     * @param fieldName - Tên field
     * @return Error message nếu invalid, null nếu valid
     */
    public static String validateLength(String value, int minLength, int maxLength, String fieldName) {
        if (value == null) {
            return fieldName + " là bắt buộc";
        }
        
        if (value.length() < minLength) {
            return fieldName + " phải có ít nhất " + minLength + " ký tự";
        }
        
        if (value.length() > maxLength) {
            return fieldName + " không được vượt quá " + maxLength + " ký tự";
        }
        
        return null;
    }
    
    /**
     * Validate email
     * 
     * @param email - Email cần validate
     * @return Error message nếu invalid, null nếu valid
     */
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email không được để trống";
        }
        
        if (!StringUtils.isValidEmail(email)) {
            return "Email không hợp lệ";
        }
        
        return null;
    }
    
    /**
     * Validate password
     * 
     * @param password - Password cần validate
     * @return Error message nếu invalid, null nếu valid
     */
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Mật khẩu không được để trống";
        }
        
        if (password.length() < 8) {
            return "Mật khẩu phải có ít nhất 8 ký tự";
        }
        
        if (!StringUtils.isStrongPassword(password)) {
            return "Mật khẩu phải chứa: chữ hoa, chữ thường, số và ký tự đặc biệt";
        }
        
        return null;
    }
    
    /**
     * Validate number range
     * 
     * @param value - Value cần validate
     * @param min - Minimum value
     * @param max - Maximum value
     * @param fieldName - Tên field
     * @return Error message nếu invalid, null nếu valid
     */
    public static String validateNumberRange(int value, int min, int max, String fieldName) {
        if (value < min) {
            return fieldName + " phải lớn hơn hoặc bằng " + min;
        }
        
        if (value > max) {
            return fieldName + " phải nhỏ hơn hoặc bằng " + max;
        }
        
        return null;
    }
    
    /**
     * Validate file
     * 
     * @param fileName - File name
     * @param fileSize - File size in bytes
     * @param allowedExtensions - Allowed extensions
     * @param maxSizeInMB - Max size in MB
     * @return Error message nếu invalid, null nếu valid
     */
    public static String validateFile(String fileName, long fileSize, String[] allowedExtensions, int maxSizeInMB) {
        if (fileName == null || fileName.isEmpty()) {
            return "Tên file không được để trống";
        }
        
        if (!FileUtils.isValidFileExtension(fileName, allowedExtensions)) {
            return "Loại file không được phép. Chỉ chấp nhận: " + String.join(", ", allowedExtensions);
        }
        
        if (!FileUtils.isValidFileSize(fileSize, maxSizeInMB)) {
            return "Kích thước file không được vượt quá " + maxSizeInMB + " MB";
        }
        
        return null;
    }
    
    /**
     * Validate phone number
     * 
     * @param phone - Phone number
     * @return Error message nếu invalid, null nếu valid
     */
    public static String validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Số điện thoại không được để trống";
        }
        
        if (!StringUtils.isValidPhoneNumber(phone)) {
            return "Số điện thoại không hợp lệ";
        }
        
        return null;
    }
    
    /**
     * Validate username (alphanumeric, dash, underscore only)
     * 
     * @param username - Username cần validate
     * @return Error message nếu invalid, null nếu valid
     */
    public static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Tên đăng nhập không được để trống";
        }
        
        if (username.length() < 3 || username.length() > 20) {
            return "Tên đăng nhập phải có từ 3 đến 20 ký tự";
        }
        
        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            return "Tên đăng nhập chỉ được chứa chữ cái, số, gạch dưới, và gạch ngang";
        }
        
        return null;
    }
    
    /**
     * Validate URL
     * 
     * @param url - URL cần validate
     * @return Error message nếu invalid, null nếu valid
     */
    public static String validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "URL không được để trống";
        }
        
        try {
            new java.net.URL(url);
            return null;
        } catch (Exception e) {
            return "URL không hợp lệ";
        }
    }
    
    /**
     * Validate duplicate email
     * 
     * @param emails - List of emails
     * @return Error message nếu có duplicate, null nếu valid
     */
    public static String validateNoDuplicateEmail(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return null;
        }
        
        Set<String> uniqueEmails = new HashSet<>();
        for (String email : emails) {
            if (!uniqueEmails.add(email.toLowerCase())) {
                return "Email trùng lặp: " + email;
            }
        }
        
        return null;
    }
}