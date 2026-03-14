package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ========== PAGINATION REQUEST DTO ==========
 * Request DTO cho pagination queries
 * 
 * Usage:
 * GET /api/lesson?page=0&pageSize=20&sort=createdAt&order=desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationRequestDTO {
    
    /**
     * Page number (0-indexed)
     * Default: 0
     */
    @Builder.Default
    private int page = 0;
    
    /**
     * Page size
     * Default: 20, Max: 100
     */
    @Builder.Default
    private int pageSize = 20;
    
    /**
     * Sort field (e.g., "createdAt", "name")
     * Default: "createdAt"
     */
    @Builder.Default
    private String sort = "createdAt";
    
    /**
     * Sort order: "asc" hoặc "desc"
     * Default: "desc"
     */
    @Builder.Default
    private String order = "desc";
    
    /**
     * Search keyword
     */
    private String search;
    
    /**
     * Filter field (dùng cho specific filters)
     */
    private String filter;
    
    /**
     * Validate pagination request
     * 
     * @return true nếu hợp lệ
     */
    public boolean isValid() {
        if (page < 0) {
            page = 0;
        }
        
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        
        if (sort == null || sort.isEmpty()) {
            sort = "createdAt";
        }
        
        if (order == null || (!order.equals("asc") && !order.equals("desc"))) {
            order = "desc";
        }
        
        return true;
    }
}