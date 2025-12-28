package com.staffalteration.service;

import com.staffalteration.dto.TimetableDTO;
import com.staffalteration.entity.ClassRoom;
import com.staffalteration.entity.Staff;
import com.staffalteration.entity.Subject;
import com.staffalteration.entity.Timetable;
import com.staffalteration.repository.ClassRoomRepository;
import com.staffalteration.repository.StaffRepository;
import com.staffalteration.repository.SubjectRepository;
import com.staffalteration.repository.TimetableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class TimetableService {
    
    @Autowired
    private TimetableRepository timetableRepository;
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private ClassRoomRepository classRoomRepository;
    
    public TimetableDTO createTimetable(TimetableDTO timetableDTO) {
        log.info("Creating timetable for staff: {}", timetableDTO.getStaffId());
        
        Staff staff = staffRepository.findByStaffId(timetableDTO.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        Subject subject = subjectRepository.findBySubjectCode(timetableDTO.getSubjectCode())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        
        ClassRoom classRoom = classRoomRepository.findByClassCode(timetableDTO.getClassCode())
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        Timetable timetable = Timetable.builder()
                .staff(staff)
                .subject(subject)
                .classRoom(classRoom)
                .dayOrder(timetableDTO.getDayOrder())
                .periodNumber(timetableDTO.getPeriodNumber())
                .build();
        
        Timetable savedTimetable = timetableRepository.save(timetable);
        return mapToDTO(savedTimetable);
    }
    
    public TimetableDTO updateTimetable(Long timetableId, TimetableDTO timetableDTO) {
        log.info("Updating timetable: {}", timetableId);
        
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new RuntimeException("Timetable not found"));
        
        if (timetableDTO.getStaffId() != null) {
            Staff staff = staffRepository.findByStaffId(timetableDTO.getStaffId())
                    .orElseThrow(() -> new RuntimeException("Staff not found"));
            timetable.setStaff(staff);
        }
        
        if (timetableDTO.getSubjectCode() != null) {
            Subject subject = subjectRepository.findBySubjectCode(timetableDTO.getSubjectCode())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            timetable.setSubject(subject);
        }
        
        if (timetableDTO.getClassCode() != null) {
            ClassRoom classRoom = classRoomRepository.findByClassCode(timetableDTO.getClassCode())
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            timetable.setClassRoom(classRoom);
        }
        
        if (timetableDTO.getDayOrder() != null) {
            timetable.setDayOrder(timetableDTO.getDayOrder());
        }
        
        if (timetableDTO.getPeriodNumber() != null) {
            timetable.setPeriodNumber(timetableDTO.getPeriodNumber());
        }
        
        Timetable updatedTimetable = timetableRepository.save(timetable);
        return mapToDTO(updatedTimetable);
    }
    
    public List<TimetableDTO> getStaffTimetable(String staffId) {
        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        return timetableRepository.findByStaffId(staff.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TimetableDTO> getTimetableByClassAndPeriod(String classCode, Integer dayOrder, Integer periodNumber) {
        ClassRoom classRoom = classRoomRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        return timetableRepository.findByDayOrderAndPeriodNumber(dayOrder, periodNumber).stream()
                .filter(t -> t.getClassRoom().getId().equals(classRoom.getId()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public void deleteTimetable(Long timetableId) {
        log.info("Deleting timetable: {}", timetableId);
        timetableRepository.deleteById(timetableId);
    }
    
    private TimetableDTO mapToDTO(Timetable timetable) {
        return TimetableDTO.builder()
                .id(timetable.getId())
                .staffId(timetable.getStaff().getStaffId())
                .subjectCode(timetable.getSubject().getSubjectCode())
                .classCode(timetable.getClassRoom().getClassCode())
                .dayOrder(timetable.getDayOrder())
                .periodNumber(timetable.getPeriodNumber())
                .createdAt(timetable.getCreatedAt())
                .updatedAt(timetable.getUpdatedAt())
                .build();
    }
}
