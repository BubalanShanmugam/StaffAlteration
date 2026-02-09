package com.staffalteration.service;

import com.staffalteration.dto.AlterationDTO;
import com.staffalteration.entity.*;
import com.staffalteration.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    
    private static final int PERIODS_PER_DAY = 6;
    private static final int DAY_ORDERS = 6;
    
    /**
     * Main alteration algorithm: Apply strict priority-driven rules
     * Priority Order:
     * 1. Staff who is present
     * 2. Staff from same department
     * 3. Staff who don't have before and after continuous hours
     * 4. Staff with minimum number of hours comparing to all sorted staff
     */
    public Alteration processAlteration(Timetable timetable, LocalDate alterationDate) {
        log.info("Processing alteration for timetable: {}, date: {}", timetable.getId(), alterationDate);
        
        Staff originalStaff = timetable.getStaff();
        
        // Check if staff is absent or in meeting on alteration date
        Attendance attendance = attendanceRepository.findByStaffIdAndAttendanceDate(originalStaff.getId(), alterationDate)
                .orElse(null);
        
        if (attendance == null) {
            log.warn("No attendance record for staff {} on {}", originalStaff.getStaffId(), alterationDate);
            return null;
        }
        
        boolean needsAlteration = attendance.getStatus().equals(Attendance.AttendanceStatus.ABSENT) ||
                                 attendance.getStatus().equals(Attendance.AttendanceStatus.MEETING);
        
        if (!needsAlteration) {
            log.debug("Staff {} is not absent/meeting on {}", originalStaff.getStaffId(), alterationDate);
            return null;
        }
        
        // For MEETING status, check if this period is affected
        if (attendance.getStatus().equals(Attendance.AttendanceStatus.MEETING)) {
            if (!attendance.getMeetingHours().contains(timetable.getPeriodNumber())) {
                log.debug("Period {} is not affected by meeting", timetable.getPeriodNumber());
                return null;
            }
        }
        
        // Find candidate substitute
        Staff substituteStaff = findSubstitute(timetable, alterationDate);
        
        if (substituteStaff == null) {
            log.warn("No suitable substitute found for timetable: {}", timetable.getId());
            return null;
        }
        
        // Create alteration record
        Alteration alteration = Alteration.builder()
                .timetable(timetable)
                .originalStaff(originalStaff)
                .substituteStaff(substituteStaff)
                .alterationDate(alterationDate)
                .status(Alteration.AlterationStatus.ASSIGNED)
                .remarks("Automatically assigned based on priority algorithm")
                .build();
        
        Alteration savedAlteration = alterationRepository.save(alteration);
        log.info("Alteration created: original={}, substitute={}", 
                 originalStaff.getStaffId(), substituteStaff.getStaffId());
        
        // Update workload summary for substitute
        updateWorkloadSummary(substituteStaff, alterationDate);
        
        // Send in-app notification
        notificationService.notifySubstituteStaff(savedAlteration);
        
        // Send email notifications to both staff members
        emailService.sendAlterationNotification(savedAlteration);
        
        return savedAlteration;
    }
    
    /**
     * Find the best substitute staff based on priority rules
     * Priority 1: Staff who is present
     * Priority 2: Staff from same department
     * Priority 3: Staff who don't have before/after continuous hours
     * Priority 4: Staff with minimum number of hours
     */
    private Staff findSubstitute(Timetable timetable, LocalDate alterationDate) {
        List<Staff> allStaff = staffRepository.findByStatus(Staff.StaffStatus.ACTIVE);
        
        // Remove the original staff from candidates
        allStaff.remove(timetable.getStaff());
        
        if (allStaff.isEmpty()) {
            log.warn("No active staff available for substitution");
            return null;
        }
        
        // PRIORITY 1: Filter for staff who are PRESENT (or don't have attendance marked)
        List<Staff> presentStaff = allStaff.stream()
                .filter(staff -> {
                    Attendance att = attendanceRepository.findByStaffIdAndAttendanceDate(staff.getId(), alterationDate)
                            .orElse(null);
                    // Staff is present if: no attendance marked OR explicitly marked PRESENT
                    return att == null || att.getStatus().equals(Attendance.AttendanceStatus.PRESENT);
                })
                .collect(Collectors.toList());
        
        if (presentStaff.isEmpty()) {
            log.warn("No present staff available for period {}", timetable.getPeriodNumber());
            return null;
        }
        
        // Filter out staff already allocated for this exact period
        List<Staff> availableStaff = presentStaff.stream()
                .filter(staff -> !isAlreadyAllocatedForPeriod(staff, timetable, alterationDate))
                .collect(Collectors.toList());
        
        if (availableStaff.isEmpty()) {
            log.warn("All present staff already allocated for period {}", timetable.getPeriodNumber());
            return presentStaff.get(0); // Fallback
        }
        
        // PRIORITY 2: Filter for same department
        List<Staff> sameDepartmentStaff = availableStaff.stream()
                .filter(staff -> staff.getDepartment().equals(timetable.getStaff().getDepartment()))
                .collect(Collectors.toList());
        
        List<Staff> candidateList = sameDepartmentStaff.isEmpty() ? availableStaff : sameDepartmentStaff;
        
        // PRIORITY 3 & 4: Select best based on continuous hours and total hours
        return selectBestStaff(candidateList, timetable, alterationDate);
    }
    
    /**
     * Select the best staff based on remaining priorities
     * Priority 3: Staff without before/after continuous hours
     * Priority 4: Staff with minimum total hours
     */
    private Staff selectBestStaff(List<Staff> candidates, Timetable timetable, LocalDate alterationDate) {
        // Score each candidate (lower is better)
        List<StaffScore> scores = candidates.stream()
                .map(staff -> {
                    double score = calculateScore(staff, timetable, alterationDate);
                    return new StaffScore(staff, score);
                })
                .sorted(Comparator.comparingDouble(StaffScore::getScore))
                .collect(Collectors.toList());
        
        if (scores.isEmpty()) {
            return null;
        }
        
        log.debug("Top candidate for period {}: {}", timetable.getPeriodNumber(), scores.get(0).getStaff().getStaffId());
        return scores.get(0).getStaff();
    }
    
    /**
     * Calculate score for staff (lower is better)
     * Score = (hasContinuousHours ? 1000 : 0) + (totalHours * 100)
     */
    private double calculateScore(Staff staff, Timetable timetable, LocalDate alterationDate) {
        double score = 0;
        
        // Priority 3: Check for continuous hours (before/after clash)
        boolean hasContinuousClash = hasConsecutiveClash(staff, timetable, alterationDate);
        if (hasContinuousClash) {
            score += 1000; // Heavy penalty for continuous hours
        }
        
        // Priority 4: Total hours assigned to staff on this date
        int hoursOnDate = countHoursForStaffOnDate(staff, alterationDate);
        score += hoursOnDate * 100;
        
        return score;
    }
    
    /**
     * Check if staff teaches the same class
     */
    private boolean teachesSameClass(Staff staff, ClassRoom classRoom) {
        return timetableRepository.findByStaffId(staff.getId()).stream()
                .anyMatch(t -> t.getClassRoom().getId().equals(classRoom.getId()));
    }
    
    /**
     * Count hours for staff on a specific date
     */
    private int countHoursForStaffOnDate(Staff staff, LocalDate date) {
        // For each day order, count periods assigned to this staff
        int count = 0;
        for (int dayOrder = 1; dayOrder <= DAY_ORDERS; dayOrder++) {
            for (int period = 1; period <= PERIODS_PER_DAY; period++) {
                if (!timetableRepository.findByStaffAndPeriod(staff.getId(), dayOrder, period).isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Check if staff has clash with previous or next period
     */
    private boolean hasConsecutiveClash(Staff staff, Timetable timetable, LocalDate alterationDate) {
        int dayOrder = timetable.getDayOrder();
        int period = timetable.getPeriodNumber();
        
        // Check previous period
        if (period > 1) {
            if (!timetableRepository.findByStaffAndPeriod(staff.getId(), dayOrder, period - 1).isEmpty()) {
                return true;
            }
        }
        
        // Check next period
        if (period < PERIODS_PER_DAY) {
            if (!timetableRepository.findByStaffAndPeriod(staff.getId(), dayOrder, period + 1).isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get subject that staff teaches for a class
     */
    private Long getStaffSubjectForClass(Staff staff, ClassRoom classRoom) {
        return timetableRepository.findByStaffId(staff.getId()).stream()
                .filter(t -> t.getClassRoom().getId().equals(classRoom.getId()))
                .map(t -> t.getSubject().getId())
                .findFirst()
                .orElse(-1L);
    }
    
    /**
     * Get weekly workload for staff
     */
    private int getWeeklyWorkload(Staff staff) {
        WorkloadSummary summary = workloadSummaryRepository.findByStaffIdAndWorkloadDate(
                staff.getId(), LocalDate.now()).orElse(null);
        return summary != null ? summary.getWeeklyTotal() : 0;
    }
    
    /**
     * Check if staff is already allocated for this specific period on this date
     */
    private boolean isAlreadyAllocatedForPeriod(Staff staff, Timetable timetable, LocalDate alterationDate) {
        List<Alteration> alterations = alterationRepository.findByAlterationDate(alterationDate);
        return alterations.stream()
                .anyMatch(a -> a.getSubstituteStaff().getId().equals(staff.getId()) &&
                             a.getTimetable().getPeriodNumber().equals(timetable.getPeriodNumber()));
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
    
    /**
     * Inner class for scoring staff
     */
    @lombok.Getter
    private static class StaffScore {
        private final Staff staff;
        private final double score;
        
        public StaffScore(Staff staff, double score) {
            this.staff = staff;
            this.score = score;
        }
    }
    
    public List<AlterationDTO> getAlterationsByDate(LocalDate date) {
        List<Alteration> alterations = alterationRepository.findByAlterationDate(date);
        return alterations.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AlterationDTO> getAlterationsByStaff(Long staffId) {
        List<Alteration> asOriginal = alterationRepository.findByOriginalStaffId(staffId);
        List<Alteration> asSubstitute = alterationRepository.findBySubstituteStaffId(staffId);
        
        List<Alteration> combined = new ArrayList<>(asOriginal);
        combined.addAll(asSubstitute);
        
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
        Alteration alteration = alterationRepository.findById(alterationId)
                .orElseThrow(() -> new RuntimeException("Alteration not found"));
        
        alteration.setStatus(Alteration.AlterationStatus.valueOf(status));
        Alteration saved = alterationRepository.save(alteration);
        return mapToDTO(saved);
    }
    
    public AlterationDTO rejectAlteration(Long alterationId) {
        log.info("Processing rejection for alteration: {}", alterationId);
        
        Alteration alteration = alterationRepository.findById(alterationId)
                .orElseThrow(() -> new RuntimeException("Alteration not found"));
        
        // Mark current alteration as rejected
        alteration.setStatus(Alteration.AlterationStatus.REJECTED);
        alterationRepository.save(alteration);
        
        // Find a new substitute (excluding the previously rejected staff)
        Staff previousSubstitute = alteration.getSubstituteStaff();
        Timetable timetable = alteration.getTimetable();
        LocalDate alterationDate = alteration.getAlterationDate();
        
        // Get all staff except original and previous substitute
        List<Staff> allStaff = staffRepository.findByStatus(Staff.StaffStatus.ACTIVE);
        allStaff.remove(timetable.getStaff());
        allStaff.remove(previousSubstitute);
        
        if (allStaff.isEmpty()) {
            log.warn("No alternative substitutes available for alteration: {}", alterationId);
            return mapToDTO(alteration);
        }
        
        // Find a new substitute with revised criteria
        Staff newSubstitute = selectBestStaff(allStaff, timetable, alterationDate);
        
        if (newSubstitute != null) {
            // Create new alteration with the new substitute
            Alteration newAlteration = Alteration.builder()
                    .timetable(timetable)
                    .originalStaff(alteration.getOriginalStaff())
                    .substituteStaff(newSubstitute)
                    .alterationDate(alterationDate)
                    .status(Alteration.AlterationStatus.ASSIGNED)
                    .remarks("Re-assigned after rejection of: " + previousSubstitute.getStaffId())
                    .build();
            
            Alteration savedNewAlteration = alterationRepository.save(newAlteration);
            log.info("New alteration created after rejection: {}", savedNewAlteration.getId());
            
            // Send notifications
            notificationService.notifySubstituteStaff(savedNewAlteration);
            emailService.sendAlterationNotification(savedNewAlteration);
            
            return mapToDTO(savedNewAlteration);
        }
        
        return mapToDTO(alteration);
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
                .periodNumber(alteration.getTimetable() != null ? alteration.getTimetable().getPeriodNumber() : null)
                .alterationDate(alteration.getAlterationDate())
                .status(alteration.getStatus() != null ? alteration.getStatus().toString() : null)
                .lessonPlanId(alteration.getLessonPlan() != null ? alteration.getLessonPlan().getId() : null)
                .remarks(alteration.getRemarks())
                .createdAt(alteration.getCreatedAt())
                .updatedAt(alteration.getUpdatedAt())
                .build();
    }
}
