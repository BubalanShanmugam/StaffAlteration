# ⚡ Quick Start Guide - Staff Alteration System

## 🏃 Quick Setup (5 minutes)

### Prerequisites
- Java 17+ installed
- Node.js 16+ installed
- PostgreSQL running (or have Supabase credentials)

### Step 1: Backend Setup (2 minutes)

```bash
# Navigate to backend
cd backend

# Update database config (if needed)
# Edit: src/main/resources/application.properties
# Update spring.datasource.url, username, password

# Build
gradle clean build -x test

# Run (Terminal 1)
gradle bootRun
```

✅ Backend starts at: `http://localhost:8080/api`

### Step 2: Frontend Setup (2 minutes)

```bash
# Navigate to frontend
cd frontend

# Install & Run (Terminal 2)
npm install
npm run dev
```

✅ Frontend starts at: `http://localhost:3000`

### Step 3: Login & Test (1 minute)

**Open browser**: `http://localhost:3000`

**Login credentials**:
- Username: `Staff1`
- Password: `password123`

---

## 🧪 Quick Test Workflow

### Test 1: Mark Attendance (As Staff)
1. Go to **Mark Attendance** page
2. Select **ABSENT** status
3. Select **FULL DAY**
4. Select today's date
5. Click **Mark Attendance**
✅ System automatically creates alterations!

### Test 2: View Alterations (As Substitute)
1. Logout (if original staff)
2. Login as `Staff2` / `password123`
3. Go to **Alterations** page
4. Switch to **"Classes I'm Substituting"** tab
✅ You should see the assigned class from Staff1's absence!

### Test 3: HOD Dashboard (As HOD)
1. This feature requires HOD role
2. Default staff are STAFF role only
3. For testing, update database directly or use admin UI

### Test 4: Manage Timetables
1. Go to **Manage Timetable** page
2. Select a class (CS or IT)
3. Click "Add Class" on empty cells
4. Fill subject and staff ID
5. Click Save
✅ Timetable updated immediately!

---

## 📊 Default Test Accounts

| Username | Password | Department | Access |
|---|---|---|---|
| Staff1 | password123 | CS | Full App |
| Staff2 | password123 | CS | Full App |
| Staff3 | password123 | IT | Full App |
| Staff4 | password123 | IT | Full App |
| Staff5 | password123 | CS | Full App |

---

## 🎯 Key Features to Try

### 1. **Attendance Marking**
```
Menu → Mark Attendance
- Full Day / Morning Only / Afternoon Only
- Single or Multiple Days (up to 7)
- Upload lesson plans & notes
```

### 2. **Alterations View**
```
Menu → Alterations
- See classes you're missing
- See classes you're substituting
- Download lesson plans
```

### 3. **Manage Timetables**
```
Menu → Manage Timetable
- Add/Edit/Delete timetable entries
- Changes apply immediately
```

### 4. **Timetable View**
```
Menu → Timetable
- View class timetables
- View by class (CS/IT)
- Weekly schedule view
```

---

## 🐛 Troubleshooting

### Backend won't start
```bash
# Check port 8080 is free
lsof -i :8080

# Kill process if needed
kill -9 <PID>

# Try different port
# Edit: application.properties
server.port=8081
```

### Frontend won't start
```bash
# Clear cache
npm cache clean --force
rm -rf node_modules

# Reinstall
npm install

# Try different port
# Edit: vite.config.ts
port: 3001
```

### Database connection fails
```
Check:
1. PostgreSQL is running
2. Database 'staff_alteration' exists
3. Username/password correct in application.properties
4. Connection URL matches your setup
```

### "Cannot connect to localhost:8080"
```
Solution:
1. Verify backend is running (gradle bootRun in another terminal)
2. Check vite.config.ts proxy settings
3. Try http://localhost:8080/api in browser directly
```

---

## 📱 Demo Scenarios

### Scenario 1: Single Absence
1. Login as Staff1
2. Mark attendance: ABSENT (Today, FULL DAY)
3. Logout → Login as Staff2
4. Check Alterations page
5. You should see Staff1's classes assigned to you!

### Scenario 2: Half-Day Absence  
1. Login as Staff3
2. Mark attendance: ABSENT (Tomorrow, MORNING ONLY)
3. System will only replace morning classes
4. Afternoon classes unaffected

### Scenario 3: Multi-Day Absence
1. Login as Staff4
2. Mark attendance: ABSENT
3. Check "Mark for multiple days"
4. Select Start: Tomorrow, End: Day after tomorrow
5. All classes for both days get alternatives!

### Scenario 4: Lesson Plan Sharing
1. Login as Staff1
2. Mark ABSENT and scroll down
3. Add notes: "Complete Chapter 5, Page 105-110"
4. Upload files: PDF, Word docs, images
5. Logout → Login as Staff2
6. Go to Alterations → View Details
7. Download the files and notes!

---

## 🔄 Data Flow

```
Staff marks ABSENT
        ↓
System finds Staff's timetable for that day
        ↓
For each class (using 4-priority algorithm):
   1. Find PRESENT staff
   2. Prefer those teaching same class
   3. Pick one with least hours today
   4. Avoid consecutive period clash
        ↓
Create ALTERATION records with status ASSIGNED
        ↓
Send NOTIFICATIONS:
   - Email to absent staff
   - Email to substitutes
   - App notification to both
        ↓
Substitutes can:
   - View assignment details
   - Download lesson plans
   - Acknowledge assignment
   - Teach the class
        ↓
HOD can:
   - Monitor all alterations
   - View statistics
   - Download reports (future)
```

---

## 📈 Performance Tips

### Backend Performance
- Database indexes automatically created
- Lazy loading for relationships
- Pagination for large lists

### Frontend Performance
- Built with Vite (fast hot reload)
- Tailwind CSS (optimized)
- Zustand (lightweight state)

---

## 🎓 Learning Resources

### Understanding the Alteration Algorithm
See: `backend/README.md` - AlterationService section

### JWT Authentication Flow
See: `backend/JWT_DOCUMENTATION_INDEX.md`

### Frontend Architecture
See: Frontend component organization in `src/components/`

---

## ✅ Pre-Deployment Checklist

- [ ] Backend builds without errors
- [ ] Frontend builds without errors
- [ ] All tests pass (login, attendance, alterations)
- [ ] Database connection works
- [ ] Default data loads on first run
- [ ] Attendance marking creates alterations
- [ ] Substitutes receive assignments
- [ ] HOD can view dashboard
- [ ] Timetables can be edited
- [ ] Logout works properly

---

## 🚀 Ready to Deploy?

Once everything works locally:

```bash
# Backend build
cd backend
gradle clean build -x test

# Frontend build
cd frontend
npm run build

# Output:
# - Backend JAR: backend/build/libs/staff-alteration-*.jar
# - Frontend: frontend/dist/

# Next: Docker or cloud deployment
```

See `FINAL_SETUP.md` for detailed deployment guide.

---

## 🆘 Still Need Help?

1. **Check logs**: Look at backend console for errors
2. **Browser console**: Open DevTools → Console tab
3. **Network tab**: Check API calls and responses
4. **Verify database**: Connect with pgAdmin or CLI
5. **Restart services**: Kill and restart both terminals

---

**Created with ❤️ for your project deadline! Good luck! 🎉**
