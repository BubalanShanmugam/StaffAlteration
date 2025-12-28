package com.staffalteration.controller;

import com.staffalteration.dto.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@Slf4j
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponseDTO<?>> health() {
        return ResponseEntity.ok(new ApiResponseDTO<>(
                200,
                "Application is running",
                "Staff Alteration System - Healthy"
        ));
    }
    
    @GetMapping("")
    public ResponseEntity<ApiResponseDTO<?>> welcome() {
        return ResponseEntity.ok(new ApiResponseDTO<>(
                200,
                "Welcome to Staff Alteration System",
                "Backend API v1.0"
        ));
    }
}
