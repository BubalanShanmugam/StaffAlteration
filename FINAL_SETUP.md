# Staff Alteration System - Complete Setup Guide

## 🎯 Project Overview
**Staff Alteration System** is an intelligent staff substitution management system that automatically handles staff absences and allocates substitutes based on priority criteria.

### Key Features:
✅ **Staff Attendance Marking** - Mark attendance (Full/Half day)  
✅ **Automatic Alteration** - Smart substitute allocation based on 4 priority criteria  
✅ **Lesson Plan Sharing** - Upload notes and materials for substitutes  
✅ **HOD Dashboard** - Monitor and manage all alterations  
✅ **Role-Based Access** - Staff, HOD, Admin roles with different permissions  
✅ **Real-time Notifications** - App notifications for alterations  
✅ **Timetable Management** - Create and edit timetables via UI  

---

## 🏗️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java
- **Database**: PostgreSQL (Supabase)
- **Build Tool**: Gradle
- **Security**: JWT Authentication
- **ORM**: JPA/Hibernate

### Frontend
- **Framework**: React 18 + TypeScript
- **Build Tool**: Vite
- **UI Framework**: Tailwind CSS
- **State Management**: Zustand
- **HTTP Client**: Axios
- **Icons**: Lucide React

---

## 📋 Prerequisites

### System Requirements
- **Java**: 17 or higher
- **Node.js**: 16+ with npm
- **PostgreSQL**: 12+ (or Supabase account)
- **Git**: Latest version

### Development Tools (Recommended)
- VS Code with extensions:
  - Java Extension Pack
  - Spring Boot Extension Pack
  - ES7+ React/Redux/React-Native snippets
  - Tailwind CSS IntelliSense

---

## 🚀 Setup Instructions

### 1. Clone & Project Structure
```bash
# Navigate to workspace
cd /path/to/StaffAlteration

# Backend setup
cd backend

# Frontend setup
cd ../frontend
```

### 2. Backend Setup

#### 2.1 Configure Database (PostgreSQL)
```sql
-- Create database
CREATE DATABASE staff_alteration;

-- Create user (if needed)
CREATE USER sa_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE staff_alteration TO sa_user;
```

#### 2.2 Update application.properties
File: `backend/src/main/resources/application.properties`

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/staff_alteration
spring.datasource.username=sa_user
spring.datasource.password=secure_password
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=your-secret-key-here-min-256-bits-long-for-security
jwt.expiration=86400000
jwt.refreshTokenExpiration=604800000

# Logging
logging.level.root=INFO
logging.level.com.staffalteration=DEBUG
```

#### 2.3 Build Backend
```bash
cd backend
gradle clean build -x test
```

#### 2.4 Run Backend
```bash
# Option 1: Using gradle
gradle bootRun

# Option 2: Using java
java -jar build/libs/staff-alteration-*.jar
```

✅ Backend running at: `http://localhost:8080/api`

### 3. Frontend Setup

#### 3.1 Install Dependencies
```bash
cd frontend
npm install
```

#### 3.2 Configure Backend URL
File: `frontend/src/api/client.ts`

```typescript
const API_BASE_URL = import.meta.env.DEV ? '/api' : '/api'
```

#### 3.3 Run Development Server
```bash
npm run dev
```

✅ Frontend running at: `http://localhost:3000`

#### 3.4 Build for Production
```bash
npm run build
```

Output: `frontend/dist/`

---

## 📊 Default Test Data

The system auto-seeds the following default data on first run:

### Users (Staff)
| ID | Username | Password | Department | Role |
|---|---|---|---|---|
| 1 | Staff1 | password123 | CS | STAFF |
| 2 | Staff2 | password123 | CS | STAFF |
| 3 | Staff3 | password123 | IT | STAFF |
| 4 | Staff4 | password123 | IT | STAFF |
| 5 | Staff5 | password123 | CS | STAFF |

**Default Password**: `password123`

### Classes
- **CS1** - Computer Science Semester 1
- **CS2** - Computer Science Semester 2
- **IT1** - Information Technology Semester 1
- **IT2** - Information Technology Semester 2

### Subjects
- **JAVA** - Java Programming
- **PY** - Python Programming
- **WEB** - Web Development
- **DB** - Database Systems

### Timetable (Pre-populated)
A sample weekly timetable is created with staff assigned to various periods.

---

## 🔐 User Roles & Permissions

### STAFF
- ✅ Mark attendance (Today or future 7 days)
- ✅ View their alterations (as original or substitute)
- ✅ Upload lesson plans when absent
- ✅ Acknowledge substitute assignments
- ✅ View timetables
- ❌ Cannot manage timetables

### HOD (Head of Department)
- ✅ All STAFF permissions
- ✅ HOD Dashboard (view all alterations for date)
- ✅ Manage timetables (edit/create)
- ✅ View workload statistics
- ✅ Contact staff

### ADMIN
- ✅ All HOD permissions
- ✅ System administration

---

## 🧮 Alteration Algorithm

When a staff marks **ABSENT**, the system automatically finds the best substitute using this **priority-driven algorithm**:

### Priority Criteria (in order):
1. **Presence** - Must be present on that day
2. **Class Familiarity** - Already teaches the same class
3. **Workload** - Lowest number of hours today
4. **No Consecutive Clash** - Doesn't teach immediately before/after
5. **Subject Match** - Prefers same subject (tie-breaker)
6. **Weekly Workload** - Least workload overall (final tie-breaker)

### Score Calculation:
```
Score = (hoursToday × 1000) + (hasClash ? 500 : 0) + (sameSubject ? 0 : 100) + weeklyWorkload
Lower score = Better candidate
```

### Example:
- Staff **absent** on Monday, Period 3 (Java, CS1)
- System finds all **present staff**
- Filters those already **teaching CS1** (if any)
- Picks staff with **least hours today**
- Ensures **no previous/next period conflict**
- Breaks ties by **weekly workload**

---

## 📱 Application Features

### 1. **Attendance Marking Page** (`/attendance`)
- Mark attendance status (Present/Absent/Leave)
- Select day type (Full Day / Morning Only / Afternoon Only)
- Mark attendance for multiple days (up to 7 days)
- Add remarks/notes
- Upload lesson plans and materials (multiple files)

### 2. **Alterations Dashboard** (`/alterations`)
- View alterations as original staff (classes missing)
- View alterations as substitute (classes assigned to teach)
- Download lesson plans shared by original staff
- Acknowledge assignments
- View detailed notes and remarks

### 3. **HOD Dashboard** (`/hod-dashboard`)
- Real-time statistics:
  - Total alterations
  - Pending acknowledgments
  - Unable to alter count
  - Average workload
- Filterable alteration list by date
- View all details for each alteration
- Contact original/substitute staff

### 4. **Timetable Management** (`/manage-timetable`)
- Weekly timetable grid view (Monday-Saturday, 6 periods)
- Add/Edit/Delete timetable entries
- Select from subjects and staff
- Changes reflected immediately

### 5. **Timetable Viewing** (`/timetables`)
- View timetables by class
- Weekly schedule view
- Detailed table view
- Staff workload information

---

## 🔄 Workflow Example

### Scenario: Staff1 marks absent on Monday

1. **Staff1** logs in and goes to Attendance page
2. **Selects**:
   - Status: ABSENT
   - Day Type: FULL DAY
   - Date: Monday
   - Uploads lesson plans (PDF, Doc, etc.)

3. **System automatically**:
   - Retrieves Staff1's Monday timetable (CS1 Java, CS1 Python, IT1 Web)
   - For each class, finds best substitute using priority algorithm
   - Creates alteration records with status "ASSIGNED"
   - Triggers notifications

4. **Notifications sent to**:
   - ✉️ Email to original staff (Staff1)
   - ✉️ Email to substitutes (Staff2, Staff3, Staff4)
   - 🔔 App notifications

5. **Substitutes**:
   - Receive notification of assignment
   - Can download lesson plans from Staff1
   - Acknowledge the assignment
   - Teach the class

6. **HOD**:
   - Sees all alterations on dashboard
   - Can monitor acknowledgments
   - Can view detailed information

---

## 📡 API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/logout` - Logout

### Attendance
- `POST /api/attendance/mark` - Mark attendance
- `GET /api/attendance/staff/{staffId}` - Get staff attendance
- `GET /api/attendance/date/{date}` - Get attendance by date
- `GET /api/attendance/absent/{date}` - Get absent staff

### Alterations
- `GET /api/alteration/date/{date}` - Get alterations by date
- `GET /api/alteration/staff/{staffId}` - Get staff alterations
- `PUT /api/alteration/{id}/status` - Update status
- `PUT /api/alteration/{id}/acknowledge` - Acknowledge

### Timetables
- `GET /api/timetable-template/class/{classCode}` - Get class timetable
- `POST /api/timetable-template/create` - Create entry
- `PUT /api/timetable-template/update/{id}` - Update entry
- `DELETE /api/timetable-template/{id}` - Delete entry

---

## 🐛 Troubleshooting

### Backend Issues

**Database Connection Error**
```
Error: Cannot connect to database at localhost:5432
Solution: 
- Verify PostgreSQL is running
- Check database URL, username, password
- Ensure database 'staff_alteration' exists
```

**Port 8080 Already in Use**
```
Solution: 
- Kill process: lsof -i :8080 | kill -9 <PID>
- OR change port in application.properties: server.port=8081
```

**JWT Token Expired**
```
Solution:
- Frontend will automatically refresh token
- If still failing, logout and login again
```

### Frontend Issues

**API Calls 404**
```
Check:
- Backend is running on http://localhost:8080
- Proxy configured correctly in vite.config.ts
- Check browser console for actual URL being called
```

**Build Fails**
```
Solution:
- Delete node_modules: rm -rf node_modules
- Reinstall: npm install
- Clear cache: npm cache clean --force
- Rebuild: npm run build
```

**Port 3000 in Use**
```
Solution:
- Change port in vite.config.ts: port: 3001
- OR kill process: lsof -i :3000 | kill -9 <PID>
```

---

## 📈 Performance Optimization

### Backend
- Database indexes on frequently queried fields
- Lazy loading for relationships
- Pagination for large datasets
- JWT token caching

### Frontend
- Code splitting with Vite
- Image optimization with Tailwind
- State management with Zustand (lightweight)
- Lazy route loading

---

## 🔒 Security Checklist

- ✅ JWT token expiration (24 hours)
- ✅ Refresh token rotation (7 days)
- ✅ Password hashing with BCrypt
- ✅ Role-based access control
- ✅ CORS configuration
- ✅ SQL injection prevention (JPA)
- ✅ XSS protection (React escaping)

### Before Production:
1. Change JWT secret to 256+ bit random string
2. Enable HTTPS/SSL
3. Configure proper CORS origins
4. Use environment variables for secrets
5. Enable database encryption
6. Setup logging and monitoring
7. Configure rate limiting
8. Setup backup strategy

---

## 📚 Project Structure

```
StaffAlteration/
├── backend/
│   ├── src/main/java/com/staffalteration/
│   │   ├── controller/          # REST API endpoints
│   │   ├── service/             # Business logic
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── repository/          # Database access
│   │   ├── security/            # JWT & security
│   │   ├── config/              # Spring configuration
│   │   └── DataInitializer.java # Test data seed
│   ├── resources/
│   │   ├── application.properties
│   │   └── db/migration/        # Flyway migrations
│   └── build.gradle
│
├── frontend/
│   ├── src/
│   │   ├── pages/               # Page components
│   │   ├── components/          # Reusable components
│   │   ├── api/                 # API client
│   │   ├── store/               # Zustand stores
│   │   ├── App.tsx              # Routes
│   │   └── main.tsx
│   ├── public/                  # Static assets
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
│
└── README.md
```

---

## 🚀 Deployment Guide

### Docker Setup (Recommended)

#### Backend Dockerfile
```dockerfile
FROM openjdk:17-jdk
COPY build/libs/staff-alteration-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

#### Frontend Dockerfile
```dockerfile
FROM node:18 as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
```

#### Docker Compose
```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/staff_alteration
  
  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
  
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: staff_alteration
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Deployment Steps
1. Build both backend and frontend
2. Push to container registry (Docker Hub, ECR, etc.)
3. Deploy to cloud platform (AWS, Azure, GCP, Heroku)
4. Configure environment variables
5. Setup SSL certificate
6. Configure domain

---

## 📞 Support & Documentation

### Key Files to Review
- `/backend/JWT_DOCUMENTATION_INDEX.md` - JWT implementation details
- `/backend/IMPLEMENTATION_GUIDE.md` - Backend architecture
- `/backend/JWT_TESTING_GUIDE.md` - Testing instructions

### Common Questions

**Q: How do I reset to default data?**
A: Delete all tables and restart backend - DataInitializer will recreate them

**Q: Can I change notification methods?**
A: Yes, update NotificationService.java with your SMS/Email provider

**Q: How do I add more departments/subjects?**
A: Update DataInitializer.java or use UI/API endpoints

**Q: Is the system production-ready?**
A: Yes, but review security checklist before deployment

---

## ✅ Completion Checklist

- [x] Backend implementation with Spring Boot
- [x] Database schema with JPA entities
- [x] JWT authentication and authorization
- [x] Attendance marking system
- [x] Smart alteration algorithm (4 priority criteria)
- [x] Half-day attendance support
- [x] Lesson plan upload feature
- [x] Multi-day attendance marking
- [x] HOD dashboard with statistics
- [x] Role-based access control
- [x] Timetable management UI
- [x] React frontend with TypeScript
- [x] Responsive design with Tailwind CSS
- [x] API integration
- [x] State management with Zustand
- [x] Default test data
- [x] Error handling and validation
- [x] Build and deployment ready

---

## 🎉 You're All Set!

The Staff Alteration System is now ready for use. Start by:

1. **Running Backend**: `gradle bootRun`
2. **Running Frontend**: `npm run dev`
3. **Logging in**: Use Staff1/password123
4. **Testing**: Mark attendance and observe automatic alteration

**Deadline**: December 26, 2025, 6 PM ✅

---

**Built with ❤️ for your final year project**
