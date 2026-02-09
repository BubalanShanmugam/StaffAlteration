package com.staffalteration.service;

import com.staffalteration.dto.ClassRoomDTO;
import com.staffalteration.dto.CreateClassDTO;
import com.staffalteration.entity.ClassRoom;
import com.staffalteration.entity.Department;
import com.staffalteration.repository.ClassRoomRepository;
import com.staffalteration.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ClassManagementService {
    
    @Autowired
    private ClassRoomRepository classRoomRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    public ClassRoomDTO createClass(CreateClassDTO request) {
        log.info("Creating new class: {}", request.getClassCode());
        
        // Check if class already exists
        if (classRoomRepository.findByClassCode(request.getClassCode()).isPresent()) {
            throw new RuntimeException("Class code already exists");
        }
        
        // Get department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
        
        // Create new class
        ClassRoom classRoom = ClassRoom.builder()
                .classCode(request.getClassCode())
                .className(request.getClassName())
                .department(department)
                .build();
        
        ClassRoom savedClass = classRoomRepository.save(classRoom);
        return mapToDTO(savedClass);
    }
    
    public List<ClassRoomDTO> getAllClasses() {
        log.info("Fetching all classes");
        return classRoomRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public ClassRoomDTO getClassByCode(String classCode) {
        ClassRoom classRoom = classRoomRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        return mapToDTO(classRoom);
    }
    
    public ClassRoomDTO updateClass(Long classId, CreateClassDTO request) {
        log.info("Updating class: {}", classId);
        
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        if (request.getClassName() != null) {
            classRoom.setClassName(request.getClassName());
        }
        
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            classRoom.setDepartment(department);
        }
        
        ClassRoom updatedClass = classRoomRepository.save(classRoom);
        return mapToDTO(updatedClass);
    }
    
    public void deleteClass(Long classId) {
        log.info("Deleting class: {}", classId);
        classRoomRepository.deleteById(classId);
    }
    
    private ClassRoomDTO mapToDTO(ClassRoom classRoom) {
        return ClassRoomDTO.builder()
                .id(classRoom.getId())
                .classCode(classRoom.getClassCode())
                .className(classRoom.getClassName())
                .departmentId(classRoom.getDepartment().getId())
                .departmentCode(classRoom.getDepartment().getDepartmentCode())
                .departmentName(classRoom.getDepartment().getDepartmentName())
                .build();
    }
}
