package com.staffalteration.service;

import com.staffalteration.dto.StaffDTO;
import com.staffalteration.entity.Department;
import com.staffalteration.entity.Role;
import com.staffalteration.entity.Staff;
import com.staffalteration.entity.User;
import com.staffalteration.repository.DepartmentRepository;
import com.staffalteration.repository.RoleRepository;
import com.staffalteration.repository.StaffRepository;
import com.staffalteration.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class StaffService {
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public StaffDTO createStaff(StaffDTO staffDTO, String password) {
        log.info("Creating staff: {}", staffDTO.getStaffId());
        
        // Create User account
        Role staffRole = roleRepository.findByRoleType(Role.RoleType.STAFF)
                .orElseThrow(() -> new RuntimeException("STAFF role not found"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(staffRole);
        
        User user = User.builder()
                .username(staffDTO.getStaffId())
                .email(staffDTO.getEmail())
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .roles(roles)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Create Staff record
        Department department = departmentRepository.findByDepartmentCode(staffDTO.getDepartmentCode())
                .orElseThrow(() -> new RuntimeException("Department not found"));
        
        Staff staff = Staff.builder()
                .staffId(staffDTO.getStaffId())
                .firstName(staffDTO.getFirstName())
                .lastName(staffDTO.getLastName())
                .email(staffDTO.getEmail())
                .phoneNumber(staffDTO.getPhoneNumber())
                .department(department)
                .user(savedUser)
                .status(Staff.StaffStatus.ACTIVE)
                .build();
        
        Staff savedStaff = staffRepository.save(staff);
        return mapToDTO(savedStaff);
    }
    
    public StaffDTO getStaffByStaffId(String staffId) {
        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        return mapToDTO(staff);
    }
    
    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<StaffDTO> getStaffByDepartment(String departmentCode) {
        Department department = departmentRepository.findByDepartmentCode(departmentCode)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        
        return staffRepository.findByDepartmentId(department.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public StaffDTO updateStaff(String staffId, StaffDTO staffDTO) {
        log.info("Updating staff: {}", staffId);
        
        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        if (staffDTO.getFirstName() != null) {
            staff.setFirstName(staffDTO.getFirstName());
        }
        
        if (staffDTO.getLastName() != null) {
            staff.setLastName(staffDTO.getLastName());
        }
        
        if (staffDTO.getPhoneNumber() != null) {
            staff.setPhoneNumber(staffDTO.getPhoneNumber());
        }
        
        if (staffDTO.getStatus() != null) {
            staff.setStatus(Staff.StaffStatus.valueOf(staffDTO.getStatus()));
        }
        
        Staff updatedStaff = staffRepository.save(staff);
        return mapToDTO(updatedStaff);
    }
    
    private StaffDTO mapToDTO(Staff staff) {
        return StaffDTO.builder()
                .id(staff.getId())
                .staffId(staff.getStaffId())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .email(staff.getEmail())
                .phoneNumber(staff.getPhoneNumber())
                .departmentCode(staff.getDepartment().getDepartmentCode())
                .status(staff.getStatus().toString())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
