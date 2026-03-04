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
import com.staffalteration.repository.TimetableTemplateRepository;
import com.staffalteration.entity.TimetableTemplate;
import com.staffalteration.entity.TimetableStatus;
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

    @Autowired
    private TimetableTemplateRepository timetableTemplateRepository;
    
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
                Attendance.AttendanceStatus newStatus = Attendance.AttendanceStatus.valueOf(statusStr);
                
                // Always cancel existing alterations before re-processing
                // If new status needs alteration → cancel old ones so fresh ones are created below
                // If new status is PRESENT → cancel and no new ones will be created
                cancelExistingAlterations(staff, date);
                
                attendance.setStatus(newStatus);
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
                
                try {
                    triggerAlterationProcess(staff, date, savedAttendance, periodsToProcess);
                } catch (Exception e) {
                    log.error("Alteration process failed for staff {} on {} (attendance still saved): {}",
                              staff.getStaffId(), date, e.getMessage(), e);
                }
            }
        }
        
        return results.isEmpty() ? null : results.get(0);
    }
    
    private void triggerAlterationProcess(Staff staff, LocalDate date, Attendance attendance, Set<Integer> absentPeriods) {
        log.info("Triggering alteration process for staff: {} on date: {}, status: {}",
                 staff.getStaffId(), date, attendance.getStatus());

        // 1=Monday ... 7=Sunday (Java DayOfWeek)
        int dayOfWeek = date.getDayOfWeek().getValue();
        log.info("Day of week: {} (1=Mon, 7=Sun)", dayOfWeek);

        // --- Use timetable_template (live data) instead of legacy timetable table ---
        List<TimetableTemplate> allTemplates =
                timetableTemplateRepository.findByStaffId(staff.getStaffId());
        log.info("Total templates found for staff {}: {}", staff.getStaffId(), allTemplates.size());

        List<TimetableTemplate> templatesForToday = new ArrayList<>();
        for (TimetableTemplate t : allTemplates) {
            if (t.getStatus() == TimetableStatus.ACTIVE
                    && t.getDayOrder() != null
                    && t.getDayOrder().equals(dayOfWeek)) {
                templatesForToday.add(t);
            }
        }

        // Determine which periods need alteration
        java.util.Set<Integer> periodsThatNeedAlteration =
                getPeriodsThatNeedAlteration(attendance, absentPeriods);

        log.warn("Processing {} matching templates for date: {}, day: {}, periods: {}",
                 templatesForToday.size(), date, dayOfWeek, periodsThatNeedAlteration);

        if (templatesForToday.isEmpty()) {
            log.error("NO ACTIVE TIMETABLE TEMPLATES found for staff {} on dayOrder {} (date: {})." +
                      " Ensure timetables are created via the Timetable Management page.",
                      staff.getStaffId(), dayOfWeek, date);
            return;
        }

        // processAlteration runs in REQUIRES_NEW — failure there never rolls back attendance.
        int alterationsCreated = 0;
        for (TimetableTemplate template : templatesForToday) {
            if (periodsThatNeedAlteration.contains(template.getPeriodNumber())) {
                log.info("Creating alteration for template: period={}, class={}",
                         template.getPeriodNumber(), template.getClassCode());

                // Ensure a Timetable row exists (needed for Alteration FK)
                Timetable timetable = getOrCreateTimetable(staff, template);
                if (timetable == null) {
                    log.error("Could not get/create Timetable for template id={}, skipping", template.getId());
                    continue;
                }

                Alteration alteration = alterationService.processAlteration(
                        timetable.getId(),
                        date,
                        attendance.getStatus(),
                        attendance.getDayType(),
                        attendance.getMeetingHours()
                );
                if (alteration != null) {
                    alterationsCreated++;
                }
            }
        }

        log.warn("Alteration process complete: {} alteration(s) created from {} matching template(s)",
                 alterationsCreated, templatesForToday.size());
    }

    /**
     * Return the existing Timetable row for staff/dayOrder/period, or create one from the template.
     * This bridges timetable_template (UI-managed) with the timetable table (used by AlterationService).
     */
    private Timetable getOrCreateTimetable(Staff staff, TimetableTemplate template) {
        List<Timetable> existing = timetableRepository.findByStaffAndPeriod(
                staff.getId(), template.getDayOrder(), template.getPeriodNumber());
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        try {
            ClassRoom classRoom = classRoomRepository
                    .findByClassCode(template.getClassCode()).orElse(null);
            Timetable timetable = Timetable.builder()
                    .staff(staff)
                    .subject(template.getSubject())
                    .classRoom(classRoom)
                    .dayOrder(template.getDayOrder())
                    .periodNumber(template.getPeriodNumber())
                    .build();
            return timetableRepository.save(timetable);
        } catch (Exception e) {
            log.error("Failed to create Timetable from template {} for staff {}: {}",
                      template.getId(), staff.getStaffId(), e.getMessage());
            return null;
        }
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


