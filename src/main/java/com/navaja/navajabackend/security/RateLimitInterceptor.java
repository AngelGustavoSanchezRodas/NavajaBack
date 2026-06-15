package com.navaja.navajabackend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private static final int MAX_REQUESTS = 20;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private static final Duration STALE_BUCKET_TTL = Duration.ofHours(2);

    private final Map<String, BucketState> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String ip = resolveClientIp(request);
        if (ip == null || ip.isBlank()) {
            ip = "unknown";
        }

        BucketState state = cache.compute(ip, (key, existing) -> {
            if (existing == null) {
                return new BucketState(createBucket(key), Instant.now());
            }
            existing.lastSeen = Instant.now();
            return existing;
        });

        if (!state.bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for ip={} path={}", ip, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("Límite excedido");
            return false;
        }

        return true;
    }

    private Bucket createBucket(String ignoredIp) {
        Bandwidth limit = Bandwidth.classic(MAX_REQUESTS, io.github.bucket4j.Refill.greedy(MAX_REQUESTS, REFILL_PERIOD));
        return Bucket.builder().addLimit(limit).build();
    }

    @Scheduled(fixedDelayString = "${app.rate-limit.cleanup-ms:3600000}")
    public void purgeStaleBuckets() {
        Instant cutoff = Instant.now().minus(STALE_BUCKET_TTL);
        int before = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().lastSeen.isBefore(cutoff));
        int removed = before - cache.size();
        if (removed > 0) {
            log.info("Rate limiter cleanup removed {} stale buckets", removed);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private static final class BucketState {
        private final Bucket bucket;
        private Instant lastSeen;

        private BucketState(Bucket bucket, Instant lastSeen) {
            this.bucket = bucket;
            this.lastSeen = lastSeen;
        }
    }
}
