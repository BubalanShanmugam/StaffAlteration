# JWT Authentication Quick Reference Card

## 🚀 Quick Start (30 seconds)

### Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"staff1","password":"password123"}'
```

Get tokens from response:
- `data.accessToken` → Use in API calls
- `data.refreshToken` → Use to refresh when expired

### Use Token in API
```bash
curl http://localhost:8080/api/staff \
  -H "Authorization: Bearer {accessToken}"
```

### Refresh When Expired (401)
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"{refreshToken}"}'
```

---

## 📋 API Endpoints

| Method | Endpoint | Purpose | Request |
|--------|----------|---------|---------|
| POST | `/api/auth/login` | Get tokens | `{username, password}` |
| POST | `/api/auth/refresh` | New access token | `{refreshToken}` |
| POST | `/api/auth/logout` | Logout | Header: `Authorization: Bearer` |
| GET | `/api/auth/user/{id}` | Get user info | Header: `Authorization: Bearer` |

---

## 🎯 Login Response

```json
{
  "data": {
    "accessToken": "eyJ...",           ← Use for API calls
    "refreshToken": "eyJ...",          ← Use to refresh
    "type": "Bearer",                  ← Always "Bearer"
    "expiresIn": 86400000,             ← 24 hours in ms
    "refreshTokenExpiresIn": 604800000, ← 7 days in ms
    "user": {
      "id": 1,
      "username": "staff1",
      "roles": ["STAFF"]
    }
  }
}
```

---

## 💾 Frontend Token Storage

### Best Practice: sessionStorage (Auto-clear on browser close)
```javascript
// After login
sessionStorage.setItem('accessToken', response.accessToken);
sessionStorage.setItem('refreshToken', response.refreshToken);

// In API calls
const token = sessionStorage.getItem('accessToken');
headers: { 'Authorization': `Bearer ${token}` }

// On logout
sessionStorage.removeItem('accessToken');
sessionStorage.removeItem('refreshToken');
```

---

## 🔄 API Request Pattern

```javascript
// 1. Make API call with token
async function apiCall(url) {
  let response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
    }
  });

  // 2. If 401 (token expired)
  if (response.status === 401) {
    // 3. Refresh the token
    const refreshRes = await fetch('/api/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({
        refreshToken: sessionStorage.getItem('refreshToken')
      })
    });
    
    const data = await refreshRes.json();
    sessionStorage.setItem('accessToken', data.data.accessToken);
    
    // 4. Retry original request
    response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${data.data.accessToken}`
      }
    });
  }

  return response.json();
}
```

---

## ⏰ Token Lifetime

```
Login Time              24h later              7d later
    |                       |                      |
    v                       v                      v
[Access Token] -----> Expired (401) -----> Need login
[Refresh Token] -----> Still valid -----> Expired (must login)
```

**Action:** When access token expires → Use refresh token → Get new access token → Continue without re-login

---

## 🧪 Testing

### 1. Start Server
```bash
cd backend && gradle bootRun
```

### 2. Run Test Script
```powershell
# Windows PowerShell
.\test-jwt.ps1
```

### 3. Manual Test
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"staff1","password":"password123"}'

# Extract token from response, then:

# Use in API call
curl http://localhost:8080/api/staff \
  -H "Authorization: Bearer eyJ..."

# Refresh
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJ..."}'

# Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJ..."
```

---

## 🔐 Production Setup

### 1. Generate Strong JWT Secret
```powershell
$secret = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 64 | % {[char]$_})
echo $secret
```

### 2. Set Environment Variables
```bash
# Linux/Mac
export JWT_SECRET="your-generated-secret-here"
export DB_PASSWORD="your-db-password"
export CORS_ORIGINS="https://app.example.com"

# Windows PowerShell
$env:JWT_SECRET="your-generated-secret-here"
$env:DB_PASSWORD="your-db-password"
$env:CORS_ORIGINS="https://app.example.com"
```

### 3. Run with Variables Set
```bash
java -jar build/libs/staff-alteration-1.0.jar
```

---

## 🐛 Debugging

### Decode Token at jwt.io
- Go to [jwt.io](https://jwt.io)
- Paste token in left box
- View decoded payload with:
  - `sub`: username
  - `iat`: issued at (Unix time)
  - `exp`: expiration (Unix time)

### Check Backend Logs
```
✅ User staff1 logged in successfully
🔄 Refreshing access token
❌ Authentication failed
🚪 Logout for user staff1
```

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| 401 "Invalid credentials" | Wrong password | Check username/password |
| 401 "Token refresh failed" | Refresh token expired | Login again |
| 401 "Invalid JWT token" | Token tampered/expired | Use new token from refresh |
| 403 Forbidden | No Authorization header | Add `Authorization: Bearer` header |
| CORS error | Frontend URL not allowed | Check `app.cors.allowed-origins` |

---

## 📊 User Types

| User | Username | Password | Role |
|------|----------|----------|------|
| Staff | staff1 | password123 | STAFF |
| Staff 2 | staff2 | password123 | STAFF |
| HR | hr_user | password123 | HR |
| Admin | admin_user | password123 | ADMIN |

### Test Each User
```bash
# Staff
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"staff1","password":"password123"}'

# HR
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"hr_user","password":"password123"}'

# Admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin_user","password":"password123"}'
```

Each user gets unique tokens with their role in user info.

---

## 🎯 Implementation Checklist

- [ ] Backend running: `gradle bootRun` ✓
- [ ] Test login endpoint ✓
- [ ] Test API with token ✓
- [ ] Test token refresh ✓
- [ ] Test logout ✓
- [ ] Frontend: Store access token ✓
- [ ] Frontend: Add token to API calls ✓
- [ ] Frontend: Handle 401 with refresh ✓
- [ ] Frontend: Clear tokens on logout ✓
- [ ] Production: Set JWT_SECRET env var ✓
- [ ] Production: Enable HTTPS ✓

---

## 📚 Full Documentation

For complete details:
- **Setup Guide**: `JWT_SESSION_MANAGEMENT.md`
- **Testing Guide**: `JWT_TESTING_GUIDE.md`
- **Implementation Details**: `JWT_IMPLEMENTATION_SUMMARY.md`

---

## 🎬 Quick Scenarios

### Scenario 1: First-Time User Login
```
1. User enters username: "staff1"
2. User enters password: "password123"
3. Frontend: POST /api/auth/login
4. Backend: Validate credentials
5. Backend: Generate accessToken (24h) + refreshToken (7d)
6. Frontend: Store both tokens
7. Frontend: Redirect to dashboard
✅ User logged in!
```

### Scenario 2: Making API Call
```
1. User clicks "View Staff"
2. Frontend gets accessToken from storage
3. Frontend: GET /api/staff with Authorization header
4. Backend: Validate token signature & expiration
5. Backend: Extract username
6. Backend: Process request as authenticated user
7. Backend: Return data
✅ Data displayed!
```

### Scenario 3: Token Expires During Session
```
1. User making API call after 24 hours
2. Backend: Token validation fails (expired)
3. Backend: Return 401 Unauthorized
4. Frontend: Detect 401 response
5. Frontend: POST /api/auth/refresh with refreshToken
6. Backend: Validate refreshToken (still valid for 7d)
7. Backend: Generate new accessToken
8. Frontend: Store new accessToken
9. Frontend: Retry original request
✅ User stays logged in!
```

### Scenario 4: User Logout After 7 Days
```
1. User logged in 7 days ago
2. User makes API call
3. Backend: accessToken valid? NO (24h expired)
4. Frontend: Try refresh
5. Backend: refreshToken valid? NO (7d expired)
6. Backend: Return 401 "Token refresh failed"
7. Frontend: Redirect to login
8. User enters credentials again
✅ Fresh login session!
```

---

## 🔗 Related Files

```
backend/
├── src/main/java/com/staffalteration/
│   ├── security/
│   │   ├── JwtTokenProvider.java (Token generation/validation)
│   │   └── JwtAuthenticationFilter.java (Token validation on requests)
│   ├── controller/
│   │   └── AuthController.java (Login/Refresh/Logout endpoints)
│   ├── service/
│   │   └── AuthenticationService.java (Auth business logic)
│   └── dto/
│       └── AuthResponseDTO.java (Token response format)
│
└── documentation/
    ├── JWT_SESSION_MANAGEMENT.md (Complete guide)
    ├── JWT_TESTING_GUIDE.md (Testing documentation)
    ├── JWT_IMPLEMENTATION_SUMMARY.md (Implementation details)
    └── JWT_QUICK_REFERENCE.md (This file)
```

---

## ✨ That's All!

Your JWT authentication system is:
- ✅ Dynamically generating tokens on login
- ✅ Returning both access and refresh tokens
- ✅ Validating tokens on API calls
- ✅ Refreshing tokens automatically
- ✅ Supporting logout
- ✅ Production-ready with env variables

**Next Step:** Integrate with your frontend using the patterns above!

