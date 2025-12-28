package com.staffalteration.controller;

import com.staffalteration.dto.AuthRequestDTO;
import com.staffalteration.dto.AuthResponseDTO;
import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Endpoints for user authentication and JWT token management:
 * - POST /api/auth/login: Login with username/password, get access + refresh tokens
 * - POST /api/auth/refresh: Refresh access token using refresh token
 * - POST /api/auth/logout: Logout and invalidate session
 * - GET /api/auth/user/{userId}: Get user by ID
 * - GET /api/auth/user/username/{username}: Get user by username
 * - GET /api/auth: List available auth endpoints
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthenticationService authenticationService;
    
    /**
     * GET /api/auth
     * List all available authentication endpoints
     */
    @GetMapping("")
    public ResponseEntity<ApiResponseDTO<?>> authInfo() {
        log.debug("Auth endpoints info requested");
        return ResponseEntity.ok(new ApiResponseDTO<>(
                200,
                "Auth endpoints available",
                "POST /api/auth/login - Login with username and password, returns access & refresh tokens\n" +
                "POST /api/auth/refresh - Refresh access token using refresh token\n" +
                "POST /api/auth/logout - Logout user and invalidate session\n" +
                "GET /api/auth/user/{userId} - Get user by ID\n" +
                "GET /api/auth/user/username/{username} - Get user by username"
        ));
    }
    
    /**
     * POST /api/auth/login
     * Authenticate user and generate JWT tokens
     * 
     * Request body: { "username": "staff1", "password": "password123" }
     * Response: Access token (24h), Refresh token (7d), User info
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(@RequestBody AuthRequestDTO authRequest) {
        log.info("🔐 Login request for user: {}", authRequest.getUsername());
        
        try {
            AuthResponseDTO response = authenticationService.login(authRequest);
            
            log.info("✅ Login successful for user: {}", authRequest.getUsername());
            
            return ResponseEntity.ok(new ApiResponseDTO<>(
                    200,
                    "Login successful",
                    response
            ));
        } catch (RuntimeException e) {
            log.error("❌ Login failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponseDTO<>(
                    401,
                    "Invalid credentials: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Login error: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(
                    500,
                    "Login failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * POST /api/auth/refresh
     * Refresh access token using refresh token
     * 
     * Request header: Authorization: Bearer {refreshToken}
     * OR Request body: { "refreshToken": "..." }
     * Response: New access token with same refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> refreshToken(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) {
        
        log.info("🔄 Token refresh request");
        
        try {
            String refreshToken = null;
            
            // Try to get refresh token from Authorization header (Bearer token)
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7);
            }
            // Try to get refresh token from request body
            else if (request != null && request.getRefreshToken() != null) {
                refreshToken = request.getRefreshToken();
            }
            
            if (refreshToken == null) {
                return ResponseEntity.status(400).body(new ApiResponseDTO<>(
                        400,
                        "Refresh token not provided in Authorization header or request body"
                ));
            }
            
            AuthResponseDTO response = authenticationService.refreshToken(refreshToken);
            
            log.info("✅ Token refresh successful");
            
            return ResponseEntity.ok(new ApiResponseDTO<>(
                    200,
                    "Token refreshed successfully",
                    response
            ));
        } catch (RuntimeException e) {
            log.error("❌ Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponseDTO<>(
                    401,
                    "Token refresh failed: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Refresh token error: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(
                    500,
                    "Refresh failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * POST /api/auth/logout
     * Logout user and clear session
     * 
     * Note: In a production system, you should:
     * 1. Implement token blacklist in Redis or database
     * 2. Add tokens to blacklist when user logs out
     * 3. Check blacklist before validating tokens
     * 
     * For now, frontend should clear localStorage/sessionStorage/cookies
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<?>> logout(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        
        log.info("🚪 Logout request");
        
        try {
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            // In production: Add token to blacklist
            // For now, just return success - frontend should clear tokens
            log.info("✅ Logout successful");
            
            return ResponseEntity.ok(new ApiResponseDTO<>(
                    200,
                    "Logout successful. Please clear your tokens from browser storage."
            ));
        } catch (Exception e) {
            log.error("❌ Logout error: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(
                    500,
                    "Logout failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/auth/user/{userId}
     * Get user information by ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> getUserById(@PathVariable Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        
        try {
            var user = authenticationService.getUserById(userId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "User retrieved", user));
        } catch (RuntimeException e) {
            log.warn("User not found: {}", userId);
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, "User not found"));
        } catch (Exception e) {
            log.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error retrieving user"));
        }
    }
    
    /**
     * GET /api/auth/user/username/{username}
     * Get user information by username
     */
    @GetMapping("/user/username/{username}")
    public ResponseEntity<ApiResponseDTO<?>> getUserByUsername(@PathVariable String username) {
        log.debug("Fetching user by username: {}", username);
        
        try {
            var user = authenticationService.getUserByUsername(username);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "User retrieved", user));
        } catch (RuntimeException e) {
            log.warn("User not found: {}", username);
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, "User not found"));
        } catch (Exception e) {
            log.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "Error retrieving user"));
        }
    }
    
    /**
     * Inner DTO for refresh token request body
     */
    public static class RefreshTokenRequest {
        private String refreshToken;
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
