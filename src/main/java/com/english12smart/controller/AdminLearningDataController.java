package com.english12smart.controller;

import com.english12smart.dto.ApiResponseDTO;
import com.english12smart.dto.LearningDataOverviewDTO;
import com.english12smart.service.LearningDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/learning-data")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminLearningDataController {

    private final LearningDataService learningDataService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponseDTO<LearningDataOverviewDTO>> getOverview() {
        LearningDataOverviewDTO overview = learningDataService.getOverview();
        return ResponseEntity.ok(ApiResponseDTO.success("Tổng quan dữ liệu học tập", overview));
    }
}
