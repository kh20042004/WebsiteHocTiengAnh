package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ========== PAGINATION RESPONSE DTO ==========
 * Response DTO cho pagination queries
 * 
 * Ví dụ:
 * {
 *   "content": [...],
 *   "page": 0,
 *   "pageSize": 20,
 *   "totalElements": 100,
 *   "totalPages": 5,
 *   "hasNextPage": true,
 *   "hasPreviousPage": false
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationResponseDTO<T> {
    
    /**
     * Content của page hiện tại
     */
    private java.util.List<T> content;
    
    /**
     * Current page number (0-indexed)
     */
    private int page;
    
    /**
     * Size của mỗi page
     */
    private int pageSize;
    
    /**
     * Total số elements trong tất cả pages
     */
    private long totalElements;
    
    /**
     * Total số pages
     */
    private int totalPages;
    
    /**
     * Check nếu có next page
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean hasNextPage() {
        return page < totalPages - 1;
    }
    
    /**
     * Check nếu có previous page
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean hasPreviousPage() {
        return page > 0;
    }
    
    /**
     * Get next page number
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getNextPage() {
        return page + 1;
    }
    
    /**
     * Get previous page number
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getPreviousPage() {
        return page - 1;
    }
}