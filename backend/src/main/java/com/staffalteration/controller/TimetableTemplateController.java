package com.staffalteration.controller;

import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.dto.CreateTimetableDTO;
import com.staffalteration.dto.TimetableTemplateDTO;
import com.staffalteration.entity.TimetableStatus;
import com.staffalteration.service.TimetableTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timetable-template")
@RequiredArgsConstructor
@Slf4j
public class TimetableTemplateController {
    
    private final TimetableTemplateService timetableTemplateService;
    
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<TimetableTemplateDTO>> createTimetable(@RequestBody CreateTimetableDTO request) {
        try {
            log.info("Creating timetable for class: {}", request.getClassCode());
            String userId = "1";
            TimetableTemplateDTO timetable = timetableTemplateService.createTimetable(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(201, "Timetable created successfully", timetable));
        } catch (Exception e) {
            log.error("Error creating timetable", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(400, "Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/class/{classCode}")
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'DEAN', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<TimetableTemplateDTO>>> getTimetablesByClass(@PathVariable String classCode) {
        try {
            List<TimetableTemplateDTO> timetables = timetableTemplateService.getTimetablesByClass(classCode);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Timetables retrieved successfully", timetables));
        } catch (Exception e) {
            log.error("Error fetching timetables", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/class/{classCode}/active")
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'DEAN', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<TimetableTemplateDTO>>> getActiveTimetablesByClass(@PathVariable String classCode) {
        try {
            List<TimetableTemplateDTO> timetables = timetableTemplateService.getActiveTimetablesByClass(classCode);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Active timetables retrieved successfully", timetables));
        } catch (Exception e) {
            log.error("Error fetching active timetables", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'DEAN', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<TimetableTemplateDTO>>> getTimetablesByStaff(@PathVariable String staffId) {
        try {
            List<TimetableTemplateDTO> timetables = timetableTemplateService.getTimetablesByStaff(staffId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Staff timetables retrieved successfully", timetables));
        } catch (Exception e) {
            log.error("Error fetching staff timetables", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/slot/{classCode}/{dayOrder}/{periodNumber}")
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'DEAN', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<TimetableTemplateDTO>> getTimetableBySlot(@PathVariable String classCode, @PathVariable Integer dayOrder, @PathVariable Integer periodNumber) {
        try {
            TimetableTemplateDTO timetable = timetableTemplateService.getTimetableBySlot(classCode, dayOrder, periodNumber);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Timetable retrieved successfully", timetable));
        } catch (Exception e) {
            log.error("Error fetching timetable", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDTO<>(404, "Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/update/{timetableId}")
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<TimetableTemplateDTO>> updateTimetable(@PathVariable Long timetableId, @RequestBody CreateTimetableDTO request) {
        try {
            TimetableTemplateDTO timetable = timetableTemplateService.updateTimetable(timetableId, request);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Timetable updated successfully", timetable));
        } catch (Exception e) {
            log.error("Error updating timetable", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(400, "Error: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{timetableId}")
    @PreAuthorize("hasAnyRole('HOD', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> deleteTimetable(@PathVariable Long timetableId) {
        try {
            timetableTemplateService.deleteTimetable(timetableId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Timetable deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting timetable", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(400, "Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{timetableId}/status")
    @PreAuthorize("hasAnyRole('HOD', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<TimetableTemplateDTO>> changeTimetableStatus(@PathVariable Long timetableId, @RequestParam TimetableStatus status) {
        try {
            TimetableTemplateDTO timetable = timetableTemplateService.changeTimetableStatus(timetableId, status);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Timetable status changed successfully", timetable));
        } catch (Exception e) {
            log.error("Error changing timetable status", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(400, "Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/staff/{staffId}/workload/{dayOrder}")
    @PreAuthorize("hasAnyRole('STAFF', 'HOD', 'DEAN', 'ADMIN')")
    public ResponseEntity<ApiResponseDTO<Long>> getStaffWorkload(@PathVariable String staffId, @PathVariable Integer dayOrder) {
        try {
            long workload = timetableTemplateService.getStaffWorkloadByDay(staffId, dayOrder);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Staff workload retrieved successfully", workload));
        } catch (Exception e) {
            log.error("Error fetching staff workload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
}