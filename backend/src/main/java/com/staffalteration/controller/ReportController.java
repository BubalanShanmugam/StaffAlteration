package com.staffalteration.controller;

import com.staffalteration.dto.AlterationDTO;
import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.service.AlterationService;
import com.staffalteration.service.ExcelExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/reports")
@Slf4j
public class ReportController {

    @Autowired
    private AlterationService alterationService;

    @Autowired
    private ExcelExportService excelExportService;

    /**
     * Export alterations to Excel file with optional date range and department filtering
     * @param fromDate Start date (format: yyyy-MM-dd)
     * @param toDate End date (format: yyyy-MM-dd)
     * @param departmentId Filter by department (optional)
     * @return Excel file as byte array
     */
    @GetMapping("/alterations/export")
    public ResponseEntity<?> exportAlterations(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long departmentId) {
        
        try {
            log.info("Exporting alterations - fromDate: {}, toDate: {}, departmentId: {}", 
                    fromDate, toDate, departmentId);

            // Parse dates
            LocalDate startDate = fromDate != null ? LocalDate.parse(fromDate, DateTimeFormatter.ISO_DATE) : LocalDate.now().minusMonths(1);
            LocalDate endDate = toDate != null ? LocalDate.parse(toDate, DateTimeFormatter.ISO_DATE) : LocalDate.now();

            // Get alterations for date range
            List<AlterationDTO> alterations = alterationService.getAlterationsByDateRange(startDate, endDate);

            // Filter by department if provided
            if (departmentId != null) {
                alterations = alterations.stream()
                        .filter(alt -> alt.getDepartmentId() != null && alt.getDepartmentId().equals(departmentId))
                        .toList();
            }

            // Generate Excel file
            byte[] excelData = excelExportService.generateAlterationReport(
                    alterations, 
                    departmentId != null ? "Department " + departmentId : "All Departments"
            );

            // Return file as response
            String filename = "alterations_" + startDate + "_to_" + endDate + ".xlsx";
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);

        } catch (IOException e) {
            log.error("Error generating Excel export: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(500, "Error generating Excel file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error exporting alterations: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }

    /**
     * Get alteration statistics for a date range
     */
    @GetMapping("/alterations/statistics")
    public ResponseEntity<?> getAlterationStatistics(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long departmentId) {
        
        try {
            LocalDate startDate = fromDate != null ? LocalDate.parse(fromDate, DateTimeFormatter.ISO_DATE) : LocalDate.now().minusMonths(1);
            LocalDate endDate = toDate != null ? LocalDate.parse(toDate, DateTimeFormatter.ISO_DATE) : LocalDate.now();

            List<AlterationDTO> alterations = alterationService.getAlterationsByDateRange(startDate, endDate);

            if (departmentId != null) {
                alterations = alterations.stream()
                        .filter(alt -> alt.getDepartmentId() != null && alt.getDepartmentId().equals(departmentId))
                        .toList();
            }

            // Calculate statistics
            long totalAlterations = alterations.size();
            long completedAlterations = alterations.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
            long pendingAlterations = alterations.stream().filter(a -> "ASSIGNED".equals(a.getStatus())).count();
            long acknowledgedAlterations = alterations.stream().filter(a -> "ACKNOWLEDGED".equals(a.getStatus())).count();

            var statistics = new java.util.HashMap<String, Object>();
            statistics.put("totalAlterations", totalAlterations);
            statistics.put("completed", completedAlterations);
            statistics.put("pending", pendingAlterations);
            statistics.put("acknowledged", acknowledgedAlterations);
            statistics.put("dateRange", startDate + " to " + endDate);
            statistics.put("departmentId", departmentId);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Statistics retrieved", statistics));

        } catch (Exception e) {
            log.error("Error getting alterations statistics: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(500, "Error: " + e.getMessage()));
        }
    }
}
