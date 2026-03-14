package com.english12smart.util;

import lombok.extern.slf4j.Slf4j;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ========== STRING UTILITY ==========
 * Các hàm tiện ích xử lý string
 */
@Slf4j
public class StringUtils {
    
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Generate random string với độ dài được chỉ định
     * 
     * @param length - độ dài chuỗi
     * @return Random string
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }
    
    /**
     * Generate random code (chỉ số)
     * 
     * @param length - độ dài
     * @return Random code
     */
    public static String generateRandomCode(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }
    
    /**
     * Validate email format
     * 
     * @param email - Email cần validate
     * @return true nếu hợp lệ
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    
    /**
     * Validate phone number (Vietnam format)
     * 
     * @param phone - Phone number
     * @return true nếu hợp lệ
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Vietnam phone: 10 digits, starts with 0 or +84
        String phoneRegex = "^(\\+84|0)\\d{9}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone).matches();
    }
    
    /**
     * Validate password strength
     * Min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
     * 
     * @param password - Password cần validate
     * @return true nếu password mạnh
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLowercase = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?]").matcher(password).find();
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
    
    /**
     * Truncate string với dấu "..."
     * 
     * @param text - Text cần truncate
     * @param maxLength - Max length
     * @return Truncated string
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Check nếu string is null or empty
     * 
     * @param text - Text cần check
     * @return true nếu null hoặc empty
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
    
    /**
     * Convert list sang comma-separated string
     * 
     * @param list - List
     * @return Comma-separated string
     */
    public static String join(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(", ", list);
    }
    
    /**
     * Convert comma-separated string sang list
     * 
     * @param text - Comma-separated string
     * @return List
     */
    public static List<String> split(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = text.split(",");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            result.add(part.trim());
        }
        return result;
    }
    
    /**
     * Convert string to slug format
     * Example: "Hello World" -> "hello-world"
     * 
     * @param text - Text cần convert
     * @return Slug
     */
    public static String toSlug(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
    
    /**
     * Capitalize first letter
     * 
     * @param text - Text cần capitalize
     * @return Capitalized string
     */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}