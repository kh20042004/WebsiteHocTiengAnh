package com.english12smart.constant;

/**
 * ========== USER ROLE ENUM ==========
 * Các roles có thể có trong ứng dụng
 */
public enum UserRole {
    
    /**
     * Role ADMIN - Quản trị hệ thống
     */
    ADMIN("ROLE_ADMIN", "Quản trị viên"),
    
    /**
     * Role TEACHER - Giáo viên
     */
    TEACHER("ROLE_TEACHER", "Giáo viên"),
    
    /**
     * Role STUDENT - Học sinh
     */
    STUDENT("ROLE_STUDENT", "Học sinh");
    
    private final String code;
    private final String displayName;
    
    UserRole(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Convert code to enum
     * 
     * @param code - Role code
     * @return UserRole or null nếu invalid
     */
    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }
    
    /**
     * Check nếu role có permission
     * 
     * @param roleCode - Role code
     * @return true nếu valid
     */
    public static boolean isValidRole(String roleCode) {
        return fromCode(roleCode) != null;
    }
}