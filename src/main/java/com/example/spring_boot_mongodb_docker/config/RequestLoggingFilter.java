package com.example.spring_boot_mongodb_docker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Generate unique request ID
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        // Get client IP
        String clientIp = request.getRemoteAddr();
        MDC.put("clientIp", clientIp);

        // Get authenticated user if available
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            MDC.put("userId", authentication.getName());
        }

        long startTime = System.currentTimeMillis();

        try {
            logger.info("Received request: {} {} from IP: {}",
                    request.getMethod(), request.getRequestURI(), clientIp);

            // Add request ID to response header for client-side tracking
            response.setHeader("X-Request-ID", requestId);

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Request completed in {}ms with status {}",
                    duration, response.getStatus());

            // Clear MDC context to prevent memory leaks
            MDC.clear();
        }
    }
}
