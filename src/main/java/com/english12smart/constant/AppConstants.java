package com.english12smart.constant;

/**
 * ========== APPLICATION CONSTANTS ==========
 * Các hằng số sử dụng trong ứng dụng
 */
public class AppConstants {
    
    // ========== HTTP CONSTANTS ==========
    public static final int HTTP_SUCCESS = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    
    // ========== API ENDPOINTS ==========
    public static final String API_PREFIX = "/api";
    public static final String API_AUTH = API_PREFIX + "/auth";
    public static final String API_USER = API_PREFIX + "/user";
    public static final String API_LESSON = API_PREFIX + "/lesson";
    public static final String API_SUBMISSION = API_PREFIX + "/submission";
    public static final String API_MEDIA = API_PREFIX + "/media";
    public static final String API_ADMIN = API_PREFIX + "/admin";
    public static final String API_TEACHER = API_PREFIX + "/teacher";
    public static final String API_STUDENT = API_PREFIX + "/student";
    
    // ========== PAGINATION ==========
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // ========== FILE UPLOAD ==========
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB
    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10 MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif", "webp"};
    public static final String[] ALLOWED_DOCUMENT_TYPES = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"};
    public static final String[] ALLOWED_AUDIO_TYPES = {"mp3", "wav", "m4a", "flac"};
    public static final String UPLOAD_DIRECTORY = "uploads/";
    
    // ========== PASSWORD RULES ==========
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 64;
    
    // ========== USERNAME RULES ==========
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    
    // ========== EMAIL RULES ==========
    public static final int MIN_EMAIL_LENGTH = 5;
    public static final int MAX_EMAIL_LENGTH = 100;
    
    // ========== PAGINATION DEFAULTS ==========
    public static final String DEFAULT_SORT = "createdAt";
    public static final String SORT_ASCENDING = "asc";
    public static final String SORT_DESCENDING = "desc";
    
    // ========== CACHE KEYS ==========
    public static final String CACHE_USER = "user:";
    public static final String CACHE_LESSON = "lesson:";
    public static final String CACHE_SUBMISSION = "submission:";
    public static final String CACHE_TOKEN_BLACKLIST = "token_blacklist:";
    
    // ========== CACHE TTL (in seconds) ==========
    public static final long CACHE_TTL_USER = 3600; // 1 hour
    public static final long CACHE_TTL_LESSON = 1800; // 30 minutes
    public static final long CACHE_TTL_TOKEN = 300; // 5 minutes
    
    // ========== DATE FORMATS ==========
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String VIETNAMESE_DATE_FORMAT = "dd/MM/yyyy";
    public static final String VIETNAMESE_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    
    // ========== VALIDATION MESSAGES ==========
    public static final String MSG_SUCCESS = "Thành công";
    public static final String MSG_ERROR = "Lỗi";
    public static final String MSG_CREATED = "Tạo mới thành công";
    public static final String MSG_UPDATED = "Cập nhật thành công";
    public static final String MSG_DELETED = "Xóa thành công";
    public static final String MSG_INVALID_INPUT = "Dữ liệu nhập vào không hợp lệ";
    public static final String MSG_UNAUTHORIZED = "Không có quyền truy cập";
    public static final String MSG_FORBIDDEN = "Bị cấm truy cập";
    public static final String MSG_NOT_FOUND = "Không tìm thấy";
    public static final String MSG_SERVER_ERROR = "Lỗi máy chủ";
    
    // ========== REGEX PATTERNS ==========
    public static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String REGEX_PHONE_VN = "^(\\+84|0)\\d{9}$";
    public static final String REGEX_USERNAME = "^[a-zA-Z0-9_-]+$";
    public static final String REGEX_URL = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
}