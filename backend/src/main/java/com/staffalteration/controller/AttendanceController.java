package com.staffalteration.controller;

import com.staffalteration.dto.AttendanceDTO;
import com.staffalteration.dto.AttendanceMarkDTO;
import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.service.AttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@Slf4j
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @PostMapping("/mark")
    public ResponseEntity<ApiResponseDTO<?>> markAttendance(@RequestBody AttendanceMarkDTO attendanceMarkDTO) {
        log.info("Marking attendance for staff: {}", attendanceMarkDTO.getStaffId());
        
        try {
            AttendanceDTO response = attendanceService.markAttendance(attendanceMarkDTO);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Attendance marked successfully", response));
        } catch (Exception e) {
            log.error("Error marking attendance: {}", e.getMessage());
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
    
    @GetMapping("/{attendanceId}")
    public ResponseEntity<ApiResponseDTO<?>> getAttendance(@PathVariable Long attendanceId) {
        try {
            AttendanceDTO response = attendanceService.getAttendance(attendanceId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Attendance retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, "Attendance not found"));
        }
    }
    
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponseDTO<?>> getStaffAttendance(@PathVariable String staffId) {
        try {
            List<AttendanceDTO> response = attendanceService.getStaffAttendance(staffId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Staff attendance retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
    
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponseDTO<?>> getAttendanceByDate(@PathVariable LocalDate date) {
        try {
            List<AttendanceDTO> response = attendanceService.getAttendanceByDate(date);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Attendance by date retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
    
    @GetMapping("/absent/{date}")
    public ResponseEntity<ApiResponseDTO<?>> getAbsentStaffByDate(@PathVariable LocalDate date) {
        try {
            List<AttendanceDTO> response = attendanceService.getAbsentStaffByDate(date);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Absent staff retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
    
    @PostMapping("/upload-lesson-plan")
    public ResponseEntity<ApiResponseDTO<?>> uploadLessonPlan(
            @RequestParam("staffId") String staffId,
            @RequestParam("classCode") String classCode,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("lessonDate") LocalDate lessonDate,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam("files") MultipartFile[] files) {
        log.info("Uploading lesson plan for staff: {}, date: {}", staffId, lessonDate);
        
        try {
            attendanceService.uploadLessonPlans(staffId, classCode, subjectId, lessonDate, notes, files);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Lesson plan uploaded successfully"));
        } catch (Exception e) {
            log.error("Error uploading lesson plan: {}", e.getMessage());
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
    
    @GetMapping("/lesson-plans/alteration/{alterationId}")
    public ResponseEntity<ApiResponseDTO<?>> getLessonPlansForAlteration(@PathVariable Long alterationId) {
        log.info("Fetching lesson plans for alteration: {}", alterationId);
        
        try {
            var lessonPlans = attendanceService.getLessonPlansForAlteration(alterationId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Lesson plans retrieved", lessonPlans));
        } catch (Exception e) {
            log.error("Error fetching lesson plans: {}", e.getMessage());
            return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, e.getMessage()));
        }
    }
}

