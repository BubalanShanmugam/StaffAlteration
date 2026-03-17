# Attendance & Alteration Update Verification Guide

## Overview

This guide verifies that when attendance is marked:
1. ✅ Attendance table is updated in the database
2. ✅ Alteration logic is triggered and substitutes are assigned
3. ✅ Alteration table is updated with new records
4. ✅ Both updates happen in the same transaction

---

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    ATTENDANCE MARKING FLOW                      │
└─────────────────────────────────────────────────────────────────┘

1. USER MARKS ATTENDANCE
   ↓
   API: POST /api/attendance/mark
   {
     "staffId": "S001",
     "attendanceDate": "2026-03-04",
     "status": "LEAVE",           ← Absence status
     "dayType": "FULL_DAY",       ← Leave type
     "remarks": "Medical leave"
   }

2. ATTENDANCE SAVED TO DATABASE
   ↓
   ✅ attendance TABLE UPDATED
   ├─ id: 123
   ├─ staff_id: 1
   ├─ attendance_date: 2026-03-04
   ├─ status: LEAVE
   ├─ day_type: FULL_DAY
   └─ created_at: 2026-03-04 10:30:00

3. ALTERATION PROCESS TRIGGERED
   ↓
   IF status IN (LEAVE, ABSENT, ONDUTY, MEETING, PERIOD_WISE_ABSENT)
   ├─ Get staff timetable for the date
   ├─ Find matching day and periods
   └─ For each timetable → Process alteration

4. ALTERATION LOGIC EXECUTES
   ↓
   ├─ Find substitute staff using priority algorithm:
   │  ├─ Priority 1: Staff who are PRESENT
   │  ├─ Priority 2: Staff from same department  
   │  ├─ Priority 3: Staff without continuous hours
   │  └─ Priority 4: Staff with minimum total hours
   └─ If substitute found → Save alteration

5. ALTERATION SAVED TO DATABASE
   ↓
   ✅ alteration TABLE UPDATED
   ├─ id: 456
   ├─ timetable_id: 789
   ├─ original_staff_id: 1       (The absent staff)
   ├─ substitute_staff_id: 2     (The replacement)
   ├─ alteration_date: 2026-03-04
   ├─ status: ASSIGNED
   ├─ absence_type: FN           (Full Day)
   ├─ period_number: NULL        (Full day = all periods)
   └─ created_at: 2026-03-04 10:30:05

6. NOTIFICATIONS SENT (Optional - Won't block save)
   ↓
   ├─ Email to original staff
   ├─ Email to substitute staff
   ├─ SMS to substitute
   ├─ WebSocket broadcast to dashboard
   └─ In-app notification to HOD

7. REAL-TIME UPDATES
   ↓
   ✅ Frontend receives WebSocket message
   └─ Alteration Dashboard updates without refresh
```

---

## Testing - Step by Step

### Test 1: Database Verification

#### Prerequisites
- Access to PostgreSQL database
- Staff member: S001 (must have timetable scheduled)
- Date: Today or future date

#### Steps

1. **Note the current time:**
   ```
   Current timestamp: 2026-03-04 10:30:00
   ```

2. **Mark attendance via API or UI:**
   - Staff ID: S001
   - Date: 2026-03-04
   - Status: LEAVE
   - Day Type: FULL_DAY

3. **Check ATTENDANCE table was updated:**
   ```sql
   SELECT * FROM attendance 
   WHERE staff_id IN (SELECT id FROM staff WHERE staff_id = 'S001')
   AND attendance_date = '2026-03-04'
   ORDER BY created_at DESC LIMIT 1;
   ```

   **Expected Output:**
   ```
   id  | staff_id | attendance_date | status | day_type  | created_at
   ────┼──────────┼─────────────────┼────────┼───────────┼────────────────────
   123 | 1        | 2026-03-04      | LEAVE  | FULL_DAY  | 2026-03-04 10:30:00
   ```
   ✅ **Verified**: Attendance is saved

4. **Check ALTERATION table was updated:**
   ```sql
   SELECT a.*, os.staff_id as original, ss.staff_id as substitute
   FROM alteration a
   JOIN staff os ON a.original_staff_id = os.id
   JOIN staff ss ON a.substitute_staff_id = ss.id
   WHERE a.alteration_date = '2026-03-04'
   AND os.staff_id = 'S001'
   ORDER BY a.created_at DESC LIMIT 10;
   ```

   **Expected Output:**
   ```
   id  | original | substitute | status   | absence_type | created_at           | updated_at
   ────┼──────────┼────────────┼──────────┼──────────────┼──────────────────────┼──────────────────────
   456 | S001     | S002       | ASSIGNED | FN           | 2026-03-04 10:30:05  | 2026-03-04 10:30:05
   457 | S001     | S003       | ASSIGNED | FN           | 2026-03-04 10:30:06  | 2026-03-04 10:30:06
   458 | S001     | S004       | ASSIGNED | FN           | 2026-03-04 10:30:07  | 2026-03-04 10:30:07
   ```
   ✅ **Verified**: Alterations are created for each class the staff teaches

5. **Count alterations created:**
   ```sql
   SELECT COUNT(*) FROM alteration 
   WHERE original_staff_id IN (SELECT id FROM staff WHERE staff_id = 'S001')
   AND alteration_date = '2026-03-04';
   ```
   
   **Expected**: Should match the number of classes S001 teaches on that day

---

### Test 2: Log File Verification

#### Steps

1. **Start the backend application:**
   ```bash
   cd backend
   ./gradlew bootRun
   # or
   java -jar build/libs/StaffAlterationSystem-*.jar
   ```

2. **Open logs file:**
   ```bash
   tail -f logs/application.log | grep -E "ATTENDANCE|ALTERATION|===|✅|❌"
   ```

3. **Mark attendance in the UI:**
   - Navigate to Attendance page
   - Select Staff: S001
   - Date: 2026-03-04
   - Status: LEAVE
   - Click "Mark Attendance"

4. **Observe logs in real-time:**

   **Expected Log Output:**
   ```
   ✅ ATTENDANCE SAVED: staffId=S001, date=2026-03-04, status=LEAVE, dayType=FULL_DAY, meetingHours=[]
   🔄 TRIGGERING ALTERATION PROCESS for staff: John Doe (S001), date: 2026-03-04, status: LEAVE
   📋 === ALTERATION PROCESS STARTED ===
      Staff: John Doe (S001)
      Absence Date: 2026-03-04
      Absence Status: LEAVE
      Day Type: FULL_DAY
      Day of Week: Wednesday (3)
      Total timetables found for staff: 6
      📍 Matching timetables: 5, Periods that need alteration: [1, 2, 3, 4, 5, 6]
   
   🔧 Creating alteration for period 1: CLASS-A (Mathematics)
      ✅ Alteration created: ID=456, original=S001, substitute=S002
   
   🔧 Creating alteration for period 2: CLASS-B (English)
      ✅ Alteration created: ID=457, original=S001, substitute=S003
   
   🔧 Creating alteration for period 3: CLASS-C (Science)
      ✅ Alteration created: ID=458, original=S001, substitute=S004
   
   ...
   
   📋 === ALTERATION PROCESS COMPLETED ===
      ✅ Alterations created: 5
      ❌ Alterations failed: 0
      📊 Matched timetables: 5/5
   
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

   ✅ **Verified**: All expected log messages appear in correct order

---

### Test 3: Real-Time UI Update

#### Steps

1. **Open Alteration Dashboard in Browser Window 1**
   ```
   URL: http://localhost:5173/alterations
   ```
   - Note the count of existing alterations

2. **Open Attendance page in Browser Window 2**
   ```
   URL: http://localhost:5173/attendance
   ```

3. **Mark attendance for a staff:**
   - Staff: S001
   - Date: 2026-03-04
   - Status: LEAF
   - Click "Mark Attendance"

4. **Check Browser Window 1:**
   - ✅ New alterations appear automatically (no refresh needed)
   - ✅ Alteration count increases
   - ✅ Original staff shows in "As Original" tab
   - ✅ Substitute staffs show in "As Substitute" tabs

---

### Test 4: Verify Period-Wise Absence

#### Test partial day absence

1. **Mark attendance for MORNING_ONLY:**
   ```
   Staff: S001
   Date: 2026-03-04
   Status: LEAVE
   Day Type: MORNING_ONLY
   ```

2. **Check database:**
   ```sql
   -- Should see alterations for periods 1-4 only (morning)
   SELECT period_number, absence_type 
   FROM alteration
   WHERE original_staff_id IN (SELECT id FROM staff WHERE staff_id = 'S001')
   AND alteration_date = '2026-03-04'
   ORDER BY period_number;
   ```

   **Expected:**
   ```
   period_number | absence_type
   ──────────────┼──────────────
   1             | AN
   2             | AN
   3             | AN
   4             | AN
   ```

3. **Check logs:**
   - Should see `Periods that need alteration: [1, 2, 3, 4]`
   - Should see 4 alterations created (one per period)

---

### Test 5: Verify Meeting Hours

#### Test when staff has a meeting

1. **Mark attendance as MEETING:**
   ```
   Staff: S001
   Date: 2026-03-04
   Status: MEETING
   Selected Periods (Meeting Hours): [2, 3]
   ```

2. **Check database:**
   ```sql
   SELECT period_number, absence_type, meeting_hours
   FROM alteration a
   JOIN attendance att ON a.alteration_date = att.attendance_date 
       AND a.original_staff_id = att.staff_id
   WHERE a.original_staff_id IN (SELECT id FROM staff WHERE staff_id = 'S001')
   AND a.alteration_date = '2026-03-04';
   ```

   **Expected:**
   ```
   period_number | absence_type | meeting_hours
   ──────────────┼──────────────┼─────────────────
   2             | PERIOD_WISE_ABSENT | [2, 3]
   3             | PERIOD_WISE_ABSENT | [2, 3]
   ```

3. **Verify:**
   - Only periods 2 and 3 have alterations (not 1, 4, 5, 6)
   - Absence type is PERIOD_WISE_ABSENT

---

### Test 6: Transaction Integrity Test

This test ensures both tables are updated in the same transaction (not partially).

#### Steps

1. **Start backend with transaction logging:**
   ```bash
   # Add to application.properties:
   logging.level.org.hibernate.SQL=DEBUG
   logging.level.org.springframework.transaction=DEBUG
   ```

2. **Mark attendance:**
   - Staff: S001
   - Date: 2026-03-04
   - Status: ABSENT

3. **Check log for transaction markers:**
   ```log
   [TRANSACTION STARTED]
   INSERT INTO attendance ...
   INSERT INTO alteration ...
   [TRANSACTION COMMITTED]
   ```

4. **Verify both inserts are present in same transaction:**
   - ✅ Both INSERT statements should appear
   - ✅ No ROLLBACK should appear
   - ✅ No "transaction was rolled back" error

---

## Troubleshooting

| Issue | Check | Solution |
|-------|-------|----------|
| Alterations not created | Logs show timetables found? | Ensure staff has timetable for the date |
| Attendance saved but alterations empty | Day of week match? | Check timetable dayOrder matches date |
| Status shows UNABLE_TO_ASSIGN | No suitable substitutes? | Check substitute staff attendance |
| Partial alterations created | Periods mismatch? | Verify selected periods vs meeting hours |
| Log shows "NO TIMETABLES FOUND" | Day mismatch? | Ensure timetable day_order matches date.getDayOfWeek() |

---

## Database Queries Summary

### Check attendance was saved:
```sql
SELECT COUNT(*) FROM attendance 
WHERE staff_id = 1 AND attendance_date = CURRENT_DATE;
```

### Check alterations were created:
```sql
SELECT COUNT(*) FROM alteration 
WHERE original_staff_id = 1 AND alteration_date = CURRENT_DATE;
```

### Check substitute assignments:
```sql
SELECT 
  os.staff_id as original,
  ss.staff_id as substitute,
  a.status,
  a.absence_type,
  a.period_number
FROM alteration a
JOIN staff os ON a.original_staff_id = os.id
JOIN staff ss ON a.substitute_staff_id = ss.id
WHERE a.alteration_date = CURRENT_DATE
ORDER BY a.created_at;
```

### Check update history:
```sql
SELECT * FROM alteration_audit
WHERE absence_date = CURRENT_DATE
ORDER BY created_at DESC;
```

---

## Expected Metrics

After marking attendance for S001 on 2026-03-04 (Full day leave):

| Metric | Expected Value | Actual Value |
|--------|---|---|
| Attendance records created | 1 | _____ |
| Timetables found for S001 | ~6 | _____ |
| Alterations created | ~5-6 | _____ |
| All same date | PASS | _____ |
| All alternative staff different from S001 | PASS | _____ |
| Status is ASSIGNED for all new alterations | PASS | _____ |

---

## Sign-Off Checklist

- [ ] ✅ Attendance saved to database
- [ ] ✅ Alteration records created
- [ ] ✅ Correct number of alterations for classes taught
- [ ] ✅ Correct substitute staff assigned (not original staff)
- [ ] ✅ Absence type matches the day type selected
- [ ] ✅ Period numbers match the absence type
- [ ] ✅ Status is ASSIGNED
- [ ] ✅ Real-time updates work on frontend
- [ ] ✅ Both tables updated in same transaction
- [ ] ✅ Error logs show no transaction rollbacks
- [ ] ✅ WebSocket broadcasts reach frontend
- [ ] ✅ Notifications sent (or logged as skipped)

---

**Verification Date**: __________
**Tester Name**: __________
**Status**: ☐ PASS  ☐ FAIL  ☐ PARTIAL

