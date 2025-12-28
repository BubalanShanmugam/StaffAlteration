package com.staffalteration.service;

import com.staffalteration.dto.AttendanceDTO;
import com.staffalteration.dto.AttendanceMarkDTO;
import com.staffalteration.entity.Attendance;
import com.staffalteration.entity.Staff;
import com.staffalteration.entity.Timetable;
import com.staffalteration.repository.AttendanceRepository;
import com.staffalteration.repository.StaffRepository;
import com.staffalteration.repository.TimetableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private TimetableRepository timetableRepository;
    
    @Autowired
    private AlterationService alterationService;
    
    @Autowired
    private NotificationService notificationService;
    
    public AttendanceDTO markAttendance(AttendanceMarkDTO attendanceMarkDTO) {
        log.info("Marking attendance for staff: {}, date: {}, status: {}", 
                 attendanceMarkDTO.getStaffId(), attendanceMarkDTO.getAttendanceDate(), attendanceMarkDTO.getStatus());
        
        Staff staff = staffRepository.findByStaffId(attendanceMarkDTO.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        // Handle multiple dates
        List<LocalDate> datesToMark = new ArrayList<>();
        if (attendanceMarkDTO.getAttendanceDates() != null && !attendanceMarkDTO.getAttendanceDates().isEmpty()) {
            datesToMark = attendanceMarkDTO.getAttendanceDates();
        } else if (attendanceMarkDTO.getAttendanceDate() != null) {
            datesToMark.add(attendanceMarkDTO.getAttendanceDate());
        }
        
        List<AttendanceDTO> results = new ArrayList<>();
        for (LocalDate date : datesToMark) {
            Attendance attendance = attendanceRepository.findByStaffIdAndAttendanceDate(staff.getId(), date).orElse(null);
            
            if (attendance == null) {
                attendance = Attendance.builder()
                        .staff(staff)
                        .attendanceDate(date)
                        .status(Attendance.AttendanceStatus.valueOf(attendanceMarkDTO.getStatus()))
                        .dayType(Attendance.DayType.valueOf(attendanceMarkDTO.getDayType() != null ? attendanceMarkDTO.getDayType() : "FULL_DAY"))
                        .remarks(attendanceMarkDTO.getRemarks())
                        .build();
                log.info("Created new attendance record with status: {}", attendance.getStatus());
            } else {
                log.info("Updating existing attendance from status {} to {}", attendance.getStatus(), attendanceMarkDTO.getStatus());
                attendance.setStatus(Attendance.AttendanceStatus.valueOf(attendanceMarkDTO.getStatus()));
                attendance.setDayType(Attendance.DayType.valueOf(attendanceMarkDTO.getDayType() != null ? attendanceMarkDTO.getDayType() : "FULL_DAY"));
                attendance.setRemarks(attendanceMarkDTO.getRemarks());
            }
            
            Attendance savedAttendance = attendanceRepository.save(attendance);
            log.info("Saved attendance with final status: {}", savedAttendance.getStatus());
            results.add(mapToDTO(savedAttendance));
            
            // If marked absent, trigger alteration process
            if (savedAttendance.getStatus().equals(Attendance.AttendanceStatus.ABSENT)) {
                triggerAlterationProcess(staff, date);
            }
        }
        
        return results.isEmpty() ? null : results.get(0);
    }
    
    private void triggerAlterationProcess(Staff staff, LocalDate date) {
        log.info("Triggering alteration process for staff: {} on date: {}", staff.getStaffId(), date);
        
        // Get all timetables for this staff
        List<Timetable> timetables = timetableRepository.findByStaffId(staff.getId());
        
        // Process alteration for each timetable
        for (Timetable timetable : timetables) {
            alterationService.processAlteration(timetable, date);
        }
    }
    
    public AttendanceDTO getAttendance(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));
        return mapToDTO(attendance);
    }
    
    public List<AttendanceDTO> getStaffAttendance(String staffId) {
        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        return attendanceRepository.findByStaffId(staff.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AttendanceDTO> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AttendanceDTO> getAbsentStaffByDate(LocalDate date) {
        return attendanceRepository.findByStatusAndAttendanceDate(
                Attendance.AttendanceStatus.ABSENT, date).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    private AttendanceDTO mapToDTO(Attendance attendance) {
        return AttendanceDTO.builder()
                .id(attendance.getId())
                .staffId(attendance.getStaff().getStaffId())
                .attendanceDate(attendance.getAttendanceDate())
                .status(attendance.getStatus().toString())
                .dayType(attendance.getDayType().toString())
                .remarks(attendance.getRemarks())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
}
