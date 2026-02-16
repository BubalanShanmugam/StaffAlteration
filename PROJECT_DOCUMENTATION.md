# STAFF ALTERATION SYSTEM
## Complete Project Documentation

---

## ACKNOWLEDGEMENT

This comprehensive Staff Alteration System has been developed as a complete solution for managing academic staff, timetables, and automatic alteration assignments based on absence marking. The project combines modern technologies including Spring Boot for backend services, React with TypeScript for frontend interface, and PostgreSQL for robust data management. Special credit to the development team for the meticulous implementation of the complex alteration algorithm and comprehensive API design.

---

## CONTENTS

1. [Acknowledgement](#acknowledgement)
2. [Synopsis](#synopsis)
3. [Introduction](#1-introduction)
   - 1.1 System Specification
   - 1.2 Hardware Configuration
   - 1.3 Software Specification
4. [System Study](#2-system-study)
   - 2.1 Existing System & Drawbacks
   - 2.2 Proposed System & Features
5. [System Design and Development](#3-system-design-and-development)
   - 3.1 File Design
   - 3.2 Input Design
   - 3.3 Output Design
   - 3.4 Database Design
   - 3.5 System Development
   - 3.6 Description of Modules
6. [Testing and Implementation](#4-testing-and-implementation)
7. [Conclusion](#5-conclusion)
8. [Bibliography](#bibliography)
9. [Appendices](#appendices)

---

## SYNOPSIS

The Staff Alteration System is an advanced, full-stack web application designed to streamline the management of academic staff, class schedules, and automatic staff substitution when regular faculty members are absent. The system eliminates manual processes and reduces administrative overhead by implementing an intelligent 6-priority algorithm that automatically identifies and assigns the most suitable substitute staff member when an absence is marked.

**Key Highlights:**
- Fully automated staff alteration based on intelligent priority scoring
- Real-time notification system for substitute staff assignments
- Comprehensive timetable and attendance management
- JWT-based authentication with role-based access control
- RESTful API architecture with PostgreSQL database backend
- Responsive React frontend with TypeScript for type safety
- Database migrations using Flyway for version control
- WebSocket support for real-time communications

The system has been architected using industry-standard 3-layer architecture pattern (Presentation Layer, Service Layer, Data Access Layer) to ensure maintainability, scalability, and testability.

---

# 1. INTRODUCTION

## 1.1 System Overview

The Staff Alteration System is a comprehensive solution developed to address the complex challenges of managing academic staff, class schedules, and automatic substitution assignments in educational institutions. The system provides a centralized platform for:

- Staff authentication and authorization with role-based access control
- Timetable creation and management for class schedules
- Daily attendance marking and tracking
- Automatic intelligent alteration assignment when staff are absent
- Real-time notification system for staff and administrators
- Workload management and reporting
- Day order and period-based scheduling

The system is built on a modern, scalable architecture that supports multiple user roles including Staff members, Heads of Department (HOD), Deans, and System Administrators. Each role has specific permissions and access levels to ensure proper governance and data security.

## 1.2 System Specification

### 1.2.1 Hardware Configuration

**Minimum Requirements:**
- **Processor**: Intel Core i5 or equivalent (2.0 GHz or higher)
- **RAM**: 8 GB minimum (16 GB recommended)
- **Storage**: 50 GB SSD (100 GB for production environments)
- **Network**: High-speed internet connection (10 Mbps minimum)
- **Display**: 1366x768 resolution or higher

**Recommended Configuration:**
- **Processor**: Intel Core i7/i9 or AMD Ryzen 7/9
- **RAM**: 16 GB or higher
- **Storage**: 256 GB SSD
- **Network**: 100 Mbps or higher
- **Load Balancer**: For production deployments with multiple instances

### 1.2.2 Software Specification

#### Software Configuration Table

| **Category** | **Component** | **Version/Details** | **Purpose** |
|--------------|---------------|---------------------|-------------|
| **Operating System** | Windows 10 / Ubuntu 20.04+ | Primary supported platforms | Development & deployment environment |
| **Programming Language** | Java | Version 17 | Backend development language |
| | TypeScript | Version 5.2.2 | Frontend type-safe development |
| | JavaScript | ES6+ | Frontend scripting |
| **Backend Framework** | Spring Boot | Version 3.2.0 | Web application server & REST APIs |
| | Spring Security | Version 3.2.0 | Authentication & authorization |
| | Spring Data JPA | Version 3.2.0 | Database ORM layer |
| | Hibernate | Version 6.x | Object-relational mapping |
| **Frontend Framework** | React | Version 18.2.0 | UI library for building interfaces |
| | Vite | Version 5.0.0 | Build tool & development server |
| **Styling & UI** | Tailwind CSS | Version 3.3.5 | Utility-first CSS framework |
| | HTML5 | Latest | Markup language |
| | CSS3 | Latest | Styling language |
| | Lucide React | Latest | Icon library |
| **Database** | PostgreSQL | Version 15+ | Primary relational database |
| | Flyway | Version 9.22.3 | Database migration & versioning |
| | Supabase | Cloud PostgreSQL | Cloud database option |
| **Authentication** | JWT (JJWT) | Version 0.12.3 | Stateless token authentication |
| | BCrypt | Latest | Password encryption |
| **State Management** | Zustand | Version 4.4.5 | Lightweight React state manager |
| **HTTP Client** | Axios | Version 1.6.2 | API communication library |
| **Routing** | React Router DOM | Version 6.20.0 | Client-side routing |
| **Build Tools** | Gradle | Version 8.x | Java build automation |
| | npm | Version 9.x | Node package manager |
| **Real-time Communication** | WebSocket | Latest | Real-time notifications |
| **Email Service** | Spring Mail | Latest | SMTP email notifications |
| **Logging** | SLF4J + Logback | Latest | Application logging |
| **Utilities** | Lombok | Latest | Java boilerplate reduction |
| | Apache Commons Lang | Latest | Java utility functions |
| **Development Tools** | Visual Studio Code | Latest | Primary IDE |
| | IntelliJ IDEA | Latest | Alternative IDE (Java-focused) |
| | Git | Version 2.x | Version control system |
| | PowerShell | Latest | Windows terminal |
| **Web Server** | Apache Tomcat | Version 10.x | Embedded in Spring Boot |
| **Container (Optional)** | Docker | Version 20.x | Application containerization |
| | Kubernetes | Latest | Container orchestration |
| **Testing Libraries** | JUnit | Version 5.x | Java unit testing |
| | Spring Test | Latest | Spring integration testing |
| | H2 Database | Latest | In-memory testing database |
| **API Documentation** | Swagger/OpenAPI | Latest (Optional) | API documentation |

#### Software Component Details by Category

| **Category** | **Component** | **Details/Version** | **Description** |
|--------------|---------------|-------------------|-----------------|
| **OS** | Windows 10/11 | Latest Build | Primary development OS |
| **OS** | Ubuntu 20.04 LTS | 20.04+ | Server deployment OS |
| **OS** | macOS | 12+ | Alternative development OS |
| **Programming Language** | Java | 17 LTS | Backend core language |
| **Programming Language** | TypeScript | 5.2.2 | Frontend type-safe language |
| **Programming Language** | JavaScript | ES6+ | Frontend runtime language |
| **Programming Language** | SQL | PostgreSQL Dialect | Database query language |
| **Libraries** | Spring Framework | 6.0+ | Java enterprise framework |
| **Libraries** | Spring Boot | 3.2.0 | Application framework |
| **Libraries** | Spring Security | 3.2.0 | Authentication & authorization |
| **Libraries** | Spring Data JPA | 3.2.0 | ORM & data access |
| **Libraries** | Hibernate | 6.x | Object-relational mapping |
| **Libraries** | JJWT | 0.12.3 | JWT token management |
| **Libraries** | Lombok | Latest | Boilerplate reduction |
| **Libraries** | Jackson | Latest | JSON serialization |
| **Libraries** | React | 18.2.0 | UI library |
| **Libraries** | TypeScript | 5.2.2 | Type system for JavaScript |
| **Libraries** | Zustand | 4.4.5 | State management |
| **Libraries** | Axios | 1.6.2 | HTTP client |
| **Libraries** | Tailwind CSS | 3.3.5 | Utility CSS framework |
| **Libraries** | Lucide React | Latest | Icon library |
| **Libraries** | React Router | 6.20.0 | Client-side routing |
| **Libraries** | Date-fns | 2.30.0 | Date utilities |
| **Libraries** | Clsx | 2.0.0 | Conditional CSS classes |
| **Backend** | Spring Boot | 3.2.0 | Web application server |
| **Backend** | PostgreSQL | 15+ | Primary database |
| **Backend** | Supabase | Cloud | Cloud database option |
| **Backend** | Flyway | 9.22.3 | Database migration tool |
| **Backend** | Apache Tomcat | 10.x | Embedded web server |
| **Backend** | WebSocket | Latest | Real-time communication |
| **Backend** | Spring Mail | Latest | Email notification service |
| **Backend** | SLF4J | Latest | Logging framework |
| **Backend** | Gradle | 8.x | Build automation tool |
| **Frontend** | React | 18.2.0 | UI framework |
| **Frontend** | Vite | 5.0.0 | Build tool & dev server |
| **Frontend** | TypeScript | 5.2.2 | Type-safe scripting |
| **Frontend** | Tailwind CSS | 3.3.5 | Styling framework |
| **Frontend** | Zustand | 4.4.5 | State management store |
| **Frontend** | Axios | 1.6.2 | API communication |
| **Frontend** | React Router | 6.20.0 | Page navigation |
| **Frontend** | Lucide React | Latest | UI icons |
| **Frontend** | PostCSS | 8.4.31 | CSS preprocessing |
| **Frontend** | Autoprefixer | 10.4.16 | CSS vendor prefixes |
| **Frontend** | ESLint | 8.53.0 | Code linting |
| **Frontend** | npm | 9.x | Package manager |
| **Dataset Source** | Database Seed Data | DataInitializer.java | Default test data |
| **Dataset Source** | Flyway Migrations | V1-V5 SQL scripts | Database initialization |
| **Dataset Source** | Test Accounts | 5 default staff users | Pre-configured test users |
| **Dataset Source** | Sample Departments | CS, IT | Pre-configured departments |
| **Dataset Source** | Sample Classes | CS1, CS2, IT1, IT2 | Pre-configured classes |
| **Dataset Source** | Sample Subjects | Data Structures, etc. | Pre-configured subjects |

#### Detailed Stack Breakdown

**Backend Stack:**
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Gradle 8.x
- **Database**: PostgreSQL 15+ or Supabase
- **ORM**: Hibernate/JPA
- **Authentication**: JWT (JJWT 0.12.3)
- **Database Migrations**: Flyway 9.22.3
- **Messaging**: WebSocket for real-time notifications
- **Email**: SMTP configuration for email notifications

**Frontend Stack:**
- **Framework**: React 18.2.0
- **Language**: TypeScript 5.2.2
- **Build Tool**: Vite 5.0.0
- **Styling**: Tailwind CSS 3.3.5
- **State Management**: Zustand 4.4.5
- **HTTP Client**: Axios 1.6.2
- **Icons**: Lucide React
- **Routing**: React Router DOM 6.20.0

**Development Environment:**
- **IDE**: Visual Studio Code, IntelliJ IDEA, or Eclipse
- **Version Control**: Git 2.x
- **Terminal**: PowerShell, Bash, or Command Prompt
- **Package Manager**: npm 9.x, Maven/Gradle

**Server Requirements:**
- **Web Server**: Apache Tomcat 10.x (embedded in Spring Boot)
- **Operating System**: Ubuntu 20.04+, Windows Server 2019+, macOS 12+
- **Container**: Docker 20.x (optional for containerization)
- **Orchestration**: Kubernetes (optional for production scaling)

---

## 1.3 Actual & Perfect Technology Stack

### Libraries Used in This Project

#### **Spring Framework & Backend Libraries**
| Library | Version | Purpose |
|---|---|---|
| Spring Boot Starter Web | 3.2.0 | REST API development |
| Spring Boot Starter Data JPA | 3.2.0 | Database ORM layer |
| Spring Boot Starter Security | 3.2.0 | Authentication & authorization |
| Spring Boot Starter WebSocket | 3.2.0 | Real-time communication |
| Spring Boot Starter Validation | 3.2.0 | Input validation |
| Spring Boot Starter Mail | 3.2.0 | Email notifications |
| Spring Security Test | 3.2.0 | Security testing |
| Spring Boot Starter Test | 3.2.0 | Testing framework |

#### **Frontend Libraries**
| Library | Version | Purpose |
|---|---|---|
| React | 18.2.0 | UI library |
| React DOM | 18.2.0 | React rendering |
| React Router DOM | 6.20.0 | Client-side routing |
| Zustand | 4.4.5 | State management |
| Axios | 1.6.2 | HTTP client |
| Tailwind CSS | 3.3.5 | CSS styling framework |
| PostCSS | 8.4.31 | CSS processing |
| Autoprefixer | 10.4.16 | CSS vendor prefixes |
| TypeScript | 5.2.2 | Type-safe JavaScript |
| ESLint | 8.53.0 | Code linting |
| Lucide React | 0.292.0 | Icon library |
| Date-fns | 2.30.0 | Date utilities |
| Clsx | 2.0.0 | CSS class composition |

#### **Utility Libraries**
| Library | Version | Purpose |
|---|---|---|
| Lombok | Latest | Boilerplate reduction |
| Jackson Databind | Latest | JSON serialization |
| Apache Commons Lang 3 | Latest | Java utilities |
| JJWT (JWT) | 0.12.3 | JWT token management |
| PostgreSQL Driver | 42.7.1 | Database driver |
| Flyway Core | 9.22.3 | Database migrations |
| H2 Database | Latest | In-memory testing DB |

---

### Backend Technology Stack

| Component | Version/Details | Purpose |
|---|---|---|
| **Framework** | Spring Boot 3.2.0 | Web application framework |
| **Language** | Java 17 | Backend programming language |
| **Database** | PostgreSQL 15+ | Primary relational database |
| **Cloud Database** | Supabase | Cloud-hosted PostgreSQL |
| **Migration Tool** | Flyway 9.22.3 | Database schema versioning |
| **Web Server** | Apache Tomcat 10.x | Embedded servlet container |
| **Authentication** | JWT (JJWT) 0.12.3 | Token-based auth |
| **Real-time** | WebSocket | Bidirectional communication |
| **Email Service** | Spring Mail | SMTP email notifications |
| **Build Tool** | Gradle 8.x | Project build automation |
| **ORM** | Hibernate (JPA) | Object-relational mapping |
| **Security** | Spring Security | Authentication & authorization |
| **Logging** | SLF4J + Logback | Application logging |
| **Testing Framework** | JUnit 5 | Unit testing |

---

### Frontend Technology Stack

| Component | Version/Details | Purpose |
|---|---|---|
| **Framework** | React 18.2.0 | UI library |
| **Build Tool** | Vite 5.0.0 | Fast development & production build |
| **Language** | TypeScript 5.2.2 | Type-safe JavaScript |
| **Styling** | Tailwind CSS 3.3.5 | Utility-first CSS framework |
| **State Management** | Zustand 4.4.5 | Lightweight state management |
| **HTTP Client** | Axios 1.6.2 | API communication |
| **Routing** | React Router DOM 6.20.0 | Client-side navigation |
| **CSS Processing** | PostCSS 8.4.31 | CSS transformation |
| **Linting** | ESLint 8.53.0 | Code quality checks |
| **Package Manager** | npm 9.x | Node package management |
| **Type Checking** | TypeScript Compiler | Type validation |

---

### Dataset Source & Initialization

| Source | Details | Purpose |
|---|---|---|
| **Database Seed Data** | DataInitializer.java | Default test data population |
| **Flyway Migrations** | V1-V5 SQL scripts | Database schema creation & versioning |
| **Migration Files** | Classpath: db/migration/ | Automatic schema initialization |
| **Test Accounts** | 5 default staff users | Pre-configured test credentials |
| **Sample Departments** | CS, IT | Pre-configured departments |
| **Sample Classes** | CS1, CS2, IT1, IT2 | Pre-configured classes |
| **Sample Subjects** | Data Structures, DB Mgmt, etc | Pre-configured subjects |

---

### Perfect Technology Stack Summary

**✅ VERIFIED & CONFIRMED TECHNOLOGIES:**

**Backend (Spring Boot 3.2.0 | Java 17):**
- Spring Boot Starter Web, Data JPA, Security, WebSocket, Validation, Mail
- PostgreSQL 15+ / Supabase
- Flyway 9.22.3 (Database Migrations)
- JWT (JJWT 0.12.3)
- Gradle 8.x
- Apache Tomcat 10.x

**Frontend (React 18.2.0 | TypeScript 5.2.2):**
- Vite 5.0.0
- Tailwind CSS 3.3.5
- Zustand 4.4.5 (State Management)
- Axios 1.6.2
- React Router DOM 6.20.0
- PostCSS 8.4.31
- Autoprefixer 10.4.16
- ESLint 8.53.0

**Database:**
- PostgreSQL 15+
- Flyway 9.22.3 (V1-V5 Migrations)
- JDBC Driver (PostgreSQL 42.7.1)

**Testing:**
- JUnit 5
- Spring Boot Test
- Spring Security Test
- H2 Database (In-memory)

**Total Technologies Used**: 35+ (All verified from build.gradle & package.json)

---

# 2. SYSTEM STUDY

## 2.1 Existing System

### 2.1.1 Current Challenges

Before the implementation of the Staff Alteration System, academic institutions typically faced the following challenges:

**Manual Processes:**
- Staff alterations were manually assigned by administrators, requiring constant coordination
- Absence notifications were communicated via email or phone calls
- No centralized database for tracking timetables and staff assignments
- Attendance marking was done on paper or in separate spreadsheets

**Time Inefficiency:**
- Finding suitable substitute staff took 30-45 minutes per absence
- Multiple back-and-forth communications required between administration and staff
- No real-time visibility into staff availability and workload
- Administrative overhead increased significantly during peak absence periods

**Lack of Intelligence:**
- No systematic approach to substitute selection based on qualifications
- Same staff members frequently assigned alterations, creating workload imbalance
- No consideration for schedule conflicts or consecutive period clashes
- Limited reporting and analytics capabilities

**Data Management Issues:**
- Scattered information across multiple systems and documents
- No audit trail for alterations and changes
- Difficulty in generating accurate workload reports
- No historical data for analysis and decision-making

**Access and Control:**
- Limited ability to track who made which alteration decisions
- No role-based access control for different administrative levels
- Difficulty in maintaining data integrity and security
- No authentication mechanism for sensitive information

## 2.2 Proposed System

### 2.2.1 Key Features

The Staff Alteration System introduces a comprehensive solution with the following features:

**1. Authentication & Authorization**
- JWT-based token authentication for secure user sessions
- Role-based access control with four user roles: STAFF, HOD, DEAN, ADMIN
- Password encryption using BCrypt
- Automatic token refresh mechanism
- Session management and login/logout functionality

**2. Staff Management**
- Create and manage staff member profiles
- Department-wise staff organization
- Staff status tracking (Active, Inactive, On Leave)
- Contact information management
- Email and phone number validation

**3. Timetable Management**
- Create and manage class timetables with staff assignments
- Support for multiple departments and classes
- Day order and period-based scheduling
- Timetable templates for recurring patterns
- Easy modification and deletion of timetable entries
- View timetables by class or by staff member

**4. Attendance Tracking**
- Mark daily attendance with status (Present, Absent, Half Day)
- Support for full-day and half-day absences
- Remarks and notes for each attendance record
- Bulk attendance marking capability
- Historical attendance records with timestamps

**5. Intelligent Alteration Algorithm** ⭐
- **6-Priority Scoring System:**
  1. Staff must be present on the alteration date
  2. Preference for staff who already teach the same class
  3. Selection of staff with minimum teaching hours that day
  4. Avoidance of schedule conflicts (no consecutive period clashes)
  5. Preference for same subject, otherwise any subject
  6. Tie-breaker using least weekly workload

- Deterministic and conflict-free substitute selection
- Prevention of double allocations
- Workload-aware distribution
- O(n) complexity for efficiency

**6. Alteration Management**
- Automatic creation of alteration records when absence is marked
- Status tracking for alteration lifecycle (Assigned, Acknowledged, Completed)
- Lesson plan upload for alterations
- Remarks and notes for each alteration
- Alteration history and audit trail

**7. Real-Time Notifications**
- WebSocket-based real-time notifications
- Email notifications for alteration assignments
- SMS notifications (with mock provider support)
- Notification management with read/unread status
- Notification type categorization (ALTERATION, ATTENDANCE, SYSTEM)

**8. Workload Management**
- Automatic workload summary calculation
- Daily and weekly workload tracking
- Workload distribution analytics
- Workload-based prioritization in alteration algorithm
- Workload reports by staff and time period

**9. Dashboard & Reporting**
- Personal dashboard with statistics and recent activities
- System-wide statistics and reports
- Staff-specific alteration history
- Department-wise analytics
- HOD dashboard for department overview
- Dean dashboard for institutional overview

**10. Responsive User Interface**
- Mobile-friendly design using Tailwind CSS
- Collapsible navigation sidebar
- Responsive grids and layouts
- Intuitive form validation
- User-friendly error messages

**11. Security Features**
- CORS configuration for cross-origin requests
- SQL injection prevention through parameterized queries
- XSS protection through React's default escaping
- CSRF protection through SameSite cookies
- Data validation at both frontend and backend
- Encrypted password storage

**12. Database Management**
- Version-controlled database schema using Flyway migrations
- PostgreSQL for robust relational data management
- Proper indexing for query optimization
- Foreign key relationships for data integrity
- Automatic timestamp management

---

# 3. SYSTEM DESIGN AND DEVELOPMENT

## 3.1 File Design

The Staff Alteration System follows a comprehensive modular file structure organized by layer and functionality:

### 3.1.1 Backend File Structure

```
backend/
├── src/main/java/com/staffalteration/
│   ├── StaffAlterationApplication.java          # Spring Boot Entry Point
│   ├── DataInitializer.java                     # Database Seeding
│   │
│   ├── controller/                              # REST API Layer
│   │   ├── AuthController.java                  # Auth endpoints
│   │   ├── AttendanceController.java            # Attendance endpoints
│   │   ├── TimetableController.java             # Timetable endpoints
│   │   ├── AlterationController.java            # Alteration endpoints
│   │   ├── NotificationController.java          # Notification endpoints
│   │   ├── StaffController.java                 # Staff endpoints
│   │   └── DepartmentController.java            # Department endpoints
│   │
│   ├── service/                                 # Business Logic Layer
│   │   ├── AuthenticationService.java           # Auth logic & JWT
│   │   ├── AttendanceService.java               # Attendance operations
│   │   ├── TimetableService.java                # Timetable operations
│   │   ├── TimetableTemplateService.java        # Template management
│   │   ├── AlterationService.java               # ⭐ Priority algorithm
│   │   ├── StaffService.java                    # Staff operations
│   │   └── NotificationService.java             # Notification handling
│   │
│   ├── repository/                              # Data Access Layer
│   │   ├── UserRepository.java                  # User queries
│   │   ├── RoleRepository.java                  # Role queries
│   │   ├── DepartmentRepository.java            # Department queries
│   │   ├── SubjectRepository.java               # Subject queries
│   │   ├── ClassRoomRepository.java             # Class queries
│   │   ├── StaffRepository.java                 # Staff queries
│   │   ├── TimetableRepository.java             # Timetable queries
│   │   ├── TimetableTemplateRepository.java     # Template queries
│   │   ├── AttendanceRepository.java            # Attendance queries
│   │   ├── AlterationRepository.java            # Alteration queries
│   │   ├── NotificationRepository.java          # Notification queries
│   │   └── WorkloadSummaryRepository.java       # Workload queries
│   │
│   ├── entity/                                  # JPA Entities
│   │   ├── User.java                            # User entity
│   │   ├── Role.java                            # Role entity
│   │   ├── Department.java                      # Department entity
│   │   ├── Subject.java                         # Subject entity
│   │   ├── ClassRoom.java                       # Class entity
│   │   ├── Staff.java                           # Staff entity
│   │   ├── Timetable.java                       # Timetable entity
│   │   ├── TimetableTemplate.java               # Template entity
│   │   ├── Attendance.java                      # Attendance entity
│   │   ├── Alteration.java                      # Alteration entity
│   │   ├── LessonPlan.java                      # Lesson plan entity
│   │   ├── Notification.java                    # Notification entity
│   │   └── WorkloadSummary.java                 # Workload entity
│   │
│   ├── dto/                                     # Data Transfer Objects
│   │   ├── UserDTO.java                         # User DTO
│   │   ├── AuthRequestDTO.java                  # Auth request
│   │   ├── AuthResponseDTO.java                 # Auth response
│   │   ├── StaffDTO.java                        # Staff DTO
│   │   ├── DepartmentDTO.java                   # Department DTO
│   │   ├── SubjectDTO.java                      # Subject DTO
│   │   ├── ClassRoomDTO.java                    # Class DTO
│   │   ├── TimetableDTO.java                    # Timetable DTO
│   │   ├── TimetableTemplateDTO.java            # Template DTO
│   │   ├── AttendanceMarkDTO.java               # Attendance mark request
│   │   ├── AttendanceDTO.java                   # Attendance response
│   │   ├── AlterationDTO.java                   # Alteration DTO
│   │   ├── NotificationDTO.java                 # Notification DTO
│   │   ├── WorkloadSummaryDTO.java              # Workload DTO
│   │   └── ApiResponseDTO.java                  # Generic API response
│   │
│   ├── security/                                # Security Layer
│   │   ├── JwtTokenProvider.java                # JWT generation/validation
│   │   ├── JwtAuthenticationFilter.java         # Auth filter
│   │   └── CustomUserDetailsService.java        # User details loader
│   │
│   ├── config/                                  # Configuration
│   │   ├── SecurityConfig.java                  # Spring Security config
│   │   ├── CorsConfig.java                      # CORS configuration
│   │   └── WebSocketConfig.java                 # WebSocket config
│   │
│   ├── notification/                            # Notification Providers
│   │   ├── EmailNotificationProvider.java       # Email provider
│   │   ├── SmsNotificationProvider.java         # SMS provider
│   │   └── NotificationFactory.java             # Factory pattern
│   │
│   └── util/                                    # Utilities
│       ├── Constants.java                       # Application constants
│       └── DateTimeUtil.java                    # Date/time utilities
│
├── src/main/resources/
│   ├── application.properties                   # App configuration
│   └── db/migration/
│       ├── V1__Create_Initial_Schema.sql        # Initial schema
│       ├── V2__Create_Timetable_Template.sql    # Template table
│       ├── V3__Fix_Timetable_Template_Staff_FK.sql  # FK fix
│       ├── V4__Add_Missing_Columns_v2.sql       # Schema updates
│       └── V5__Add_Meeting_Hours_Table.sql      # Meeting hours
│
└── build.gradle                                 # Gradle build config
```

### 3.1.2 Frontend File Structure

```
frontend/
├── src/
│   ├── main.tsx                                 # React entry point
│   ├── App.tsx                                  # Root component
│   ├── index.css                                # Global styles
│   │
│   ├── pages/                                   # Page Components
│   │   ├── LoginPage.tsx                        # Login interface
│   │   ├── DashboardPage.tsx                    # Main dashboard
│   │   ├── AttendancePage.tsx                   # Attendance marking
│   │   ├── TimetablesPage.tsx                   # View timetables
│   │   ├── TimetableManagementPage.tsx          # Manage timetables
│   │   ├── StaffManagementPage.tsx              # Staff management
│   │   ├── ClassManagementPage.tsx              # Class management
│   │   ├── DayOrderSchedulePage.tsx             # Day order schedule
│   │   ├── AlterationDashboardPage.tsx          # Alteration dashboard
│   │   ├── SettingsPage.tsx                     # Settings
│   │   ├── HodDashboardPage.tsx                 # HOD dashboard
│   │   └── AttendancePage_old.tsx               # Legacy attendance page
│   │
│   ├── components/                              # Reusable Components
│   │   ├── Layout.tsx                           # Main layout wrapper
│   │   ├── ProtectedRoute.tsx                   # Route protection
│   │   ├── common.tsx                           # Common components
│   │   ├── TimetableTable.tsx                   # Timetable display
│   │   ├── CreateTimetableModal.tsx             # Timetable creation modal
│   │   └── ProfileModal.tsx                     # User profile modal
│   │
│   ├── store/                                   # State Management
│   │   ├── authStore.ts                         # Auth state (Zustand)
│   │   └── timetableStore.ts                    # Timetable state
│   │
│   └── api/                                     # API Integration
│       ├── client.ts                            # Axios configuration
│       └── index.ts                             # API endpoints
│
├── vite.config.ts                               # Vite configuration
├── vite.config.d.ts                             # Type definitions
├── tailwind.config.js                           # Tailwind CSS config
├── tsconfig.json                                # TypeScript config
├── tsconfig.node.json                           # TS config for build tools
├── postcss.config.js                            # PostCSS config
├── index.html                                   # HTML entry point
└── package.json                                 # Dependencies
```

### 3.1.3 Database Files

```
src/main/resources/db/migration/
├── V1__Create_Initial_Schema.sql                # 12 core tables
├── V2__Create_Timetable_Template.sql            # Template functionality
├── V3__Fix_Timetable_Template_Staff_FK.sql      # Foreign key fix
├── V4__Add_Missing_Columns_v2.sql               # Additional columns
└── V5__Add_Meeting_Hours_Table.sql              # Meeting hours tracking
```

## 3.2 Input Design

### 3.2.1 User Input Interfaces

**Authentication Input:**
- Username (Text field, alphanumeric, 4-50 characters)
- Password (Secure password field, minimum 8 characters, BCrypt encrypted)
- Remember Me (Checkbox for persistent login)

**Attendance Marking Input:**
- Staff Member (Dropdown, auto-populated from logged-in user)
- Attendance Status (Radio buttons: Present, Absent, Half Day)
- Day Type (Radio buttons: Full Day, First Half, Second Half)
- Attendance Date (Date picker, selectable from today backwards 30 days)
- Remarks (Text area, optional, up to 500 characters)

**Timetable Management Input:**
- Class (Dropdown, all available classes)
- Staff Member (Dropdown, all available staff)
- Subject (Dropdown, based on selected class)
- Day Order (Number input, 1-5 for Mon-Fri)
- Period Number (Number input, 1-8 typical)
- Start Time (Time picker, optional)
- End Time (Time picker, optional)

**Staff Management Input:**
- Staff ID (Text field, unique identifier)
- First Name (Text field, alphanumeric)
- Last Name (Text field, alphanumeric)
- Email (Email field, validated)
- Phone Number (Phone field, 10-15 digits)
- Department (Dropdown, select department)
- Status (Dropdown: Active, Inactive, On Leave)

**Class/Department Input:**
- Code (Text field, unique identifier)
- Name (Text field, descriptive name)
- Description (Text area, optional)
- Department (Dropdown, applicable department)

### 3.2.2 Data Validation

**Frontend Validation:**
- Required field validation
- Email format validation
- Phone number format validation
- Date range validation
- Input length validation
- Password strength requirements
- Unique value validation for usernames and IDs

**Backend Validation:**
- Spring Validation annotations (@Valid, @NotNull, @Email, @Pattern)
- Custom validators for business logic
- Database constraint enforcement
- Duplicate entry prevention
- Foreign key constraint validation

### 3.2.3 Input Error Handling

```
Example: Attendance Marking Error Scenarios
- Invalid Date: "Cannot mark attendance for future dates"
- Duplicate Entry: "Attendance already marked for this date"
- Invalid Status: "Please select a valid attendance status"
- Missing Required Fields: "Please fill all required fields"
- System Error: "An error occurred. Please try again."
```

## 3.3 Output Design

### 3.3.1 User Output Interfaces

**Dashboard Output:**
- Welcome message with user's name
- Statistics cards (Total Staff, Total Classes, Total Alterations, Pending Alterations)
- Recent activity feed (Last 5-10 activities)
- Quick action buttons for common tasks
- System status and alerts

**Attendance Viewing Output:**
- Table format with: Date, Status, Day Type, Remarks, Created At
- Filtering by date range
- Export to CSV functionality
- Color-coded status indicators (Green=Present, Red=Absent, Yellow=Half Day)

**Alteration Dashboard Output:**
- List of alterations with: Original Staff, Substitute, Date, Status, Subject, Class
- Filter by date, status, staff member
- Pagination (10-50 items per page)
- Status badges (Assigned, Acknowledged, Completed)
- Action buttons (View Details, Acknowledge, Upload Lesson Plan)

**Timetable Viewing Output:**
- Grid format (Days × Periods)
- Color-coded cells by subject
- Staff name and subject displayed in each cell
- Empty cells marked as "Free"
- Edit/Delete buttons for admin users
- Print-friendly view

**Notification Output:**
- Notification list with: Title, Message, Type, Date/Time, Read Status
- Unread notification badge counter
- Filter by type and date
- Mark as read/unread functionality
- Delete notification option
- Real-time notification toast messages

**Reports Output:**
- Workload summary reports (PDF/Excel export)
- Staff alteration history reports
- Department-wise statistics
- Monthly/quarterly analytics
- Graphical representations (Charts, Graphs)

### 3.3.2 API Response Format

**Success Response:**
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data object
  },
  "timestamp": "2026-02-12T10:30:00Z"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "errors": [
    {
      "field": "fieldName",
      "message": "Field validation error"
    }
  ],
  "timestamp": "2026-02-12T10:30:00Z"
}
```

### 3.3.3 Notification Output

**Real-Time Toast Notifications:**
- Success: Green background with checkmark
- Error: Red background with error icon
- Warning: Yellow background with warning icon
- Info: Blue background with info icon
- Auto-dismiss after 3-5 seconds
- Manual close button

**Persistent Notifications:**
- WebSocket-based real-time updates
- Notification bell icon with unread count
- Notification drawer with full list
- Read/Unread status tracking

## 3.4 Database Design

### 3.4.1 Entity Relationship Diagram (ERD)

```
┌─────────────────┐
│   User Account  │
├─────────────────┤
│ id (PK)         │
│ username        │
│ password        │
│ email           │
│ enabled         │
│ created_at      │
│ updated_at      │
└────────┬────────┘
         │
    ┌────┴─────┐
    │ (1 to M) │
    │           │
    ▼           ▼
┌──────────┐  ┌──────────┐
│  Staff   │  │   Role   │
├──────────┤  ├──────────┤
│ id (FK)  │  │ id (PK)  │
│ staff_id │  │role_type │
│ name     │  │desc      │
│ email    │  └──────────┘
│ dept_id  │       ▲
└────┬─────┘       │
     │          (M to M)
     │             │
     └─────────────┴────────┐
                            │
                   ┌────────┴─────────┐
                   │                  │
    ┌──────────────┘                  │
    │                          ┌──────▼───────┐
    │                          │  Department  │
    │                          ├──────────────┤
    │                          │ id (PK)      │
    │                          │ code         │
    │                          │ name         │
    │                          │ description  │
    │                          └──────┬───────┘
    │                                 │
    ├─────────────────────────────────┤
    │                                 │
    ▼                                 ▼
┌──────────────┐         ┌──────────────────┐
│   Timetable  │◄────────│    Subject       │
├──────────────┤         ├──────────────────┤
│ id (PK)      │         │ id (PK)          │
│ staff_id(FK) │         │ subject_code     │
│ subject(FK)  │         │ subject_name     │
│ class_id(FK) │         │ dept_id(FK)      │
│ day_order    │         └──────────────────┘
│ period       │
│ created_at   │        ┌──────────────────┐
│ updated_at   │◄───────│    ClassRoom     │
└──────┬───────┘        ├──────────────────┤
       │                │ id (PK)          │
       │                │ class_code       │
       │                │ class_name       │
       │                │ dept_id(FK)      │
       │                └──────────────────┘
       │
       ├─────────────────┬─────────────────┐
       │                 │                 │
       ▼                 ▼                 ▼
┌───────────────┐  ┌──────────────┐  ┌───────────────┐
│  Attendance   │  │  Alteration  │  │ WorkloadSummary
├───────────────┤  ├──────────────┤  ├───────────────┤
│ id (PK)       │  │ id (PK)      │  │ id (PK)       │
│ staff_id(FK)  │  │ timetable(FK)│  │ staff_id(FK)  │
│ date          │  │ orig_staff   │  │ workload_date │
│ status        │  │ sub_staff(FK)│  │ total_hours   │
│ day_type      │  │ date         │  │ regular_hours │
│ remarks       │  │ status       │  │ alt_hours     │
│ created_at    │  │ lesson(FK)   │  │ weekly_total  │
└───────────────┘  │ remarks      │  └───────────────┘
                   │ created_at   │
                   │ updated_at   │
                   └────┬─────────┘
                        │
                        ▼
                  ┌───────────────┐
                  │  LessonPlan   │
                  ├───────────────┤
                  │ id (PK)       │
                  │ file_path     │
                  │ notes         │
                  │ file_type     │
                  │ file_size     │
                  │ created_at    │
                  └───────────────┘

┌──────────────────────────────────────────────────┐
│            Notification                          │
├──────────────────────────────────────────────────┤
│ id (PK)                                          │
│ staff_id (FK) ─────────► Staff                   │
│ title                                            │
│ message                                          │
│ notification_type (ALTERATION, ATTENDANCE, etc)  │
│ is_read                                          │
│ alteration_id (FK, optional)                     │
│ created_at                                       │
│ read_at                                          │
└──────────────────────────────────────────────────┘
```

### 3.4.2 Table Structures

**USER_ACCOUNT Table:**
```sql
CREATE TABLE user_account (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**STAFF Table:**
```sql
CREATE TABLE staff (
    id SERIAL PRIMARY KEY,
    staff_id VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    department_id BIGINT NOT NULL REFERENCES department(id),
    user_id BIGINT NOT NULL UNIQUE REFERENCES user_account(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**TIMETABLE Table:**
```sql
CREATE TABLE timetable (
    id SERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    day_order INTEGER NOT NULL,
    period_number INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**ATTENDANCE Table:**
```sql
CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PRESENT',
    day_type VARCHAR(50) NOT NULL DEFAULT 'FULL_DAY',
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(staff_id, attendance_date)
);
```

**ALTERATION Table:**
```sql
CREATE TABLE alteration (
    id SERIAL PRIMARY KEY,
    timetable_id BIGINT NOT NULL REFERENCES timetable(id) ON DELETE CASCADE,
    original_staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    substitute_staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    alteration_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
    lesson_plan_id BIGINT REFERENCES lesson_plan(id) ON DELETE SET NULL,
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**NOTIFICATION Table:**
```sql
CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    alteration_id BIGINT REFERENCES alteration(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);
```

### 3.4.3 Indexing Strategy

```sql
-- Performance indexes
CREATE INDEX idx_staff_user_id ON staff(user_id);
CREATE INDEX idx_timetable_staff_id ON timetable(staff_id);
CREATE INDEX idx_timetable_day_period ON timetable(day_order, period_number);
CREATE INDEX idx_attendance_staff_date ON attendance(staff_id, attendance_date);
CREATE INDEX idx_alteration_date ON alteration(alteration_date);
CREATE INDEX idx_alteration_original_staff ON alteration(original_staff_id);
CREATE INDEX idx_alteration_substitute_staff ON alteration(substitute_staff_id);
CREATE INDEX idx_notification_staff_id ON notification(staff_id);
CREATE INDEX idx_workload_summary_staff_date ON workload_summary(staff_id, workload_date);
```

## 3.5 System Development

### 3.5.1 Architecture Overview

**3-Layer Architecture Pattern:**

```
┌─────────────────────────────────────────────┐
│     Presentation Layer (Frontend)           │
│  React + TypeScript + Tailwind CSS + Vite   │
│                                             │
│  - React Components                         │
│  - Zustand State Management                 │
│  - Axios HTTP Client                        │
│  - React Router Navigation                  │
└──────────────────┬──────────────────────────┘
                   │
         REST API (JSON over HTTP)
                   │
┌──────────────────▼──────────────────────────┐
│     Business Logic Layer (Backend)          │
│     Spring Boot 3.2.0 + Java 17             │
│                                             │
│  - REST Controllers (API Endpoints)         │
│  - Service Classes (Business Logic)         │
│  - DTOs (Data Transfer Objects)             │
│  - Security & JWT Authentication            │
│  - Validation & Error Handling              │
│  - WebSocket for Real-time Updates          │
└──────────────────┬──────────────────────────┘
                   │
            JDBC/SQL Queries
                   │
┌──────────────────▼──────────────────────────┐
│     Data Access Layer (Persistence)         │
│  Spring Data JPA + Hibernate + PostgreSQL   │
│                                             │
│  - JPA Repositories                         │
│  - Hibernate ORM Mapping                    │
│  - Database Connection Pool                 │
│  - Transaction Management                   │
│  - Flyway Database Migrations               │
└─────────────────────────────────────────────┘
```

### 3.5.2 Development Technologies

**Backend Technologies:**
- Spring Boot Framework: Web development and application setup
- Spring Security: Authentication and authorization
- Spring Data JPA: Database operations and queries
- Hibernate: Object-relational mapping
- PostgreSQL: Relational database management
- JWT (JJWT): Stateless authentication tokens
- Flyway: Database schema versioning and migration
- Lombok: Reduce boilerplate code (Getters, Setters, Constructors)
- SLF4J: Logging framework
- Gradle: Build automation and dependency management

**Frontend Technologies:**
- React: UI library for building interactive components
- TypeScript: Type-safe JavaScript for better code quality
- Vite: Fast build tool for development and production
- Tailwind CSS: Utility-first CSS framework for styling
- Zustand: Lightweight state management library
- Axios: HTTP client for API communication
- React Router: Client-side routing and navigation
- Lucide React: SVG icon library
- PostCSS: CSS processing and Tailwind compilation

**Database Technologies:**
- PostgreSQL: Robust relational database
- Flyway: Version control for database schema
- JDBC: Database connectivity from Java

### 3.5.3 Build and Deployment Process

**Backend Build Process:**
```bash
# Compile Java source code
./gradlew clean build

# Output: build/libs/staff-alteration-system-1.0.0.jar
```

**Frontend Build Process:**
```bash
# Install dependencies
npm install

# Build optimized production bundle
npm run build

# Output: dist/ directory with bundled files
```

**Development Environment:**
```bash
# Terminal 1: Backend
cd backend
./gradlew bootRun
# Runs on http://localhost:8080/api

# Terminal 2: Frontend
cd frontend
npm run dev
# Runs on http://localhost:3000
```

## 3.5.4 Description of Modules (Detailed Implementation)

### Module 1: Authentication & Authorization Module

**Purpose:** Secure user login and role-based access control

**Components:**
- **AuthController.java**: REST endpoints for login and user retrieval
- **AuthenticationService.java**: Business logic for authentication and JWT token generation
- **JwtTokenProvider.java**: JWT token creation, validation, and extraction
- **JwtAuthenticationFilter.java**: Spring filter for request-level authentication
- **SecurityConfig.java**: Spring Security configuration and bean definitions
- **CustomUserDetailsService.java**: User details loading from database
- **AuthRequestDTO.java**: Username and password input
- **AuthResponseDTO.java**: JWT token response

**Key Features:**
- Stateless authentication using JWT tokens
- Token expiration and refresh mechanism
- Role-based authorization (STAFF, HOD, DEAN, ADMIN)
- Password encryption using BCrypt
- CORS configuration for frontend communication
- Session-less operation for scalability

**Database Interactions:**
- User lookup by username in UserRepository
- Role retrieval from RoleRepository
- Staff information from StaffRepository

**Sample Implementation:**
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody AuthRequestDTO request) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(), 
            request.getPassword()
        )
    );
    
    String token = jwtTokenProvider.generateToken(auth);
    User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
    
    return ResponseEntity.ok(new AuthResponseDTO(token, user));
}
```

### Module 2: Attendance Management Module

**Purpose:** Track daily staff attendance and automatically trigger alterations

**Components:**
- **AttendanceController.java**: REST endpoints for marking and viewing attendance
- **AttendanceService.java**: Business logic for attendance operations
- **Attendance.java**: JPA entity for persistence
- **AttendanceRepository.java**: Data access for attendance records
- **AttendanceMarkDTO.java**: Request DTO for marking attendance
- **AttendanceDTO.java**: Response DTO for attendance data

**Key Features:**
- Mark attendance with status (Present, Absent, Half Day)
- Support for full-day and half-day absences
- Automatic alteration creation when absence is marked
- Historical attendance tracking
- Validation to prevent duplicate entries
- Remarks and notes capability
- Bulk operations support

**Workflow:**
1. Staff marks attendance (typically in morning)
2. System validates input and checks for duplicates
3. If absent, AlterationService is triggered
4. Alteration algorithm runs to find best substitute
5. Notification created and sent to substitute staff
6. Workload summary updated

**Sample Implementation:**
```java
@PostMapping("/mark")
public ResponseEntity<?> markAttendance(
    @RequestBody AttendanceMarkDTO dto, 
    Principal principal) {
    
    Staff staff = staffService.getStaffByUser(principal.getName());
    
    // Check for duplicate
    if (attendanceRepository.existsByStaffIdAndDate(staff.getId(), dto.getDate())) {
        throw new DuplicateAttendanceException("Already marked for date");
    }
    
    Attendance attendance = new Attendance();
    attendance.setStaff(staff);
    attendance.setAttendanceDate(dto.getDate());
    attendance.setStatus(dto.getStatus());
    
    attendanceRepository.save(attendance);
    
    // Trigger alteration if absent
    if (dto.getStatus().equals("ABSENT")) {
        alterationService.processAlteration(staff, dto.getDate());
    }
    
    return ResponseEntity.ok(mapToDTO(attendance));
}
```

### Module 3: Timetable Management Module

**Purpose:** Create, manage, and display class timetables

**Components:**
- **TimetableController.java**: REST endpoints for timetable operations
- **TimetableService.java**: Business logic for timetable management
- **TimetableTemplateService.java**: Template-based timetable creation
- **Timetable.java**: JPA entity for timetable records
- **TimetableRepository.java**: Data access queries
- **TimetableDTO.java**: Data transfer object
- **CreateTimetableDTO.java**: Request DTO for creation

**Key Features:**
- Create timetables with staff, subject, class, day, and period
- Timetable templates for recurring patterns
- View timetables by class or staff
- Conflict detection (prevent double booking)
- Bulk timetable upload capability
- Edit and delete operations
- Weekly schedule view

**Database Schema:**
- Timetable: Stores individual schedule entries
- TimetableTemplate: Stores recurring schedule patterns
- Indexes on (day_order, period_number) and staff_id for fast queries

**Sample Implementation:**
```java
@PostMapping("/create")
public ResponseEntity<?> createTimetable(@RequestBody TimetableDTO dto) {
    
    Staff staff = staffRepository.findById(dto.getStaffId()).orElseThrow();
    Subject subject = subjectRepository.findById(dto.getSubjectId()).orElseThrow();
    ClassRoom classRoom = classRoomRepository.findById(dto.getClassId()).orElseThrow();
    
    // Check for conflicts
    List<Timetable> conflicts = timetableRepository.findByDayOrderAndPeriodNumber(
        dto.getDayOrder(), 
        dto.getPeriodNumber()
    );
    
    if (!conflicts.isEmpty()) {
        throw new ConflictException("Time slot already occupied");
    }
    
    Timetable timetable = new Timetable();
    timetable.setStaff(staff);
    timetable.setSubject(subject);
    timetable.setClassRoom(classRoom);
    timetable.setDayOrder(dto.getDayOrder());
    timetable.setPeriodNumber(dto.getPeriodNumber());
    
    return ResponseEntity.ok(mapToDTO(timetableRepository.save(timetable)));
}
```

### Module 4: Intelligent Alteration Module ⭐

**Purpose:** Automatically select the most suitable substitute staff using priority algorithm

**Components:**
- **AlterationController.java**: REST endpoints for alteration operations
- **AlterationService.java**: Core alteration algorithm and business logic
- **Alteration.java**: JPA entity for alteration records
- **AlterationRepository.java**: Data access queries
- **AlterationDTO.java**: Data transfer object
- **WorkloadSummary.java**: Tracks staff workload for prioritization

**Alteration Algorithm (6-Priority System):**

```
Priority 1: Staff Presence Check
├── Candidate must be marked PRESENT for the alteration date
├── Skip if absent, on leave, or not in system
└── Continue to Priority 2 if passed

Priority 2: Class Experience Check
├── Preference for staff who already teach the same class
├── Bonus points if staff teaches subject in other classes
└── Continuation to Priority 3 if no match found

Priority 3: Workload Distribution Check
├── Count teaching hours for candidate on alteration date
├── Prefer staff with lowest hours (most available)
├── Consider both regular and alteration hours
└── Continue to Priority 4

Priority 4: Schedule Conflict Detection
├── Check for consecutive period clashes
├── Ensure no conflicts with existing timetable
├── Avoid double allocation to same class
└── Continue to Priority 5 if no conflicts

Priority 5: Subject Expertise Check
├── Prefer staff teaching same subject (primary)
├── Accept staff teaching different subjects (secondary)
├── Optional: Department preference
└── Continue to Priority 6 for tie-breaking

Priority 6: Weekly Workload Tie-Breaker
├── If multiple candidates have equal scores above
├── Select staff with lowest weekly total hours
├── Ensure balanced workload distribution
└── Final selection made
```

**Scoring Algorithm:**
```
Base Score = 100

Score += 50  if (teachesSameClass)
Score += 30  if (noScheduleClash)
Score += 20  if (sameSubject)
Score -= hoursOnDay * 5  (reducing score for busy staff)
Score -= weeklyHours * 1  (reducing score for heavily loaded)

Final Selection = Candidate with highest score
```

**Key Features:**
- Deterministic: Same inputs always produce same output
- Conflict-free: Prevents double allocations
- Workload-aware: Balances teaching load
- Efficient: O(n) time complexity
- Testable: Pure function design

**Sample Implementation:**
```java
@Transactional
public AlterationDTO processAlteration(Staff originalStaff, LocalDate date) {
    
    // Get all classes for the absent staff on that date
    List<Timetable> timetables = timetableRepository.findByStaffIdAndDate(
        originalStaff.getId(), 
        date
    );
    
    // Process each class
    for (Timetable timetable : timetables) {
        Staff substitute = findSubstitute(timetable, date);
        
        if (substitute != null) {
            Alteration alteration = new Alteration();
            alteration.setTimetable(timetable);
            alteration.setOriginalStaff(originalStaff);
            alteration.setSubstituteStaff(substitute);
            alteration.setAlterationDate(date);
            alteration.setStatus("ASSIGNED");
            
            alterationRepository.save(alteration);
            
            // Create notification
            notificationService.notifySubstitute(substitute, alteration);
            
            // Update workload
            updateWorkloadSummary(substitute, date);
        }
    }
}

private Staff findSubstitute(Timetable timetable, LocalDate date) {
    
    List<Staff> candidates = staffRepository.findByDepartmentId(
        timetable.getClassRoom().getDepartment().getId()
    );
    
    Map<Staff, Integer> scores = new HashMap<>();
    
    for (Staff candidate : candidates) {
        int score = calculateScore(candidate, timetable, date);
        scores.put(candidate, score);
    }
    
    return scores.entrySet().stream()
        .filter(e -> e.getValue() > 0)
        .max(Comparator.comparingInt(Map.Entry::getValue))
        .map(Map.Entry::getKey)
        .orElse(null);
}

private int calculateScore(Staff staff, Timetable timetable, LocalDate date) {
    
    int score = 100;
    
    // Priority 1: Check if present
    if (isAbsent(staff, date)) return 0;
    
    // Priority 2: Same class preference
    if (teachesSameClass(staff, timetable.getClassRoom())) {
        score += 50;
    }
    
    // Priority 3: Workload check
    int hoursOnDay = getHoursOnDay(staff, date);
    score -= hoursOnDay * 5;
    
    // Priority 4: Schedule clash check
    if (hasConsecutiveClash(staff, timetable)) {
        score -= 30;
    }
    
    // Priority 5: Subject preference
    if (teachesSameSubject(staff, timetable.getSubject())) {
        score += 20;
    }
    
    // Priority 6: Weekly workload
    int weeklyHours = getWeeklyHours(staff, date);
    score -= weeklyHours;
    
    return score;
}
```

### Module 5: Notification Module

**Purpose:** Provide real-time notifications to staff about alterations and events

**Components:**
- **NotificationController.java**: REST endpoints for notification retrieval
- **NotificationService.java**: Business logic for notification management
- **Notification.java**: JPA entity for notifications
- **NotificationRepository.java**: Data access queries
- **NotificationDTO.java**: Data transfer object
- **EmailNotificationProvider.java**: Email sending
- **SmsNotificationProvider.java**: SMS sending
- **WebSocketConfig.java**: WebSocket configuration

**Key Features:**
- Real-time WebSocket notifications
- Email notifications with SMTP integration
- SMS notifications with mock provider
- Notification types: ALTERATION, ATTENDANCE, SYSTEM
- Read/unread status tracking
- Notification history
- Filter and search capabilities
- Automatic cleanup of old notifications

**Sample Implementation:**
```java
@Service
public class NotificationService {
    
    public void notifySubstitute(Staff staff, Alteration alteration) {
        
        // Create notification record
        Notification notification = new Notification();
        notification.setStaff(staff);
        notification.setTitle("Staff Alteration Assignment");
        notification.setMessage(buildMessage(alteration));
        notification.setType("ALTERATION");
        notification.setAlteration(alteration);
        notification.setRead(false);
        
        notificationRepository.save(notification);
        
        // Send email
        emailProvider.send(
            staff.getEmail(),
            notification.getTitle(),
            notification.getMessage()
        );
        
        // Send WebSocket notification
        websocketService.notifyUser(staff.getId(), notification);
    }
}
```

### Module 6: Staff Management Module

**Purpose:** Manage staff information and department assignments

**Components:**
- **StaffController.java**: REST endpoints for staff operations
- **StaffService.java**: Business logic for staff management
- **Staff.java**: JPA entity
- **StaffRepository.java**: Data access queries
- **StaffDTO.java**: Data transfer object

**Key Features:**
- Create and update staff records
- Department assignment
- Status management (Active, Inactive, On Leave)
- Staff search and filtering
- Staff deactivation without data loss
- Bulk import capability

---

# 4. TESTING AND IMPLEMENTATION

## 4.1 Testing Strategy

### 4.1.1 Unit Testing

**Backend Unit Tests:**
- Service layer logic testing with mocked repositories
- DTO mapping and transformation testing
- Utility function testing
- Algorithm correctness testing (especially alteration algorithm)

**Frontend Unit Tests:**
- Component rendering tests
- Hook testing
- State management testing
- Utility function testing

### 4.1.2 Integration Testing

**Backend Integration Tests:**
- API endpoint testing
- Database interaction testing
- Authentication flow testing
- Alteration algorithm with real database

**Frontend Integration Tests:**
- Component interaction testing
- API client testing
- Navigation flow testing
- Form submission testing

### 4.1.3 Test Data

**Test Accounts:**
```
Staff1 / password123 - CS Department
Staff2 / password123 - CS Department
Staff3 / password123 - IT Department
Staff4 / password123 - IT Department
Staff5 / password123 - CS Department
```

**Test Scenarios:**
1. Mark attendance as absent
2. Verify alteration creation
3. Check substitute assignment accuracy
4. Validate notification delivery
5. Test timetable creation and conflicts
6. Verify workload calculations

### 4.1.4 Test Workflow

**Scenario 1: Attendance → Alteration Flow**
```
1. Login as Staff1
2. Mark attendance as ABSENT for today
3. Verify alteration created automatically
4. Check notification sent to substitute
5. Verify workload updated
```

**Scenario 2: Timetable Management**
```
1. Login as HOD
2. Create new timetable for CS1 class
3. Verify conflict detection works
4. Edit existing timetable entry
5. Delete timetable entry
```

**Scenario 3: Role-Based Access**
```
1. Test STAFF role permissions
2. Test HOD role permissions
3. Test ADMIN role permissions
4. Verify unauthorized access denied
```

## 4.2 Implementation Checklist

- [x] Backend: Spring Boot application set up
- [x] Frontend: React + TypeScript + Vite configured
- [x] Database: PostgreSQL schema with migrations
- [x] Authentication: JWT implementation
- [x] Attendance marking: Working and tested
- [x] Alteration algorithm: 6-priority implementation complete
- [x] Notifications: Email and WebSocket configured
- [x] Timetable management: Full CRUD operations
- [x] Staff management: Complete implementation
- [x] Error handling: Comprehensive exception handling
- [x] Logging: SLF4J configured
- [x] CORS: Frontend-backend communication enabled
- [x] Data validation: Frontend and backend validation
- [x] Responsive UI: Tailwind CSS styling applied

## 4.3 Deployment Steps

### Backend Deployment:
```bash
cd backend
gradle clean build
java -jar build/libs/staff-alteration-system-1.0.0.jar
```

### Frontend Deployment:
```bash
cd frontend
npm install
npm run build
# Deploy dist/ folder to web server
```

---

# 5. CONCLUSION

The Staff Alteration System represents a comprehensive solution for managing academic staff, class schedules, and automatic substitute assignments in educational institutions. Through the implementation of intelligent algorithms and modern web technologies, the system significantly reduces administrative burden and improves operational efficiency.

## 5.1 Key Achievements

1. **Automation**: Fully automated alteration assignment process eliminates manual work
2. **Intelligence**: 6-priority algorithm ensures optimal substitute selection
3. **Scalability**: 3-layer architecture allows for easy expansion and modification
4. **Security**: JWT-based authentication with role-based access control
5. **User Experience**: Intuitive React frontend with responsive design
6. **Data Integrity**: PostgreSQL with Flyway migrations ensures data consistency
7. **Real-time Updates**: WebSocket integration for instant notifications
8. **Reliability**: Comprehensive error handling and validation

## 5.2 Future Enhancements

1. **Advanced Analytics**: Machine learning for predicting absences
2. **Mobile App**: Native mobile applications for iOS and Android
3. **Multi-language Support**: Internationalization for multiple languages
4. **Integration**: Integration with existing institutional systems (ERP, HRMS)
5. **Performance Optimization**: Caching strategies and database optimization
6. **Advanced Scheduling**: AI-based automatic timetable generation
7. **Audit Trail**: Comprehensive logging and audit capabilities
8. **API Rate Limiting**: Implement rate limiting for API security

## 5.3 Maintenance and Support

- Regular database backups (daily)
- Security patches and updates
- Performance monitoring and optimization
- Log monitoring and analysis
- User support and training
- Documentation updates

## 5.4 Project Success Metrics

- **System Uptime**: Target 99.9% availability
- **Response Time**: API responses within 200ms
- **User Adoption**: 95%+ of staff using the system
- **Alteration Accuracy**: 98%+ correct substitute assignments
- **User Satisfaction**: 4.5+/5 rating in user surveys

---

# BIBLIOGRAPHY

1. **Spring Boot Documentation**
   - Official Spring Boot Reference Guide: https://spring.io/projects/spring-boot
   - Spring Security Guide: https://spring.io/projects/spring-security

2. **React Documentation**
   - React Official Documentation: https://react.dev
   - TypeScript Handbook: https://www.typescriptlang.org/docs

3. **Database Technologies**
   - PostgreSQL Official Documentation: https://www.postgresql.org/docs
   - Flyway Database Migration: https://flywaydb.org/documentation

4. **JWT Authentication**
   - JWT Introduction: https://jwt.io/introduction
   - JJWT Library: https://github.com/jwtk/jjwt

5. **Frontend Technologies**
   - Tailwind CSS Documentation: https://tailwindcss.com/docs
   - Zustand State Management: https://github.com/pmndrs/zustand
   - Vite Build Tool: https://vitejs.dev/guide

6. **Best Practices**
   - RESTful API Design: https://restfulapi.net
   - Clean Code Principles: https://www.oreilly.com/library/view/clean-code-a/9780136083238
   - SOLID Principles: https://en.wikipedia.org/wiki/SOLID

7. **Project Management**
   - Agile Development Practices
   - Software Architecture Patterns
   - Version Control with Git

---

# APPENDICES

## A. Data Flow Diagram

### A.1 System-Level DFD

```
┌─────────────────┐
│   End User      │
└────────┬────────┘
         │
    ┌────┴───────────────────────────────────┐
    │                                        │
    ▼                                        ▼
┌─────────────┐                      ┌──────────────┐
│   Login     │                      │ Admin Panel  │
└────┬────────┘                      └──────────────┘
     │
     └──────────────────┬──────────────────┘
                        │
                  ┌─────▼─────┐
                  │  Spring   │
                  │  Backend  │
                  └─────┬─────┘
                        │
                    ┌───┴───────────────────────┐
                    │                           │
           ┌────────▼────────┐      ┌──────────▼──────────┐
           │ Attendance      │      │ Alteration         │
           │ Service         │      │ Service            │
           └────────┬────────┘      └──────────┬──────────┘
                    │                           │
                    │                      ┌────▼─────────────┐
                    │                      │ Scoring & Select │
                    │                      │ Substitute       │
                    │                      └────┬─────────────┘
                    │                           │
                    └───────────────┬───────────┘
                                    │
                            ┌───────▼────────┐
                            │ PostgreSQL DB  │
                            │ (Persisted)    │
                            └────────────────┘
```

### A.2 Alteration Process DFD

```
Staff marks
  Absence
    │
    ▼
Absence Entry
  Validated
    │
    ▼
Query Timetable
 for that day
    │
    ▼
For each class:
   │
   ├─► Get Candidates
   │     from Department
   │
   ├─► Priority Check 1
   │   (Is candidate present?)
   │
   ├─► Priority Check 2
   │   (Same class experience)
   │
   ├─► Priority Check 3
   │   (Least hours today)
   │
   ├─► Priority Check 4
   │   (No schedule clash)
   │
   ├─► Priority Check 5
   │   (Same subject)
   │
   ├─► Priority Check 6
   │   (Weekly workload)
   │
   ▼
Select Best
  Candidate
    │
    ▼
Create Alteration
   Record
    │
    ▼
Send Notification
    │
    ▼
Update Workload
    │
    ▼
Return Success
```

## B. Table Structure

### B.1 Complete Table Schemas with Sample Data

**USER_ACCOUNT:**
| id | username | email | enabled | created_at |
|----|----------|-------|---------|-----------|
| 1 | Staff1 | staff1@college.edu | true | 2026-01-01 |
| 2 | Staff2 | staff2@college.edu | true | 2026-01-01 |
| 3 | Staff3 | staff3@college.edu | true | 2026-01-01 |

**STAFF:**
| id | staff_id | first_name | last_name | email | dept_id | user_id | status |
|----|----------|-----------|-----------|-------|---------|--------|--------|
| 1 | S001 | John | Doe | john@college.edu | 1 | 1 | ACTIVE |
| 2 | S002 | Jane | Smith | jane@college.edu | 1 | 2 | ACTIVE |
| 3 | S003 | Bob | Johnson | bob@college.edu | 2 | 3 | ACTIVE |

**DEPARTMENT:**
| id | code | name |
|----|------|------|
| 1 | CS | Computer Science |
| 2 | IT | Information Technology |

**CLASS:**
| id | code | name | dept_id |
|----|------|------|---------|
| 1 | CS1 | B.Tech CS 1st Year | 1 |
| 2 | CS2 | B.Tech CS 2nd Year | 1 |

**SUBJECT:**
| id | code | name | dept_id |
|----|------|------|---------|
| 1 | C01 | Data Structures | 1 |
| 2 | C02 | Database Management | 1 |

**TIMETABLE:**
| id | staff_id | subject_id | class_id | day_order | period |
|----|----------|-----------|----------|-----------|---------|
| 1 | 1 | 1 | 1 | 1 | 1 |
| 2 | 2 | 2 | 2 | 2 | 2 |

**ATTENDANCE:**
| id | staff_id | date | status | day_type |
|----|----------|------|--------|----------|
| 1 | 1 | 2026-02-12 | PRESENT | FULL_DAY |
| 2 | 2 | 2026-02-12 | ABSENT | FULL_DAY |

**ALTERATION:**
| id | timetable_id | original_staff_id | substitute_staff_id | date | status |
|----|--------------|------------------|-------------------|------|--------|
| 1 | 1 | 2 | 3 | 2026-02-12 | ASSIGNED |

## C. Sample Coding

### C.1 Backend Code Samples

**Authentication Controller:**
```java
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    @Autowired
    private AuthenticationService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request) {
        try {
            AuthResponseDTO response = authService.authenticate(request);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(new ApiResponseDTO(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        try {
            UserDTO user = authService.getUserById(userId);
            return ResponseEntity.ok(new ApiResponseDTO(true, "User retrieved", user));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                .body(new ApiResponseDTO(false, "User not found", null));
        }
    }
}
```

**Attendance Service:**
```java
@Service
@Slf4j
@Transactional
public class AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private AlterationService alterationService;
    
    public AttendanceDTO markAttendance(Long staffId, AttendanceMarkDTO dto) {
        
        Staff staff = staffRepository.findById(staffId).orElseThrow();
        
        // Check duplicate
        if (attendanceRepository.existsByStaffIdAndAttendanceDate(staffId, dto.getAttendanceDate())) {
            throw new DuplicateAttendanceException("Already marked");
        }
        
        Attendance attendance = Attendance.builder()
            .staff(staff)
            .attendanceDate(dto.getAttendanceDate())
            .status(dto.getStatus())
            .dayType(dto.getDayType())
            .remarks(dto.getRemarks())
            .build();
        
        attendanceRepository.save(attendance);
        
        // Trigger alteration if absent
        if ("ABSENT".equals(dto.getStatus())) {
            alterationService.processAlteration(staff, dto.getAttendanceDate());
        }
        
        return mapToDTO(attendance);
    }
}
```

### C.2 Frontend Code Samples

**Login Component:**
```typescript
export function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const setAuthToken = useAuthStore(s => s.setAuthToken);
  
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await axios.post('/api/auth/login', {
        username,
        password
      });
      setAuthToken(response.data.data.token);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed');
    }
  };
  
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 to-blue-800">
      <form onSubmit={handleLogin} className="max-w-md mx-auto pt-20">
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className="w-full px-4 py-2 mb-4 border rounded"
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full px-4 py-2 mb-4 border rounded"
        />
        {error && <p className="text-red-500 mb-4">{error}</p>}
        <button type="submit" className="w-full bg-white text-blue-600 font-bold py-2">
          Login
        </button>
      </form>
    </div>
  );
}
```

**Attendance Marking Component:**
```typescript
export function AttendancePage() {
  const [date, setDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [status, setStatus] = useState<string>('PRESENT');
  const [dayType, setDayType] = useState<string>('FULL_DAY');
  const [loading, setLoading] = useState(false);
  
  const handleMarkAttendance = async () => {
    setLoading(true);
    try {
      await axios.post('/api/attendance/mark', {
        attendanceDate: date,
        status,
        dayType
      });
      alert('Attendance marked successfully!');
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to mark attendance');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="p-6 bg-white rounded-lg shadow">
      <h2 className="text-2xl font-bold mb-6">Mark Attendance</h2>
      
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-2">Date</label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="w-full px-4 py-2 border rounded"
          />
        </div>
        
        <div>
          <label className="block text-sm font-medium mb-2">Status</label>
          <div className="space-y-2">
            {['PRESENT', 'ABSENT', 'HALF_DAY'].map(s => (
              <label key={s} className="flex items-center">
                <input
                  type="radio"
                  name="status"
                  value={s}
                  checked={status === s}
                  onChange={(e) => setStatus(e.target.value)}
                  className="mr-2"
                />
                {s}
              </label>
            ))}
          </div>
        </div>
        
        <button
          onClick={handleMarkAttendance}
          disabled={loading}
          className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
        >
          {loading ? 'Marking...' : 'Mark Attendance'}
        </button>
      </div>
    </div>
  );
}
```

## D. Sample Input

### D.1 Login Input
```json
{
  "username": "Staff1",
  "password": "password123"
}
```

### D.2 Mark Attendance Input
```json
{
  "attendanceDate": "2026-02-12",
  "status": "ABSENT",
  "dayType": "FULL_DAY",
  "remarks": "Medical emergency"
}
```

### D.3 Create Timetable Input
```json
{
  "staffId": 1,
  "subjectId": 1,
  "classId": 1,
  "dayOrder": 1,
  "periodNumber": 1
}
```

### D.4 Create Staff Input
```json
{
  "staffId": "S006",
  "firstName": "Alice",
  "lastName": "Williams",
  "email": "alice@college.edu",
  "phoneNumber": "9876543210",
  "departmentId": 1,
  "password": "securePassword123"
}
```

## E. Sample Output

### E.1 Login Response
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "Staff1",
      "email": "staff1@college.edu",
      "roles": ["STAFF"]
    }
  },
  "timestamp": "2026-02-12T10:30:00Z"
}
```

### E.2 Mark Attendance Response
```json
{
  "success": true,
  "message": "Attendance marked successfully",
  "data": {
    "id": 1,
    "staffId": 1,
    "attendanceDate": "2026-02-12",
    "status": "ABSENT",
    "dayType": "FULL_DAY",
    "remarks": "Medical emergency",
    "createdAt": "2026-02-12T10:30:00Z"
  },
  "timestamp": "2026-02-12T10:30:00Z"
}
```

### E.3 Alteration Creation Response
```json
{
  "success": true,
  "message": "Alteration created automatically",
  "data": {
    "id": 1,
    "originalStaffId": 1,
    "originalStaffName": "John Doe",
    "substituteStaffId": 3,
    "substituteStaffName": "Bob Johnson",
    "alterationDate": "2026-02-12",
    "className": "CS1",
    "subjectName": "Data Structures",
    "dayOrder": 1,
    "periodNumber": 1,
    "status": "ASSIGNED",
    "createdAt": "2026-02-12T10:32:00Z"
  },
  "timestamp": "2026-02-12T10:32:00Z"
}
```

### E.4 Get Alterations Response
```json
{
  "success": true,
  "message": "Alterations retrieved successfully",
  "data": [
    {
      "id": 1,
      "originalStaffName": "John Doe",
      "substituteStaffName": "Bob Johnson",
      "alterationDate": "2026-02-12",
      "className": "CS1",
      "subjectName": "Data Structures",
      "status": "ASSIGNED"
    },
    {
      "id": 2,
      "originalStaffName": "Jane Smith",
      "substituteStaffName": "Alice Williams",
      "alterationDate": "2026-02-12",
      "className": "CS2",
      "subjectName": "Database Management",
      "status": "ACKNOWLEDGED"
    }
  ],
  "timestamp": "2026-02-12T10:35:00Z"
}
```

### E.5 Get Notifications Response
```json
{
  "success": true,
  "message": "Notifications retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "Staff Alteration Assignment",
      "message": "You have been assigned as substitute for John Doe's CS1 class on 2026-02-12, Period 1",
      "type": "ALTERATION",
      "isRead": false,
      "createdAt": "2026-02-12T10:32:00Z"
    },
    {
      "id": 2,
      "title": "System Alert",
      "message": "Your workload has exceeded 8 hours today",
      "type": "SYSTEM",
      "isRead": false,
      "createdAt": "2026-02-12T14:00:00Z"
    }
  ],
  "timestamp": "2026-02-12T14:05:00Z"
}
```

### E.6 Dashboard Statistics Response
```json
{
  "success": true,
  "message": "Dashboard data retrieved successfully",
  "data": {
    "statistics": {
      "totalStaff": 15,
      "totalClasses": 12,
      "totalAlterations": 45,
      "pendingAlterations": 3
    },
    "recentActivities": [
      {
        "action": "Marked Absent",
        "time": "10:30 AM",
        "class": "CS1"
      },
      {
        "action": "Substitution Assigned",
        "time": "10:32 AM",
        "class": "CS1"
      }
    ]
  },
  "timestamp": "2026-02-12T14:10:00Z"
}
```

---

## Document Information

**Document Version**: 1.0
**Last Updated**: February 12, 2026
**Project Name**: Staff Alteration System
**Project Version**: 1.0.0
**Total Pages**: 50+
**Status**: Complete and Production-Ready

---

# APPENDIX F: PAGE OUTPUT SPECIFICATIONS & FIELD DETAILS

This section provides detailed output fields for each page/module in the Staff Alteration System.

---

## F.1 LOGIN PAGE

**Page Purpose**: User authentication and session initiation

**Display Fields**:
- Application Logo
- Title: "Staff Alteration System"
- Subtitle: "Login to your account"

**Input Fields**:
- Username (Text input, alphanumeric)
- Password (Secure password field)
- Remember Me (Checkbox)
- Login Button
- Language/Theme selector (optional)

**Output/Feedback Fields**:
- Error messages (Red background with error icon)
- Loading state (Spinner during authentication)
- Success toast notification (Green toast)
- Redirect to Dashboard on successful login

**User Roles Affected**: All users (STAFF, HOD, DEAN, ADMIN)

---

## F.2 DASHBOARD PAGE

**Page Purpose**: Main system overview and quick access to features

### Header Section
- Welcome message: "Welcome, [Username]! 👋"
- User's first name or username
- Dashboard type indicator (Staff/HOD/Admin Dashboard)
- Quick action buttons (View Timetables, Mark Attendance)

### Quick Info Cards
| Field | Display | Type |
|---|---|---|
| Staff ID | [User's Staff ID] | Text |
| Department | [User's Department] | Text |

### Quick Actions Grid (6 buttons)
- View Timetables
- Mark Attendance
- View Alterations
- Staff Directory (HOD/ADMIN only)
- Manage Classes (HOD/ADMIN only)
- Settings

### System Status Overview
| Component | Status | Indicator |
|---|---|---|
| Application Status | Operational | Green dot |
| Database Connection | Connected | Green dot |
| Email Service | Active/Inactive | Green/Red dot |

### Features Overview Section
- Timetable Management feature card
- Staff Assignment feature card
- Smart Notifications feature card

---

## F.3 MARK ATTENDANCE PAGE

**Page Purpose**: Record staff absence/leave/meeting and trigger automatic alterations

### Form Section - Attendance Status Selection
- **Status Options** (Radio buttons):
  - [ ] LEAVE (Full day absence)
  - [ ] MEETING (Partial absence for meeting)
  - [ ] PRESENT (Default)

### Date Selection Fields
| Field | Type | Range |
|---|---|---|
| Attendance Date | Date picker | Today to 30 days past |
| Multiple Days Toggle | Checkbox | Yes/No |
| End Date (if multiple) | Date picker | If multiple days enabled |

### Day Type Selection (for LEAVE)
- Full Day
- Morning Only (First Half)
- Afternoon Only (Second Half)

### Period Selection (for MEETING)
| Period | Time Range | Selectable |
|---|---|---|
| Period 1 | 9:00-10:00 | Checkbox |
| Period 2 | 10:00-11:00 | Checkbox |
| Period 3 | 11:00-12:00 | Checkbox |
| Period 4 | 12:00-1:00 | Checkbox |
| Period 5 | 1:00-2:00 | Checkbox |
| Period 6 | 2:00-3:00 | Checkbox |

### Additional Fields
- Remarks/Notes (Text area, optional)
- File Upload (Optional lesson plan documents)
- Submit Button

### Output Sections - After Submission
**Success Modal**:
- ✅ Attendance marked successfully for [N] day(s)
- Message: "Alterations created automatically"
- Alterations Created Count: [N]

**Created Alterations List** (if available):
| Field | Data |
|---|---|
| Original Staff | [Name] |
| Substitute Staff | [Name] |
| Class | [Class Code] |
| Subject | [Subject Name] |
| Day/Period | [Day] Period [N] |
| Status | ASSIGNED |

**Validation Errors** (if any):
- Red alert box with error message
- Field-level error highlights

---

## F.4 ATTENDANCE HISTORY PAGE

**Display Format**: Table view

### Attendance List Table
| Column | Data Type | Width |
|---|---|---|
| Date | Date | 15% |
| Status | Badge (Present/Absent/Half Day) | 15% |
| Day Type | Text | 15% |
| Remarks | Text (truncated) | 30% |
| Time Created | DateTime | 15% |
| Action | Edit/Delete buttons | 10% |

### Filters/Controls
- Date Range Filter (From - To)
- Status Filter (Dropdown: All, Present, Absent, Half Day)
- Search box (by remarks)
- Export to CSV button
- Items per page selector (10/25/50)

### Color Coding
- PRESENT: Green badge
- ABSENT: Red badge
- HALF_DAY: Yellow badge

---

## F.5 TIMETABLES PAGE

**Page Purpose**: View and manage class timetables

### Header Section
- Title: "Timetable Management"
- Current date display: [Day, Date]
- Date navigation (Previous/Next day buttons)

### Selection Controls
- **Class Filter** (Dropdown):
  - CS1 (B.Tech CS 1st Year)
  - CS2 (B.Tech CS 2nd Year)
  - IT1 (B.Tech IT 1st Year)
  - IT2 (B.Tech IT 2nd Year)

- **View Mode** (Toggle):
  - Grid View (Default)
  - Table View

- **Create New Timetable** Button (HOD/ADMIN only)

### Grid View (Main Display)

#### Days Header Row
| Monday | Tuesday | Wednesday | Thursday | Friday | Saturday |

#### Period Rows (1-5)
| Period | Mon | Tue | Wed | Thu | Fri | Sat |
|---|---|---|---|---|---|---|
| Period 1 (9:00-10:10) | [Details] | [Details] | [Details] | [Details] | [Details] | [Details] |
| Period 2 (10:10-11:05) | [Details] | [Details] | [Details] | [Details] | [Details] | [Details] |
| Period 3 (11:30-12:25) | [Details] | [Details] | [Details] | [Details] | [Details] | [Details] |
| Period 4 (12:25-1:20) | [Details] | [Details] | [Details] | [Details] | [Details] | [Details] |
| Period 5 (2:15-3:10) | [Details] | [Details] | [Details] | [Details] | [Details] | [Details] |

#### Break Rows
| Break (11:05-11:30) | Lunch (1:20-2:15) |

#### Cell Details (for each timetable slot)
- Staff Name
- Subject Code
- Subject Name
- Color-coded by subject
- Edit button (pencil icon)
- Delete button (trash icon)
- Hover shows full details

### Timetable Entry Details
```json
{
  "id": 1,
  "staffName": "John Doe",
  "subjectCode": "C01",
  "subjectName": "Data Structures",
  "classCode": "CS1",
  "dayOrder": 1,
  "periodNumber": 1,
  "createdAt": "2026-01-15"
}
```

### Empty Cells
- Display: "Free"
- Background: Light gray
- Click to add (if authorized)

---

## F.6 ALTERATIONS DASHBOARD PAGE

**Page Purpose**: Track staff substitutions and alteration assignments

### Tab Navigation
- **Tab 1**: "My Alterations (As Original Staff)"
- **Tab 2**: "My Alterations (As Substitute)"

### Filter & Control Section
- **Date Filter** (Dropdown):
  - Select specific alteration date
  - Sorted chronologically

- **Status Filter** (Dropdown):
  - All
  - ASSIGNED
  - ACKNOWLEDGED
  - COMPLETED
  - CANCELLED

### Alterations List (Accordion/Expandable format)

#### Summary Row (Collapsed)
| Column | Data |
|---|---|
| Date | [Alteration Date] |
| Original Staff | [Name] |
| Substitute Staff | [Name] |
| Class | [Class Code] |
| Subject | [Subject Name] |
| Period | [Day] Period [N] |
| Status Badge | [Status with color] |
| Actions | View / Acknowledge / Reject |

#### Details Row (Expanded)
```
Original Staff: [Name] (Staff ID: [ID])
Substitute Staff: [Name] (Staff ID: [ID])
Class: [Class Code] - [Class Name]
Subject: [Subject Code] - [Subject Name]
Schedule: [Day] - Period [N] ([Time])
Alteration Date: [Date]
Status: [Status]
Absence Type: [FN/AN/AF/ONDUTY]
Remarks: [Text]

Actions:
- View Lesson Plan (if exists)
- Download Lesson Plan
- Acknowledge (for substitute)
- Reject (for substitute)
- Update Status (for HOD/ADMIN)
```

### Status Badges
- **ASSIGNED**: Yellow background
- **ACKNOWLEDGED**: Blue background
- **COMPLETED**: Green background
- **CANCELLED**: Gray background

### Pagination
- Items per page: 5/10/20
- Page numbers or Load More button

---

## F.7 TIMETABLE MANAGEMENT PAGE (HOD/ADMIN)

**Page Purpose**: Create, edit, and manage timetables

### Create Modal Form Fields
```
Class: [Dropdown]
Staff Member: [Dropdown]
Subject: [Dropdown]
Day Order: [1-6 selector]
Period Number: [1-5 selector]
[Save] [Cancel]
```

### Timetable List View
- Same as Timetables Page (F.5) but with edit/delete permissions

### Edit Modal Form Fields
```
Class: [Editable dropdown]
Staff Member: [Editable dropdown]
Subject: [Editable dropdown]
Day Order: [Editable selector]
Period Number: [Editable selector]
[Update] [Cancel] [Delete]
```

---

## F.8 STAFF MANAGEMENT PAGE (HOD/ADMIN)

**Page Purpose**: Manage staff member profiles and assignments

### Control Section
- **Add New Staff** Button
- **Search Box** (by name, staff ID, email)
- **Filter** (by department, status)
- **Export** (to CSV)

### Staff List Table
| Column | Data Type | Features |
|---|---|---|
| Staff ID | Text | Sortable |
| First Name | Text | Sortable |
| Last Name | Text | Sortable |
| Email | Email | Sortable, clickable |
| Phone | Phone | Sortable |
| Department | Text | Sortable, filterable |
| Status | Badge | Color-coded |
| Created Date | Date | Sortable |
| Actions | Buttons | Edit, Delete, View Details |

### Status Badge Colors
- ACTIVE: Green
- INACTIVE: Gray
- ON_LEAVE: Yellow

### Create Staff Modal
```
Staff ID: [Text input]
First Name: [Text input]
Last Name: [Text input]
Email: [Email input]
Phone Number: [Phone input]
Department: [Dropdown]
Password: [Password input]
Confirm Password: [Password input]
[Create] [Cancel]
```

### Edit Staff Modal
```
Staff ID: [Display only]
First Name: [Text input]
Last Name: [Text input]
Email: [Email input]
Phone Number: [Phone input]
Department: [Dropdown]
Status: [Dropdown: Active, Inactive, On Leave]
[Update] [Cancel] [Delete]
```

### Staff Details View
```
Staff ID: S001
Name: John Doe
Email: john@college.edu
Phone: +91-9876543210
Department: Computer Science
Status: ACTIVE
Joined: 15 Jan 2025
Total Alterations: 12
Average Workload: 6.5 hours/day
[Edit] [Delete] [View History]
```

---

## F.9 CLASS MANAGEMENT PAGE (HOD/ADMIN)

**Page Purpose**: Manage classes/batches

### Control Section
- **Add New Class** Button
- **Search/Filter** (by class code, department)
- **Export** (to CSV)

### Classes List Table
| Column | Data Type | Features |
|---|---|---|
| Class Code | Text | Sortable |
| Class Name | Text | Sortable |
| Department | Text | Sortable, filterable |
| Created Date | Date | Sortable |
| Actions | Buttons | Edit, Delete |

### Create Class Modal
```
Class Code: [Text input] (e.g., CS1)
Class Name: [Text input] (e.g., B.Tech CS 1st Year)
Department: [Dropdown]
[Create] [Cancel]
```

### Edit Class Modal
```
Class Code: [Display only]
Class Name: [Text input]
Department: [Dropdown]
[Update] [Cancel] [Delete]
```

---

## F.10 DAY ORDER SCHEDULE PAGE

**Page Purpose**: View weekly schedule overview

### Display Format
- **Week View** (Monday-Saturday)
- **Day Order** (1-6 mapping to days)
- **All Classes** displayed simultaneously

### Schedule Grid
```
         Monday    Tuesday   Wednesday  Thursday  Friday   Saturday
Break    -         -         -          -         -        -
Period1  Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj
Period2  Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj
Period3  Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj
Lunch    -         -         -          -         -        -
Period4  Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj
Period5  Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj Class/Subj
```

### Filter Options
- Select Class (Dropdown)
- Select Staff (Dropdown)
- Export to PDF/Image

---

## F.11 HOD DASHBOARD PAGE

**Page Purpose**: Department-level analytics and alteration overview

### Statistics Cards
| Metric | Value | Icon |
|---|---|---|
| Total Alterations | [Number] | 📊 |
| Pending Alterations | [Number] | ⏳ |
| Unable to Alter | [Number] | ⚠️ |
| Average Workload | [Hours] | 📈 |

### Department Alterations List
**Filter by Date**: [Date Picker]

| Field | Display |
|---|---|
| Original Staff | [Name] |
| Substitute Staff | [Name] |
| Subject | [Subject Name] |
| Class | [Class Code] |
| Schedule | [Day] Period [N] |
| Status | [Badge] |
| Expand Details | [Arrow] |

### Details (Expanded)
```
Original Staff: [Name] ([Staff ID])
Substitute Staff: [Name] ([Staff ID])
Subject: [Code] - [Name]
Class: [Code] - [Name]
Day: [Day Name]
Period: [N] ([Time])
Alteration Date: [Date]
Status: [Status]
Absence Type: [Type]
[View Full Details] [Update Status]
```

### Department Statistics
- Total staff: [Number]
- Total classes: [Number]
- Absent today: [List]
- Staff on duty: [Number]

---

## F.12 PROFILE MODAL/PAGE

**Page Purpose**: Display and manage user profile information

### Header Section
- User Avatar (Large circular icon)
- User's Full Name (or Username)
- Username (secondary text)
- Close button (X)

### Profile Information Fields

| Icon | Field | Value | Type |
|---|---|---|---|
| 👤 | Username | [Username] | Display |
| 💼 | Staff ID | [Staff ID] | Display |
| ✉️ | Email Address | [Email] | Display/Editable |
| 📞 | Phone Number | [Phone] | Display/Editable |
| 🏢 | Department | [Department] | Display |
| 🛡️ | Assigned Roles | [Badge List] | Display |

### Role Badges (Colored)
- STAFF: Blue badge
- HOD: Purple badge
- DEAN: Orange badge
- ADMIN: Red badge

### Action Buttons
- Edit Profile (if permission)
- Change Password
- View History
- Logout
- Close Profile

### Additional Information (Optional)
- Account Status: [Active/Inactive]
- Account Created: [Date]
- Last Login: [Date & Time]
- Total Alterations as Original: [Count]
- Total Alterations as Substitute: [Count]

---

## F.13 SETTINGS PAGE

**Page Purpose**: User preferences and account settings

### Settings Tabs
1. **General Settings**
   - Theme (Light/Dark)
   - Language (English/etc.)
   - Notifications enabled/disabled
   - [Save]

2. **Account Settings**
   - Change Password
     - Current Password: [Input]
     - New Password: [Input]
     - Confirm Password: [Input]
     - [Save] [Cancel]
   
   - Email Preferences
     - Email for alterations: [Checkbox]
     - Email for attendance: [Checkbox]
     - Email digest: [Dropdown]
     - [Save]

3. **Security Settings**
   - Active Sessions: [List with IP/device]
   - Logout All Sessions: [Button]
   - Two-Factor Authentication: [Toggle]
   - API Keys (if applicable): [List with manage option]

4. **Privacy**
   - Data Download (GDPR): [Button]
   - Account Deletion: [Button with warning]

---

## F.14 NOTIFICATIONS PAGE

**Page Purpose**: View and manage notifications

### Notification List
| Field | Display | Type |
|---|---|---|
| Icon | Notification type icon | Visual |
| Title | [Notification Title] | Text |
| Message | [Message text, truncated] | Text |
| Date & Time | [When received] | DateTime |
| Read Status | Unread indicator (blue dot) | Visual |
| Actions | Mark read, Delete | Buttons |

### Notification Types
- **ALTERATION**: 🔄 Alteration assignment
- **ATTENDANCE**: 📅 Attendance-related
- **SYSTEM**: ⚙️ System updates

### Filters
- By Type (Dropdown)
- By Date Range (Date picker)
- Unread only (Checkbox)

### Bulk Actions
- Mark all as read
- Delete selected
- Clear all notifications

### Notification Detail (Expanded)
```
Title: [Full title]
Type: [Type badge]
Message: [Full message]
Received: [Full date & time]
Related To: [If applicable - Alteration ID/Link]
[Mark as Read] [Delete] [Close]
```

---

## Summary of All Page Output Fields

| Page | Primary Fields | Status Output | Action Output |
|---|---|---|---|
| Login | Username, Password | Success/Error toast | Redirect/Modal |
| Dashboard | Stats cards, Quick actions | System status | Navigation |
| Attendance | Date, Status, Day type, Remarks | Success count | Alteration list |
| Attendance History | Table data | Filters | Export |
| Timetables | Grid cells, Staff/Subject | Color-coded | Edit/Delete |
| Alterations | Alteration details | Status badge | Acknowledge/Reject |
| Staff Management | Staff table | CRUD status | Modal forms |
| Class Management | Classes table | CRUD status | Modal forms |
| Day Schedule | Weekly grid | Classes displayed | PDF export |
| HOD Dashboard | Stat cards, Alterations list | Department metrics | Status updates |
| Profile | User info fields | Account status | Edit/Security |
| Settings | Toggle/Input fields | Save confirmations | Apply changes |
| Notifications | Notification list | Type badges | Read/Delete |

---

**End of Appendix F**
