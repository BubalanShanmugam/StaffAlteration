package com.staffalteration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider - Handles generation, validation, and refresh of JWT tokens
 * 
 * This component manages the complete JWT lifecycle:
 * - Access token generation (short-lived, 24 hours)
 * - Refresh token generation (long-lived, 7 days)
 * - Token validation and claim extraction
 * - Proper token expiration handling
 */
@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;
    
    // Refresh token expiration: 7 days
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000;
    
    /**
     * Generate an access token from authentication
     * Used during login to create short-lived JWT token
     */
    public String generateToken(Authentication authentication) {
        org.springframework.security.core.userdetails.UserDetails userPrincipal = 
            (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        
        return generateTokenFromUsername(userPrincipal.getUsername(), jwtExpirationInMs);
    }
    
    /**
     * Generate an access token from username
     * Used for token refresh operations
     */
    public String generateTokenFromUsername(String username) {
        return generateTokenFromUsername(username, jwtExpirationInMs);
    }
    
    /**
     * Internal method to generate token with custom expiration
     */
    private String generateTokenFromUsername(String username, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
        
        log.debug("Generated token for user: {} (expires in {}ms)", username, expirationMs);
        return token;
    }
    
    /**
     * Generate a refresh token for long-lived session management
     * Refresh tokens typically have longer expiration (7 days)
     * Used to obtain new access tokens without re-authentication
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MS);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("type", "refresh")
                .signWith(key)
                .compact();
        
        log.debug("Generated refresh token for user: {} (expires in {}ms)", username, REFRESH_TOKEN_EXPIRATION_MS);
        return token;
    }
    
    /**
     * Extract username from JWT token
     */
    public String getUsernameFromJWT(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
    /**
     * Get expiration time from token
     */
    public long getTokenExpiration(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getExpiration().getTime();
    }
    
    /**
     * Validate JWT token
     * Checks signature validity and expiration
     */
    public boolean validateToken(String authToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken);
            
            log.debug("Token validation successful");
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            log.warn("JWT token is expired: {}", ex.getMessage());
            return false;
        } catch (io.jsonwebtoken.UnsupportedJwtException ex) {
            log.warn("JWT token is unsupported: {}", ex.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        } catch (io.jsonwebtoken.SignatureException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
            return false;
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }
}
