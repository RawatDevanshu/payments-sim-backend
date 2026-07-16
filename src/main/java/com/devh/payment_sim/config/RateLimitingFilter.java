package com.devh.payment_sim.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Simple in-memory cache to store buckets per client key
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Create a bucket configuration: Allow 10 requests per 1 minute
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.greedy(10, java.time.Duration.ofMinutes(1))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
        HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Resolve key ( User email if logged in, fallback to client IP)
        String clientKey = resolveClientKey(request); 

        // 2. Fetch or create the bucket for this client
        Bucket bucket = cache.computeIfAbsent(clientKey, k -> createNewBucket());

        // 3. Consume a token and check if allowed
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response); // Proceed with the request
        } else {
            // Rate limit exceeded: return HTTP 429 structured JSON
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}");
        }
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // if authenticated and not an anonymous user, limit by user email
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "USER:" + auth.getName(); // Use email or username as key
        }
        
        // Fallback: Limit by client IP address
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "IP:" + ip;
    }
    
}
