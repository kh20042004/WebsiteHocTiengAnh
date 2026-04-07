package com.english12smart.controller;

import com.english12smart.dto.AdminContentDTO;
import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.exception.BadRequestException;
import com.english12smart.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminContentController {

    private final AdminContentService adminContentService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<AdminContentDTO.ContentListResponse>> listContent(
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {

        AdminContentDTO.ContentFilter filter = AdminContentDTO.ContentFilter.builder()
                .contentType(parseContentType(contentType))
                .keyword(keyword)
                .status(status)
                .isActive(isActive)
                .createdBy(createdBy)
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .build();

        AdminContentDTO.ContentListResponse response = adminContentService.listContent(filter);
        return ResponseEntity.ok(ApiResponseDTO.success("Danh sách nội dung học tập", response));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDTO<AdminContentDTO.ContentStats>> getStats() {
        return ResponseEntity.ok(ApiResponseDTO.success("Thống kê nội dung", adminContentService.getContentStats()));
    }

    @PutMapping("/{contentType}/{contentId}/status")
    public ResponseEntity<ApiResponseDTO<AdminContentDTO.ContentSummary>> updateStatus(
            @PathVariable String contentType,
            @PathVariable String contentId,
            @RequestBody AdminContentDTO.StatusUpdateRequest request) {
        AdminContentDTO.ContentType type = parseContentType(contentType);
        AdminContentDTO.ContentSummary summary = adminContentService.updateContentStatus(type, contentId, request);
        return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật trạng thái thành công", summary));
    }

    @DeleteMapping("/{contentType}/{contentId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteContent(
            @PathVariable String contentType,
            @PathVariable String contentId) {
        AdminContentDTO.ContentType type = parseContentType(contentType);
        adminContentService.deleteContent(type, contentId);
        return ResponseEntity.ok(ApiResponseDTO.success("Xóa nội dung thành công", null));
    }

    private AdminContentDTO.ContentType parseContentType(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return AdminContentDTO.ContentType.from(raw);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Loại nội dung không hợp lệ: " + raw);
        }
    }
}
