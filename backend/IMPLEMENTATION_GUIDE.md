# Staff Alteration System - Complete Backend Implementation

## 📋 Project Overview

A production-ready Spring Boot backend application that automatically assigns substitute staff when a staff member marks absent before college hours. The system uses a strict priority-driven alteration algorithm and provides real-time notifications.

## 🏗️ Complete Architecture

```
Staff Alteration System Backend
│
├── Controller Layer (REST APIs)
│   ├── AuthController              → Authentication/Login
│   ├── AttendanceController        → Attendance Management
│   ├── TimetableController         → Timetable Management
│   ├── AlterationController        → Alteration Processing
│   ├── NotificationController      → Notification Retrieval
│   └── StaffController             → Staff Management
│
├── Service Layer (Business Logic)
│   ├── AuthenticationService       → JWT & User Auth
│   ├── AttendanceService           → Absence Marking & Trigger
│   ├── TimetableService            → Schedule Management
│   ├── AlterationService           → Priority Algorithm ⭐
│   ├── StaffService                → Staff Operations
│   └── NotificationService         → Notification Management
│
├── Repository Layer (Data Access)
│   ├── UserRepository              → User Queries
│   ├── RoleRepository              → Role Queries
│   ├── DepartmentRepository        → Department Queries
│   ├── SubjectRepository           → Subject Queries
│   ├── ClassRoomRepository         → Class Queries
│   ├── StaffRepository             → Staff Queries
│   ├── TimetableRepository         → Timetable Queries
│   ├── AttendanceRepository        → Attendance Queries
│   ├── AlterationRepository        → Alteration Queries
│   ├── LessonPlanRepository        → Lesson Plan Queries
│   ├── NotificationRepository      → Notification Queries
│   └── WorkloadSummaryRepository   → Workload Queries
│
├── Entity Layer (JPA Models)
│   ├── User                        → User Account
│   ├── Role                        → Role Definition
│   ├── Department                  → Department
│   ├── Subject                     → Subject
│   ├── ClassRoom                   → Class
│   ├── Staff                       → Staff Member
│   ├── Timetable                   → Schedule Entry
│   ├── Attendance                  → Attendance Record
│   ├── Alteration                  → Substitution Record
│   ├── LessonPlan                  → Lesson Material
│   ├── Notification                → Notification
│   └── WorkloadSummary             → Workload Tracking
│
├── Security Layer
│   ├── JwtTokenProvider            → JWT Generation & Validation
│   ├── JwtAuthenticationFilter     → Request Filter
│   ├── CustomUserDetailsService    → User Loading
│   ├── SecurityConfig              → Spring Security Setup
│   └── CorsConfig                  → CORS Configuration
│
├── Configuration Layer
│   ├── WebSocketConfig             → Real-time Notifications
│   ├── CorsConfig                  → Cross-Origin Setup
│   └── SecurityConfig              → Authentication Setup
│
├── Support Layer
│   ├── DTOs                        → Data Transfer Objects
│   ├── SMSNotificationProvider     → SMS Integration
│   ├── DateUtil                    → Date Utilities
│   └── Constants                   → Application Constants
│
└── Database
    ├── PostgreSQL 15+
    └── Flyway Migrations
```

## 🔐 Security Features

### Authentication
- **JWT Tokens**: 24-hour expiration
- **BCrypt Password Encoding**: Secure password storage
- **Role-Based Access Control**: 4 roles (STAFF, HOD, DEAN, ADMIN)
- **Token Validation Filter**: Every request authenticated

### Authorization
```
/api/staff/**               → STAFF, HOD, ADMIN
/api/attendance/**         → STAFF, HOD
/api/timetable/**          → STAFF, HOD
/api/alteration/**         → STAFF, HOD, DEAN
/api/notification/**       → STAFF
/api/dashboard/**          → STAFF, HOD, DEAN, ADMIN
```

## 🎯 Alteration Algorithm (Priority-Driven)

The system implements a strict 6-priority algorithm:

```
Priority 1: Staff must be present today
  ↓ (Filter only present staff)
  
Priority 2: Staff already teaches the same class
  ↓ (If found, use selectBestStaff)
  
Priority 3: Staff with least hours that day
  ↓ (Score: hoursToday * 1000)
  
Priority 4: Staff with no previous/next period clash
  ↓ (Score +500 if clash exists)
  
Priority 5: Prefer same subject over other subjects
  ↓ (Score +100 if different subject)
  
Priority 6: Tie-breaker - least weekly workload
  ↓ (Score += weeklyWorkload)
  
Result: Staff with lowest score selected
```

### Algorithm Properties
- **Deterministic**: Same inputs always produce same output
- **Conflict-Free**: Avoids double allocations
- **Workload-Aware**: Distributes teaching load evenly
- **Efficient**: O(n) complexity

## 📊 Database Schema

### Core Tables

**user_account**
```sql
id (PK) | username (UNIQUE) | password | email (UNIQUE) | enabled | created_at | updated_at
```

**role**
```sql
id (PK) | role_type (UNIQUE: STAFF, HOD, DEAN, ADMIN) | description
```

**department**
```sql
id (PK) | department_code (UNIQUE) | department_name | description
```

**subject**
```sql
id (PK) | subject_code (UNIQUE) | subject_name | department_id (FK)
```

**class**
```sql
id (PK) | class_code (UNIQUE) | class_name | department_id (FK)
```

**staff**
```sql
id (PK) | staff_id (UNIQUE) | first_name | last_name | email (UNIQUE)
        | phone_number | department_id (FK) | user_id (FK) | status
```

**timetable**
```sql
id (PK) | staff_id (FK) | subject_id (FK) | class_id (FK)
        | day_order (1-6) | period_number (1-6)
```

**attendance**
```sql
id (PK) | staff_id (FK) | attendance_date | status (PRESENT/ABSENT/LEAVE)
        | remarks | created_at | updated_at
        | UNIQUE(staff_id, attendance_date)
```

**alteration**
```sql
id (PK) | timetable_id (FK) | original_staff_id (FK) | substitute_staff_id (FK)
        | alteration_date | status (ASSIGNED/ACKNOWLEDGED/COMPLETED/CANCELLED)
        | lesson_plan_id (FK) | remarks | created_at | updated_at
```

**notification**
```sql
id (PK) | staff_id (FK) | title | message | notification_type
        | is_read | alteration_id (FK) | created_at | read_at
```

**workload_summary**
```sql
id (PK) | staff_id (FK) | workload_date | total_hours
        | regular_hours | alteration_hours | weekly_total
```

**lesson_plan**
```sql
id (PK) | file_path | notes | file_type | file_size | created_at
```

### Indexes (Performance)
- `idx_staff_user_id` on staff(user_id)
- `idx_timetable_staff_id` on timetable(staff_id)
- `idx_timetable_day_period` on timetable(day_order, period_number)
- `idx_attendance_staff_date` on attendance(staff_id, attendance_date)
- `idx_alteration_date` on alteration(alteration_date)
- `idx_alteration_original_staff` on alteration(original_staff_id)
- `idx_alteration_substitute_staff` on alteration(substitute_staff_id)
- `idx_notification_staff_id` on notification(staff_id)
- `idx_workload_summary_staff_date` on workload_summary(staff_id, workload_date)

## 🌐 REST API Endpoints

### Authentication
```
POST   /api/auth/login                      → Login
GET    /api/auth/user/{userId}             → Get user by ID
GET    /api/auth/user/username/{username}  → Get user by username
```

### Attendance Management
```
POST   /api/attendance/mark                → Mark attendance (triggers alteration)
GET    /api/attendance/{attendanceId}      → Get attendance record
GET    /api/attendance/staff/{staffId}     → Get staff attendance history
GET    /api/attendance/date/{date}         → Get attendance by date
GET    /api/attendance/absent/{date}       → Get absent staff on date
```

### Timetable Management
```
POST   /api/timetable/create                          → Create timetable entry
PUT    /api/timetable/update/{timetableId}           → Update timetable
GET    /api/timetable/staff/{staffId}                → Get staff schedule
GET    /api/timetable/class/{classCode}/{dayOrder}/{periodNumber}  → Get class schedule
DELETE /api/timetable/{timetableId}                  → Delete timetable entry
```

### Alteration Processing
```
GET    /api/alteration/date/{date}        → Get alterations on date
GET    /api/alteration/staff/{staffId}    → Get staff alterations
PUT    /api/alteration/{alterationId}/status  → Update alteration status
```

### Notification Delivery
```
GET    /api/notification/staff/{staffId}              → Get notifications
GET    /api/notification/staff/{staffId}/unread       → Get unread notifications
GET    /api/notification/staff/{staffId}/unread-count → Get unread count
PUT    /api/notification/{notificationId}/read       → Mark notification as read
```

### Staff Management
```
POST   /api/staff/create                              → Create staff (with password)
GET    /api/staff/{staffId}                          → Get staff by ID
GET    /api/staff                                     → Get all staff
GET    /api/staff/department/{departmentCode}        → Get staff by department
PUT    /api/staff/{staffId}                          → Update staff info
```

## 📝 Request/Response Examples

### Login
```json
POST /api/auth/login
{
  "username": "Staff1",
  "password": "password123"
}

Response (200):
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGc...",
    "type": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "Staff1",
      "email": "staff1@college.edu",
      "roles": ["STAFF"]
    }
  },
  "timestamp": 1671267600000
}
```

### Mark Absence (Triggers Alteration)
```json
POST /api/attendance/mark
{
  "staffId": "Staff1",
  "attendanceDate": "2024-12-18",
  "status": "ABSENT",
  "remarks": "Medical emergency"
}

Response (200):
{
  "code": 200,
  "message": "Attendance marked successfully",
  "data": {
    "id": 10,
    "staffId": "Staff1",
    "attendanceDate": "2024-12-18",
    "status": "ABSENT",
    "remarks": "Medical emergency"
  }
}

Trigger: System automatically processes alteration algorithm for all of Staff1's classes
```

### Create Timetable
```json
POST /api/timetable/create
{
  "staffId": "Staff1",
  "subjectCode": "JAVA",
  "classCode": "CS1",
  "dayOrder": 1,
  "periodNumber": 1
}

Response (200):
{
  "code": 200,
  "message": "Timetable created successfully",
  "data": {
    "id": 5,
    "staffId": "Staff1",
    "subjectCode": "JAVA",
    "classCode": "CS1",
    "dayOrder": 1,
    "periodNumber": 1
  }
}
```

### Get Unread Notifications
```json
GET /api/notification/staff/Staff2/unread

Response (200):
{
  "code": 200,
  "message": "Unread notifications retrieved",
  "data": [
    {
      "id": 1,
      "staffId": "Staff2",
      "title": "New Class Assigned",
      "message": "You have been assigned to teach CS1 (Period 1, Day 1) on behalf of Staff1",
      "notificationType": "ALTERATION_ASSIGNED",
      "isRead": false,
      "alterationId": 5,
      "createdAt": "2024-12-18T09:30:00"
    }
  ]
}
```

## 🚀 Build & Deployment

### Build Project
```bash
cd backend
./gradlew clean build
```

### Run Development
```bash
./gradlew bootRun
```

Server: `http://localhost:8080/api`

### Run Tests
```bash
./gradlew test
```

### Docker (Optional)
```dockerfile
FROM openjdk:17-slim
COPY build/libs/staff-alteration-system-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 🗄️ Database Setup

### PostgreSQL Local
```bash
# Install PostgreSQL
# Create database
createdb staff_alteration

# Environment variables (.env)
DATABASE_URL=jdbc:postgresql://localhost:5432/staff_alteration
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### PostgreSQL Supabase (Production)
```bash
# Update application.yml with Supabase credentials
DATABASE_URL=jdbc:postgresql://db.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=your-supabase-password
```

## 📦 Dependencies

```gradle
// Core
spring-boot-starter-web: REST APIs
spring-boot-starter-data-jpa: Database ORM
spring-boot-starter-security: Authentication
spring-boot-starter-websocket: Real-time messaging

// Database
postgresql: PostgreSQL driver (v42.7.1)
flyway-core: Database migrations

// Security
jjwt: JWT handling (v0.12.3)
bcrypt: Password encoding

// Utilities
lombok: Boilerplate reduction
jackson: JSON processing
commons-lang3: Common utilities
```

## 🧪 Default Seed Data

### Staff Members (5)
```
Staff1: First0 Last0, CS Dept, staff1@college.edu
Staff2: First1 Last1, CS Dept, staff2@college.edu
Staff3: First2 Last2, IT Dept, staff3@college.edu
Staff4: First3 Last3, IT Dept, staff4@college.edu
Staff5: First4 Last4, CS Dept, staff5@college.edu
```

### Departments (2)
```
CS: Computer Science
IT: Information Technology
```

### Subjects (4)
```
JAVA: Java Programming (CS)
PY: Python Programming (CS)
WEB: Web Development (IT)
DB: Database Systems (IT)
```

### Classes (4)
```
CS1: CS - Semester 1
CS2: CS - Semester 2
IT1: IT - Semester 1
IT2: IT - Semester 2
```

### Sample Timetables (3)
```
Staff1 teaches JAVA to CS1 on Day 1, Period 1
Staff2 teaches PY to CS1 on Day 1, Period 2
Staff3 teaches WEB to IT1 on Day 1, Period 3
```

**Login**: `Staff1` / `password123`

## 🔄 Workflow

1. **Attendance Marking** (Before 9:00 AM)
   - Staff marks absent via UI
   - Triggers `AttendanceService.markAttendance()`

2. **Alteration Processing** (Automatic)
   - `AlterationService.processAlteration()` invoked
   - Algorithm evaluates all candidates
   - Best substitute selected
   - Alteration record created

3. **Notification Delivery**
   - In-app notification sent to substitute staff
   - SMS sent (mock implementation)
   - WebSocket broadcasts real-time update

4. **Acknowledgment**
   - Substitute staff receives notification
   - Can mark as read or acknowledge

5. **Completion**
   - Class conducted by substitute
   - Alteration marked completed

## 📊 Data Flow Diagram

```
Attendance Marking
    ↓
Is Staff Absent?
    ↓ YES
Get All Timetables for Staff
    ↓
For Each Timetable:
    ├─ Get All Active Staff
    ├─ Filter Present Staff
    ├─ Remove Already Allocated
    ├─ Apply Priority Rules
    │   ├─ Priority 1: Present
    │   ├─ Priority 2: Same Class
    │   ├─ Priority 3: Least Hours
    │   ├─ Priority 4: No Clash
    │   ├─ Priority 5: Same Subject
    │   └─ Priority 6: Workload
    ├─ Select Best Staff (Lowest Score)
    ├─ Create Alteration Record
    ├─ Update Workload Summary
    └─ Send Notifications (App + SMS)

Complete ✓
```

## 📞 Support & Maintenance

### Troubleshooting

**Database Connection Error**
```
Check: DATABASE_URL, username, password in application.yml
Verify: PostgreSQL service running
```

**JWT Token Invalid**
```
Ensure: JWT_SECRET environment variable set
Check: Token not expired (24 hours)
```

**WebSocket Connection Failed**
```
Verify: WebSocket endpoint accessible
Check: CORS origins configured
```

### Logging

```
Development: DEBUG level - logs/application.log
Production: INFO level - structured JSON logs
```

### Monitoring

```
JVM Metrics: Via Spring Boot Actuator
Database: Flyway migration logs
API: Request/Response logging
```

## 📝 Notes

- All timestamps are in UTC
- Database auto-migrates on startup
- Seed data created on first run
- CORS enabled for localhost:3000, 4200, 8080
- No frontend required for backend testing

## 🎓 Learning Resources

- Spring Boot: https://spring.io/projects/spring-boot
- JPA/Hibernate: https://hibernate.org
- PostgreSQL: https://www.postgresql.org
- JWT: https://jwt.io
- REST APIs: https://restfulapi.net

---

**Version**: 1.0.0
**Last Updated**: December 2024
**Status**: Production Ready ✓
