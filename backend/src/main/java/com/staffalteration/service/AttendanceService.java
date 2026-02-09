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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                        .meetingHours(attendanceMarkDTO.getMeetingHours() != null ? new java.util.HashSet<>(attendanceMarkDTO.getMeetingHours()) : new java.util.HashSet<>())
                        .remarks(attendanceMarkDTO.getRemarks())
                        .build();
                log.info("Created new attendance record with status: {}", attendance.getStatus());
            } else {
                log.info("Updating existing attendance from status {} to {}", attendance.getStatus(), attendanceMarkDTO.getStatus());
                attendance.setStatus(Attendance.AttendanceStatus.valueOf(attendanceMarkDTO.getStatus()));
                attendance.setDayType(Attendance.DayType.valueOf(attendanceMarkDTO.getDayType() != null ? attendanceMarkDTO.getDayType() : "FULL_DAY"));
                if (attendanceMarkDTO.getMeetingHours() != null) {
                    attendance.setMeetingHours(new java.util.HashSet<>(attendanceMarkDTO.getMeetingHours()));
                }
                attendance.setRemarks(attendanceMarkDTO.getRemarks());
            }
            
            Attendance savedAttendance = attendanceRepository.save(attendance);
            log.info("Saved attendance with final status: {}", savedAttendance.getStatus());
            results.add(mapToDTO(savedAttendance));
            
            // If marked absent or meeting, trigger alteration process
            if (savedAttendance.getStatus().equals(Attendance.AttendanceStatus.ABSENT) ||
                savedAttendance.getStatus().equals(Attendance.AttendanceStatus.MEETING)) {
                triggerAlterationProcess(staff, date, savedAttendance);
            }
        }
        
        return results.isEmpty() ? null : results.get(0);
    }
    
    private void triggerAlterationProcess(Staff staff, LocalDate date, Attendance attendance) {
        log.info("Triggering alteration process for staff: {} on date: {}, status: {}", 
                 staff.getStaffId(), date, attendance.getStatus());
        
        // Get all timetables for this staff
        List<Timetable> timetables = timetableRepository.findByStaffId(staff.getId());
        
        // Determine which periods need alteration
        java.util.Set<Integer> periodsThatNeedAlteration = getPeriodsThatNeedAlteration(attendance);
        
        log.info("Processing {} timetables for periods: {}", timetables.size(), periodsThatNeedAlteration);
        
        // Process alteration for each relevant timetable
        for (Timetable timetable : timetables) {
            if (periodsThatNeedAlteration.contains(timetable.getPeriodNumber())) {
                alterationService.processAlteration(timetable, date);
            }
        }
    }
    
    /**
     * Determine which periods need alteration based on attendance status
     */
    private java.util.Set<Integer> getPeriodsThatNeedAlteration(Attendance attendance) {
        java.util.Set<Integer> periods = new java.util.HashSet<>();
        
        if (attendance.getStatus().equals(Attendance.AttendanceStatus.ABSENT)) {
            // For ABSENT, alteration needed for all assigned periods based on dayType
            if (attendance.getDayType().equals(Attendance.DayType.FULL_DAY)) {
                // All periods 1-6
                for (int i = 1; i <= 6; i++) {
                    periods.add(i);
                }
            } else if (attendance.getDayType().equals(Attendance.DayType.MORNING_ONLY)) {
                // Morning periods: 1-2 (9AM-1PM)
                periods.add(1);
                periods.add(2);
            } else if (attendance.getDayType().equals(Attendance.DayType.AFTERNOON_ONLY)) {
                // Afternoon periods: 3-5 (1PM-5PM)
                periods.add(3);
                periods.add(4);
                periods.add(5);
            }
        } else if (attendance.getStatus().equals(Attendance.AttendanceStatus.MEETING)) {
            // For MEETING, alteration needed only for specified meeting hours
            if (attendance.getMeetingHours() != null && !attendance.getMeetingHours().isEmpty()) {
                periods.addAll(attendance.getMeetingHours());
            }
        }
        
        return periods;
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
