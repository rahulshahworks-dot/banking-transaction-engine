package com.rahul.bankingsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    private static final String IDEMPOTENCY_KEY_PREFIX = "idem:transfer:";

    // Initial short TTL while the transaction is being processed
    private static final Duration PROCESSING_TTL = Duration.ofMinutes(5);

    // Long TTL after successful processing to prevent future duplicates
    private static final Duration COMPLETED_TTL = Duration.ofHours(24);

    public enum IdempotencyStatus {
        PROCESSING,
        COMPLETED
    }

    /**
     * Attempts to acquire a lock for the given idempotency key.
     * @param idempotencyKey the unique key from the client request
     * @return true if the lock was successfully acquired, false if it already exists
     */
    public boolean acquireLock(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, IdempotencyStatus.PROCESSING.name(), PROCESSING_TTL);

        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Idempotency lock acquired for key: {}", idempotencyKey);
            return true;
        } else {
            String currentStatus = redisTemplate.opsForValue().get(redisKey);
            log.warn("Idempotency lock rejected for key: {}. Current status: {}", idempotencyKey, currentStatus);
            return false;
        }
    }

    /**
     * Updates the status to COMPLETED and extends the TTL to ensure
     * exactly-once semantics for retry requests over a long period.
     * @param idempotencyKey the unique key from the client request
     */
    public void markCompleted(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(redisKey, IdempotencyStatus.COMPLETED.name(), COMPLETED_TTL);
        log.debug("Idempotency key marked as COMPLETED: {}", idempotencyKey);
    }

    /**
     * Releases the lock by deleting the key. Useful if validation fails
     * or a transient error occurs, allowing the client to safely retry.
     * @param idempotencyKey the unique key from the client request
     */
    public void releaseLock(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.delete(redisKey);
        log.debug("Idempotency lock released (deleted) for key: {}", idempotencyKey);
    }
}
