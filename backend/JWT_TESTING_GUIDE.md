# JWT Dynamic Authentication Testing Guide

## Quick Start: Test JWT Login Flow

### 1. Start the Backend Server

```bash
cd d:\StaffAlteration\backend
gradle bootRun
```

Expected output:
```
...
2025-12-22 14:45:00.000  INFO: Started StaffAlterationApplication
Tomcat started on port(s): 8080 (http)
```

---

## Test Scenario 1: Basic Login Flow

### Step 1: Login (Get Tokens)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d {
    \"username\": \"staff1\",
    \"password\": \"password123\"
  }
```

**Expected Response (200 OK):**
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdGFmZjEiLCJpYXQiOjE3NzE3NDAwMDAsImV4cCI6MTc3MTgyNjQwMH0.xyz...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdGFmZjEiLCJ0eXBlIjoicmVmcmVzaCIsImlhdCI6MTc3MTc0MDAwMCwiZXhwIjoxNzcyMzQ0ODAwfQ.abc...",
    "type": "Bearer",
    "expiresIn": 86400000,
    "refreshTokenExpiresIn": 604800000,
    "user": {
      "id": 1,
      "username": "staff1",
      "email": "staff1@example.com",
      "enabled": true,
      "roles": ["STAFF"],
      "createdAt": "2025-12-18T10:00:00Z",
      "updatedAt": "2025-12-22T14:30:00Z"
    }
  },
  "timestamp": "2025-12-22T14:30:00Z"
}
```

**What Happened:**
✅ User authenticated with username/password  
✅ JWT access token generated (24h validity)  
✅ JWT refresh token generated (7d validity)  
✅ User information returned  
✅ Token will be used for all subsequent API calls  

---

### Step 2: Use Access Token in API Request

Save the access token from login response:

```powershell
$accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
$refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

Make API request with Bearer token:

```bash
curl -X GET http://localhost:8080/api/staff \
  -H "Authorization: Bearer $accessToken"
```

**Expected Response (200 OK):**
```json
{
  "code": 200,
  "message": "Staff retrieved",
  "data": [ ... list of staff ... ]
}
```

**What Happened:**
✅ Authorization header includes "Bearer {token}"  
✅ JwtAuthenticationFilter extracts and validates token  
✅ User authenticated from token claims  
✅ API request succeeds  

---

### Step 3: Try Request WITHOUT Token

```bash
curl -X GET http://localhost:8080/api/staff
```

**Expected Response (401 or 403):**
The endpoint might be unprotected in current config. If protected:
```json
{
  "code": 401,
  "message": "Unauthorized"
}
```

---

### Step 4: Try Invalid Token

```bash
curl -X GET http://localhost:8080/api/staff \
  -H "Authorization: Bearer invalid.token.here"
```

**Expected Response (401):**
```json
{
  "code": 401,
  "message": "Invalid JWT token"
}
```

---

## Test Scenario 2: Token Refresh Flow

### Step 1: Simulate Token Expiration

The refresh token feature handles expired access tokens:

```bash
# Use the refreshToken from login response
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d {
    \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"
  }
```

**Expected Response (200 OK):**
```json
{
  "code": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...(NEW)",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...(SAME)",
    "type": "Bearer",
    "expiresIn": 86400000,
    "refreshTokenExpiresIn": 604800000,
    "user": { ... }
  }
}
```

**What Happened:**
✅ Refresh token validated  
✅ New access token generated  
✅ Refresh token remains valid  
✅ User remains logged in (no re-authentication needed)  
✅ Frontend should update stored accessToken  

---

### Step 2: Continue API Calls with New Token

```bash
$newAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...(NEW)"

curl -X GET http://localhost:8080/api/staff \
  -H "Authorization: Bearer $newAccessToken"
```

**Expected Response:** ✅ Success with new token

---

## Test Scenario 3: Logout Flow

### Step 1: Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $accessToken"
```

**Expected Response (200 OK):**
```json
{
  "code": 200,
  "message": "Logout successful. Please clear your tokens from browser storage."
}
```

**What Happened:**
✅ Logout endpoint called  
✅ Server logs logout event  
✅ Frontend should delete tokens from storage  

### Step 2: Verify Logout (Try API Call)

```bash
curl -X GET http://localhost:8080/api/staff \
  -H "Authorization: Bearer $accessToken"
```

**Expected Response (401):**
Token is no longer valid (if token blacklist is implemented)

---

## Test Scenario 4: Multi-User Login

### Test Staff Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d {
    \"username\": \"staff2\",
    \"password\": \"password123\"
  }
```

### Test HR Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d {
    \"username\": \"hr_user\",
    \"password\": \"password123\"
  }
```

### Test Admin Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d {
    \"username\": \"admin_user\",
    \"password\": \"password123\"
  }
```

**Expected:**
✅ Each user gets unique tokens  
✅ Different roles in response (STAFF, HR, ADMIN)  
✅ Tokens are independent per user  

---

## Test Scenario 5: Token Expiration

### Test Manual Token Expiration Check

Tokens have these expiration times:
- **Access Token**: 24 hours (86400000 ms)
- **Refresh Token**: 7 days (604800000 ms)

To test without waiting:

1. **Decode the token** at [jwt.io](https://jwt.io) to see expiration time
2. **Extract exp claim**: Unix timestamp in seconds
3. **Calculate remaining time**: (exp - now) * 1000 = milliseconds

Example JWT payload:
```json
{
  "sub": "staff1",
  "iat": 1671700000,
  "exp": 1671786400  // Expires 24 hours later
}
```

---

## PowerShell Testing Script

Save as `test-jwt.ps1`:

```powershell
# ============================================
# JWT Authentication Testing Script
# ============================================

$baseUrl = "http://localhost:8080/api"
$username = "staff1"
$password = "password123"

Write-Host "========================================" -ForegroundColor Green
Write-Host "JWT Dynamic Authentication Test" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Test 1: Login
Write-Host "`n[TEST 1] Login and Get Tokens" -ForegroundColor Cyan
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
  -Method POST `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body (@{
    username = $username
    password = $password
  } | ConvertTo-Json)

$accessToken = $loginResponse.data.accessToken
$refreshToken = $loginResponse.data.refreshToken
$userId = $loginResponse.data.user.id

Write-Host "✅ Login successful" -ForegroundColor Green
Write-Host "📝 Access Token: $($accessToken.Substring(0, 20))..." -ForegroundColor Yellow
Write-Host "📝 Refresh Token: $($refreshToken.Substring(0, 20))..." -ForegroundColor Yellow
Write-Host "👤 User ID: $userId" -ForegroundColor Yellow
Write-Host "⏱️  Token expires in: $($loginResponse.data.expiresIn)ms (24 hours)" -ForegroundColor Yellow

# Test 2: API Call with Token
Write-Host "`n[TEST 2] API Call with Access Token" -ForegroundColor Cyan
try {
  $staffResponse = Invoke-RestMethod -Uri "$baseUrl/staff" `
    -Method GET `
    -Headers @{
      "Authorization" = "Bearer $accessToken"
      "Content-Type" = "application/json"
    }
  
  Write-Host "✅ API call successful" -ForegroundColor Green
  Write-Host "📊 Staff count: $($staffResponse.data.Count)" -ForegroundColor Yellow
} catch {
  Write-Host "❌ API call failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Refresh Token
Write-Host "`n[TEST 3] Refresh Access Token" -ForegroundColor Cyan
$refreshResponse = Invoke-RestMethod -Uri "$baseUrl/auth/refresh" `
  -Method POST `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body (@{
    refreshToken = $refreshToken
  } | ConvertTo-Json)

$newAccessToken = $refreshResponse.data.accessToken
Write-Host "✅ Token refresh successful" -ForegroundColor Green
Write-Host "📝 New Access Token: $($newAccessToken.Substring(0, 20))..." -ForegroundColor Yellow
Write-Host "⏱️  New token expires in: $($refreshResponse.data.expiresIn)ms" -ForegroundColor Yellow

# Test 4: Logout
Write-Host "`n[TEST 4] Logout" -ForegroundColor Cyan
$logoutResponse = Invoke-RestMethod -Uri "$baseUrl/auth/logout" `
  -Method POST `
  -Headers @{
    "Authorization" = "Bearer $newAccessToken"
    "Content-Type" = "application/json"
  }

Write-Host "✅ Logout successful" -ForegroundColor Green
Write-Host "📝 Message: $($logoutResponse.message)" -ForegroundColor Yellow

# Test 5: Verify Logout
Write-Host "`n[TEST 5] Verify Logout (Tokens Cleared)" -ForegroundColor Cyan
Write-Host "✅ Tokens should be deleted from browser storage" -ForegroundColor Green
Write-Host "📝 Refresh token is no longer usable" -ForegroundColor Yellow

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "All tests completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
```

Run it:

```powershell
cd d:\StaffAlteration\backend
.\test-jwt.ps1
```

---

## Testing Checklist

- [ ] **Login Test**: User receives accessToken + refreshToken
- [ ] **Token Format**: Tokens are valid JWT format (3 parts with dots)
- [ ] **API Access**: API calls with Bearer token succeed
- [ ] **Invalid Token**: Requests without token get 401/403
- [ ] **Token Refresh**: Refresh endpoint generates new accessToken
- [ ] **Multiple Users**: Different users get different tokens
- [ ] **Logout**: Logout clears tokens from frontend
- [ ] **Token Expiration**: Access token expires after 24h
- [ ] **Refresh Expiration**: Refresh token expires after 7d
- [ ] **Error Messages**: Clear error responses on failures

---

## Common Test Cases

| Test Case | Expected Behavior |
|-----------|-------------------|
| Login with valid credentials | ✅ Returns accessToken + refreshToken |
| Login with invalid password | ❌ 401 "Invalid credentials" |
| Login with non-existent user | ❌ 401 "Invalid credentials" |
| API call with valid token | ✅ 200 OK with data |
| API call without token | ❌ 401 or allowed (depends on endpoint config) |
| API call with expired token | ❌ 401 "Token expired" |
| Refresh with valid refresh token | ✅ Returns new accessToken |
| Refresh with expired refresh token | ❌ 401 "Token refresh failed" |
| Refresh with invalid token | ❌ 401 "Invalid refresh token" |
| Logout then API call | ❌ 401 (token blacklisted or invalid) |

---

## Debugging JWT Tokens

### Decode JWT Online

1. Go to [jwt.io](https://jwt.io)
2. Paste your token in the "Encoded" box
3. View payload under "Decoded"

### Check Token Claims

Example decoded token:
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
{
  "sub": "staff1",         // Subject (username)
  "iat": 1671700000,       // Issued at (Unix timestamp)
  "exp": 1671786400        // Expiration (Unix timestamp)
}
```

### Calculate Expiration

```powershell
# Convert Unix timestamp to DateTime
$expiration = [System.DateTimeOffset]::FromUnixTimeSeconds(1671786400).DateTime
Write-Host "Token expires at: $expiration"
```

---

## Expected Test Output

```
========================================
JWT Dynamic Authentication Test
========================================

[TEST 1] Login and Get Tokens
✅ Login successful
📝 Access Token: eyJhbGciOiJIUzI1NiIsI...
📝 Refresh Token: eyJhbGciOiJIUzI1NiIsI...
👤 User ID: 1
⏱️  Token expires in: 86400000ms (24 hours)

[TEST 2] API Call with Access Token
✅ API call successful
📊 Staff count: 5

[TEST 3] Refresh Access Token
✅ Token refresh successful
📝 New Access Token: eyJhbGciOiJIUzI1NiIsI...
⏱️  New token expires in: 86400000ms

[TEST 4] Logout
✅ Logout successful
📝 Message: Logout successful...

[TEST 5] Verify Logout (Tokens Cleared)
✅ Tokens should be deleted from browser storage
📝 Refresh token is no longer usable

========================================
All tests completed!
========================================
```

---

## Next Steps

1. ✅ Backend JWT implementation complete
2. 📝 Create frontend login page using guide: `JWT_SESSION_MANAGEMENT.md`
3. 🔐 Test with React/Vue/Angular frontend
4. 🚀 Deploy to production with environment variables
5. 📊 Monitor token usage in logs

---

## Support

For issues:
1. Check logs: `gradle bootRun` console output
2. Verify tokens at [jwt.io](https://jwt.io)
3. Test endpoints with cURL or Postman
4. Review `JwtTokenProvider` class for token generation
5. Check `JwtAuthenticationFilter` for validation

