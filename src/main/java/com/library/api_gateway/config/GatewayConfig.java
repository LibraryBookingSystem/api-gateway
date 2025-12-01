package com.library.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routing configuration
 * Note: JWT authentication is handled by GlobalFilter (JwtAuthenticationFilter)
 */
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth Service routes
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .uri("http://localhost:3002")
            )
            
            // User Service routes
            .route("user-service", r -> r
                .path("/api/users/**")
                .uri("http://localhost:3001")
            )
            
            // Catalog Service routes
            .route("catalog-service", r -> r
                .path("/api/resources/**")
                .uri("http://localhost:3003")
            )
            
            // Policy Service routes
            .route("policy-service", r -> r
                .path("/api/policies/**")
                .uri("http://localhost:3005")
            )
            
            // Booking Service routes
            .route("booking-service", r -> r
                .path("/api/bookings/**")
                .uri("http://localhost:3004")
            )
            
            // Notification Service routes
            .route("notification-service", r -> r
                .path("/api/notifications/**")
                .uri("http://localhost:3006")
            )
            
            // Analytics Service routes
            .route("analytics-service", r -> r
                .path("/api/analytics/**")
                .uri("http://localhost:3007")
            )
            
            .build();
    }
}

