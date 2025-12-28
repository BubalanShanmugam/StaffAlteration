package com.staffalteration.controller;

import com.staffalteration.dto.TimetableDTO;
import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.service.TimetableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timetable")
@Slf4j
public class TimetableController {
    
    @Autowired
    private TimetableService timetableService;
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<?>> createTimetable(@RequestBody TimetableDTO timetableDTO) {
        log.info("Creating timetable for staff: {}", timetableDTO.getStaffId());
        
        try {
            TimetableDTO response = timetableService.createTimetable(timetableDTO);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Timetable created successfully", response));
        } catch (Exception e) {
            log.error("Error creating timetable: {}", e.getMessage());
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
    
    @PutMapping("/update/{timetableId}")
    public ResponseEntity<ApiResponseDTO<?>> updateTimetable(
            @PathVariable Long timetableId,
            @RequestBody TimetableDTO timetableDTO) {
        log.info("Updating timetable: {}", timetableId);
        
        try {
            TimetableDTO response = timetableService.updateTimetable(timetableId, timetableDTO);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Timetable updated successfully", response));
        } catch (Exception e) {
            log.error("Error updating timetable: {}", e.getMessage());
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
    
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponseDTO<?>> getStaffTimetable(@PathVariable String staffId) {
        try {
            List<TimetableDTO> response = timetableService.getStaffTimetable(staffId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Staff timetable retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
    
    @GetMapping("/class/{classCode}/{dayOrder}/{periodNumber}")
    public ResponseEntity<ApiResponseDTO<?>> getTimetableByClassAndPeriod(
            @PathVariable String classCode,
            @PathVariable Integer dayOrder,
            @PathVariable Integer periodNumber) {
        try {
            List<TimetableDTO> response = timetableService.getTimetableByClassAndPeriod(classCode, dayOrder, periodNumber);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Timetable retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
    
    @DeleteMapping("/{timetableId}")
    public ResponseEntity<ApiResponseDTO<?>> deleteTimetable(@PathVariable Long timetableId) {
        try {
            timetableService.deleteTimetable(timetableId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Timetable deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
}
