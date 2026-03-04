package com.staffalteration.service;

import com.staffalteration.dto.CreateTimetableDTO;
import com.staffalteration.dto.TimetableTemplateDTO;
import com.staffalteration.entity.*;
import com.staffalteration.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimetableTemplateService {
    
    private final TimetableTemplateRepository timetableTemplateRepository;
    private final StaffRepository staffRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TimetableRepository timetableRepository;
    
    /**
     * Create a new timetable entry
     * Only STAFF and HOD can create timetables
     */
    @Transactional
    public TimetableTemplateDTO createTimetable(CreateTimetableDTO request, String currentUserId) {
        log.info("Creating timetable for class: {}, day: {}, period: {}", 
                 request.getClassCode(), request.getDayOrder(), request.getPeriodNumber());
        
        // Validate input
        validateTimetableInput(request);
        
        // Check if slot is already occupied
        if (isSlotOccupied(request.getClassCode(), request.getDayOrder(), request.getPeriodNumber())) {
            log.warn("Slot already occupied for class: {}, day: {}, period: {}", 
                     request.getClassCode(), request.getDayOrder(), request.getPeriodNumber());
            throw new RuntimeException("This slot is already assigned. Please choose another time.");
        }
        
        // Get entities
        Staff staff = staffRepository.findByStaffId(request.getStaffId())
            .orElseThrow(() -> new RuntimeException("Staff not found: " + request.getStaffId()));
        
        Subject subject = subjectRepository.findBySubjectCode(request.getSubjectCode())
            .orElseThrow(() -> new RuntimeException("Subject not found: " + request.getSubjectCode()));
        
        User createdByUser = userRepository.findById(Long.parseLong(currentUserId))
            .orElseThrow(() -> new RuntimeException("User not found: " + currentUserId));
        
        // Check if class exists
        classRoomRepository.findByClassCode(request.getClassCode())
            .orElseThrow(() -> new RuntimeException("Class not found: " + request.getClassCode()));
        
        // Create timetable entry
        TimetableTemplate timetable = TimetableTemplate.builder()
            .templateName(request.getTemplateName())
            .classCode(request.getClassCode())
            .dayOrder(request.getDayOrder())
            .periodNumber(request.getPeriodNumber())
            .subject(subject)
            .assignedStaff(staff)
            .createdBy(createdByUser)
            .createdAt(LocalDateTime.now())
            .status(TimetableStatus.ACTIVE)
            .remarks(request.getRemarks())
            .build();
        
        TimetableTemplate saved = timetableTemplateRepository.save(timetable);
        log.info("Timetable created successfully with ID: {}", saved.getId());

        // Keep timetable table in sync so the alteration algorithm finds the slot
        syncTimetableFromTemplate(saved);

        return mapToDTO(saved);
    }
    
    /**
     * Get all timetables for a class
     */
    @Transactional(readOnly = true)
    public List<TimetableTemplateDTO> getTimetablesByClass(String classCode) {
        log.info("Fetching timetables for class: {}", classCode);
        return timetableTemplateRepository.findByClassCode(classCode)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get active timetables for a class (ordered by day and period)
     */
    @Transactional(readOnly = true)
    public List<TimetableTemplateDTO> getActiveTimetablesByClass(String classCode) {
        log.info("Fetching active timetables for class: {}", classCode);
        return timetableTemplateRepository.findActiveByClass(classCode)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all timetables assigned to a staff member
     */
    @Transactional(readOnly = true)
    public List<TimetableTemplateDTO> getTimetablesByStaff(String staffId) {
        log.info("Fetching timetables for staff: {}", staffId);
        return timetableTemplateRepository.findByStaffId(staffId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get timetable for a specific slot
     */
    @Transactional(readOnly = true)
    public TimetableTemplateDTO getTimetableBySlot(String classCode, Integer dayOrder, Integer periodNumber) {
        log.info("Fetching timetable for class: {}, day: {}, period: {}", classCode, dayOrder, periodNumber);
        return timetableTemplateRepository.findByClassCodeAndDayOrderAndPeriodNumber(classCode, dayOrder, periodNumber)
            .map(this::mapToDTO)
            .orElseThrow(() -> new RuntimeException("No timetable found for this slot"));
    }
    
    /**
     * Update timetable
     */
    @Transactional
    public TimetableTemplateDTO updateTimetable(Long timetableId, CreateTimetableDTO request) {
        log.info("Updating timetable ID: {}", timetableId);
        
        TimetableTemplate timetable = timetableTemplateRepository.findById(timetableId)
            .orElseThrow(() -> new RuntimeException("Timetable not found"));
        
        // Check if new slot is available (if changing slot)
        if (!timetable.getDayOrder().equals(request.getDayOrder()) || 
            !timetable.getPeriodNumber().equals(request.getPeriodNumber())) {
            if (isSlotOccupied(request.getClassCode(), request.getDayOrder(), request.getPeriodNumber())) {
                throw new RuntimeException("New slot is already occupied");
            }
        }
        
        // Update fields
        timetable.setTemplateName(request.getTemplateName());
        timetable.setDayOrder(request.getDayOrder());
        timetable.setPeriodNumber(request.getPeriodNumber());
        timetable.setRemarks(request.getRemarks());
        timetable.setUpdatedAt(LocalDateTime.now());
        
        // Update staff if changed
        if (!timetable.getAssignedStaff().getStaffId().equals(request.getStaffId())) {
            Staff newStaff = staffRepository.findByStaffId(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));
            timetable.setAssignedStaff(newStaff);
        }
        
        TimetableTemplate updated = timetableTemplateRepository.save(timetable);
        log.info("Timetable updated successfully");

        // Re-sync the timetable row
        syncTimetableFromTemplate(updated);

        return mapToDTO(updated);
    }
    
    /**
     * Delete timetable
     */
    @Transactional
    public void deleteTimetable(Long timetableId) {
        log.info("Deleting timetable ID: {}", timetableId);
        
        TimetableTemplate timetable = timetableTemplateRepository.findById(timetableId)
            .orElseThrow(() -> new RuntimeException("Timetable not found"));
        
        timetableTemplateRepository.delete(timetable);
        log.info("Timetable deleted successfully");
    }
    
    /**
     * Change timetable status
     */
    @Transactional
    public TimetableTemplateDTO changeTimetableStatus(Long timetableId, TimetableStatus newStatus) {
        log.info("Changing timetable ID: {} status to: {}", timetableId, newStatus);
        
        TimetableTemplate timetable = timetableTemplateRepository.findById(timetableId)
            .orElseThrow(() -> new RuntimeException("Timetable not found"));
        
        timetable.setStatus(newStatus);
        timetable.setUpdatedAt(LocalDateTime.now());
        
        TimetableTemplate updated = timetableTemplateRepository.save(timetable);

        // If activating, ensure timetable row exists
        if (newStatus == TimetableStatus.ACTIVE) {
            syncTimetableFromTemplate(updated);
        }

        return mapToDTO(updated);
    }
    
    /**
     * Get staff workload for a specific day
     */
    @Transactional(readOnly = true)
    public long getStaffWorkloadByDay(String staffId, Integer dayOrder) {
        return timetableTemplateRepository.getStaffWorkloadByDay(staffId, dayOrder);
    }
    
    /**
     * Check if a slot is already occupied
     */
    private boolean isSlotOccupied(String classCode, Integer dayOrder, Integer periodNumber) {
        return timetableTemplateRepository.countBySlot(classCode, dayOrder, periodNumber) > 0;
    }
    
    /**
     * Validate timetable input
     */
    private void validateTimetableInput(CreateTimetableDTO request) {
        if (request.getTemplateName() == null || request.getTemplateName().trim().isEmpty()) {
            throw new RuntimeException("Template name is required");
        }
        if (request.getClassCode() == null || request.getClassCode().trim().isEmpty()) {
            throw new RuntimeException("Class code is required");
        }
        if (request.getDayOrder() == null || request.getDayOrder() < 1 || request.getDayOrder() > 6) {
            throw new RuntimeException("Day order must be between 1 and 6");
        }
        if (request.getPeriodNumber() == null || request.getPeriodNumber() < 1 || request.getPeriodNumber() > 6) {
            throw new RuntimeException("Period number must be between 1 and 6");
        }
        if (request.getSubjectCode() == null || request.getSubjectCode().trim().isEmpty()) {
            throw new RuntimeException("Subject code is required");
        }
        if (request.getStaffId() == null || request.getStaffId().trim().isEmpty()) {
            throw new RuntimeException("Staff ID is required");
        }
    }
    
    /**
     * Ensure the legacy 'timetable' table has a row matching this template.
     * AlterationService queries the timetable table (via TimetableRepository); this method
     * keeps them in sync so that alterations are created correctly when attendance is marked.
     */
    private void syncTimetableFromTemplate(TimetableTemplate template) {
        if (template.getAssignedStaff() == null) return;
        try {
            Staff staff = template.getAssignedStaff();
            List<Timetable> existing = timetableRepository.findByStaffAndPeriod(
                    staff.getId(), template.getDayOrder(), template.getPeriodNumber());

            ClassRoom classRoom = classRoomRepository
                    .findByClassCode(template.getClassCode()).orElse(null);

            if (existing.isEmpty()) {
                Timetable timetable = Timetable.builder()
                        .staff(staff)
                        .subject(template.getSubject())
                        .classRoom(classRoom)
                        .dayOrder(template.getDayOrder())
                        .periodNumber(template.getPeriodNumber())
                        .build();
                timetableRepository.save(timetable);
                log.info("Synced timetable row for staff={}, day={}, period={}",
                         staff.getStaffId(), template.getDayOrder(), template.getPeriodNumber());
            } else {
                // Update subject / classRoom in case they changed
                Timetable timetable = existing.get(0);
                timetable.setSubject(template.getSubject());
                timetable.setClassRoom(classRoom);
                timetableRepository.save(timetable);
                log.info("Updated timetable row id={} for staff={}, day={}, period={}",
                         timetable.getId(), staff.getStaffId(), template.getDayOrder(), template.getPeriodNumber());
            }
        } catch (Exception e) {
            log.error("syncTimetableFromTemplate failed for template id={}: {}",
                      template.getId(), e.getMessage());
        }
    }

    /**
     * Map entity to DTO (with null safety for optional relations)
     */
    private TimetableTemplateDTO mapToDTO(TimetableTemplate timetable) {
        return TimetableTemplateDTO.builder()
            .id(timetable.getId())
            .templateName(timetable.getTemplateName())
            .classCode(timetable.getClassCode())
            .dayOrder(timetable.getDayOrder())
            .periodNumber(timetable.getPeriodNumber())
            .subjectCode(timetable.getSubject() != null ? timetable.getSubject().getSubjectCode() : "")
            .subjectName(timetable.getSubject() != null ? timetable.getSubject().getSubjectName() : "")
            .staffId(timetable.getAssignedStaff() != null ? timetable.getAssignedStaff().getStaffId() : "")
            .staffName(timetable.getAssignedStaff() != null ? 
                timetable.getAssignedStaff().getFirstName() + " " + timetable.getAssignedStaff().getLastName() : "")
            .createdBy(timetable.getCreatedBy() != null ? timetable.getCreatedBy().getUsername() : "")
            .createdAt(timetable.getCreatedAt())
            .updatedAt(timetable.getUpdatedAt())
            .status(timetable.getStatus() != null ? timetable.getStatus().toString() : "ACTIVE")
            .remarks(timetable.getRemarks())
            .build();
    }
}
