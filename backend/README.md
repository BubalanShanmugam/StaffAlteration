# Staff Alteration System - Backend

Production-ready Spring Boot backend for the Staff Alteration System.

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/staffalteration/
│   │   │   ├── controller/          # REST API Controllers
│   │   │   ├── service/             # Business Logic Layer
│   │   │   ├── repository/          # Data Access Layer
│   │   │   ├── entity/              # JPA Entities
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── security/            # JWT & Security
│   │   │   ├── config/              # Spring Configurations
│   │   │   ├── notification/        # Notification Services
│   │   │   ├── util/                # Utility Classes
│   │   │   └── DataInitializer.java # Seed Data
│   │   └── resources/
│   │       ├── application.yml      # Configuration
│   │       └── db/migration/        # Flyway Migrations
│   └── test/
└── build.gradle                    # Gradle Build Config
```

## Technologies

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security (JWT)**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL 15+**
- **WebSocket** (for real-time notifications)
- **Flyway** (Database migration)

## Building

```bash
./gradlew clean build
```

## Running

### Development

```bash
./gradlew bootRun
```

Server starts on: `http://localhost:8080/api`

### Production

```bash
export JWT_SECRET=your-secure-key-min-32-chars
export DB_PASSWORD=your-db-password     
export FRONTEND_URL=https://your-frontend.com
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Database Setup

### PostgreSQL (Local)

```sql
CREATE DATABASE staff_alteration;
```

### Environment Variables

```env
# Development
DATABASE_URL=jdbc:postgresql://localhost:5432/staff_alteration
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Production (Supabase)
DATABASE_URL=jdbc:postgresql://db.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=your-password
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `GET /api/auth/user/{userId}` - Get user by ID

### Attendance
- `POST /api/attendance/mark` - Mark attendance
- `GET /api/attendance/{attendanceId}` - Get attendance
- `GET /api/attendance/staff/{staffId}` - Get staff attendance
- `GET /api/attendance/date/{date}` - Get attendance by date
- `GET /api/attendance/absent/{date}` - Get absent staff

### Timetable
- `POST /api/timetable/create` - Create timetable
- `PUT /api/timetable/update/{timetableId}` - Update timetable
- `GET /api/timetable/staff/{staffId}` - Get staff timetable
- `GET /api/timetable/class/{classCode}/{dayOrder}/{periodNumber}` - Get timetable by class
- `DELETE /api/timetable/{timetableId}` - Delete timetable

### Alteration
- `GET /api/alteration/date/{date}` - Get alterations by date
- `GET /api/alteration/staff/{staffId}` - Get alterations by staff
- `PUT /api/alteration/{alterationId}/status` - Update alteration status

### Notifications
- `GET /api/notification/staff/{staffId}` - Get staff notifications
- `GET /api/notification/staff/{staffId}/unread` - Get unread notifications
- `GET /api/notification/staff/{staffId}/unread-count` - Get unread count
- `PUT /api/notification/{notificationId}/read` - Mark notification as read

### Staff
- `POST /api/staff/create` - Create staff
- `GET /api/staff/{staffId}` - Get staff by ID
- `GET /api/staff` - Get all staff
- `GET /api/staff/department/{departmentCode}` - Get staff by department
- `PUT /api/staff/{staffId}` - Update staff

## Alteration Algorithm

The system implements a strict priority-driven algorithm:

1. **Staff must be present today** - Only considers present staff
2. **Staff already teaches the same class** - Prioritizes those familiar with the class
3. **Staff with least hours that day** - Distributes workload evenly
4. **Staff with no previous/next period clash** - Avoids scheduling conflicts
5. **Prefer same subject** - Maintains subject continuity
6. **Tie-breaker: least weekly workload** - Considers overall workload

## Default Data

On first run, the system automatically creates:

- **Staff**: Staff1, Staff2, Staff3, Staff4, Staff5
- **Departments**: CS (Computer Science), IT (Information Technology)
- **Subjects**: JAVA, PY, WEB, DB
- **Classes**: CS1, CS2, IT1, IT2
- **Sample Timetables**: 3 default entries
- **Attendance**: All staff marked present for today

### Default Credentials

```
Username: Staff1
Password: password123
```

## Security

- JWT token-based authentication
- Role-based authorization (STAFF, HOD, DEAN, ADMIN)
- Password encryption with BCrypt
- CORS enabled for frontend communication

## WebSocket

Real-time notifications via WebSocket:

```
Endpoint: ws://localhost:8080/api/ws/notifications
Topic: /topic/notifications/{staffId}
```

## Notifications

- **App Notifications**: Real-time via WebSocket
- **SMS Notifications**: Mock implementation (ready for Twilio/AWS SNS integration)

## Error Handling

All endpoints return standardized responses:

```json
{
  "code": 200,
  "message": "Success message",
  "data": {},
  "timestamp": 1671267600000
}
```

## Logging

- **Development**: DEBUG level for com.staffalteration
- **Production**: INFO level with structured logging

Logs: `logs/application.log`

## Testing

```bash
./gradlew test
```

## Notes

- Database migrations are automatic via Flyway
- Seed data is initialized on startup if DB is empty
- JWT expiration: 24 hours
- All timestamps in UTC

## Next Steps

1. Configure PostgreSQL database
2. Set environment variables
3. Run `./gradlew bootRun`
4. Test endpoints with Postman/Insomnia
5. Connect frontend to this backend

## Support

For issues or questions, refer to the API documentation or contact the development team.
