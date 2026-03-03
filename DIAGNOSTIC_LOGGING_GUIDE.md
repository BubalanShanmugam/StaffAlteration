# Diagnostic Logging Guide - Alteration Triggering Issue

## Overview
Comprehensive diagnostic logging has been added to the alteration triggering system to help identify why alterations are not being created when attendance is marked.

## What Was Added

### 1. **AttendanceService.triggerAlterationProcess()** - Lines 151-219
This method is called when attendance is marked. It now logs:

**Checkpoint 1: Process Start**
```
========== ALTERATION TRIGGER START ==========
Staff: JOHN_SMITH (ID: 5), Date: 2026-03-03, Status: LEAVE
Day of week: 2 (1=Monday, 7=Sunday)
```

**Checkpoint 2: Total Timetables**
```
Total timetables for staff JOHN_SMITH: 18
```
- If this shows **0**, no timetables exist for the staff member (LIKELY ROOT CAUSE)

**Checkpoint 3: Matching Timetables by Day**
```
Matching timetables for day 2: 6
Periods needing alteration: [1, 2, 3, 4]
```
- Shows how many timetables match the day of week

**Checkpoint 4: Alteration Creation**
```
✓ Creating alteration for: period=1, class=10-A
✓ Alteration created: ID=456, substitute=MARY_JONES
```
- Shows each alteration being created successfully

**Checkpoint 5: Summary**
```
========== ALTERATION TRIGGER END: 4 alterations created ==========
```

---

### 2. **AlterationService.processAlteration()** - Lines 66-157
More granular logging for each individual alteration:

```
========== PROCESS ALTERATION START ==========
Timetable: 42, Date: 2026-03-03, Class: 10-A, Period: 1
Original Staff: JOHN_SMITH (ID: 5)
Attendance found: status=LEAVE, dayType=FULL_DAY
✓ Alteration IS needed for this staff
Absence type determined: FN
Finding suitable substitute...
✓ Substitute found: MARY_JONES (ID: 8)
✓✓✓ ALTERATION CREATED: ID=456, original=JOHN_SMITH, substitute=MARY_JONES, absenceType=FN
========== PROCESS ALTERATION END (SUCCESS) ==========
```

---

### 3. **AlterationService.findSubstitute()** - Lines 236-300
Detailed logging for substitute selection:

```
[SUBSTITUTE SEARCH] Starting substitute search for class 10-A, period 1
[SUBSTITUTE SEARCH] Total active staff in system: 25
[SUBSTITUTE SEARCH] Staff after removing original: 24
[SUBSTITUTE SEARCH] Present staff (Priority 1): 18
[SUBSTITUTE SEARCH] Available (not allocated) staff: 15
[SUBSTITUTE SEARCH] Same department staff (Priority 2): 10
[SUBSTITUTE SEARCH] Selected: MARY_JONES (ID: 8)
```

If no substitute found, you'll see:
```
❌ [SUBSTITUTE SEARCH] No present staff available for period 1
```

---

### 4. **AlterationService.selectBestStaff()** - Lines 312-330
Scoring and prioritization:

```
[SELECT BEST] Scoring 10 candidates for period 1
  [SELECT BEST] Staff MARY_JONES has score: 12.5
  [SELECT BEST] Staff JOHN_DOE has score: 25.0
  [SELECT BEST] Staff JANE_SMITH has score: 35.5
✓✓ [SELECT BEST] Best candidate: MARY_JONES with score: 12.5
```

---

## How to Test

### Step 1: Stop and Restart Backend
```bash
cd d:\StaffAlteration\backend
gradle bootRun
```
Wait for: `Started Application in X seconds`

### Step 2: Keep Terminal Open
Keep the backend console visible to capture logs.

### Step 3: Mark Attendance
1. Go to Frontend (http://localhost:3000)
2. Navigate to Attendance Page
3. Select a staff member (yourself if logged in as staff)
4. Choose a date (tomorrow or next week)
5. Select Status: **LEAVE** or **ABSENT**
6. Choose Day Type: **FULL_DAY**
7. Click **Mark Attendance**

### Step 4: Check Backend Logs
In the backend console, look for lines starting with:
```
WARN: ========== ALTERATION TRIGGER START ==========
```

---

## Expected Behavior by Scenario

### ✅ SCENARIO 1: SUCCESS - Alterations Created
**In backend console:**
```
WARN: ========== ALTERATION TRIGGER START ==========
WARN: Staff: YOUR_STAFF_ID (ID: X), Date: 2026-03-XX, Status: LEAVE
WARN: Day of week: 3 (3=Wednesday)
WARN: Total timetables for staff YOUR_STAFF_ID: 18
DEBUG:   Timetable: dayOrder=3, period=1, class=CLASS10A, subject=Mathematics
DEBUG:   Timetable: dayOrder=3, period=2, class=CLASS10A, subject=Mathematics
...
WARN: Matching timetables for day 3: 6
WARN: Periods needing alteration: [1, 2, 3, 4, 5, 6]
WARN: ✓ Creating alteration for: period=1, class=CLASS10A
WARN: ✓ Alteration created: ID=999, substitute=SUBSTITUTE_STAFF
...
WARN: ========== ALTERATION TRIGGER END: 6 alterations created ==========
```

**In frontend:**
- Success modal shows "Alterations created: 6"
- Alteration Dashboard auto-updates with new entries

---

### ❌ SCENARIO 2: NO TIMETABLES - Root Cause Found
**In backend console:**
```
WARN: ========== ALTERATION TRIGGER START ==========
WARN: Staff: YOUR_STAFF_ID (ID: X), Date: 2026-03-XX, Status: LEAVE
WARN: Day of week: 3
WARN: Total timetables for staff YOUR_STAFF_ID: 0
ERROR: ❌ CRITICAL: No timetables found for staff YOUR_STAFF_ID. Cannot create alterations!
WARN: ========== ALTERATION TRIGGER END (NO TIMETABLES) ==========
```

**What this means:**
- The staff member has NO timetable entries in the database
- No classes assigned to them
- Alterations cannot be created without timetables to substitute for

**Fix needed:**
- Add timetable entries for the staff member in the database
- Ensure timetables have correct dayOrder values (1-7 for Mon-Sun)

---

### ❌ SCENARIO 3: No Substitutes Available
**In backend console:**
```
[SUBSTITUTE SEARCH] Starting substitute search for class 10-A, period 1
[SUBSTITUTE SEARCH] Total active staff in system: 5
[SUBSTITUTE SEARCH] Present staff (Priority 1): 1
❌ [SUBSTITUTE SEARCH] No present staff available for period 1
```

**What this means:**
- No staff members are marked as PRESENT on that day
- All other staff are also absent/on-leave/in-meeting
- Cannot find anyone to substitute

**Fix:**
- Test with a date when other staff members are present
- Or manually mark some staff as PRESENT

---

### ❌ SCENARIO 4: Day Doesn't Match
**In backend console:**
```
WARN: ========== ALTERATION TRIGGER START ==========
WARN: Total timetables for staff YOUR_STAFF_ID: 15
WARN: Day of week: 4 (Thursday)
WARN: Matching timetables for day 4: 0
```

**What this means:**
- Staff HAS timetables, but none for Thursday
- All their timetables have dayOrder values 1-3 and 5-7
- No classes on Thursday

**Fix:**
- Test with a different day (Monday, Tuesday, Wednesday, etc.)
- Or add Thursday timetables for the staff member

---

## Critical Debug Points

| Log Message | Means | Action |
|---|---|---|
| `Total timetables for staff: 0` | No timetables exist | Add timetables to database |
| `Matching timetables for day X: 0` | No timetables for that day | Test different day or add timetables |
| `No present staff available` | No substitutes available | Test with different date |
| `ALTERATION CREATED: ID=XXX` | ✅ Success - alteration was created | Check frontend for display |
| `ALTERATION TRIGGER END: 6 alterations created` | ✅ Success - all alterations made | Frontend should auto-update |

---

## Frontend Validation

After marking attendance:

1. **Immediate Response** - Success modal should show alteration count
2. **Auto-Refresh** - Alteration Dashboard should automatically load new alterations
3. **WebSocket** - Real-time updates via WebSocket should display new alterations

If alterations aren't showing in frontend but logs show "created", check:
- Browser console for JavaScript errors
- Network tab for API calls to `/alteration/staff/{staffId}`
- Confirm WebSocket connection is active

---

## Next Steps

1. ✅ Backend build complete with enhanced logging
2. ⏳ Start backend and test attendance marking
3. ⏳ Check logs to identify which scenario you hit
4. ⏳ Apply appropriate fix based on scenario
5. ⏳ Re-test and confirm alterations appear

