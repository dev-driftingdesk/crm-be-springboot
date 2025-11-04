package com.ceedpods.crmbuild.service.authService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter for password reset requests to prevent abuse.
 * Limits the number of password reset requests per email address.
 */
@Service
@Slf4j
public class PasswordResetRateLimiter {

    // Store email -> list of request timestamps
    private final Map<String, LocalDateTime> lastRequestTime = new ConcurrentHashMap<>();
    private final Map<String, Integer> requestCount = new ConcurrentHashMap<>();

    @Value("${app.password-reset.rate-limit.max-requests-per-hour:5}")
    private int maxRequestsPerHour;

    @Value("${app.password-reset.rate-limit.min-minutes-between-requests:2}")
    private int minMinutesBetweenRequests;

    /**
     * Check if a password reset request is allowed for the given email
     *
     * @param email User's email address
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isRequestAllowed(String email) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRequest = lastRequestTime.get(email);

        // First request or after an hour - allow and reset counter
        if (lastRequest == null || lastRequest.isBefore(now.minusHours(1))) {
            lastRequestTime.put(email, now);
            requestCount.put(email, 1);
            log.debug("Password reset allowed for: {} (first request or hourly reset)", email);
            return true;
        }

        // Check minimum time between requests
        if (lastRequest.isAfter(now.minusMinutes(minMinutesBetweenRequests))) {
            log.warn("Password reset rate limit: too soon for email: {} (last request: {})",
                email, lastRequest);
            return false;
        }

        // Check maximum requests per hour
        Integer count = requestCount.getOrDefault(email, 0);
        if (count >= maxRequestsPerHour) {
            log.warn("Password reset rate limit exceeded for email: {} (count: {})", email, count);
            return false;
        }

        // Allow request and increment counter
        lastRequestTime.put(email, now);
        requestCount.put(email, count + 1);
        log.debug("Password reset allowed for: {} (count: {}/{})", email, count + 1, maxRequestsPerHour);
        return true;
    }

    /**
     * Get remaining requests for an email address in the current hour
     *
     * @param email User's email address
     * @return Number of remaining requests
     */
    public int getRemainingRequests(String email) {
        LocalDateTime lastRequest = lastRequestTime.get(email);
        LocalDateTime now = LocalDateTime.now();

        // If no requests or after an hour, return max
        if (lastRequest == null || lastRequest.isBefore(now.minusHours(1))) {
            return maxRequestsPerHour;
        }

        Integer count = requestCount.getOrDefault(email, 0);
        return Math.max(0, maxRequestsPerHour - count);
    }

    /**
     * Get time until next request is allowed (in minutes)
     *
     * @param email User's email address
     * @return Minutes until next request, or 0 if allowed now
     */
    public long getMinutesUntilNextRequest(String email) {
        LocalDateTime lastRequest = lastRequestTime.get(email);
        LocalDateTime now = LocalDateTime.now();

        if (lastRequest == null) {
            return 0;
        }

        LocalDateTime nextAllowedTime = lastRequest.plusMinutes(minMinutesBetweenRequests);
        if (now.isAfter(nextAllowedTime)) {
            return 0;
        }

        return java.time.Duration.between(now, nextAllowedTime).toMinutes();
    }
}
