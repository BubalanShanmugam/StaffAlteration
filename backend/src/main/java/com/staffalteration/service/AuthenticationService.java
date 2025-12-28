package com.staffalteration.service;

import com.staffalteration.dto.AuthRequestDTO;
import com.staffalteration.dto.AuthResponseDTO;
import com.staffalteration.dto.UserDTO;
import com.staffalteration.entity.Role;
import com.staffalteration.entity.Staff;
import com.staffalteration.entity.User;
import com.staffalteration.repository.RoleRepository;
import com.staffalteration.repository.StaffRepository;
import com.staffalteration.repository.UserRepository;
import com.staffalteration.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication Service
 * 
 * Handles user authentication and JWT token management:
 * - Login: Authenticates user and generates access + refresh tokens
 * - Token Refresh: Generates new access token from refresh token
 * - Token Validation: Validates JWT tokens
 * - Logout: Invalidates user session
 */
@Service
@Slf4j
@Transactional
public class AuthenticationService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Login user with username and password
     * Generates both access and refresh tokens
     * 
     * @param authRequest Login credentials (username + password)
     * @return AuthResponseDTO with tokens and user info
     * @throws RuntimeException if user not found or authentication fails
     */
    public AuthResponseDTO login(AuthRequestDTO authRequest) {
        log.info("🔐 Login attempt for user: {}", authRequest.getUsername());
        
        try {
            // Authenticate using Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );
            
            // Generate access token (24 hours)
            String accessToken = tokenProvider.generateToken(authentication);
            
            // Get user and generate refresh token (7 days)
            User user = userRepository.findByUsername(authRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());
            
            log.info("✅ User {} logged in successfully", authRequest.getUsername());
            
            return AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .type("Bearer")
                    .expiresIn(86400000L)  // 24 hours in milliseconds
                    .refreshTokenExpiresIn(604800000L)  // 7 days in milliseconds
                    .user(mapUserToDTO(user))
                    .build();
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("❌ Authentication failed for user: {}", authRequest.getUsername());
            throw new RuntimeException("Invalid credentials");
        } catch (Exception e) {
            log.error("❌ Login failed for user {}: {}", authRequest.getUsername(), e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }
    
    /**
     * Refresh access token using refresh token
     * Called when access token is expired but refresh token is still valid
     * 
     * @param refreshToken Long-lived refresh token
     * @return AuthResponseDTO with new access token
     */
    public AuthResponseDTO refreshToken(String refreshToken) {
        log.info("🔄 Refreshing access token");
        
        try {
            // Validate refresh token
            if (!tokenProvider.validateToken(refreshToken)) {
                log.error("❌ Invalid or expired refresh token");
                throw new RuntimeException("Invalid refresh token");
            }
            
            // Extract username from refresh token
            String username = tokenProvider.getUsernameFromJWT(refreshToken);
            
            // Generate new access token
            String newAccessToken = tokenProvider.generateTokenFromUsername(username);
            
            // Get user information
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            log.info("✅ Access token refreshed for user: {}", username);
            
            return AuthResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)  // Refresh token remains the same
                    .type("Bearer")
                    .expiresIn(86400000L)  // 24 hours in milliseconds
                    .refreshTokenExpiresIn(604800000L)  // 7 days in milliseconds
                    .user(mapUserToDTO(user))
                    .build();
        } catch (Exception e) {
            log.error("❌ Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }
    
    /**
     * Logout user (token invalidation)
     * Note: JWT tokens cannot be truly revoked without a token blacklist
     * In production, implement token blacklist in Redis or database
     */
    public void logout(String username) {
        log.info("🚪 Logout for user: {}", username);
        // In production, add token to blacklist (Redis, database, etc.)
        // For now, frontend should delete tokens from storage
    }
    
    /**
     * Get user by ID
     */
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapUserToDTO(user);
    }
    
    /**
     * Get user by username
     */
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapUserToDTO(user);
    }
    
    /**
     * Convert User entity to UserDTO
     * Includes staffId from associated Staff entity if available
     */
    private UserDTO mapUserToDTO(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleType().toString())
                .collect(Collectors.toSet());
        
        // Try to get staffId from associated Staff entity using user ID
        String staffId = null;
        try {
            java.util.Optional<Staff> staff = staffRepository.findByUserId(user.getId());
            if (staff.isPresent()) {
                staffId = staff.get().getStaffId();
            }
        } catch (Exception e) {
            log.warn("Could not fetch staffId for user {}: {}", user.getUsername(), e.getMessage());
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .staffId(staffId)
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roles)
                .build();
    }
}
