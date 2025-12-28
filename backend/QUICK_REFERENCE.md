# 📝 QUICK REFERENCE & COMPILATION GUIDE

## 🔨 Compilation Commands

### Build Project
```bash
# Full clean build
./gradlew clean build

# Build without tests
./gradlew clean build -x test

# Build and run
./gradlew bootRun

# Check dependencies
./gradlew dependencies
```

### Run Application
```bash
# Development (default profile)
./gradlew bootRun

# Production profile
./gradlew bootRun --args='--spring.profiles.active=prod'

# With custom port
./gradlew bootRun --args='--server.port=9090'
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests AuthControllerTest

# Run with coverage
./gradlew test jacocoTestReport
```

---

## 📂 Project Files Quick Reference

### Entry Point
- **StaffAlterationApplication.java** - Main Spring Boot class

### Core Components (Ordered by Dependency)
1. **Entity Layer** (`entity/`) - 12 JPA entities
2. **Repository Layer** (`repository/`) - 12 data access interfaces
3. **Service Layer** (`service/`) - 6 business logic services
4. **Controller Layer** (`controller/`) - 6 REST controllers
5. **Security Layer** (`security/`) - JWT & authentication
6. **Configuration Layer** (`config/`) - Spring configurations
7. **Support** (`util/`, `notification/`) - Utilities & providers

### Key Files by Purpose

**Authentication & Security**
```
security/JwtTokenProvider.java          - JWT token generation/validation
security/JwtAuthenticationFilter.java   - Request-level authentication
security/CustomUserDetailsService.java  - User loading service
config/SecurityConfig.java              - Spring Security configuration
```

**Alteration Algorithm** ⭐
```
service/AlterationService.java
├── processAlteration()           - Main orchestration
├── findSubstitute()              - Candidate filtering
├── selectBestStaff()             - Scoring and selection
├── calculateScore()              - 6-priority evaluation
└── [helper methods]              - Priority checks
```

**Business Services**
```
service/AuthenticationService.java      - Login & user auth
service/AttendanceService.java          - Attendance marking
service/TimetableService.java           - Schedule management
service/StaffService.java               - Staff operations
service/NotificationService.java        - Notification handling
```

**REST Controllers**
```
controller/AuthController.java          - Authentication endpoints
controller/AttendanceController.java    - Attendance endpoints
controller/TimetableController.java     - Timetable endpoints
controller/AlterationController.java    - Alteration endpoints
controller/NotificationController.java  - Notification endpoints
controller/StaffController.java         - Staff endpoints
```

**Data Access**
```
repository/UserRepository.java          - User queries
repository/StaffRepository.java         - Staff queries
repository/AttendanceRepository.java    - Attendance queries
repository/TimetableRepository.java     - Timetable queries
repository/AlterationRepository.java    - Alteration queries
[8 more repositories...]
```

**Configuration**
```
config/SecurityConfig.java              - Spring Security setup
config/CorsConfig.java                  - CORS policy
config/WebSocketConfig.java             - WebSocket configuration
application.yml                         - Application properties
```

**Database & Seed Data**
```
DataInitializer.java                    - Auto-create seed data
db/migration/V1__Create_Initial_Schema.sql - Database schema
```

---

## 🔍 Code Navigation Map

### To Understand Authentication Flow
```
AuthController.login()
    ↓
AuthenticationService.login()
    ↓
JwtTokenProvider.generateToken()
    ↓
Return token in AuthResponseDTO
```

### To Understand Alteration Flow
```
AttendanceController.markAttendance()
    ↓
AttendanceService.markAttendance()
    ↓
Check if ABSENT → triggerAlterationProcess()
    ↓
AlterationService.processAlteration() [FOR EACH TIMETABLE]
    ↓
findSubstitute()
    ├── Filter present staff
    ├── Check same class
    └── selectBestStaff() with 6-priority scoring
    ↓
Create Alteration record
    ↓
Update WorkloadSummary
    ↓
NotificationService.notifySubstituteStaff()
    ↓
Create & send Notification
```

### To Understand API Response Format
```
All endpoints return ApiResponseDTO:
{
  "code": HTTP_CODE,
  "message": "Description",
  "data": {ACTUAL_DATA},
  "timestamp": UNIX_TIMESTAMP
}
```

---

## 🗂️ File Checklist (50+ files)

### Controllers (6 files)
- [x] AuthController.java
- [x] AttendanceController.java
- [x] TimetableController.java
- [x] AlterationController.java
- [x] NotificationController.java
- [x] StaffController.java

### Services (6 files)
- [x] AuthenticationService.java
- [x] AttendanceService.java
- [x] TimetableService.java
- [x] AlterationService.java ⭐
- [x] StaffService.java
- [x] NotificationService.java

### Repositories (12 files)
- [x] UserRepository.java
- [x] RoleRepository.java
- [x] DepartmentRepository.java
- [x] SubjectRepository.java
- [x] ClassRoomRepository.java
- [x] StaffRepository.java
- [x] TimetableRepository.java
- [x] AttendanceRepository.java
- [x] AlterationRepository.java
- [x] LessonPlanRepository.java
- [x] NotificationRepository.java
- [x] WorkloadSummaryRepository.java

### Entities (12 files)
- [x] User.java
- [x] Role.java
- [x] Department.java
- [x] Subject.java
- [x] ClassRoom.java
- [x] Staff.java
- [x] Timetable.java
- [x] Attendance.java
- [x] Alteration.java
- [x] LessonPlan.java
- [x] Notification.java
- [x] WorkloadSummary.java

### DTOs (14 files)
- [x] UserDTO.java
- [x] AuthRequestDTO.java
- [x] AuthResponseDTO.java
- [x] StaffDTO.java
- [x] DepartmentDTO.java
- [x] SubjectDTO.java
- [x] ClassRoomDTO.java
- [x] TimetableDTO.java
- [x] AttendanceMarkDTO.java
- [x] AttendanceDTO.java
- [x] AlterationDTO.java
- [x] NotificationDTO.java
- [x] WorkloadSummaryDTO.java
- [x] ApiResponseDTO.java

### Security (3 files)
- [x] JwtTokenProvider.java
- [x] JwtAuthenticationFilter.java
- [x] CustomUserDetailsService.java

### Configuration (3 files)
- [x] SecurityConfig.java
- [x] CorsConfig.java
- [x] WebSocketConfig.java

### Support (3 files)
- [x] SMSNotificationProvider.java
- [x] DateUtil.java
- [x] Constants.java

### Initialization & Main (2 files)
- [x] StaffAlterationApplication.java
- [x] DataInitializer.java

### Build & Config (3 files)
- [x] build.gradle
- [x] settings.gradle
- [x] .gitignore

### Database (1 file)
- [x] V1__Create_Initial_Schema.sql

### Documentation (5 files)
- [x] README.md
- [x] QUICKSTART.md
- [x] IMPLEMENTATION_GUIDE.md
- [x] FILE_STRUCTURE.md
- [x] DELIVERY_SUMMARY.md

### Configuration Resources (1 file)
- [x] application.yml

---

## 🎯 Common Tasks

### Add New Endpoint
```
1. Create method in Controller
2. Create Service method if needed
3. Create/Update DTO
4. Add to appropriate Repository
5. Test in Postman
```

### Add New Entity
```
1. Create Entity class in entity/
2. Create Repository interface
3. Add Service methods
4. Create DTO
5. Update DataInitializer if needed
6. Create Flyway migration
```

### Debug Issue
```
1. Check logs in console
2. Verify database connection: test SELECT 1;
3. Check JWT token expiration
4. Verify CORS configuration
5. Review Spring Security filters
```

---

## 🔌 API Quick Test Commands

### Linux/Mac
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"Staff1","password":"password123"}'

# Mark Absence (replace TOKEN)
curl -X POST http://localhost:8080/api/attendance/mark \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "staffId":"Staff2",
    "attendanceDate":"2024-12-18",
    "status":"ABSENT",
    "remarks":"Sick leave"
  }'

# Get Notifications
curl -X GET http://localhost:8080/api/notification/staff/Staff2/unread \
  -H "Authorization: Bearer TOKEN"
```

### Windows PowerShell
```powershell
# Login
$json = @{
    username = "Staff1"
    password = "password123"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $json

# Mark Absence
$json = @{
    staffId = "Staff2"
    attendanceDate = "2024-12-18"
    status = "ABSENT"
    remarks = "Sick leave"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/attendance/mark" `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{"Authorization" = "Bearer TOKEN"} `
  -Body $json
```

---

## 📊 Database Query Examples

### View Staff Records
```sql
SELECT * FROM staff;
SELECT * FROM staff WHERE status = 'ACTIVE';
```

### View Today's Attendance
```sql
SELECT s.staff_id, a.status FROM attendance a
JOIN staff s ON a.staff_id = s.id
WHERE a.attendance_date = CURRENT_DATE;
```

### Check Alterations
```sql
SELECT * FROM alteration 
WHERE alteration_date = CURRENT_DATE
ORDER BY created_at DESC;
```

### View Workload
```sql
SELECT s.staff_id, w.total_hours, w.weekly_total
FROM workload_summary w
JOIN staff s ON w.staff_id = s.id
WHERE w.workload_date = CURRENT_DATE;
```

### Check Notifications
```sql
SELECT * FROM notification 
WHERE is_read = false
ORDER BY created_at DESC;
```

---

## 🚀 Deployment Checklist

### Pre-deployment
- [ ] All tests pass
- [ ] No compilation warnings
- [ ] Security review completed
- [ ] Performance tested
- [ ] Database backed up

### Deployment
- [ ] Build JAR: `./gradlew clean build`
- [ ] Set environment variables (JWT_SECRET, DB_PASSWORD)
- [ ] Configure database connection
- [ ] Deploy to server/cloud
- [ ] Run database migrations
- [ ] Verify application startup
- [ ] Test critical endpoints

### Post-deployment
- [ ] Monitor application logs
- [ ] Verify database backups
- [ ] Test all API endpoints
- [ ] Monitor system resources
- [ ] Set up alerting

---

## 📚 Documentation Index

| Document | Purpose |
|----------|---------|
| README.md | Project overview & quick setup |
| QUICKSTART.md | Step-by-step setup guide |
| IMPLEMENTATION_GUIDE.md | Complete technical documentation |
| FILE_STRUCTURE.md | Project file organization |
| DELIVERY_SUMMARY.md | Final delivery summary |
| This file | Quick reference & compilation |

---

## ⚠️ Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Port 8080 already in use | Change port in application.yml or kill process |
| Database connection error | Check PostgreSQL running, verify credentials |
| JWT token invalid | Token expired or JWT_SECRET mismatch |
| CORS error | Verify frontend URL in CorsConfig |
| 404 Not Found | Check endpoint path, verify controller mapping |
| 401 Unauthorized | Provide valid JWT token in Authorization header |
| 403 Forbidden | Check user roles, verify role-based authorization |

---

## 🎓 Learning Resources

**Spring Boot**
- Official: https://spring.io/projects/spring-boot
- Guides: https://spring.io/guides

**Spring Data JPA**
- Documentation: https://spring.io/projects/spring-data-jpa
- Hibernate: https://hibernate.org

**Spring Security**
- Documentation: https://spring.io/projects/spring-security
- JWT: https://jwt.io

**PostgreSQL**
- Official: https://www.postgresql.org
- Documentation: https://www.postgresql.org/docs/

---

## 💡 Pro Tips

1. **Enable Debug Logging**
   ```yaml
   logging:
     level:
       com.staffalteration: DEBUG
   ```

2. **Monitor Database Queries**
   ```yaml
   properties:
     hibernate:
       generate_statistics: true
   ```

3. **Use Postman Collections**
   - Import API requests for easier testing
   - Save environment variables
   - Automate test scenarios

4. **Performance Tuning**
   - Add JVM options: `-Xms512m -Xmx2g`
   - Configure connection pool size
   - Enable query caching

5. **Security Best Practices**
   - Rotate JWT secrets regularly
   - Enable HTTPS in production
   - Use strong database passwords
   - Implement rate limiting

---

## 📞 Support Contacts

For issues:
1. Check logs: `tail -f logs/application.log`
2. Review documentation files
3. Verify environment setup
4. Check database connectivity
5. Review code comments

---

**Last Updated**: December 18, 2024
**Status**: ✅ COMPLETE & PRODUCTION-READY
