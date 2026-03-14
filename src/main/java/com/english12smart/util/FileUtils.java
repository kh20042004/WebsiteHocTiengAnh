package com.english12smart.util;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.nio.file.*;
import java.util.*;

/**
 * ========== FILE UTILITY ==========
 * Các hàm tiện ích xử lý file
 */
@Slf4j
public class FileUtils {
    
    /**
     * Get file extension
     * 
     * @param filename - File name
     * @return Extension (e.g., "pdf", "jpg")
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }
    
    /**
     * Get file name without extension
     * 
     * @param filename - File name
     * @return File name without extension
     */
    public static String getFileNameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(0, lastDot) : filename;
    }
    
    /**
     * Check nếu file extension hợp lệ
     * 
     * @param filename - File name
     * @param allowedExtensions - List of allowed extensions (e.g., "pdf", "jpg", "png")
     * @return true nếu hợp lệ
     */
    public static boolean isValidFileExtension(String filename, String... allowedExtensions) {
        String extension = getFileExtension(filename);
        for (String allowed : allowedExtensions) {
            if (extension.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check nếu file size không vượt quá limit
     * 
     * @param fileSize - File size in bytes
     * @param maxSizeInMB - Max size in MB
     * @return true nếu hợp lệ
     */
    public static boolean isValidFileSize(long fileSize, int maxSizeInMB) {
        long maxSizeInBytes = (long) maxSizeInMB * 1024 * 1024;
        return fileSize <= maxSizeInBytes;
    }
    
    /**
     * Convert bytes to human readable format
     * 
     * @param bytes - File size in bytes
     * @return Formatted string (e.g., "5.23 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * Create directory nếu chưa tồn tại
     * 
     * @param directoryPath - Directory path
     * @return true nếu thành công
     */
    public static boolean createDirectoryIfNotExists(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created directory: {}", directoryPath);
            }
            return true;
        } catch (Exception e) {
            log.error("Error creating directory: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete file
     * 
     * @param filePath - File path
     * @return true nếu thành công
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Deleted file: {}", filePath);
            }
            return true;
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate unique filename
     * 
     * @param originalFilename - Original filename
     * @return Unique filename (e.g., "file_1704067200000.pdf")
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String nameWithoutExtension = getFileNameWithoutExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        return nameWithoutExtension + "_" + timestamp + "." + extension;
    }
    
    /**
     * Sanitize filename (remove special characters)
     * 
     * @param filename - Original filename
     * @return Sanitized filename
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        // Remove special characters, keep only alphanumeric, dash, underscore, and dot
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Check nếu file type là image
     * 
     * @param filename - File name
     * @return true nếu là image
     */
    public static boolean isImage(String filename) {
        return isValidFileExtension(filename, "jpg", "jpeg", "png", "gif", "bmp", "webp");
    }
    
    /**
     * Check nếu file type là document
     * 
     * @param filename - File name
     * @return true nếu là document
     */
    public static boolean isDocument(String filename) {
        return isValidFileExtension(filename, "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");
    }
}