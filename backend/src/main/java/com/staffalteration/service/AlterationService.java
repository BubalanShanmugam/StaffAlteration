package com.staffalteration.service;

import com.staffalteration.controller.WebSocketController;
import com.staffalteration.dto.AlterationDTO;
import com.staffalteration.entity.*;
import com.staffalteration.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AlterationService {
    
    @Autowired
    private AlterationRepository alterationRepository;
    
    @Autowired
    private TimetableRepository timetableRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private WorkloadSummaryRepository workloadSummaryRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private SMSService smsService;
    
    @Autowired
    private WebSocketController webSocketController;
    
    @Autowired
    private AlterationAuditRepository alterationAuditRepository;
    
    private static final int PERIODS_PER_DAY = 6;
    private static final int DAY_ORDERS = 6;
    
    /**
     * Main alteration algorithm — runs in its OWN transaction (REQUIRES_NEW).
     *
     * WHY REQUIRES_NEW: AttendanceService.markAttendance() is @Transactional. If this method
     * shared that transaction and threw any exception, Spring would mark the outer transaction
     * rollback-only and the attendance save would also be lost. REQUIRES_NEW isolates failures.
     *
     * Attendance values are passed as parameters because REQUIRES_NEW starts a fresh Hibernate
     * session that cannot see the uncommitted attendance row from the caller's transaction.
     *
     * Priority Order:
     * 1. Staff who is present
     * 2. Staff from same department
     * 3. Staff who don't have before/after continuous hours
     * 4. Staff with minimum number of hours
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Alteration processAlteration(Long timetableId, LocalDate alterationDate,
                                        Attendance.AttendanceStatus attendanceStatus,
                                        Attendance.DayType attendanceDayType,
                                        Set<Integer> meetingHours) {
        try {
            log.info("processAlteration START — timetableId={}, date={}, status={}",
                     timetableId, alterationDate, attendanceStatus);

            // Re-fetch timetable inside this new transaction to avoid LazyInitializationException
            Timetable timetable = timetableRepository.findById(timetableId).orElse(null);
            if (timetable == null) {
                log.warn("Timetable {} not found, skipping alteration", timetableId);
                return null;
            }

            // Duplicate guard: skip if an active alteration already exists for this slot+date
            List<Alteration> existing = alterationRepository.findByTimetableAndDate(timetableId, alterationDate);
            if (!existing.isEmpty() && existing.get(0).getStatus() != Alteration.AlterationStatus.CANCELLED) {
                log.info("Active alteration id={} already exists for timetable {} on {}, skipping",
                         existing.get(0).getId(), timetableId, alterationDate);
                return existing.get(0);
            }

            Staff originalStaff = timetable.getStaff();

            // Determine absence type from the passed attendance values (no DB query needed)
            Alteration.AbsenceType absenceType = Alteration.AbsenceType.FN;
            Integer periodNumber = timetable.getPeriodNumber();

            if (attendanceStatus.equals(Attendance.AttendanceStatus.MEETING)) {
                if (meetingHours != null && !meetingHours.isEmpty() && meetingHours.contains(timetable.getPeriodNumber())) {
                    absenceType = Alteration.AbsenceType.PERIOD_WISE_ABSENT;
                } else {
                    log.debug("Period {} not in meeting hours {}, skipping", timetable.getPeriodNumber(), meetingHours);
                    return null;
                }
            } else if (attendanceStatus.equals(Attendance.AttendanceStatus.PERIOD_WISE_ABSENT)) {
                absenceType = Alteration.AbsenceType.PERIOD_WISE_ABSENT;
            } else {
                Attendance.DayType dayType = attendanceDayType != null ? attendanceDayType : Attendance.DayType.FULL_DAY;
                if (attendanceStatus.equals(Attendance.AttendanceStatus.LEAVE) ||
                    attendanceStatus.equals(Attendance.AttendanceStatus.ABSENT)) {
                    if (dayType.equals(Attendance.DayType.MORNING_ONLY))    absenceType = Alteration.AbsenceType.AN;
                    else if (dayType.equals(Attendance.DayType.AFTERNOON_ONLY)) absenceType = Alteration.AbsenceType.AF;
                    else absenceType = Alteration.AbsenceType.FN;
                } else if (attendanceStatus.equals(Attendance.AttendanceStatus.ONDUTY)) {
                    if (dayType.equals(Attendance.DayType.MORNING_ONLY))    absenceType = Alteration.AbsenceType.AN;
                    else if (dayType.equals(Attendance.DayType.AFTERNOON_ONLY)) absenceType = Alteration.AbsenceType.AF;
                    else absenceType = Alteration.AbsenceType.ONDUTY;
                }
            }

            // Find best substitute using priority algorithm
            Staff substituteStaff = findSubstitute(timetable, alterationDate);
            if (substituteStaff == null) {
                log.warn("No substitute found for timetable {}, skipping alteration", timetableId);
                return null;
            }

            // Persist the alteration record
            Alteration alteration = Alteration.builder()
                    .timetable(timetable)
                    .originalStaff(originalStaff)
                    .substituteStaff(substituteStaff)
                    .alterationDate(alterationDate)
                    .absenceType(absenceType)
                    .periodNumber(periodNumber)
                    .status(Alteration.AlterationStatus.ASSIGNED)
                    .remarks("Automatically assigned based on priority algorithm")
                    .build();

            @SuppressWarnings("null")
            Alteration savedAlteration = alterationRepository.save(alteration);
            log.info("Alteration SAVED to DB — id={}, original={}, substitute={}, absenceType={}",
                     savedAlteration.getId(), originalStaff.getStaffId(), substituteStaff.getStaffId(), absenceType);

            // Audit (has its own internal try-catch)
            logAlterationAudit(savedAlteration);

            // --- Every post-save side-effect is individually try-catched ---
            try {
                AlterationDTO dto = mapToDTO(savedAlteration);
                webSocketController.broadcastAlterationCreated(dto);
                webSocketController.broadcastToDepartment(originalStaff.getDepartment().getId(), "ALTERATION_CREATED", dto);
            } catch (Exception e) { log.error("WebSocket broadcast failed: {}", e.getMessage()); }

            try { updateWorkloadSummary(substituteStaff, alterationDate); }
            catch (Exception e) { log.error("Workload update failed: {}", e.getMessage()); }

            try { notificationService.notifySubstituteStaff(savedAlteration); }
            catch (Exception e) { log.error("Substitute in-app notification failed: {}", e.getMessage()); }

            try {
                String classCode = (timetable.getClassRoom() != null) ? timetable.getClassRoom().getClassCode() : "N/A";
                staffRepository.findHodByDepartmentId(originalStaff.getDepartment().getId(), Role.RoleType.HOD).ifPresent(hod -> {
                    try { notificationService.notifyHodOnSubstitution(savedAlteration, hod); } catch (Exception ex) { log.error("HOD notify: {}", ex.getMessage()); }
                    try { emailService.sendAlterationNotificationToHod(hod, savedAlteration); } catch (Exception ex) { log.error("HOD email: {}", ex.getMessage()); }
                    if (hod.getPhoneNumber() != null) {
                        try { smsService.notifyHodOnSubstitution(hod.getPhoneNumber(),
                                originalStaff.getFirstName() + " " + originalStaff.getLastName(),
                                substituteStaff.getFirstName() + " " + substituteStaff.getLastName(),
                                classCode, alterationDate.toString());
                        } catch (Exception ex) { log.error("HOD SMS: {}", ex.getMessage()); }
                    }
                });
            } catch (Exception e) { log.error("HOD block failed: {}", e.getMessage()); }

            try { emailService.sendAlterationNotification(savedAlteration); }
            catch (Exception e) { log.error("Staff email failed: {}", e.getMessage()); }

            try { emailService.sendLessonPlanToSubstitute(savedAlteration); }
            catch (Exception e) { log.error("Lesson plan email failed: {}", e.getMessage()); }

            try {
                if (substituteStaff.getPhoneNumber() != null) {
                    String classCode = (timetable.getClassRoom() != null) ? timetable.getClassRoom().getClassCode() : "N/A";
                    smsService.notifySubstituteAssigned(substituteStaff.getPhoneNumber(),
                            originalStaff.getFirstName() + " " + originalStaff.getLastName(),
                            classCode, alterationDate.toString());
                }
            } catch (Exception e) { log.error("Substitute SMS failed: {}", e.getMessage()); }

            return savedAlteration;

        } catch (Exception e) {
            // Catch-all: log and return null so the caller's transaction is never affected
            log.error("processAlteration FAILED — timetableId={}, date={}: {}", timetableId, alterationDate, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Find the best available substitute staff.
     *
     * Strategy (always returns a non-null result if any active staff exists):
     * 1. Get all ACTIVE staff, exclude the absent staff.
     * 2. Pre-fetch all existing alterations for this date ONCE (avoids per-candidate DB round-trips
     *    and avoids lazy-load NPEs by reading IDs while the session is open).
     * 3. Build in-memory maps: who is already substituting which period.
     * 4. Prefer staff NOT already assigned to the same period (avoid double-booking).
     * 5. Within that pool, prefer same-department staff.
     * 6. Among same-dept candidates, pick the one with the fewest substitute assignments today.
     * 7. Absolute last resort: first active staff member.
     */
    private Staff findSubstitute(Timetable timetable, LocalDate alterationDate) {
        // --- 1. Candidate pool: all active staff except the absent one ---
        List<Staff> allStaff = new ArrayList<>(staffRepository.findByStatus(Staff.StaffStatus.ACTIVE));
        Long absentId = (timetable.getStaff() != null) ? timetable.getStaff().getId() : null;
        if (absentId != null) {
            allStaff.removeIf(s -> s.getId().equals(absentId));
        }
        if (allStaff.isEmpty()) {
            log.warn("findSubstitute: no active staff other than the absent one");
            return null;
        }

        // --- 2. Pre-fetch existing alterations for this date (ONE query) ---
        List<Alteration> todayAlterations = alterationRepository.findByAlterationDate(alterationDate);

        // --- 3. Build in-memory structures (read .getId() while session is still open) ---
        // substituteLoadMap : staffId → number of periods they're substituting today
        Map<Long, Long> substituteLoadMap = new HashMap<>();
        // allocatedKeys : "staffId:periodNumber" pairs already taken
        java.util.Set<String> allocatedKeys = new java.util.HashSet<>();

        for (Alteration a : todayAlterations) {
            try {
                if (a.getSubstituteStaff() == null) continue;
                Long subId = a.getSubstituteStaff().getId(); // ID is always safe even with proxy
                substituteLoadMap.merge(subId, 1L, Long::sum);

                Integer period = (a.getTimetable() != null) ? a.getTimetable().getPeriodNumber() : null;
                if (period != null) {
                    allocatedKeys.add(subId + ":" + period);
                }
            } catch (Exception e) {
                log.debug("Skipping alteration during substitute search: {}", e.getMessage());
            }
        }

        // --- 4. Prefer staff not double-booked for this exact period ---
        int targetPeriod = (timetable.getPeriodNumber() != null) ? timetable.getPeriodNumber() : 0;
        List<Staff> notDoubleBooked = allStaff.stream()
                .filter(s -> !allocatedKeys.contains(s.getId() + ":" + targetPeriod))
                .collect(Collectors.toList());
        List<Staff> candidates = notDoubleBooked.isEmpty() ? allStaff : notDoubleBooked;
        log.info("findSubstitute: {} total candidates, {} not double-booked for period {}",
                 allStaff.size(), notDoubleBooked.size(), targetPeriod);

        // --- 5. Prefer same-department staff ---
        Long deptId = null;
        try {
            if (timetable.getStaff() != null && timetable.getStaff().getDepartment() != null) {
                deptId = timetable.getStaff().getDepartment().getId();
            }
        } catch (Exception e) {
            log.debug("Could not read department from timetable staff: {}", e.getMessage());
        }
        final Long finalDeptId = deptId;
        if (finalDeptId != null) {
            List<Staff> sameDept = candidates.stream()
                    .filter(s -> s.getDepartment() != null && finalDeptId.equals(s.getDepartment().getId()))
                    .collect(Collectors.toList());
            if (!sameDept.isEmpty()) {
                log.info("findSubstitute: using {} same-dept candidates (dept={})", sameDept.size(), finalDeptId);
                candidates = sameDept;
            } else {
                log.info("findSubstitute: no same-dept candidates, using full pool of {}", candidates.size());
            }
        }

        // --- 6. Pick least-loaded ---
        final Map<Long, Long> loadMap = substituteLoadMap;
        Staff chosen = candidates.stream()
                .min(Comparator.comparingLong(s -> loadMap.getOrDefault(s.getId(), 0L)))
                .orElse(null);

        // --- 7. Absolute fallback ---
        if (chosen == null) {
            chosen = allStaff.get(0);
            log.warn("findSubstitute: using absolute fallback staff {}", chosen.getStaffId());
        } else {
            log.info("findSubstitute: chose substitute={} (load={}) for period {}",
                     chosen.getStaffId(), loadMap.getOrDefault(chosen.getId(), 0L), targetPeriod);
        }
        return chosen;
    }
    
    /**
     * Update workload summary for substitute staff
     */
    private void updateWorkloadSummary(Staff staff, LocalDate date) {
        WorkloadSummary summary = workloadSummaryRepository.findByStaffIdAndWorkloadDate(staff.getId(), date)
                .orElse(WorkloadSummary.builder()
                        .staff(staff)
                        .workloadDate(date)
                        .totalHours(0)
                        .regularHours(0)
                        .alterationHours(0)
                        .weeklyTotal(0)
                        .build());
        
        summary.setAlterationHours(summary.getAlterationHours() + 1);
        summary.setTotalHours(summary.getRegularHours() + summary.getAlterationHours());
        summary.setWeeklyTotal(summary.getWeeklyTotal() + 1);
        
        workloadSummaryRepository.save(summary);
    }
    
    public List<AlterationDTO> getAlterationsByDate(LocalDate date) {
        List<Alteration> alterations = alterationRepository.findByAlterationDate(date);
        return alterations.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AlterationDTO> getAlterationsByDateRange(LocalDate fromDate, LocalDate toDate) {
        List<Alteration> alterations = alterationRepository.findByAlterationDateBetween(fromDate, toDate);
        return alterations.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AlterationDTO> getAlterationsByStaff(Long staffId) {
        List<Alteration> asOriginal = alterationRepository.findByOriginalStaffId(staffId);
        List<Alteration> asSubstitute = alterationRepository.findBySubstituteStaffId(staffId);
        
        List<Alteration> combined = new ArrayList<>(asOriginal);
        combined.addAll(asSubstitute);
        
        // Sort by alteration date in descending order (newest first)
        combined.sort((a, b) -> b.getAlterationDate().compareTo(a.getAlterationDate()));
        
        return combined.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AlterationDTO> getAlterationsByDepartment(Long departmentId) {
        log.info("Fetching alterations for department: {}", departmentId);
        
        // Get all staff in the department
        List<Staff> deptStaff = staffRepository.findByDepartmentId(departmentId);
        List<Long> staffIds = deptStaff.stream().map(Staff::getId).collect(Collectors.toList());
        
        if (staffIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get all alterations where original staff is from this department
        List<Alteration> alterations = alterationRepository.findByOriginalStaffIdIn(staffIds);
        
        return alterations.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public AlterationDTO updateAlterationStatus(Long alterationId, String status) {
        @SuppressWarnings("null")
        Alteration alteration = alterationRepository.findById(alterationId)
                .orElseThrow(() -> new RuntimeException("Alteration not found"));
        
        alteration.setStatus(Alteration.AlterationStatus.valueOf(status));
        @SuppressWarnings("null")
        Alteration saved = alterationRepository.save(alteration);
        return mapToDTO(saved);
    }
    
    public AlterationDTO rejectAlteration(Long alterationId) {
        log.info("Processing rejection for alteration: {}", alterationId);
        
        @SuppressWarnings("null")
        Alteration alteration = alterationRepository.findById(alterationId)
                .orElseThrow(() -> new RuntimeException("Alteration not found"));
        
        // Get the old substitute before trying to find a new one
        Staff previousSubstitute = alteration.getSubstituteStaff();
        Timetable timetable = alteration.getTimetable();
        LocalDate alterationDate = alteration.getAlterationDate();
        Staff originalStaff = alteration.getOriginalStaff();
        
        log.info("Alteration rejected by {}, finding alternative substitute", previousSubstitute.getStaffId());
        
        // Get all active staff except original staff and previous substitute
        List<Staff> candidates = new ArrayList<>(staffRepository.findByStatus(Staff.StaffStatus.ACTIVE));
        candidates.removeIf(staff -> staff.getId().equals(originalStaff.getId()) || 
                                     staff.getId().equals(previousSubstitute.getId()));
        
        if (candidates.isEmpty()) {
            log.warn("No alternative substitutes available for alteration: {}", alterationId);
            // Mark as rejected if no alternatives exist
            alteration.setStatus(Alteration.AlterationStatus.REJECTED);
            alteration.setRemarks("Rejected by " + previousSubstitute.getStaffId() + ". No alternative substitutes available.");
            Alteration rejectedAlt = alterationRepository.save(alteration);
            
            AlterationDTO rejectedDTO = mapToDTO(rejectedAlt);
            try { webSocketController.broadcastAlterationRejected(rejectedDTO); } catch (Exception ex) { log.error("WebSocket broadcast failed: {}", ex.getMessage()); }
            try { webSocketController.broadcastToDepartment(originalStaff.getDepartment().getId(), "ALTERATION_REJECTED", rejectedDTO); } catch (Exception ex) { log.error("Dept broadcast failed: {}", ex.getMessage()); }
            
            // Notify HOD about rejection with no replacement
            try {
                staffRepository.findHodByDepartmentId(originalStaff.getDepartment().getId(), Role.RoleType.HOD).ifPresent(hod -> {
                    try { notificationService.notifyHodOnSubstitution(rejectedAlt, hod); } catch (Exception ex) { log.error("HOD notification failed: {}", ex.getMessage()); }
                    try { emailService.sendAlterationNotificationToHod(hod, rejectedAlt); } catch (Exception ex) { log.error("HOD email failed: {}", ex.getMessage()); }
                });
            } catch (Exception e) { log.error("HOD notify block failed: {}", e.getMessage()); }
            
            return rejectedDTO;
        }
        
        // Find the least-loaded alternative substitute from remaining candidates
        Staff newSubstitute = candidates.stream()
                .min(Comparator.comparingLong(s ->
                        alterationRepository.findByAlterationDate(alterationDate).stream()
                                .filter(a -> a.getSubstituteStaff() != null &&
                                             a.getSubstituteStaff().getId().equals(s.getId()))
                                .count()))
                .orElse(null);
        
        if (newSubstitute != null) {
            // Update the same alteration with the new substitute (AUTOMATIC ASSIGNMENT)
            alteration.setSubstituteStaff(newSubstitute);
            alteration.setStatus(Alteration.AlterationStatus.ASSIGNED); // Reset to ASSIGNED for new substitute
            alteration.setRemarks("Auto-reassigned from " + previousSubstitute.getStaffId() + " (rejected) to " + newSubstitute.getStaffId());
            
            @SuppressWarnings("null")
            Alteration updatedAlteration = alterationRepository.save(alteration);
            
            // Update audit record with second substitute assignment
            updateAuditOnSecondSubstituteAssignment(updatedAlteration, previousSubstitute);
            
            log.info("Alteration automatically re-assigned to second substitute: {} (previous: {})", 
                     newSubstitute.getStaffId(), previousSubstitute.getStaffId());
            
            // Broadcast the updated alteration to the frontend
            AlterationDTO updatedDTO = mapToDTO(updatedAlteration);
            try { webSocketController.broadcastAlterationUpdated(updatedDTO); } catch (Exception ex) { log.error("WebSocket update broadcast failed: {}", ex.getMessage()); }
            try { webSocketController.broadcastToDepartment(originalStaff.getDepartment().getId(), "ALTERATION_REASSIGNED", updatedDTO); } catch (Exception ex) { log.error("Dept broadcast failed: {}", ex.getMessage()); }
            
            // Notify the new substitute
            try { notificationService.notifySubstituteStaff(updatedAlteration); } catch (Exception ex) { log.error("Substitute notification failed: {}", ex.getMessage()); }
            try { emailService.sendAlterationNotification(updatedAlteration); } catch (Exception ex) { log.error("Substitute email failed: {}", ex.getMessage()); }
            
            try {
                if (newSubstitute.getPhoneNumber() != null) {
                    String classCode = (timetable.getClassRoom() != null) ? timetable.getClassRoom().getClassCode() : "N/A";
                    smsService.notifySubstituteAssigned(
                        newSubstitute.getPhoneNumber(),
                        originalStaff.getFirstName() + " " + originalStaff.getLastName(),
                        classCode,
                        alterationDate.toString()
                    );
                }
            } catch (Exception ex) { log.error("Substitute SMS failed: {}", ex.getMessage()); }
            
            // Notify HOD about successful reassignment
            try {
                staffRepository.findHodByDepartmentId(originalStaff.getDepartment().getId(), Role.RoleType.HOD).ifPresent(hod -> {
                    try { notificationService.notifyHodOnSubstitution(updatedAlteration, hod); } catch (Exception ex) { log.error("HOD notification failed: {}", ex.getMessage()); }
                    try { emailService.sendAlterationNotificationToHod(hod, updatedAlteration); } catch (Exception ex) { log.error("HOD email failed: {}", ex.getMessage()); }
                });
            } catch (Exception e) { log.error("HOD notify block failed: {}", e.getMessage()); }
            
            return updatedDTO;
        } else {
            // No suitable substitute found among candidates
            alteration.setStatus(Alteration.AlterationStatus.REJECTED);
            alteration.setRemarks("Rejected by " + previousSubstitute.getStaffId() + ". No suitable alternative substitute found.");
            Alteration rejectedAlt = alterationRepository.save(alteration);
            
            log.warn("No suitable alternative substitute found for alteration: {}", alterationId);
            AlterationDTO rejectedDTO = mapToDTO(rejectedAlt);
            
            try { webSocketController.broadcastAlterationRejected(rejectedDTO); } catch (Exception ex) { log.error("WebSocket reject broadcast failed: {}", ex.getMessage()); }
            try { webSocketController.broadcastToDepartment(originalStaff.getDepartment().getId(), "ALTERATION_REJECTED", rejectedDTO); } catch (Exception ex) { log.error("Dept broadcast failed: {}", ex.getMessage()); }
            
            // Notify HOD about rejection with no replacement
            try {
                staffRepository.findHodByDepartmentId(originalStaff.getDepartment().getId(), Role.RoleType.HOD).ifPresent(hod -> {
                    try { notificationService.notifyHodOnSubstitution(rejectedAlt, hod); } catch (Exception ex) { log.error("HOD notification failed: {}", ex.getMessage()); }
                    try { emailService.sendAlterationNotificationToHod(hod, rejectedAlt); } catch (Exception ex) { log.error("HOD email failed: {}", ex.getMessage()); }
                });
            } catch (Exception e) { log.error("HOD notify block failed: {}", e.getMessage()); }
            
            return rejectedDTO;
        }
    }
    
    private AlterationDTO mapToDTO(Alteration alteration) {
        if (alteration == null) {
            return null;
        }
        
        return AlterationDTO.builder()
                .id(alteration.getId())
                .timetableId(alteration.getTimetable() != null ? alteration.getTimetable().getId() : null)
                .originalStaffId(alteration.getOriginalStaff() != null ? alteration.getOriginalStaff().getStaffId() : null)
                .originalStaffName(alteration.getOriginalStaff() != null ? 
                    alteration.getOriginalStaff().getFirstName() + " " + alteration.getOriginalStaff().getLastName() : null)
                .substituteStaffId(alteration.getSubstituteStaff() != null ? alteration.getSubstituteStaff().getStaffId() : null)
                .substituteStaffName(alteration.getSubstituteStaff() != null ?
                    alteration.getSubstituteStaff().getFirstName() + " " + alteration.getSubstituteStaff().getLastName() : null)
                .classCode(alteration.getTimetable() != null && alteration.getTimetable().getClassRoom() != null ? 
                    alteration.getTimetable().getClassRoom().getClassCode() : null)
                .subjectName(alteration.getTimetable() != null && alteration.getTimetable().getSubject() != null ?
                    alteration.getTimetable().getSubject().getSubjectName() : null)
                .dayOrder(alteration.getTimetable() != null ? alteration.getTimetable().getDayOrder() : null)
                .periodNumber(alteration.getPeriodNumber() != null ? alteration.getPeriodNumber() : 
                    (alteration.getTimetable() != null ? alteration.getTimetable().getPeriodNumber() : null))
                .alterationDate(alteration.getAlterationDate())
                .absenceType(alteration.getAbsenceType() != null ? alteration.getAbsenceType().toString() : null)
                .status(alteration.getStatus() != null ? alteration.getStatus().toString() : null)
                .lessonPlanId(alteration.getLessonPlan() != null ? alteration.getLessonPlan().getId() : null)
                .remarks(alteration.getRemarks())
                .departmentId(alteration.getOriginalStaff() != null && alteration.getOriginalStaff().getDepartment() != null ? 
                    alteration.getOriginalStaff().getDepartment().getId() : null)
                .createdAt(alteration.getCreatedAt())
                .updatedAt(alteration.getUpdatedAt())
                .build();
    }
    
    /**
     * Create an audit record for alteration creation
     */
    private void logAlterationAudit(Alteration alteration) {
        try {
            Staff originalStaff = alteration.getOriginalStaff();
            Staff firstSubstitute = alteration.getSubstituteStaff();
            
            AlterationAudit audit = AlterationAudit.builder()
                    .originalStaffId(originalStaff.getId())
                    .originalStaffName(originalStaff.getFirstName() + " " + originalStaff.getLastName())
                    .originalStaffEmail(originalStaff.getEmail())
                    .absenceDate(alteration.getAlterationDate())
                    .absenceType(alteration.getAbsenceType().toString())
                    .className(alteration.getTimetable() != null && alteration.getTimetable().getClassRoom() != null ?
                            alteration.getTimetable().getClassRoom().getClassCode() : null)
                    .subject(alteration.getTimetable() != null && alteration.getTimetable().getSubject() != null ?
                            alteration.getTimetable().getSubject().getSubjectName() : null)
                    .periodNumber(alteration.getPeriodNumber())
                    .firstSubstituteId(firstSubstitute.getId())
                    .firstSubstituteName(firstSubstitute.getFirstName() + " " + firstSubstitute.getLastName())
                    .firstSubstituteEmail(firstSubstitute.getEmail())
                    .firstSubstituteStatus("PENDING")
                    .finalStatus("PENDING")
                    .remarks("Alteration created and assigned to first substitute")
                    .build();
            
            alterationAuditRepository.save(audit);
            log.info("Audit record created for alteration ID: {}", alteration.getId());
        } catch (Exception e) {
            log.error("Error creating audit record for alteration: {}", alteration.getId(), e);
            // Don't throw exception - audit logging should not block alteration creation
        }
    }
    
    /**
     * Update audit record when first substitute accepts/rejects
     */
    private void updateAuditOnFirstSubstituteResponse(Alteration alteration, String status) {
        try {
            Staff originalStaff = alteration.getOriginalStaff();
            Staff firstSubstitute = alteration.getSubstituteStaff();
            
            // Find existing audit record for this alteration
            // For now, query by original staff and absence date (can be improved)
            List<AlterationAudit> existingAudits = alterationAuditRepository.findByOriginalStaffId(originalStaff.getId());
            
            AlterationAudit matchingAudit = existingAudits.stream()
                    .filter(a -> a.getAbsenceDate().equals(alteration.getAlterationDate()) &&
                                 a.getPeriodNumber().equals(alteration.getPeriodNumber()) &&
                                 a.getFirstSubstituteId().equals(firstSubstitute.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (matchingAudit != null) {
                matchingAudit.setFirstSubstituteStatus(status);
                matchingAudit.setFirstSubstituteResponseTime(LocalDateTime.now());
                
                if (status.equals("ACCEPTED")) {
                    matchingAudit.setFinalStatus("FULFILLED");
                }
                
                alterationAuditRepository.save(matchingAudit);
                log.info("Audit record updated for first substitute response: {}", status);
            }
        } catch (Exception e) {
            log.error("Error updating audit record for alteration response: {}", alteration.getId(), e);
        }
    }
    
    /**
     * Update audit record when first substitute rejects and second substitute is assigned
     */
    private void updateAuditOnSecondSubstituteAssignment(Alteration alteration, Staff previousSubstitute) {
        try {
            Staff originalStaff = alteration.getOriginalStaff();
            Staff secondSubstitute = alteration.getSubstituteStaff();
            
            // Find existing audit record
            List<AlterationAudit> existingAudits = alterationAuditRepository.findByOriginalStaffId(originalStaff.getId());
            
            AlterationAudit matchingAudit = existingAudits.stream()
                    .filter(a -> a.getAbsenceDate().equals(alteration.getAlterationDate()) &&
                                 a.getPeriodNumber().equals(alteration.getPeriodNumber()) &&
                                 a.getFirstSubstituteId().equals(previousSubstitute.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (matchingAudit != null) {
                // Update the record with second substitute information
                matchingAudit.setFirstSubstituteStatus("REJECTED");
                matchingAudit.setSecondSubstituteId(secondSubstitute.getId());
                matchingAudit.setSecondSubstituteName(secondSubstitute.getFirstName() + " " + secondSubstitute.getLastName());
                matchingAudit.setSecondSubstituteEmail(secondSubstitute.getEmail());
                matchingAudit.setSecondSubstituteStatus("PENDING");
                matchingAudit.setFinalStatus("PENDING");
                matchingAudit.setRemarks("First substitute rejected. Automatically reassigned to second substitute: " + 
                                        secondSubstitute.getFirstName() + " " + secondSubstitute.getLastName());
                
                alterationAuditRepository.save(matchingAudit);
                log.info("Audit record updated with second substitute assignment");
            }
        } catch (Exception e) {
            log.error("Error updating audit record for second substitute assignment: {}", alteration.getId(), e);
        }
    }
}
