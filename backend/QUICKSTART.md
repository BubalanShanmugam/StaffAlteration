# 🚀 Quick Start Guide

## Prerequisites

- **Java 17+** installed
- **Gradle 7+** installed (or use `./gradlew`)
- **PostgreSQL 15+** installed and running
- **Git** (optional)

## Step-by-Step Setup

### 1. Create Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE staff_alteration;

# Exit
\q
```

### 2. Clone/Navigate to Project

```bash
cd d:\StaffAlteration\backend
```

### 3. Configure Database (Optional)

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/staff_alteration
    username: postgres
    password: postgres  # Change if needed
```

### 4. Build Project

```bash
# Windows
gradlew clean build

# Linux/Mac
./gradlew clean build
```

### 5. Run Application

```bash
# Windows
gradlew bootRun

# Linux/Mac
./gradlew bootRun
```

Expected output:
```
...
Started StaffAlterationApplication in 8.234 seconds (process running for 8.512)
Application is running at: http://localhost:8080/api
```

### 6. Verify Installation

Open browser or use curl:

```bash
# Check if server is running
curl http://localhost:8080/api/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"Staff1","password":"password123"}'
```

Expected response:
```json
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

## Default Credentials

All staff members have the same password:

```
Username: Staff1, Staff2, Staff3, Staff4, Staff5
Password: password123
```

## Testing with Postman/Insomnia

### 1. Create Environment

```
BASE_URL: http://localhost:8080/api
TOKEN: (get from login response)
```

### 2. Import Requests

#### Login
```
POST {{BASE_URL}}/auth/login
Body (raw JSON):
{
  "username": "Staff1",
  "password": "password123"
}
```

#### Mark Absence
```
POST {{BASE_URL}}/attendance/mark
Headers:
  Authorization: Bearer {{TOKEN}}
Body (raw JSON):
{
  "staffId": "Staff2",
  "attendanceDate": "2024-12-18",
  "status": "ABSENT",
  "remarks": "Medical leave"
}
```

#### Get Unread Notifications
```
GET {{BASE_URL}}/notification/staff/Staff2/unread
Headers:
  Authorization: Bearer {{TOKEN}}
```

#### Get Staff Info
```
GET {{BASE_URL}}/staff/Staff1
Headers:
  Authorization: Bearer {{TOKEN}}
```

## Workflow Test Scenario

### Scenario: Staff2 marked absent, system auto-assigns substitute

1. **Login as HOD**
   ```
   POST /api/auth/login
   Body: {"username":"Staff1", "password":"password123"}
   Save token from response
   ```

2. **Mark Staff2 as Absent**
   ```
   POST /api/attendance/mark
   Headers: Authorization: Bearer <TOKEN>
   Body: 
   {
     "staffId": "Staff2",
     "attendanceDate": "2024-12-18",
     "status": "ABSENT",
     "remarks": "Sick leave"
   }
   ```

3. **System Processes Alteration** (automatic)
   - Finds all classes Staff2 teaches
   - Evaluates substitute candidates
   - Applies priority algorithm
   - Selects best match
   - Creates alteration records

4. **Check Assigned Staff Notification**
   ```
   GET /api/notification/staff/<substitute_staff_id>/unread
   Headers: Authorization: Bearer <TOKEN>
   ```

5. **View Alteration Details**
   ```
   GET /api/alteration/date/2024-12-18
   Headers: Authorization: Bearer <TOKEN>
   ```

## Stopping the Server

```bash
# Press Ctrl+C in the terminal
```

## Troubleshooting

### Port Already in Use
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Database Connection Error
```
Error: org.postgresql.util.PSQLException: Connection refused

Fix:
1. Verify PostgreSQL is running
2. Check database name: staff_alteration
3. Check username/password in application.yml
4. Verify localhost:5432 is correct
```

### Gradle Build Fails
```bash
# Clean cache
gradlew clean

# Check Java version
java -version
# Should be 17+

# Rebuild
gradlew build
```

### Port 8080 Not Accessible
```
Check firewall settings
Try: http://127.0.0.1:8080/api instead of localhost
```

## Next Steps

1. **Frontend Setup**
   - Connect React/Angular to this backend
   - Use JWT token for authentication

2. **WebSocket Integration**
   - Connect to `ws://localhost:8080/api/ws/notifications`
   - Subscribe to `/topic/notifications/{staffId}`

3. **Production Deployment**
   - Build JAR: `./gradlew build`
   - Deploy to: AWS, Azure, Heroku, or Docker
   - Set environment variables

4. **Database Backup**
   ```bash
   # Backup
   pg_dump -U postgres staff_alteration > backup.sql
   
   # Restore
   psql -U postgres staff_alteration < backup.sql
   ```

## Database Inspection

### Connect to Database
```bash
psql -U postgres -d staff_alteration
```

### Useful Queries
```sql
-- List all tables
\dt

-- Check staff records
SELECT * FROM staff;

-- Check today's attendance
SELECT * FROM attendance WHERE attendance_date = CURRENT_DATE;

-- Check alterations
SELECT * FROM alteration;

-- Check notifications
SELECT * FROM notification ORDER BY created_at DESC;

-- Exit
\q
```

## API Documentation

For complete API documentation, see:
- `IMPLEMENTATION_GUIDE.md` - Detailed API endpoints
- `README.md` - Overview and setup
- `FILE_STRUCTURE.md` - Project structure

## Performance Tips

### Database Optimization
```sql
-- Add more indexes if needed
CREATE INDEX idx_custom ON table_name(column);

-- Analyze query plans
EXPLAIN ANALYZE SELECT ...;
```

### JVM Optimization
```bash
# Increase heap size for large datasets
export JAVA_OPTS="-Xms512m -Xmx1g"
./gradlew bootRun
```

### Caching
- Results are automatically cached by Hibernate
- Adjust cache settings in application.yml

## Security Notes

⚠️ **For Development Only**
- Change default passwords in production
- Use strong JWT secret (min 32 chars)
- Enable HTTPS
- Configure proper CORS origins
- Use environment variables for secrets

✅ **Production Checklist**
```
□ Change database password
□ Set strong JWT_SECRET in .env
□ Enable HTTPS/TLS
□ Configure firewall
□ Set up monitoring/logging
□ Enable database backups
□ Configure load balancer
□ Set up CI/CD pipeline
```

## Support Resources

- Spring Boot Docs: https://spring.io/projects/spring-boot
- PostgreSQL Docs: https://www.postgresql.org/docs/
- JWT Guide: https://jwt.io/introduction
- REST Best Practices: https://restfulapi.net

---

**Happy Coding! 🎉**

For issues, check the logs:
```bash
tail -f logs/application.log
```
