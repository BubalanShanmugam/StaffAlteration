package com.staffalteration.controller;

import com.staffalteration.dto.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class GlobalErrorController implements ErrorController {
    
    @RequestMapping("/error")
    public ResponseEntity<ApiResponseDTO<?>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String message = (String) request.getAttribute("javax.servlet.error.message");
        String path = (String) request.getAttribute("javax.servlet.error.request_uri");
        
        if (statusCode == null) {
            statusCode = 500;
        }
        
        log.warn("Error {} on path: {}", statusCode, path);
        
        String errorMessage;
        switch (statusCode) {
            case 400:
                errorMessage = "Bad Request - Invalid parameters";
                break;
            case 404:
                errorMessage = "Endpoint not found. Use POST /api/auth/login to login, GET /api/health for health check";
                break;
            case 405:
                errorMessage = "Method Not Allowed - Check your HTTP method (GET/POST/PUT/DELETE)";
                break;
            case 500:
                errorMessage = "Internal Server Error";
                break;
            default:
                errorMessage = "An error occurred";
        }
        
        return ResponseEntity.status(statusCode).body(new ApiResponseDTO<>(
                statusCode,
                errorMessage,
                null
        ));
    }
}
