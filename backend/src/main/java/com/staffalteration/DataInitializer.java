package com.staffalteration;

import com.staffalteration.entity.*;
import com.staffalteration.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private ClassRoomRepository classRoomRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private TimetableRepository timetableRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Starting data initialization...");
            if (roleRepository.count() == 0) {
                log.info("Database is empty, initializing...");
                initializeRoles();
                initializeDepartments();
                initializeSubjects();
                initializeClasses();
                initializeStaff();
                initializeTimetables();
                log.info("✓ Data initialization completed successfully!");
            } else {
                log.info("Database already has data, skipping initialization");
            }
        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
            log.warn("Data initialization failed - continuing with empty database. You can manually initialize data later.");
        }
    }
    
    private void initializeRoles() {
        log.info("Initializing roles...");
        
        Role staffRole = new Role(null, Role.RoleType.STAFF, "Staff member role");
        Role hodRole = new Role(null, Role.RoleType.HOD, "Head of Department role");
        Role deanRole = new Role(null, Role.RoleType.DEAN, "Dean role");
        Role adminRole = new Role(null, Role.RoleType.ADMIN, "Administrator role");
        
        roleRepository.save(staffRole);
        roleRepository.save(hodRole);
        roleRepository.save(deanRole);
        roleRepository.save(adminRole);
        
        log.info("Roles initialized: 4 roles created");
    }
    
    private void initializeDepartments() {
        log.info("Initializing departments...");
        
        Department csDept = new Department(null, "CS", "Computer Science", "Computer Science Department");
        Department itDept = new Department(null, "IT", "Information Technology", "Information Technology Department");
        
        departmentRepository.save(csDept);
        departmentRepository.save(itDept);
        
        log.info("Departments initialized: CS, IT");
    }
    
    private void initializeSubjects() {
        log.info("Initializing subjects...");
        
        Department csDept = departmentRepository.findByDepartmentCode("CS").get();
        Department itDept = departmentRepository.findByDepartmentCode("IT").get();
        
        Subject java = new Subject(null, "JAVA", "Java Programming", csDept);
        Subject python = new Subject(null, "PY", "Python Programming", csDept);
        Subject webdev = new Subject(null, "WEB", "Web Development", itDept);
        Subject database = new Subject(null, "DB", "Database Systems", itDept);
        
        subjectRepository.save(java);
        subjectRepository.save(python);
        subjectRepository.save(webdev);
        subjectRepository.save(database);
        
        log.info("Subjects initialized: 4 subjects created");
    }
    
    private void initializeClasses() {
        log.info("Initializing classes...");
        
        Department csDept = departmentRepository.findByDepartmentCode("CS").get();
        Department itDept = departmentRepository.findByDepartmentCode("IT").get();
        
        ClassRoom cs1 = new ClassRoom(null, "CS1", "CS - Semester 1", csDept);
        ClassRoom cs2 = new ClassRoom(null, "CS2", "CS - Semester 2", csDept);
        ClassRoom it1 = new ClassRoom(null, "IT1", "IT - Semester 1", itDept);
        ClassRoom it2 = new ClassRoom(null, "IT2", "IT - Semester 2", itDept);
        
        classRoomRepository.save(cs1);
        classRoomRepository.save(cs2);
        classRoomRepository.save(it1);
        classRoomRepository.save(it2);
        
        log.info("Classes initialized: 4 classes created");
    }
    
    private void initializeStaff() {
        log.info("Initializing staff...");
        
        Role staffRole = roleRepository.findByRoleType(Role.RoleType.STAFF).get();
        Department csDept = departmentRepository.findByDepartmentCode("CS").get();
        Department itDept = departmentRepository.findByDepartmentCode("IT").get();
        
        String[] staffNames = {"Staff1", "Staff2", "Staff3", "Staff4", "Staff5"};
        Department[] depts = {csDept, csDept, itDept, itDept, csDept};
        
        for (int i = 0; i < staffNames.length; i++) {
            String staffId = staffNames[i];
            String email = staffId.toLowerCase() + "@college.edu";
            
            // Create User
            Set<Role> roles = new HashSet<>();
            roles.add(staffRole);
            
            User user = User.builder()
                    .username(staffId)
                    .email(email)
                    .password(passwordEncoder.encode("password123"))
                    .enabled(true)
                    .roles(roles)
                    .build();
            
            User savedUser = userRepository.save(user);
            
            // Create Staff
            Staff staff = Staff.builder()
                    .staffId(staffId)
                    .firstName("First" + i)
                    .lastName("Last" + i)
                    .email(email)
                    .phoneNumber("9876543210")
                    .department(depts[i])
                    .user(savedUser)
                    .status(Staff.StaffStatus.ACTIVE)
                    .build();
            
            staffRepository.save(staff);
        }
        
        log.info("Staff initialized: 5 staff members created");
    }
    
    private void initializeTimetables() {
        log.info("Initializing timetables...");
        
        Department csDept = departmentRepository.findByDepartmentCode("CS").get();
        Department itDept = departmentRepository.findByDepartmentCode("IT").get();
        
        Subject javaSubj = subjectRepository.findBySubjectCode("JAVA").get();
        Subject pythonSubj = subjectRepository.findBySubjectCode("PY").get();
        Subject webdevSubj = subjectRepository.findBySubjectCode("WEB").get();
        
        ClassRoom cs1 = classRoomRepository.findByClassCode("CS1").get();
        ClassRoom it1 = classRoomRepository.findByClassCode("IT1").get();
        
        // Get staff
        java.util.List<Staff> staffList = staffRepository.findAll();
        
        if (staffList.size() >= 3) {
            // Create sample timetables
            Timetable t1 = new Timetable(null, staffList.get(0), javaSubj, cs1, 1, 1, null, null);
            Timetable t2 = new Timetable(null, staffList.get(1), pythonSubj, cs1, 1, 2, null, null);
            Timetable t3 = new Timetable(null, staffList.get(2), webdevSubj, it1, 1, 3, null, null);
            
            timetableRepository.save(t1);
            timetableRepository.save(t2);
            timetableRepository.save(t3);
            
            log.info("Timetables initialized: 3 timetables created");
        }
        
        // Create sample attendance records
        for (Staff staff : staffList) {
            Attendance att = Attendance.builder()
                    .staff(staff)
                    .attendanceDate(LocalDate.now())
                    .status(Attendance.AttendanceStatus.PRESENT)
                    .dayType(Attendance.DayType.FULL_DAY)
                    .remarks("Daily attendance")
                    .build();
            attendanceRepository.save(att);
        }
        
        log.info("Attendance records initialized");
    }
}
