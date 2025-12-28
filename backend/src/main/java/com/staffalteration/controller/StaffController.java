package com.staffalteration.controller;

import com.staffalteration.dto.StaffDTO;
import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.service.StaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
@Slf4j
public class StaffController {
    
    @Autowired
    private StaffService staffService;
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<?>> createStaff(
            @RequestBody StaffDTO staffDTO,
            @RequestParam String password) {
        log.info("Creating staff: {}", staffDTO.getStaffId());
        
        try {
            StaffDTO response = staffService.createStaff(staffDTO, password);
            return ResponseEntity.ok(new ApiResponseDTO<>(201, "Staff created successfully", response));
        } catch (Exception e) {
            log.error("Error creating staff: {}", e.getMessage());
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
    
    @GetMapping("/{staffId}")
    public ResponseEntity<ApiResponseDTO<?>> getStaff(@PathVariable String staffId) {
        try {
            StaffDTO response = staffService.getStaffByStaffId(staffId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Staff retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponseDTO<?>> getAllStaff() {
        try {
            List<StaffDTO> response = staffService.getAllStaff();
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "All staff retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
    
    @GetMapping("/department/{departmentCode}")
    public ResponseEntity<ApiResponseDTO<?>> getStaffByDepartment(@PathVariable String departmentCode) {
        try {
            List<StaffDTO> response = staffService.getStaffByDepartment(departmentCode);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Staff by department retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
    
    @PutMapping("/{staffId}")
    public ResponseEntity<ApiResponseDTO<?>> updateStaff(
            @PathVariable String staffId,
            @RequestBody StaffDTO staffDTO) {
        log.info("Updating staff: {}", staffId);
        
        try {
            StaffDTO response = staffService.updateStaff(staffId, staffDTO);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Staff updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
}
