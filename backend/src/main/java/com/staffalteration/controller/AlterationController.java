package com.staffalteration.controller;

import com.staffalteration.dto.AlterationDTO;
import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.service.AlterationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/alteration")
@Slf4j
public class AlterationController {
    
    @Autowired
    private AlterationService alterationService;
    
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponseDTO<?>> getAlterationsByDate(@PathVariable LocalDate date) {
        log.info("Fetching alterations for date: {}", date);
        
        try {
            List<AlterationDTO> response = alterationService.getAlterationsByDate(date);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Alterations retrieved", response));
        } catch (Exception e) {
            log.error("Error fetching alterations by date: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponseDTO<?>> getAlterationsByStaff(@PathVariable String staffId) {
        log.info("Fetching alterations for staff: {}", staffId);
        
        try {
            List<AlterationDTO> response = alterationService.getAlterationsByStaff(Long.parseLong(staffId));
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Alterations retrieved", response));
        } catch (Exception e) {
            log.error("Error fetching alterations by staff: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{alterationId}/status")
    public ResponseEntity<ApiResponseDTO<?>> updateAlterationStatus(
            @PathVariable Long alterationId,
            @RequestBody Map<String, String> statusUpdate) {
        log.info("Updating alteration status: {}", alterationId);
        
        try {
            var alteration = alterationService.updateAlterationStatus(alterationId, statusUpdate.get("status"));
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Alteration status updated", alteration));
        } catch (Exception e) {
            log.error("Error updating alteration status: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{alterationId}/acknowledge")
    public ResponseEntity<ApiResponseDTO<?>> acknowledgeAlteration(@PathVariable Long alterationId) {
        log.info("Acknowledging alteration: {}", alterationId);
        
        try {
            var alteration = alterationService.updateAlterationStatus(alterationId, "ACKNOWLEDGED");
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Alteration acknowledged", alteration));
        } catch (Exception e) {
            log.error("Error acknowledging alteration: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{alterationId}/reject")
    public ResponseEntity<ApiResponseDTO<?>> rejectAlteration(@PathVariable Long alterationId) {
        log.info("Rejecting alteration: {}", alterationId);
        
        try {
            var alteration = alterationService.rejectAlteration(alterationId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Alteration rejected. Finding new substitute...", alteration));
        } catch (Exception e) {
            log.error("Error rejecting alteration: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponseDTO<?>> getAlterationsByDepartment(@PathVariable Long departmentId) {
        log.info("Fetching alterations for department: {}", departmentId);
        
        try {
            List<AlterationDTO> response = alterationService.getAlterationsByDepartment(departmentId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Alterations retrieved", response));
        } catch (Exception e) {
            log.error("Error fetching alterations by department: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{alterationId}/mark-completed")
    public ResponseEntity<ApiResponseDTO<?>> markCompleted(@PathVariable Long alterationId) {
        log.info("Marking alteration as completed: {}", alterationId);
        
        try {
            var alteration = alterationService.updateAlterationStatus(alterationId, "COMPLETED");
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Alteration marked as completed", alteration));
        } catch (Exception e) {
            log.error("Error marking alteration as completed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
}
