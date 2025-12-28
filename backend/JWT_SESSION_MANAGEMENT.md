# JWT Session Management Guide

## Overview

This document provides complete guidance on JWT token management for dynamic authentication in the Staff Alteration System. The system implements a **two-token strategy**:

- **Access Token**: Short-lived (24 hours) for API requests
- **Refresh Token**: Long-lived (7 days) for obtaining new access tokens

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (Browser)                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. Login Form                                                   │
│     ↓                                                             │
│  2. POST /api/auth/login {username, password}                   │
│     ↓                                                             │
│  3. Receive {accessToken, refreshToken, user}                   │
│     ↓                                                             │
│  4. Store tokens in browser                                      │
│     - accessToken: sessionStorage or Cookie (HttpOnly)          │
│     - refreshToken: Cookie with HttpOnly flag                   │
│     ↓                                                             │
│  5. Include in API requests                                      │
│     Header: "Authorization: Bearer {accessToken}"               │
│     ↓                                                             │
│  6. On 401 response:                                             │
│     POST /api/auth/refresh with refreshToken                    │
│     → Get new accessToken                                       │
│     ↓                                                             │
│  7. On logout:                                                   │
│     - DELETE both tokens from storage                            │
│     - POST /api/auth/logout (optional)                          │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
           ↓                           ↓                ↓
┌─────────────────────────────────────────────────────────────────┐
│                      BACKEND (Spring Boot)                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  JwtTokenProvider                                                │
│  ├── generateToken(username): accessToken (24h)                 │
│  ├── generateRefreshToken(username): refreshToken (7d)          │
│  ├── validateToken(token): boolean                               │
│  └── getUsernameFromJWT(token): username                         │
│                                                                   │
│  JwtAuthenticationFilter                                         │
│  ├── Extract "Authorization: Bearer {token}" header             │
│  ├── Validate token signature & expiration                       │
│  ├── Load user from CustomUserDetailsService                     │
│  └── Set SecurityContext for current request                     │
│                                                                   │
│  AuthenticationService                                           │
│  ├── login(): Generate accessToken + refreshToken               │
│  ├── refreshToken(): Generate new accessToken                   │
│  └── logout(): Invalidate session (optional)                    │
│                                                                   │
│  AuthController                                                  │
│  ├── POST /api/auth/login                                       │
│  ├── POST /api/auth/refresh                                     │
│  ├── POST /api/auth/logout                                      │
│  └── GET /api/auth/user/{id}                                    │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## API Endpoints

### 1. Login (Create Session)

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "staff1",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
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

**Key Details:**
- `expiresIn`: 86400000 ms = 24 hours
- `refreshTokenExpiresIn`: 604800000 ms = 7 days
- Tokens are dynamically generated for each login
- Different users get different tokens

---

### 2. Refresh Access Token

When access token expires (401 response), use refresh token to get a new one:

```http
POST /api/auth/refresh
Authorization: Bearer {refreshToken}
```

**OR** with JSON body:
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (NEW)",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (SAME)",
    "type": "Bearer",
    "expiresIn": 86400000,
    "refreshTokenExpiresIn": 604800000,
    "user": { ... }
  }
}
```

**What Happens:**
- New access token is generated (24h validity)
- Same refresh token is returned (maintains 7d validity)
- No user re-authentication needed

---

### 3. Logout (Destroy Session)

```http
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Logout successful. Please clear your tokens from browser storage."
}
```

**What Happens:**
- Frontend should delete both tokens from storage
- In production, tokens are added to a blacklist (if implemented)
- User is logged out

---

## Frontend Implementation

### React Example

#### 1. **Login Handler**

```javascript
// services/authService.js
export const login = async (username, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // Include cookies
    body: JSON.stringify({ username, password })
  });

  if (!response.ok) {
    throw new Error('Login failed');
  }

  const data = await response.json();
  
  // Store tokens
  const { accessToken, refreshToken } = data.data;
  
  // Option 1: Store in sessionStorage (auto-cleared on browser close)
  sessionStorage.setItem('accessToken', accessToken);
  sessionStorage.setItem('refreshToken', refreshToken);
  
  // Option 2: Store in localStorage (persists across sessions)
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
  
  // Option 3: Store in secure cookie (HttpOnly for security)
  // This should be handled by backend with Set-Cookie header
  
  // Store user info
  sessionStorage.setItem('user', JSON.stringify(data.data.user));
  
  return data.data;
};
```

#### 2. **API Request Helper (Auto-attach Token)**

```javascript
// services/apiClient.js
export const apiCall = async (url, options = {}) => {
  const accessToken = sessionStorage.getItem('accessToken');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };
  
  // Attach access token to every request
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }
  
  let response = await fetch(url, {
    ...options,
    headers,
    credentials: 'include'
  });
  
  // Handle token expiration (401 response)
  if (response.status === 401) {
    const refreshToken = sessionStorage.getItem('refreshToken');
    
    if (refreshToken) {
      // Try to refresh the token
      const newAccessToken = await refreshAccessToken(refreshToken);
      
      if (newAccessToken) {
        sessionStorage.setItem('accessToken', newAccessToken);
        
        // Retry the original request with new token
        headers['Authorization'] = `Bearer ${newAccessToken}`;
        response = await fetch(url, {
          ...options,
          headers,
          credentials: 'include'
        });
      } else {
        // Refresh failed, redirect to login
        redirectToLogin();
      }
    } else {
      // No refresh token, redirect to login
      redirectToLogin();
    }
  }
  
  return response.json();
};
```

#### 3. **Refresh Token Function**

```javascript
// services/authService.js
export const refreshAccessToken = async (refreshToken) => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ refreshToken })
    });

    if (!response.ok) {
      throw new Error('Token refresh failed');
    }

    const data = await response.json();
    return data.data.accessToken;
  } catch (error) {
    console.error('Refresh token error:', error);
    return null;
  }
};
```

#### 4. **Logout Handler**

```javascript
// services/authService.js
export const logout = async () => {
  const accessToken = sessionStorage.getItem('accessToken');
  
  try {
    await fetch('http://localhost:8080/api/auth/logout', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`
      },
      credentials: 'include'
    });
  } catch (error) {
    console.error('Logout error:', error);
  }
  
  // Clear all tokens and user data
  sessionStorage.removeItem('accessToken');
  sessionStorage.removeItem('refreshToken');
  sessionStorage.removeItem('user');
  
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  
  // Redirect to login
  window.location.href = '/login';
};
```

#### 5. **Login Component**

```jsx
// components/LoginForm.jsx
import React, { useState } from 'react';
import { login } from '../services/authService';
import { useNavigate } from 'react-router-dom';

export default function LoginForm() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await login(username, password);
      console.log('✅ Login successful:', response);
      
      // Redirect to dashboard
      navigate('/dashboard');
    } catch (err) {
      setError('Invalid username or password');
      console.error('❌ Login failed:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Login</h2>
      
      {error && <div className="error">{error}</div>}
      
      <div>
        <label>Username:</label>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="staff1"
          required
        />
      </div>
      
      <div>
        <label>Password:</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="password123"
          required
        />
      </div>
      
      <button type="submit" disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
}
```

---

### Vue.js Example

```javascript
// composables/useAuth.js
import { ref } from 'vue';

export function useAuth() {
  const accessToken = ref(sessionStorage.getItem('accessToken'));
  const user = ref(JSON.parse(sessionStorage.getItem('user') || 'null'));

  const login = async (username, password) => {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    const data = await response.json();
    
    accessToken.value = data.data.accessToken;
    user.value = data.data.user;
    
    sessionStorage.setItem('accessToken', data.data.accessToken);
    sessionStorage.setItem('refreshToken', data.data.refreshToken);
    sessionStorage.setItem('user', JSON.stringify(data.data.user));
    
    return data.data;
  };

  const logout = async () => {
    await fetch('http://localhost:8080/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken.value}`
      }
    });

    accessToken.value = null;
    user.value = null;
    
    sessionStorage.removeItem('accessToken');
    sessionStorage.removeItem('refreshToken');
    sessionStorage.removeItem('user');
  };

  return {
    accessToken,
    user,
    login,
    logout
  };
}
```

---

## Token Storage Best Practices

### Option 1: **sessionStorage** (Recommended for Most Cases)
```javascript
// Auto-clears when browser tab closes
sessionStorage.setItem('accessToken', token);
sessionStorage.getItem('accessToken'); // Get token
sessionStorage.removeItem('accessToken'); // Clear
```

**Pros:**
- Auto-clears when tab closes (secure)
- No cross-tab sharing (isolated)
- XSS attackers cannot access

**Cons:**
- Lost on page refresh (need refresh token)
- Not shared across tabs

### Option 2: **localStorage** (For Persistent Sessions)
```javascript
// Persists across browser restarts
localStorage.setItem('accessToken', token);
```

**Pros:**
- Persists across sessions
- User stays logged in

**Cons:**
- XSS attackers can access
- Manual cleanup needed

### Option 3: **Secure HttpOnly Cookie** (Most Secure)
```javascript
// Backend should set cookie in response
// Set-Cookie: accessToken=...; HttpOnly; Secure; SameSite=Strict
// Frontend cannot access (automatic sending in requests)
```

**Pros:**
- Cannot be accessed by JavaScript (immune to XSS)
- Automatically sent with every request
- CSRF protection with SameSite

**Cons:**
- Backend must set cookie
- More complex to manage

---

## Session Flow Diagram

```
User Navigation
    ↓
┌───────────────────────────────────────┐
│ 1. User not logged in (no token)      │
│    → Redirect to /login               │
└───────────────────────────────────────┘
    ↓ User enters credentials
┌───────────────────────────────────────┐
│ 2. POST /api/auth/login               │
│    → Receive accessToken + refreshToken│
│    → Store in sessionStorage           │
│    → Set user info                    │
└───────────────────────────────────────┘
    ↓ Logged in
┌───────────────────────────────────────┐
│ 3. User visits protected page         │
│    → API request with Authorization   │
│    → Header: Bearer {accessToken}     │
└───────────────────────────────────────┘
    ↓ API Response
┌───────────────────────────────────────┐
│ 4a. Status 200/201/etc → Success      │
│    → Use data                         │
└───────────────────────────────────────┘
    
    OR
    
┌───────────────────────────────────────┐
│ 4b. Status 401 → Token Expired        │
│    → POST /api/auth/refresh           │
│    → Get new accessToken              │
│    → Retry original request           │
└───────────────────────────────────────┘
    
    OR
    
┌───────────────────────────────────────┐
│ 4c. Refresh fails → Redirect to login │
│    → Clear tokens                     │
│    → User needs to login again        │
└───────────────────────────────────────┘
    ↓ User clicks Logout
┌───────────────────────────────────────┐
│ 5. POST /api/auth/logout              │
│    → Clear tokens from storage        │
│    → Redirect to /login               │
└───────────────────────────────────────┘
```

---

## Testing with cURL

### 1. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"staff1","password":"password123"}'
```

Response:
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ...
  }
}
```

### 2. Use Token in API Request

```bash
ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/api/staff \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 3. Refresh Token

```bash
REFRESH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

### 4. Logout

```bash
ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

---

## Key Concepts

### Dynamic Token Generation
- **New token on every login**: Each login generates unique access + refresh tokens
- **Stateless**: Backend doesn't store sessions; validates tokens via signature
- **Expiration**: Access tokens expire after 24 hours, refresh after 7 days

### Token Validation
- **Signature validation**: Ensures token wasn't tampered with
- **Expiration check**: Validates token hasn't expired
- **User extraction**: Gets username from token claims

### Session Management
- **Automatic renewal**: Use refresh token to get new access token
- **No re-authentication**: User doesn't need to login again during refresh
- **Graceful degradation**: If refresh fails, redirect to login

### Security Features
- **Bearer token**: Standard JWT transport method
- **HttpOnly cookies**: Not accessible to JavaScript (XSS protection)
- **HTTPS only**: Tokens encrypted in transit
- **CORS validation**: Only allowed origins can access

---

## Troubleshooting

### Issue: "Invalid credentials" on login
- ✓ Verify username/password are correct
- ✓ Check user exists in database with correct role

### Issue: "Token refresh failed" with 401
- ✓ Refresh token may have expired (7 days)
- ✓ User needs to login again

### Issue: API returns 401 even with valid token
- ✓ Token may be expired (check `expiresIn`)
- ✓ Token signature may be invalid (don't modify token)
- ✓ Bearer format incorrect (use `Bearer {token}`, not just token)

### Issue: "CORS error" from frontend
- ✓ Check `app.cors.allowed-origins` in application.properties
- ✓ Frontend URL must match allowed origins
- ✓ Use credentials: 'include' in fetch for cookies

---

## Environment Variables (Production)

Set these before running the application:

```bash
# JWT Secret (use strong random string)
export JWT_SECRET="your-super-secure-random-256-bit-key-here-minimum-32-chars"

# Database Password
export DB_PASSWORD="your-supabase-password"

# CORS Origins (comma-separated)
export CORS_ORIGINS="https://app.example.com,https://admin.example.com"
```

---

## Summary

The JWT session management system provides:

✅ **Dynamic token generation** on login  
✅ **Automatic token refresh** when expired  
✅ **Secure storage** in browser (sessionStorage/cookies)  
✅ **Stateless authentication** via JWT signature  
✅ **Production-ready** with environment variables  
✅ **7-day session persistence** with refresh tokens  
✅ **Graceful logout** clearing tokens  

The backend generates tokens dynamically, and the frontend handles:
1. Storing tokens after login
2. Including tokens in API requests
3. Refreshing tokens on 401 responses
4. Clearing tokens on logout

This ensures users stay logged in seamlessly while maintaining security!
