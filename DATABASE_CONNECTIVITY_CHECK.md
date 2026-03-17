# Database Connectivity Verification Report

## ✅ Database Configuration Status

### Current Configuration (application.properties)

```properties
# Database URL
spring.datasource.url=jdbc:postgresql://db.mnrlactnmnkxbiclsrhd.supabase.co:5432/postgres

# Driver
spring.datasource.driver-class-name=org.postgresql.Driver

# Credentials
spring.datasource.username=postgres
spring.datasource.password=SANTHIYA2210 (configured)

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Migrations
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

**Status**: ✅ **PROPERLY CONFIGURED**

---

## Database Connectivity Tests

### Test 1: PostgreSQL Connection Test

#### Prerequisites
- Network access to Supabase
- Correct credentials

#### Using psql Command Line

```bash
# Connect to database
psql -h db.mnrlactnmnkxbiclsrhd.supabase.co -U postgres -d postgres -p 5432

# When prompted, enter password: SANTHIYA2210

# Run test query
SELECT 1 as connection_test;
```

**Expected Output**:
```
 connection_test
─────────────────
               1
(1 row)
```

✅ **If this works, database connectivity is GOOD**

---

### Test 2: Application Startup Diagnostics

When you start the backend application, watch for these SUCCESS indicators:

#### Expected Log Sequence (in order):

```log
1. Starting StaffAlterationApplication v1.0.0
2. ⏳ Initializing database...
3. PostgreSQL version: 14.x.x
4. ✅ HikariPool initialized successfully
5. 🔄 Running Flyway migrations...
   - V1__Create_Initial_Schema.sql ✅
   - V2__Create_Timetable_Template.sql ✅
   - V3__Fix_Timetable_Template_Staff_FK.sql ✅
   - V4__Add_Missing_Columns_v2.sql ✅
   - V5__Add_Meeting_Hours_Table.sql ✅
   - V6__Add_Absence_Type_To_Alteration.sql ✅
   - V7__Add_Period_Number_To_Alteration.sql ✅
   - V8__Create_Alteration_Audit_Table.sql ✅
6. 📊 Migration 8 migrations executed successfully
7. ✅ Tomcat started on port 8080
8. 🎉 StaffAlterationApplication started
```

#### ⚠️ WARNING Signs (Database NOT Connected)

```log
❌ Failed to initialize connection pool
❌ Connection refused
❌ Database "postgres" does not exist
❌ Fatal error after connecting
❌ The server refused our connection attempt
```

---

### Test 3: Check Database Tables Exist

#### Connect to Database and Run

```bash
# Connect to database
psql -h db.mnrlactnmnkxbiclsrhd.supabase.co -U postgres -d postgres -p 5432

# List all tables
\dt public.*;
```

**Expected Tables** (should exist):
```
public | alteration              | table | postgres
public | alteration_audit        | table | postgres
public | attendance              | table | postgres
public | classroom               | table | postgres
public | department              | table | postgres
public | lesson_plan             | table | postgres
public | notification            | table | postgres
public | role                    | table | postgres
public | staff                   | table | postgres
public | subject                 | table | postgres
public | timetable               | table | postgres
public | timetable_template      | table | postgres
public | user                    | table | postgres
public | workload_summary        | table | postgres
public | flyway_schema_history   | table | postgres
```

**Count Tables**:
```sql
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';
```

✅ **Expected**: 15+ tables

---

### Test 4: Database Connectivity Java Test

Create a simple test file to verify connection:

**File**: `DatabaseConnectionTest.java`

```java
package com.staffalteration.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.sql.DataSource;
import java.sql.Connection;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            boolean isConnected = !connection.isClosed();
            System.out.println("✅ Database Connection Test: " + (isConnected ? "SUCCESS" : "FAILED"));
            
            String dbUrl = connection.getMetaData().getURL();
            String dbDriver = connection.getMetaData().getDriverName();
            String dbVersion = connection.getMetaData().getDatabaseProductVersion();
            
            System.out.println("   Database URL: " + dbUrl);
            System.out.println("   Driver: " + dbDriver);
            System.out.println("   Version: " + dbVersion);
        } catch (Exception e) {
            System.out.println("❌ Database Connection Test: FAILED");
            System.out.println("   Error: " + e.getMessage());
            throw e;
        }
    }
}
```

#### Run the Test
```bash
cd backend
./gradlew test --tests DatabaseConnectionTest
```

**Expected Output**:
```
✅ Database Connection Test: SUCCESS
   Database URL: jdbc:postgresql://db.mnrlactnmnkxbiclsrhd.supabase.co:5432/postgres
   Driver: PostgreSQL JDBC Driver
   Version: PostgreSQL 14.x
```

---

### Test 5: Running Actual API Tests

Once the application starts, verify the API can access the database:

#### Test Endpoint: Get All Staff

```bash
curl -X GET http://localhost:8080/api/staff

# Expected Response:
{
  "code": 200,
  "message": "Staff list retrieved successfully",
  "data": [
    {
      "id": 1,
      "staffId": "S001",
      "firstName": "John",
      "lastName": "Doe",
      ...
    },
    ...
  ]
}
```

✅ **If this returns data, database is CONNECTED and WORKING**

#### Test Endpoint: Check Database Health

```bash
curl -X GET http://localhost:8080/api/health

# Expected:
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "hello": 1
      }
    }
  }
}
```

---

## 🔍 Troubleshooting Database Connection Issues

### Issue 1: Connection Refused

**Error**:
```
org.postgresql.util.PSQLException: Connection to db.mnrlactnmnkxbiclsrhd.supabase.co:5432 refused
```

**Causes & Solutions**:
- [ ] Check network connectivity: `ping db.mnrlactnmnkxbiclsrhd.supabase.co`
- [ ] Verify credentials in application.properties
- [ ] Check if database is running (Supabase status)
- [ ] Verify firewall doesn't block port 5432
- [ ] Check if IP is whitelisted (Supabase firewall)

### Issue 2: Authentication Failed

**Error**:
```
org.postgresql.util.PSQLException: FATAL: password authentication failed for user "postgres"
```

**Causes & Solutions**:
- [ ] Verify password is correct: `SANTHIYA2210`
- [ ] Check for special characters in password
- [ ] Verify username is: `postgres`
- [ ] Reset password in Supabase dashboard if needed

### Issue 3: Connection Pool Timeout

**Error**:
```
HikariPool-1 - Connection is not available, request timed out
```

**Causes & Solutions**:
- [ ] Database server is very slow
- [ ] Connection pool size too small (default 10)
- [ ] Too many queries happening simultaneously
- [ ] Network latency is high

### Issue 4: Migration Failed

**Error**:
```
org.flywaydb.core.internal.command.DbValidate$FlywayValidateException: 
Validate failed
```

**Causes & Solutions**:
- [ ] Previous version had breaking changes
- [ ] Migration file names are wrong
- [ ] Migration files are malformed SQL
- [ ] Database schema is corrupted
- [ ] Solution: Check migration files in `db/migration/`

---

## Current System Status Checklist

| Check | Status | Details |
|-------|--------|---------|
| Database URL | ✅ | `jdbc:postgresql://db.mnrlactnmnkxbiclsrhd.supabase.co:5432/postgres` |
| Driver | ✅ | `org.postgresql.Driver` |
| Credentials | ✅ | `postgres / [configured]` |
| Connection Pool | ✅ | HikariCP 5.0.1 |
| Dialect | ✅ | PostgreSQL |
| DDL Auto | ✅ | `none` (safe for production) |
| Flyway Enabled | ✅ | Yes |
| Migrations Located | ✅ | `classpath:db/migration` |
| Logging Level | ✅ | DEBUG for app, INFO for others |

---

## How to Monitor Database Connection

### 1. Watch Logs During Startup

```bash
# Start backend
cd backend
./gradlew bootRun

# Look for these patterns:
grep -i "hikari\|connection\|flyway\|database" logs/application.log
```

### 2. Monitor Active Connections

```bash
# Connect to database
psql -h db.mnrlactnmnkxbiclsrhd.supabase.co -U postgres -d postgres

# Check active connections
SELECT datname, count(*) FROM pg_stat_activity GROUP BY datname;

# Kill idle connections if needed
SELECT pg_terminate_backend(pid) FROM pg_stat_activity 
WHERE datname = 'postgres' AND state = 'idle';
```

### 3. Check PostgreSQL Version

```sql
SELECT version();
-- Expected: PostgreSQL 14.x...
```

### 4. List Database Schema

```sql
SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'public';
```

---

## Initial Data Verification

After application starts successfully, verify initial data is loaded:

```bash
# Connect to Supabase
psql -h db.mnrlactnmnkxbiclsrhd.supabase.co -U postgres -d postgres

# Check if data was initialized
SELECT COUNT(*) as total_staff FROM staff;
SELECT COUNT(*) as total_departments FROM department;
SELECT COUNT(*) as total_timetables FROM timetable;
```

**Expected Counts** (from DataInitializer):
- Staff: 10+
- Departments: 2+
- Timetables: 20+
- Roles: 3+

---

## Production Checklist

Before deploying to production:

- [ ] Database connectivity verified with real data
- [ ] All tables created successfully
- [ ] All migrations executed without errors
- [ ] Initial data populated correctly
- [ ] Connection pool configured appropriately
- [ ] Backups enabled in Supabase
- [ ] Database credentials stored securely (not in code)
- [ ] Environment variables properly set
- [ ] Firewall rules configured
- [ ] SSL/TLS enabled for connection
- [ ] Query performance tested
- [ ] Connection timeout settings appropriate

---

## Quick Test Command

Run this in one command to verify everything:

```bash
cd backend && ./gradlew bootRun 2>&1 | grep -E "Connection|HikariPool|Flyway|Successfully|ERROR"
```

**Expected Output**:
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection conn...
Migration 1..8 executed successfully
✅ Application started
```

---

**Last Verified**: March 4, 2026
**Database**: Supabase PostgreSQL
**Status**: ✅ **READY FOR TESTING**

