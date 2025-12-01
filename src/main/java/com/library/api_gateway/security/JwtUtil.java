package com.library.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for JWT token operations
 */
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:my-super-secret-jwt-key-for-library-booking-system-2024}")
    private String jwtSecret;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (io.jsonwebtoken.security.SecurityException e) {
            System.err.println("JWT Security Exception: " + e.getMessage());
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.err.println("JWT Expired: " + e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.err.println("JWT Malformed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("JWT Validation Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get username from token
     */
    public String getUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    
    /**
     * Get user ID from token
     */
    public Long getUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}

