# Quick Testing Guide - Alteration Triggering Fix

## Build Status
✅ **Backend compiled successfully** - Commit: `90924eb`

---

## Step 1: Start the Backend
```bash
cd d:\StaffAlteration\backend
gradle bootRun
```

**Wait for this message in the console:**
```
Started Application in X.XXX seconds
```

Keep this terminal open to watch the logs.

---

## Step 2: Start the Frontend (NEW TERMINAL)
```bash
cd d:\StaffAlteration\frontend
npm run dev
```

**Should show:**
```
  ➜  Local:   http://localhost:3000/
```

---

## Step 3: Test Attendance Marking

1. **Open** http://localhost:3000 in browser
2. **Log in** as a staff member (if not already logged in)
3. **Navigate** to "Attendance" page (sidebar menu)
4. **Fill the form:**
   - Status: **LEAVE** or **ABSENT**
   - Day Type: **FULL_DAY** (for easier testing)
   - Date: Tomorrow or next week (avoid today)
   - Remarks: (optional)
5. **Click** "Mark Attendance" button

---

## Step 4: Watch Backend Logs

In the **backend terminal**, look for lines starting with:
```
WARN: ========== ALTERATION TRIGGER START ==========
```

This section will tell you exactly what's happening.

---

## What to Look For

### ✅ IF YOU SEE:
```
WARN: ========== ALTERATION TRIGGER START ==========
WARN: Staff: YOUR_STAFF_ID (ID: X), Date: 2026-03-XX, Status: LEAVE
WARN: Day of week: 3
WARN: Total timetables for staff YOUR_STAFF_ID: 18
...
WARN: ✓✓✓ ALTERATION CREATED: ID=999, original=YOUR_STAFF_ID, substitute=ANOTHER_STAFF
...
WARN: ========== ALTERATION TRIGGER END: 1 alterations created ==========
```

**GOOD NEWS!** ✅ Alterations were created. Check frontend Alteration Dashboard.

---

### ❌ IF YOU SEE:
```
WARN: Total timetables for staff YOUR_STAFF_ID: 0
ERROR: ❌ CRITICAL: No timetables found for staff YOUR_STAFF_ID
```

**ROOT CAUSE FOUND!** ❌ 

The staff member has no timetable entries. Need to:
1. Add timetable entries for the staff in the database, OR
2. Use a staff member who has timetables assigned

**Quick Check:** Go to Timetable Management → View timetables for the staff member. If empty, that's the issue.

---

### ⚠️ IF YOU SEE:
```
WARN: Matching timetables for day 3: 0
```

The staff HAS timetables, but not for the day you're testing (Thursday is day 3).

**Fix:** Test with a different day or check which days the staff has classes.

---

### ⚠️ IF YOU SEE:
```
❌ [SUBSTITUTE SEARCH] No present staff available for period 1
```

No other staff are marked as PRESENT on that date.

**Fix:** Test with a date when multiple staff are available (not a holiday/weekend).

---

## Frontend Validation

After clicking "Mark Attendance":

1. **Immediate**: Modal should show "Attendance marked successfully"
2. **If LEAVE/ABSENT**: Another section should show "X alterations created"
3. **Go to Alteration Dashboard**: New alterations should appear automatically

---

## Troubleshooting

### Backend won't start?
- Port 8080 already in use: `netstat -ano | findstr :8080`
- Kill the process and try again

### Frontend shows nothing?
- Clear browser cache: Ctrl+Shift+Delete
- Check browser console (F12 → Console tab) for errors

### Logs not showing?
- Make sure you're looking at the **BACKEND terminal**, not frontend
- Look for `WARN:` level logs (they're highlighted)

### Alterations created but frontend not updating?
- Manual refresh: F5 in browser
- Check Network tab (F12 → Network) for `/alteration/staff/` API calls
- Verify WebSocket connection is active (look for `ws://localhost:8080`)

---

## Key Files to Check

If things still don't work:

1. **Backend Logs** → `backend/build/logs/` or console output
2. **Frontend Logs** → Browser console (F12)
3. **Database** → Check `timetable` table for staff timetables
4. **Database** → Check `attendance` table for marked attendance
5. **Database** → Check `alteration` table for created alterations

---

## Expected Flow

```
User marks attendance for JOHN_SMITH on Day=Wednesday
        ↓
Backend receives in AttendanceController
        ↓
Calls AttendanceService.markAttendance(...)
        ↓
Calls triggerAlterationProcess(...) [DETAILED LOGS START HERE]
        ↓
Fetches all timetables for JOHN_SMITH (18 found)
        ↓
Filters for Wednesday timetables (6 found)
        ↓
For each timetable:
  - Checks if substitute available
  - Creates alteration record
  - Broadcasts via WebSocket
        ↓
6 alterations created
        ↓
Frontend receives WebSocket update
        ↓
Alteration Dashboard refreshes automatically
        ↓
User sees "6 alterations created" message ✅
```

---

## Report Back With

Once you test, please share:

1. **Backend console output** (the ALTERATION TRIGGER section)
2. **Frontend behavior** (did modal show? did count appear?)
3. **What day/date you tested** 
4. **Staff member ID/name** used for testing

This will help pinpoint exactly where the process is breaking.

