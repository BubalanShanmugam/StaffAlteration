package com.staffalteration.controller;

import com.staffalteration.dto.AlterationDTO;
import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.entity.AlterationAudit;
import com.staffalteration.repository.AlterationAuditRepository;
import com.staffalteration.service.AlterationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/alteration")
@Slf4j
public class AlterationController {
    
    @Autowired
    private AlterationService alterationService;
    
    @Autowired
    private AlterationAuditRepository alterationAuditRepository;
    
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
    
    /**
     * Get all alteration audit records for export
     */
    @GetMapping("/audit/export")
    public ResponseEntity<ApiResponseDTO<?>> getAuditRecords() {
        log.info("Exporting alteration audit records");
        
        try {
            List<AlterationAudit> auditRecords = alterationAuditRepository.findAll();
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Audit records retrieved successfully", auditRecords));
        } catch (Exception e) {
            log.error("Error fetching audit records: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get audit records for a specific staff member
     */
    @GetMapping("/audit/staff/{staffId}")
    public ResponseEntity<ApiResponseDTO<?>> getAuditRecordsByStaff(@PathVariable Long staffId) {
        log.info("Fetching audit records for staff: {}", staffId);
        
        try {
            List<AlterationAudit> auditRecords = alterationAuditRepository.findByOriginalStaffId(staffId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Audit records retrieved successfully", auditRecords));
        } catch (Exception e) {
            log.error("Error fetching audit records for staff: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get unfulfilled alterations for HOD/Admin dashboard
     */
    @GetMapping("/audit/unfulfilled")
    public ResponseEntity<ApiResponseDTO<?>> getUnfulfilledAlterations() {
        log.info("Fetching unfulfilled alterations");
        
        try {
            List<AlterationAudit> unfulfilledAlterations = alterationAuditRepository.findUnfulfilledAlterations();
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Unfulfilled alterations retrieved", unfulfilledAlterations));
        } catch (Exception e) {
            log.error("Error fetching unfulfilled alterations: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Export audit records as CSV
     */
    @GetMapping("/audit/export-csv")
    public ResponseEntity<?> exportAuditAsCSV() {
        log.info("Exporting audit records as CSV");
        
        try {
            List<AlterationAudit> auditRecords = alterationAuditRepository.findAll();
            StringBuilder csv = new StringBuilder();
            
            // CSV Header
            csv.append("Absence Date,Original Staff,Email,Absence Type,Class,Subject,Period,")
               .append("First Substitute,Email,First Status,Response Time,")
               .append("Second Substitute,Email,Second Status,Response Time,")
               .append("Final Status,Created At,Updated At,Remarks\n");
            
            // CSV Data rows
            for (AlterationAudit audit : auditRecords) {
                csv.append(escapeCsvField(audit.getAbsenceDate().toString())).append(",")
                   .append(escapeCsvField(audit.getOriginalStaffName())).append(",")
                   .append(escapeCsvField(audit.getOriginalStaffEmail())).append(",")
                   .append(escapeCsvField(audit.getAbsenceType())).append(",")
                   .append(escapeCsvField(audit.getClassName())).append(",")
                   .append(escapeCsvField(audit.getSubject())).append(",")
                   .append(audit.getPeriodNumber()).append(",")
                   .append(escapeCsvField(audit.getFirstSubstituteName())).append(",")
                   .append(escapeCsvField(audit.getFirstSubstituteEmail())).append(",")
                   .append(escapeCsvField(audit.getFirstSubstituteStatus())).append(",")
                   .append(audit.getFirstSubstituteResponseTime() != null ? audit.getFirstSubstituteResponseTime().toString() : "").append(",")
                   .append(escapeCsvField(audit.getSecondSubstituteName())).append(",")
                   .append(escapeCsvField(audit.getSecondSubstituteEmail())).append(",")
                   .append(escapeCsvField(audit.getSecondSubstituteStatus())).append(",")
                   .append(audit.getSecondSubstituteResponseTime() != null ? audit.getSecondSubstituteResponseTime().toString() : "").append(",")
                   .append(escapeCsvField(audit.getFinalStatus())).append(",")
                   .append(audit.getCreatedAt()).append(",")
                   .append(audit.getLastUpdatedAt()).append(",")
                   .append(escapeCsvField(audit.getRemarks())).append("\n");
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "text/csv; charset=UTF-8");
            headers.add("Content-Disposition", "attachment; filename=\"alteration_audit_" + LocalDate.now() + ".csv\"");
            
            return ResponseEntity.status(HttpStatus.OK)
                    .headers(headers)
                    .body(csv.toString());
        } catch (Exception e) {
            log.error("Error exporting audit records as CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Escape CSV field values to handle commas, quotes, and newlines
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
}
