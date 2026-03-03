# Staff Alteration System

A comprehensive staff management and alteration system built with Spring Boot (backend) and React + TypeScript (frontend).

## 📋 Project Overview

The Staff Alteration System is designed to manage:
- **Staff Authentication**: JWT-based authentication with role-based access control
- **Timetable Management**: Create and manage class timetables
- **Attendance Tracking**: Mark daily attendance and track absences
- **Staff Alteration**: Automatically assign substitute staff when regular staff members are absent
- **Dashboard & Reporting**: View statistics, alterations, and staff information

## 🏗️ Architecture

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15+ (Supabase)
- **Authentication**: JWT (JJWT 0.12.3)
- **ORM**: Hibernate/JPA
- **Database Migrations**: Flyway

### Frontend
- **Framework**: React 18.2.0
- **Language**: TypeScript 5.2.2
- **Build Tool**: Vite 5.0.0
- **Styling**: Tailwind CSS 3.3.5
- **State Management**: Zustand 4.4.5
- **HTTP Client**: Axios 1.6.2
- **Icons**: Lucide React

## 📁 Project Structure

```
StaffAlteration/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/staffalteration/
│   │   ├── config/                  # Spring configuration
│   │   ├── controller/              # REST API endpoints
│   │   ├── dto/                     # Data Transfer Objects
│   │   ├── entity/                  # JPA entities
│   │   ├── repository/              # Spring Data repositories
│   │   ├── service/                 # Business logic
│   │   ├── security/                # JWT & security
│   │   ├── DataInitializer.java     # Database initialization
│   │   └── StaffAlterationApplication.java
│   ├── src/main/resources/
│   │   ├── application.properties   # App configuration
│   │   └── db/migration/            # Flyway migrations
│   ├── build.gradle
│   └── README.md
│
├── frontend/                         # React TypeScript application
│   ├── src/
│   │   ├── api/                     # API client & endpoints
│   │   ├── components/              # React components
│   │   ├── pages/                   # Page components
│   │   ├── store/                   # Zustand stores
│   │   ├── App.tsx                  # Main app component
│   │   └── main.tsx                 # Entry point
│   ├── vite.config.ts               # Vite configuration
│   ├── tailwind.config.js           # Tailwind configuration
│   ├── package.json
│   └── README.md
│
├── .gitignore
└── README.md (this file)
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 16+ and npm
- PostgreSQL 15+ (or Supabase account)
- Git

### Backend Setup

1. **Configure Database**
   ```bash
   cd backend
   # Edit src/main/resources/application.properties
   # Set your database credentials
   ```

2. **Build and Run**
   ```bash
   gradle clean build
   java -jar build/libs/staff-alteration-system-1.0.0.jar
   ```

   The backend will start on `http://localhost:8080/api`

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start Development Server**
   ```bash
   npm run dev
   ```

   The frontend will start on `http://localhost:3000`

## 🔐 Authentication

### Default Test Credentials
- **Username**: Staff1
- **Password**: password123

### JWT Configuration
- **Access Token Expiry**: 24 hours
- **Refresh Token Expiry**: 7 days
- **Token Type**: Bearer

## 📚 API Endpoints

### Authentication
- `POST /auth/login` - Login with credentials
- `POST /auth/logout` - Logout
- `POST /auth/refresh` - Refresh access token

### Attendance
- `POST /attendance/mark` - Mark attendance
- `GET /attendance/staff/{staffId}` - Get staff attendance records
- `GET /attendance/date/{date}` - Get attendance by date
- `GET /attendance/absent/{date}` - Get absent staff for a date

### Timetables
- `GET /timetable` - Get all timetables
- `POST /timetable` - Create new timetable
- `GET /timetable/{id}` - Get timetable details
- `PUT /timetable/{id}` - Update timetable
- `DELETE /timetable/{id}` - Delete timetable

### Alterations
- `GET /alteration` - Get all alterations
- `GET /alteration/date/{date}` - Get alterations for a date
- `POST /alteration` - Create alteration
- `PUT /alteration/{id}` - Update alteration status

## 🗄️ Database Schema

### Key Entities
- **User Account**: System users with authentication
- **Role**: User roles (STAFF, HOD, DEAN, ADMIN)
- **Staff**: Staff member information
- **Department**: Department information
- **Subject**: Course subjects
- **ClassRoom**: Class information
- **Timetable**: Class schedules
- **Timetable Template**: Template for creating timetables
- **Attendance**: Daily attendance records
- **Alteration**: Staff substitution records

## 🔄 Features

### Staff Management
- View all staff members
- Manage staff roles and departments
- Track staff status

### Timetable Management
- Create class timetables
- Assign subjects and staff
- View weekly schedules
- Template-based timetable creation

### Attendance System
- Mark daily attendance (Present/Absent/Leave)
- Support for half-day options
- Automatic alteration assignment on absences
- Attendance history tracking

### Alteration System
- Automatic substitute staff assignment
- Priority-based staff selection
- Workload balancing
- Alteration status tracking

### Dashboard
- Statistics and KPIs
- Recent activities
- Quick actions
- Role-based views

## 🛠️ Development

### Backend Development
```bash
cd backend
gradle bootRun  # Run with hot reload
gradle test     # Run tests
```

### Frontend Development
```bash
cd frontend
npm run dev     # Development server with hot reload
npm run build   # Build for production
npm run preview # Preview production build
```

## 📝 Database Migrations

Flyway migrations are managed in `backend/src/main/resources/db/migration/`

Current migrations:
- `V1__Create_Initial_Schema.sql` - Initial database schema
- `V2__Create_Timetable_Template.sql` - Timetable template tables
- `V3__Fix_Timetable_Template_Staff_FK.sql` - Schema corrections
- `V4__Add_Missing_Columns_v2.sql` - Additional columns

## 🔒 Security

- **JWT Authentication**: Token-based authentication
- **Password Hashing**: BCrypt encryption
- **Role-Based Access Control**: @PreAuthorize annotations
- **CORS Configuration**: Restricted to localhost/configured origins
- **Database Security**: SSL connections to database

## 📊 Monitoring

Enable detailed logging:
```properties
logging.level.root=INFO
logging.level.com.staffalteration=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## 🐛 Troubleshooting

### Backend Issues
- Ensure PostgreSQL is running and accessible
- Check database credentials in `application.properties`
- Verify Flyway migrations have run successfully

### Frontend Issues
- Clear `node_modules` and `npm install` again
- Check proxy configuration in `vite.config.ts`
- Ensure backend is running on port 8080

## 📦 Dependencies

See `backend/build.gradle` and `frontend/package.json` for complete dependency lists.

### Key Backend Dependencies
- Spring Boot Starter Web
- Spring Data JPA
- Spring Security
- Spring WebSocket
- JJWT (JWT library)
- Lombok
- PostgreSQL Driver
- Flyway

### Key Frontend Dependencies
- React
- React Router
- Zustand
- Axios
- Tailwind CSS
- TypeScript

## 🚢 Deployment

### Backend Deployment
```bash
cd backend
gradle clean build
# Deploy staff-alteration-system-1.0.0.jar to your server
java -jar staff-alteration-system-1.0.0.jar
```

### Frontend Deployment
```bash
cd frontend
npm run build
# Deploy dist/ folder to your web server
```

## 📄 License

This project is proprietary. All rights reserved.

## 👥 Contributors

- Bubbalan Shanmugam

## 📞 Support

For issues and questions, please create a GitHub issue or contact the development team.

---

**Last Updated**: December 28, 2025
**Version**: 1.0.0