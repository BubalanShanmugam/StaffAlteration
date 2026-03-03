package com.staffalteration.service;

import com.staffalteration.dto.AttendanceDTO;
import com.staffalteration.dto.AttendanceMarkDTO;
import com.staffalteration.entity.Attendance;
import com.staffalteration.entity.Staff;
import com.staffalteration.entity.Timetable;
import com.staffalteration.entity.LessonPlan;
import com.staffalteration.entity.ClassRoom;
import com.staffalteration.entity.Subject;
import com.staffalteration.entity.Alteration;
import com.staffalteration.repository.AttendanceRepository;
import com.staffalteration.repository.StaffRepository;
import com.staffalteration.repository.TimetableRepository;
import com.staffalteration.repository.LessonPlanRepository;
import com.staffalteration.repository.ClassRoomRepository;
import com.staffalteration.repository.SubjectRepository;
import com.staffalteration.repository.AlterationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private AlterationRepository alterationRepository;
    
    @Autowired
    private LessonPlanRepository lessonPlanRepository;
    
    @Autowired
    private ClassRoomRepository classRoomRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private AlterationService alterationService;
    
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
                // Determine which periods to store
                Set<Integer> periodsToStore = attendanceMarkDTO.getSelectedPeriods();
                if (periodsToStore == null || periodsToStore.isEmpty()) {
                    periodsToStore = attendanceMarkDTO.getMeetingHours();
                }
                
                // Normalize status: ON_DUTY (from UI) -> ONDUTY (enum value)
                String statusStr = normalizeAttendanceStatus(attendanceMarkDTO.getStatus());
                
                attendance = Attendance.builder()
                        .staff(staff)
                        .attendanceDate(date)
                        .status(Attendance.AttendanceStatus.valueOf(statusStr))
                        .dayType(Attendance.DayType.valueOf(attendanceMarkDTO.getDayType() != null ? attendanceMarkDTO.getDayType() : "FULL_DAY"))
                        .meetingHours(periodsToStore != null ? new java.util.HashSet<>(periodsToStore) : new java.util.HashSet<>())
                        .remarks(attendanceMarkDTO.getRemarks())
                        .build();
                log.info("Created new attendance record with status: {}, periods: {}", attendance.getStatus(), periodsToStore);
            } else {
                String statusStr = normalizeAttendanceStatus(attendanceMarkDTO.getStatus());
                log.info("Updating existing attendance from status {} to {}", attendance.getStatus(), statusStr);
                attendance.setStatus(Attendance.AttendanceStatus.valueOf(statusStr));
                attendance.setDayType(Attendance.DayType.valueOf(attendanceMarkDTO.getDayType() != null ? attendanceMarkDTO.getDayType() : "FULL_DAY"));
                
                // Handle selected periods or meeting hours
                Set<Integer> periodsToStore = attendanceMarkDTO.getSelectedPeriods();
                if (periodsToStore == null || periodsToStore.isEmpty()) {
                    periodsToStore = attendanceMarkDTO.getMeetingHours();
                }
                
                if (periodsToStore != null) {
                    attendance.setMeetingHours(new java.util.HashSet<>(periodsToStore));
                } else if (attendance.getMeetingHours() == null) {
                    attendance.setMeetingHours(new java.util.HashSet<>());
                }
                
                attendance.setRemarks(attendanceMarkDTO.getRemarks());
                
                // Cancel existing alterations if status changed
                if (!attendance.getStatus().equals(Attendance.AttendanceStatus.LEAVE) &&
                    !attendance.getStatus().equals(Attendance.AttendanceStatus.ONDUTY) &&
                    !attendance.getStatus().equals(Attendance.AttendanceStatus.ABSENT) &&
                    !attendance.getStatus().equals(Attendance.AttendanceStatus.MEETING) &&
                    !attendance.getStatus().equals(Attendance.AttendanceStatus.PERIOD_WISE_ABSENT)) {
                    cancelExistingAlterations(staff, date);
                }
            }
            
            Attendance savedAttendance = attendanceRepository.save(attendance);
            log.info("Saved attendance with final status: {}, dayType: {}", savedAttendance.getStatus(), savedAttendance.getDayType());
            results.add(mapToDTO(savedAttendance));
            
            // If marked as leave, absent, on duty, period-wise absent or meeting, trigger alteration process
            if (savedAttendance.getStatus().equals(Attendance.AttendanceStatus.LEAVE) ||
                savedAttendance.getStatus().equals(Attendance.AttendanceStatus.ONDUTY) ||
                savedAttendance.getStatus().equals(Attendance.AttendanceStatus.ABSENT) ||
                savedAttendance.getStatus().equals(Attendance.AttendanceStatus.MEETING) ||
                savedAttendance.getStatus().equals(Attendance.AttendanceStatus.PERIOD_WISE_ABSENT)) {
                
                log.warn("TRIGGERING ALTERATION PROCESS for staff: {}, date: {}, status: {}", 
                        staff.getStaffId(), date, savedAttendance.getStatus());
                
                Set<Integer> periodsToProcess = attendanceMarkDTO.getSelectedPeriods();
                if (periodsToProcess == null || periodsToProcess.isEmpty()) {
                    periodsToProcess = attendanceMarkDTO.getAbsentPeriods();
                }
                
                triggerAlterationProcess(staff, date, savedAttendance, periodsToProcess);
            }
        }
        
        return results.isEmpty() ? null : results.get(0);
    }
    
    private void triggerAlterationProcess(Staff staff, LocalDate date, Attendance attendance, Set<Integer> absentPeriods) {
        log.warn("========== ALTERATION TRIGGER START ==========");
        log.warn("Staff: {} (ID: {}), Date: {}, Status: {}", 
                 staff.getStaffId(), staff.getId(), date, attendance.getStatus());
        
        // Get day of week (1=Monday, 7=Sunday in Java DayOfWeek)
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        log.warn("Day of week: {} (1=Monday, 7=Sunday)", dayOfWeek);
        
        // Get all timetables for this staff on this day of week
        List<Timetable> allTimetables = timetableRepository.findByStaffId(staff.getId());
        log.warn("Total timetables for staff {}: {}", staff.getStaffId(), allTimetables.size());
        
        if (allTimetables.isEmpty()) {
            log.error("❌ CRITICAL: No timetables found for staff {}. Cannot create alterations!", staff.getStaffId());
            log.warn("========== ALTERATION TRIGGER END (NO TIMETABLES) ==========");
            return;
        }
        
        // Log all timetables
        allTimetables.forEach(t -> log.debug("  Timetable: dayOrder={}, period={}, class={}, subject={}",
            t.getDayOrder(), t.getPeriodNumber(),
            t.getClassRoom() != null ? t.getClassRoom().getClassCode() : "UNKNOWN",
            t.getSubject() != null ? t.getSubject().getSubjectName() : "UNKNOWN"));
        
        List<Timetable> timetablesForToday = new ArrayList<>();
        
        for (Timetable timetable : allTimetables) {
            Integer tableDay = timetable.getDayOrder();
            if (tableDay != null && tableDay.equals(dayOfWeek)) {
                timetablesForToday.add(timetable);
            }
        }
        
        log.warn("Matching timetables for day {}: {}", dayOfWeek, timetablesForToday.size());
        
        // Determine which periods need alteration
        java.util.Set<Integer> periodsThatNeedAlteration = getPeriodsThatNeedAlteration(attendance, absentPeriods);
        log.warn("Periods needing alteration: {}", periodsThatNeedAlteration);
        
        if (timetablesForToday.isEmpty()) {
            log.error("❌ CRITICAL: No timetables found for staff {} on day {} (date: {}). Alteration process cannot proceed.", 
                     staff.getStaffId(), dayOfWeek, date);
            log.warn("========== ALTERATION TRIGGER END (NO MATCHING TIMETABLES) ==========");
            return;
        }
        
        // Process alteration for each relevant timetable
        int alterationsCreated = 0;
        for (Timetable timetable : timetablesForToday) {
            if (periodsThatNeedAlteration.contains(timetable.getPeriodNumber())) {
                log.warn("✓ Creating alteration for: period={}, class={}", 
                        timetable.getPeriodNumber(), 
                        timetable.getClassRoom() != null ? timetable.getClassRoom().getClassCode() : "UNKNOWN");
                Alteration alteration = alterationService.processAlteration(timetable, date);
                if (alteration != null) {
                    alterationsCreated++;
                    log.warn("✓ Alteration created: ID={}, substitute={}", alteration.getId(), alteration.getSubstituteStaff().getStaffId());
                } else {
                    log.error("❌ Failed to create alteration for period {}: No substitute found or other issue", timetable.getPeriodNumber());
                }
            }
        }
        
        log.warn("========== ALTERATION TRIGGER END: {} alterations created out of {} matching timetables ==========", 
                alterationsCreated, timetablesForToday.size());
    }
    
    private void cancelExistingAlterations(Staff staff, LocalDate date) {
        log.info("Cancelling alterations for staff {} on date {}", staff.getStaffId(), date);
        List<Alteration> existingAlterations = alterationRepository.findByOriginalStaffIdAndAlterationDate(staff.getId(), date);
        for (Alteration alteration : existingAlterations) {
            alteration.setStatus(Alteration.AlterationStatus.CANCELLED);
            alterationRepository.save(alteration);
        }
    }
    
    /**
     * Determine which periods need alteration based on attendance status and specific periods
     */
    private java.util.Set<Integer> getPeriodsThatNeedAlteration(Attendance attendance, Set<Integer> absentPeriods) {
        java.util.Set<Integer> periods = new java.util.HashSet<>();
        
        // If absentPeriods (selectedPeriods) is provided, use those directly
        if (absentPeriods != null && !absentPeriods.isEmpty()) {
            return new java.util.HashSet<>(absentPeriods);
        }
        
        if (attendance.getStatus().equals(Attendance.AttendanceStatus.LEAVE)) {
            // For LEAVE, alteration needed for all assigned periods based on dayType
            if (attendance.getDayType().equals(Attendance.DayType.FULL_DAY)) {
                // All periods 1-6
                for (int i = 1; i <= 6; i++) {
                    periods.add(i);
                }
            } else if (attendance.getDayType().equals(Attendance.DayType.MORNING_ONLY)) {
                // Morning periods: 1-4 (9AM-1PM)
                periods.add(1);
                periods.add(2);
                periods.add(3);
                periods.add(4);
            } else if (attendance.getDayType().equals(Attendance.DayType.AFTERNOON_ONLY)) {
                // Afternoon periods: 5-6 (1PM-5PM)
                periods.add(5);
                periods.add(6);
            }
        } else if (attendance.getStatus().equals(Attendance.AttendanceStatus.ABSENT)) {
            // For ABSENT, alteration needed for all assigned periods based on dayType
            if (attendance.getDayType().equals(Attendance.DayType.FULL_DAY)) {
                // All periods 1-6
                for (int i = 1; i <= 6; i++) {
                    periods.add(i);
                }
            } else if (attendance.getDayType().equals(Attendance.DayType.MORNING_ONLY)) {
                // Morning periods: 1-4 (9AM-1PM)
                periods.add(1);
                periods.add(2);
                periods.add(3);
                periods.add(4);
            } else if (attendance.getDayType().equals(Attendance.DayType.AFTERNOON_ONLY)) {
                // Afternoon periods: 5-6 (1PM-5PM)
                periods.add(5);
                periods.add(6);
            }
        } else if (attendance.getStatus().equals(Attendance.AttendanceStatus.MEETING)) {
            // For MEETING, alteration needed only for specified meeting hours
            if (attendance.getMeetingHours() != null && !attendance.getMeetingHours().isEmpty()) {
                periods.addAll(attendance.getMeetingHours());
            }
        } else if (attendance.getStatus().equals(Attendance.AttendanceStatus.ONDUTY)) {
            // For ONDUTY, alteration needed based on dayType
            if (attendance.getDayType().equals(Attendance.DayType.FULL_DAY)) {
                // All periods 1-6
                for (int i = 1; i <= 6; i++) {
                    periods.add(i);
                }
            } else if (attendance.getDayType().equals(Attendance.DayType.MORNING_ONLY)) {
                // Morning periods: 1-4 (9AM-1PM)
                periods.add(1);
                periods.add(2);
                periods.add(3);
                periods.add(4);
            } else if (attendance.getDayType().equals(Attendance.DayType.AFTERNOON_ONLY)) {
                // Afternoon periods: 5-6 (1PM-5PM)
                periods.add(5);
                periods.add(6);
            }
        } else if (attendance.getStatus().equals(Attendance.AttendanceStatus.PERIOD_WISE_ABSENT)) {
            // For PERIOD_WISE_ABSENT, use specific periods provided
            if (absentPeriods != null && !absentPeriods.isEmpty()) {
                periods.addAll(absentPeriods);
            }
        }
        
        return periods;
    }
    
    /**
     * Normalize attendance status string for enum parsing.
     * Handles ON_DUTY -> ONDUTY (enum uses ONDUTY, not ON_DUTY)
     */
    private String normalizeAttendanceStatus(String status) {
        if (status == null || status.isEmpty()) return "PRESENT";
        // Handle ON_DUTY, onDuty, On Duty -> ONDUTY (enum uses ONDUTY)
        String normalized = status.replace("_", "").replace(" ", "").toUpperCase();
        if ("ONDUTY".equals(normalized)) return "ONDUTY";
        return status;
    }
    
    public AttendanceDTO getAttendance(Long attendanceId) {
        @SuppressWarnings("null")
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
    
    public void uploadLessonPlans(String staffId, String classCode, Long subjectId, LocalDate lessonDate, 
                                  String notes, MultipartFile[] files) throws Exception {
        log.info("Uploading lesson plans for staff: {}, class: {}, date: {}", staffId, classCode, lessonDate);
        
        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        @SuppressWarnings("null")
        ClassRoom classRoom = classRoomRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        @SuppressWarnings("null")
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        
        // Upload each file
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String uploadDir = "lesson_plans/" + staffId + "/" + classCode;
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String filePath = uploadDir + "/" + fileName;
                
                // Create LessonPlan record
                LessonPlan lessonPlanToSave = LessonPlan.builder()
                        .staff(staff)
                        .classRoom(classRoom)
                        .subject(subject)
                        .lessonDate(lessonDate)
                        .filePath(filePath)
                        .originalFileName(file.getOriginalFilename())
                        .notes(notes)
                        .fileType(file.getContentType())
                        .fileSize(file.getSize())
                        .build();
                
                @SuppressWarnings({"null", "unused"})
                LessonPlan result = lessonPlanRepository.save(lessonPlanToSave);
                log.info("Lesson plan uploaded: {}", filePath);
            }
        }
    }

    public List<LessonPlan> getLessonPlansForAlteration(Long alterationId) {
        @SuppressWarnings("null")
        Alteration alteration = alterationRepository.findById(alterationId)
                .orElseThrow(() -> new RuntimeException("Alteration not found"));
        
        // Get lesson plans for the original staff on the alteration date
        Staff originalStaff = alteration.getOriginalStaff();
        LocalDate alterationDate = alteration.getAlterationDate();
        
        @SuppressWarnings("null")
        List<LessonPlan> lessonPlans = lessonPlanRepository.findByStaffIdAndLessonDate(originalStaff.getId(), alterationDate);
        return lessonPlans;
    }
}


