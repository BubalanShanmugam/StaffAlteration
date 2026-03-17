# Attendance & Alteration Flow - Complete Implementation Summary

## What Was Updated

### 1. AttendanceService.java - Enhanced Logging

#### Changes Made:
1. **markAttendance() method** - Added detailed logging
   - Shows when attendance is saved to database
   - Displays all key fields: status, dayType, meetingHours
   - Logs whether alteration process will be triggered

2. **triggerAlterationProcess() method** - Complete rewrite with enhanced logging
   - Shows start/completion of alteration process
   - Displays staff info, absence date, status, day type
   - Lists timetables found and periods needing alteration
   - Shows detailed results: created count, failed count
   - Provides visual indicators (✅✅❌🔄📋🔧)

3. **cancelExistingAlterations() method** - Added tracking
   - Logs when existing alterations are cancelled
   - Shows count of cancelled records
   - Displays IDs of cancelled alterations

4. **getDayName() helper method** - Added for readability
   - Converts day number (1-7) to day name
   - Used in logs for better readability

### 2. AlterationService.java - Database Save Logging

#### Changes Made:
1. **processAlteration() method** - Enhanced save notification
   - Added detailed box-formatted log showing alteration details
   - Displays all 10+ fields of saved alteration record
   - Shows IDs, names, and timestamps
   - Confirms successful database save

---

## Database Schema - How Tables Are Linked

### ATTENDANCE TABLE
```
┌─────────────────────────────────────┐
│          ATTENDANCE                  │
├─────────────────────────────────────┤
│ id (PK)              INTEGER         │
│ staff_id (FK)        BIGINT         │──────┐
│ attendance_date      DATE            │      │
│ status              VARCHAR(50)      │      │ Links to
│ day_type            VARCHAR(50)      │      │ STAFF table
│ remarks             VARCHAR(500)     │      │
│ created_at          TIMESTAMP        │      │
│ updated_at          TIMESTAMP        │      │
│ meeting_hours (set) INTEGER[]        │      │
└─────────────────────────────────────┘      │
                                              │
                                         ┌────▼──────────┐
                                         │  STAFF        │
                                         │  id (PK)      │
                                         │  staff_id     │
                                         └───────────────┘
```

### ALTERATION TABLE
```
┌─────────────────────────────────────┐
│         ALTERATION                  │
├─────────────────────────────────────┤
│ id (PK)                  INTEGER     │
│ timetable_id (FK)        BIGINT     │────┐
│ original_staff_id (FK)   BIGINT     │    │     ┌──────────────────┐
│ substitute_staff_id(FK)  BIGINT     │    │────►│  TIMETABLE       │
│ alteration_date          DATE       │    │     │  ORIGINAL_STAFF  │
│ status                   VARCHAR    │    │     │  SUBSTITUTE_STAFF│
│ absence_type             VARCHAR    │    │     │  (dates match)   │
│ period_number            INTEGER    │    │     └──────────────────┘
│ remarks                  VARCHAR    │    │
│ created_at              TIMESTAMP   │    │
│ updated_at              TIMESTAMP   │    │
└─────────────────────────────────────┘    │
                                      ┌─────┘
                              ┌──────═╧════════────┐
                              │                   │
                         ┌────▼──────┐    ┌──────▼───┐
                         │ STAFF     │    │ STAFF    │
                         │ (Original)│    │ (Subst)  │
                         └───────────┘    └──────────┘
```

---

## How It Works - Step by Step

### Step 1: Attendance Marking
```java
// User marks attendance
POST /api/attendance/mark

{
  "staffId": "S001",
  "attendanceDate": "2026-03-04",
  "status": "LEAVE",           // ← Triggers alteration check
  "dayType": "FULL_DAY"
}
```

**Database Action**: 
```sql
INSERT INTO attendance (staff_id, attendance_date, status, day_type, ...)
VALUES (1, '2026-03-04', 'LEAVE', 'FULL_DAY', ...)
RETURNING id;  -- inserted as id = 123
```

**Log Output**:
```
✅ ATTENDANCE SAVED: staffId=S001, date=2026-03-04, status=LEAVE, dayType=FULL_DAY
```

---

### Step 2: Alteration Process Check
```java
if (savedAttendance.getStatus() in {LEAVE, ABSENT, ONDUTY, MEETING, PERIOD_WISE_ABSENT}) {
    triggerAlterationProcess(staff, date, savedAttendance, periodsToProcess);
}
```

**Log Output**:
```
🔄 TRIGGERING ALTERATION PROCESS for staff: John Doe (S001), date: 2026-03-04, status: LEAVE
```

---

### Step 3: Timetable Matching
```java
// Get all timetables for this staff
List<Timetable> allTimetables = timetableRepository.findByStaffId(staff.getId());

// Filter for timetables matching the absence date
int dayOfWeek = date.getDayOfWeek().getValue();  // 1-7 (Mon-Sun)
for (Timetable t : allTimetables) {
    if (t.getDayOrder() == dayOfWeek) {
        timetablesForToday.add(t);  // Matches!
    }
}
```

**Log Output**:
```
📋 === ALTERATION PROCESS STARTED ===
   Staff: John Doe (S001)
   Absence Date: 2026-03-04
   Day of Week: Wednesday (3)
   Total timetables found for staff: 6
   📍 Matching timetables: 5, Periods that need alteration: [1, 2, 3, 4, 5, 6]
```

---

### Step 4: For Each Matching Timetable
```java
for (Timetable timetable : timetablesForToday) {
    if (periodsThatNeedAlteration.contains(timetable.getPeriodNumber())) {
        // Execute alteration logic
        Alteration alteration = alterationService.processAlteration(timetable, date);
    }
}
```

**For each class/period, the algorithm**:
1. Checks if staff is present on that date
2. Finds suitable substitute based on 4 priorities
3. Creates alteration record

**Log Output**:
```
🔧 Creating alteration for period 1: CLASS-A (Mathematics)
   ✅ Alteration created: ID=456, original=S001, substitute=S002
   
🔧 Creating alteration for period 2: CLASS-B (English)
   ✅ Alteration created: ID=457, original=S001, substitute=S003
```

---

### Step 5: Save to Alteration Table
```java
Alteration alteration = Alteration.builder()
    .timetable(timetable)
    .originalStaff(originalStaff)        // S001
    .substituteStaff(substituteStaff)    // S002
    .alterationDate(alterationDate)      // 2026-03-04
    .absenceType(absenceType)            // FN (Full Day)
    .periodNumber(periodNumber)          // 1
    .status(ASSIGNED)
    .build();

Alteration savedAlteration = alterationRepository.save(alteration);
```

**Database Action**:
```sql
INSERT INTO alteration (
    timetable_id, 
    original_staff_id, 
    substitute_staff_id, 
    alteration_date, 
    status, 
    absence_type, 
    period_number, 
    ...
)
VALUES (
    789,           -- timetable.id
    1,             -- original staff (S001)
    2,             -- substitute (S002)
    '2026-03-04',  -- alteration date
    'ASSIGNED',
    'FN',
    1,
    ...
)
RETURNING id;  -- inserted as id = 456
```

**Log Output**:
```
═════════════════════════════════════════════════════════════════════════
✅ ALTERATION SAVED TO DATABASE
═════════════════════════════════════════════════════════════════════════
   ID: 456
   Original Staff: John Doe (S001)
   Substitute Staff: Jane Smith (S002)
   Class: CLASS-A
   Subject: Mathematics
   Period: 1
   Alteration Date: 2026-03-04
   Absence Type: FN
   Status: ASSIGNED
═════════════════════════════════════════════════════════════════════════
```

---

### Step 6: Process Completion
```java
log.warn("📋 === ALTERATION PROCESS COMPLETED ===");
log.warn("   ✅ Alterations created: 5");
log.warn("   ❌ Alterations failed: 0");
log.warn("   📊 Matched timetables: 5/5");
```

**Summary**:
- Total staff classes: 6
- Timetables matching the date: 5
- Alterations successfully created: 5
- Alterations failed: 0

---

## Query Examples

### Example: Mark S001 LEAVE on 2026-03-04

#### Attendance Table After Mark
```sql
SELECT * FROM attendance 
WHERE staff_id = 1 AND attendance_date = '2026-03-04';

-- Result:
id | staff_id | attendance_date | status | day_type  | created_at
123| 1        | 2026-03-04      | LEAVE  | FULL_DAY  | 2026-03-04 10:30:00
```

#### Alteration Table After Mark
```sql
SELECT a.id, os.staff_id, ss.staff_id, cr.class_code, subj.subject_name, a.period_number, a.status
FROM alteration a
JOIN staff os ON a.original_staff_id = os.id
JOIN staff ss ON a.substitute_staff_id = ss.id
JOIN timetable t ON a.timetable_id = t.id
JOIN classroom cr ON t.classroom_id = cr.id
JOIN subject subj ON t.subject_id = subj.id
WHERE a.alteration_date = '2026-03-04' AND os.staff_id = 'S001';

-- Result:
id  | original | substitute | class_code | subject_name | period | status
456 | S001     | S002       | CLASS-A    | Mathematics  | 1      | ASSIGNED
457 | S001     | S003       | CLASS-B    | English      | 2      | ASSIGNED
458 | S001     | S004       | CLASS-C    | Science      | 3      | ASSIGNED
459 | S001     | S002       | CLASS-D    | History      | 4      | ASSIGNED
460 | S001     | S005       | CLASS-E    | Geography    | 5      | ASSIGNED
```

---

## Transaction Isolation

Both tables are updated in the **same @Transactional context**:

```
BEGIN TRANSACTION
  ├─ INSERT INTO attendance VALUES (...) → ID: 123
  ├─ FOR EACH timetable
  │   └─ INSERT INTO alteration VALUES (...) → ID: 456, 457, 458, ...
COMMIT
```

**Key Points**:
✅ All updates are atomic (all-or-nothing)
✅ If any step fails, both tables rollback together
✅ No partial updates possible
✅ Guaranteed consistency

---

## Error Handling

If an error occurs during alteration creation:

```
Example: No suitable substitutes found
├─ Original staff: S001
├─ Period: 1 (Mathematics)
├─ Result: No alteration created for this period
├─ Other periods: Continue processing
└─ Log: ⚠️ No suitable substitute found for period 1
```

The attendance record is **still saved**, but that specific alteration is skipped.

---

## Real-Time Updates

After both tables are updated:

```
1. Alteration is saved to DB ✅
   ↓
2. Alteration DTO created with all details
   ↓
3. WebSocket broadcasts to frontend
   ├─ /topic/alterations/created
   ├─ /topic/department/{id}/alterations
   └─ Frontend receives and updates UI
   ↓
4. User sees new alteration in dashboard (no refresh needed)
```

---

## Summary Table

| Step | Table | Action | Status |
|------|-------|--------|--------|
| 1 | Attendance | INSERT | ✅ Saved |
| 2 | Alteration | INSERT (Period 1) | ✅ Saved |
| 3 | Alteration | INSERT (Period 2) | ✅ Saved |
| 4 | Alteration | INSERT (Period 3) | ✅ Saved |
| ... | Alteration | INSERT (Period N) | ✅ Saved |
| Last | WebSocket | BROADCAST | ✅ Sent |

**Total Records Created for Full Day LEAVE**:
- Attendance: 1 record
- Alterations: 4-6 records (one per class period)
- **Total database updates: 5-7**

---

## Key Metrics to Verify

After marking attendance:

| Metric | Expected | How to Check |
|--------|----------|--------------|
| Attendance records | 1 | `SELECT COUNT(*) FROM attendance WHERE staff_id = X AND attendance_date = Y` |
| Alteration records | Sum of timetables for that day | `SELECT COUNT(*) FROM alteration WHERE original_staff_id = X AND alteration_date = Y` |
| All same date | ✅ YES | Both attendance_date and alteration_date should match |
| Substitute ≠ Original | ✅ YES | original_staff_id ≠ substitute_staff_id |
| Status is ASSIGNED | ✅ YES | `WHERE status = 'ASSIGNED'` |
| Transaction rolled back | ❌ NO | No "transaction rolled back" in logs |

---

**Status**: ✅ Complete Implementation
**Date**: March 4, 2026
**Tested**: Attendance → Alteration flow works end-to-end
