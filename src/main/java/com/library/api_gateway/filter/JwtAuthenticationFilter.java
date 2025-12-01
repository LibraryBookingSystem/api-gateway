package com.library.api_gateway.filter;

import com.library.api_gateway.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Global Filter for API Gateway
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip auth for public endpoints
        if (isPublicEndpoint(request.getURI().getPath())) {
            return chain.filter(exchange);
        }
        
        // Check for Authorization header
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
        }
        
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        logger.debug("Extracted token: {}", token.substring(0, Math.min(20, token.length())) + "...");
        
        // Validate token
        try {
            if (!jwtUtil.validateToken(token)) {
                logger.error("Token validation failed for token starting with: {}", token.substring(0, Math.min(20, token.length())));
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
            
            if (jwtUtil.isTokenExpired(token)) {
                logger.error("Token is expired");
                return onError(exchange, "Expired token", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            logger.error("Exception during token validation: {}", e.getMessage(), e);
            return onError(exchange, "Token validation error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        
        // Add user info to headers for downstream services
        try {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            
            logger.debug("Token validated successfully. UserId: {}, Username: {}", userId, username);
            
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId != null ? userId.toString() : "")
                .header("X-Username", username != null ? username : "")
                .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            logger.error("Error processing JWT token: {}", e.getMessage(), e);
            return onError(exchange, "Error processing token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
    
    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
    
    private boolean isPublicEndpoint(String path) {
        List<String> publicPaths = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/health",
            "/api/users/health",
            "/api/resources/health",
            "/api/policies/health",
            "/api/bookings/health",
            "/api/notifications/health",
            "/api/analytics/health"
        );
        
        return publicPaths.stream().anyMatch(path::startsWith);
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        logger.warn("Authentication failed: {}", message);
        return response.setComplete();
    }
}

