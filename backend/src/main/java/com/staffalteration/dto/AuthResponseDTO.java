package com.staffalteration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Response DTO
 * 
 * Returned after successful login with:
 * - accessToken: Short-lived JWT token (24 hours) for API requests
 * - refreshToken: Long-lived token (7 days) for obtaining new access tokens
 * - expiresIn: Expiration time in milliseconds (24 hours = 86400000ms)
 * - refreshTokenExpiresIn: Refresh token expiration in milliseconds (7 days = 604800000ms)
 * - user: User information and roles
 * 
 * Frontend should:
 * 1. Store accessToken in secure cookie or sessionStorage (auto-cleared on browser close)
 * 2. Store refreshToken in secure cookie with HttpOnly flag
 * 3. Include accessToken in "Authorization: Bearer {token}" header on API requests
 * 4. On 401 response, use refreshToken to obtain new accessToken
 * 5. Clear both tokens on logout
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String type;
    private Long expiresIn;
    private Long refreshTokenExpiresIn;
    private UserDTO user;
}
