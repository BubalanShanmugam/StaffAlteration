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
    
    private static final int PERIODS_PER_DAY = 6;
    private static final int DAY_ORDERS = 6;
    
    /**
     * Main alteration algorithm: Apply strict priority-driven rules
     * Priority Order:
     * 1. Staff must be present today
     * 2. Staff already teaches the same class
     * 3. Staff with least number of hours that day
     * 4. Staff with no previous or next period clash
     * 5. Prefer same subject, else other subject
     * 6. Tie-breaker: least weekly workload
     */
    public Alteration processAlteration(Timetable timetable, LocalDate alterationDate) {
        log.info("Processing alteration for timetable: {}, date: {}", timetable.getId(), alterationDate);
        
        Staff originalStaff = timetable.getStaff();
        
        // Check if staff is absent on alteration date
        Attendance attendance = attendanceRepository.findByStaffIdAndAttendanceDate(originalStaff.getId(), alterationDate)
                .orElse(null);
        
        if (attendance == null || !attendance.getStatus().equals(Attendance.AttendanceStatus.ABSENT)) {
            log.warn("Staff {} is not absent on {}", originalStaff.getStaffId(), alterationDate);
            return null;
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
        
        // Send notification
        notificationService.notifySubstituteStaff(savedAlteration);
        
        return savedAlteration;
    }
    
    /**
     * Find the best substitute staff based on priority rules
     */
    private Staff findSubstitute(Timetable timetable, LocalDate alterationDate) {
        List<Staff> allStaff = staffRepository.findByStatus(Staff.StaffStatus.ACTIVE);
        
        // Priority 1: Staff must be present today
        List<Staff> presentStaff = allStaff.stream()
                .filter(staff -> {
                    Attendance att = attendanceRepository.findByStaffIdAndAttendanceDate(staff.getId(), alterationDate)
                            .orElse(null);
                    return att == null || att.getStatus().equals(Attendance.AttendanceStatus.PRESENT);
                })
                .collect(Collectors.toList());
        
        if (presentStaff.isEmpty()) {
            log.warn("No present staff available for alteration");
            return null;
        }
        
        // Remove the original staff from candidates
        presentStaff.remove(timetable.getStaff());
        
        // Check for existing allocations (avoid double allocation)
        List<Staff> availableStaff = presentStaff.stream()
                .filter(staff -> !isAlreadyAllocated(staff, timetable, alterationDate))
                .collect(Collectors.toList());
        
        if (availableStaff.isEmpty()) {
            log.warn("All available staff are already allocated for this period");
            return null; // No available staff for allocation
        }
        
        // Priority 2: Staff already teaches the same class
        List<Staff> sameClassStaff = availableStaff.stream()
                .filter(staff -> teachesSameClass(staff, timetable.getClassRoom()))
                .collect(Collectors.toList());
        
        if (!sameClassStaff.isEmpty()) {
            return selectBestStaff(sameClassStaff, timetable, alterationDate);
        }
        
        // If no same class, use all available staff
        return selectBestStaff(availableStaff, timetable, alterationDate);
    }
    
    /**
     * Select the best staff based on remaining priority rules
     */
    private Staff selectBestStaff(List<Staff> candidates, Timetable timetable, LocalDate alterationDate) {
        return candidates.stream()
                .map(staff -> new StaffScore(staff, calculateScore(staff, timetable, alterationDate)))
                .min(Comparator.comparingDouble(StaffScore::getScore))
                .map(StaffScore::getStaff)
                .orElse(null);
    }
    
    /**
     * Calculate score for staff (lower is better)
     * Score = (hoursToday * 1000) + (hasClash ? 500 : 0) + (subjectMatch ? 0 : 100) + (weeklyWorkload)
     */
    private double calculateScore(Staff staff, Timetable timetable, LocalDate alterationDate) {
        double score = 0;
        
        // Priority 3: Staff with least number of hours that day
        int hoursToday = countHoursForStaffOnDate(staff, alterationDate);
        score += hoursToday * 1000;
        
        // Priority 4: Staff with no previous or next period clash
        boolean hasClash = hasConsecutiveClash(staff, timetable, alterationDate);
        if (hasClash) {
            score += 500;
        }
        
        // Priority 5: Prefer same subject
        boolean sameSubject = timetable.getSubject().getId().equals(getStaffSubjectForClass(staff, timetable.getClassRoom()));
        if (!sameSubject) {
            score += 100;
        }
        
        // Priority 6: Tie-breaker - least weekly workload
        int weeklyWorkload = getWeeklyWorkload(staff);
        score += weeklyWorkload;
        
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
     * Check if staff is already allocated for this period
     */
    private boolean isAlreadyAllocated(Staff staff, Timetable timetable, LocalDate alterationDate) {
        List<Alteration> alterations = alterationRepository.findByTimetableAndDate(
                timetable.getId(), alterationDate);
        return alterations.stream()
                .anyMatch(a -> a.getSubstituteStaff().getId().equals(staff.getId()));
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
    
    public AlterationDTO updateAlterationStatus(Long alterationId, String status) {
        Alteration alteration = alterationRepository.findById(alterationId)
                .orElseThrow(() -> new RuntimeException("Alteration not found"));
        
        alteration.setStatus(Alteration.AlterationStatus.valueOf(status));
        Alteration saved = alterationRepository.save(alteration);
        return mapToDTO(saved);
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
