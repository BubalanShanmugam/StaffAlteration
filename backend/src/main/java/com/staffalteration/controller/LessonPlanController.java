package com.staffalteration.controller;

import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.entity.LessonPlan;
import com.staffalteration.repository.LessonPlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/lesson-plan")
@Slf4j
public class LessonPlanController {

    @Autowired
    private LessonPlanRepository lessonPlanRepository;

    /**
     * Get lesson plan details
     */
    @GetMapping("/{lessonPlanId}")
    public ResponseEntity<ApiResponseDTO<?>> getLessonPlan(@PathVariable Long lessonPlanId) {
        log.info("Fetching lesson plan: {}", lessonPlanId);
        
        try {
            Optional<LessonPlan> lessonPlan = lessonPlanRepository.findById(lessonPlanId);
            
            if (lessonPlan.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ApiResponseDTO<>(404, "Lesson plan not found"));
            }
            
            var response = new java.util.HashMap<String, Object>();
            LessonPlan lp = lessonPlan.get();
            response.put("id", lp.getId());
            response.put("lessonDate", lp.getLessonDate());
            response.put("notes", lp.getNotes());
            response.put("filePath", lp.getFilePath());
            response.put("originalFileName", lp.getOriginalFileName());
            response.put("fileType", lp.getFileType());
            response.put("fileSize", lp.getFileSize());
            response.put("classCode", lp.getClassRoom() != null ? lp.getClassRoom().getClassCode() : "");
            response.put("subjectName", lp.getSubject() != null ? lp.getSubject().getSubjectName() : "");
            response.put("staffName", lp.getStaff() != null ? 
                lp.getStaff().getFirstName() + " " + lp.getStaff().getLastName() : "");
            
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Lesson plan retrieved", response));
        } catch (Exception e) {
            log.error("Error fetching lesson plan: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }

    /**
     * Download lesson plan file
     */
    @GetMapping("/{lessonPlanId}/download")
    public ResponseEntity<?> downloadLessonPlan(@PathVariable Long lessonPlanId) {
        log.info("Downloading lesson plan: {}", lessonPlanId);
        
        try {
            Optional<LessonPlan> lessonPlan = lessonPlanRepository.findById(lessonPlanId);
            
            if (lessonPlan.isEmpty()) {
                return ResponseEntity.status(404).body("Lesson plan not found");
            }
            
            LessonPlan lp = lessonPlan.get();
            Path filePath = Paths.get(lp.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                log.error("File not found or not readable: {}", filePath);
                return ResponseEntity.status(404).body("File not found");
            }
            
            // Determine media type
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (lp.getFileType() != null) {
                if (lp.getFileType().contains("pdf")) {
                    mediaType = MediaType.APPLICATION_PDF;
                } else if (lp.getFileType().contains("word") || lp.getFileType().contains("document")) {
                    mediaType = MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                } else if (lp.getFileType().contains("image")) {
                    mediaType = MediaType.IMAGE_JPEG;
                }
            }
            
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + lp.getOriginalFileName() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(lp.getFileSize()))
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading lesson plan: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error downloading file: " + e.getMessage());
        }
    }

    /**
     * Get lesson plan preview (for viewing in UI)
     */
    @GetMapping("/{lessonPlanId}/preview")
    public ResponseEntity<ApiResponseDTO<?>> getPreviewUrl(@PathVariable Long lessonPlanId) {
        log.info("Getting preview URL for lesson plan: {}", lessonPlanId);
        
        try {
            Optional<LessonPlan> lessonPlan = lessonPlanRepository.findById(lessonPlanId);
            
            if (lessonPlan.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ApiResponseDTO<>(404, "Lesson plan not found"));
            }
            
            LessonPlan lp = lessonPlan.get();
            
            // For PDFs and images, return download URL
            // For documents, return preview URL or download URL
            var response = new java.util.HashMap<String, Object>();
            response.put("previewUrl", "/api/lesson-plan/" + lessonPlanId + "/download");
            response.put("canPreview", lp.getFileType() != null && 
                    (lp.getFileType().contains("pdf") || lp.getFileType().contains("image")));
            response.put("fileName", lp.getOriginalFileName());
            response.put("fileSize", lp.getFileSize());
            
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Preview URL retrieved", response));
        } catch (Exception e) {
            log.error("Error getting preview URL: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
}
