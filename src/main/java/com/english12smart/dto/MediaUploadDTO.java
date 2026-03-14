package com.english12smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Media Upload Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadDTO {

    /**
     * URL của media đã upload
     */
    private String url;

    /**
     * Tên file gốc
     */
    private String fileName;

    /**
     * Kích thước file (bytes)
     */
    private Long fileSize;

    /**
     * Loại file (image, audio, video, pdf)
     */
    private String mediaType;

    /**
     * Public ID của file trên Cloudinary (dùng để xóa)
     */
    private String publicId;

    /**
     * Format của file
     */
    private String format;

    /**
     * Thời gian upload
     */
    private Long uploadedAt;

    /**
     * Người upload
     */
    private String uploadedBy;
}
