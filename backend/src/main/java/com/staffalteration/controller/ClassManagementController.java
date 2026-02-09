package com.staffalteration.controller;

import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.dto.ClassRoomDTO;
import com.staffalteration.dto.CreateClassDTO;
import com.staffalteration.service.ClassManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/class-management")
@RequiredArgsConstructor
@Slf4j
public class ClassManagementController {
    
    private final ClassManagementService classManagementService;
    
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('HOD', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<ClassRoomDTO>> createClass(@RequestBody CreateClassDTO request) {
        try {
            log.info("Creating new class: {}", request.getClassCode());
            ClassRoomDTO classRoom = classManagementService.createClass(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(201, "Class created successfully", classRoom));
        } catch (Exception e) {
            log.error("Error creating class", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(400, "Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<ClassRoomDTO>>> getAllClasses() {
        try {
            List<ClassRoomDTO> classes = classManagementService.getAllClasses();
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Classes retrieved successfully", classes));
        } catch (Exception e) {
            log.error("Error fetching classes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{classCode}")
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<ClassRoomDTO>> getClassByCode(@PathVariable String classCode) {
        try {
            ClassRoomDTO classRoom = classManagementService.getClassByCode(classCode);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Class retrieved successfully", classRoom));
        } catch (Exception e) {
            log.error("Error fetching class", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDTO<>(404, "Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/update/{classId}")
    @PreAuthorize("hasAnyRole('HOD', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<ClassRoomDTO>> updateClass(@PathVariable Long classId, @RequestBody CreateClassDTO request) {
        try {
            ClassRoomDTO classRoom = classManagementService.updateClass(classId, request);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Class updated successfully", classRoom));
        } catch (Exception e) {
            log.error("Error updating class", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(400, "Error: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{classId}")
    @PreAuthorize("hasAnyRole('HOD', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<?>> deleteClass(@PathVariable Long classId) {
        try {
            classManagementService.deleteClass(classId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Class deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting class", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(400, "Error: " + e.getMessage()));
        }
    }
}
