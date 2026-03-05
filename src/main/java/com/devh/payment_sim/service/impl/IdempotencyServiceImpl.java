package com.devh.payment_sim.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.devh.payment_sim.dto.response.IdempotencyResponse;
import com.devh.payment_sim.model.IdempotencyKey;
import com.devh.payment_sim.model.IdempotencyStatus;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.repository.IdempotencyKeyRepository;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.service.IdempotencyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService{
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final UserRepository userRepository;

    /**
     * Check if request was already processed
     * If yes, return cached response
     * If no, store it as PROCESSING
     */
    @Override
    public IdempotencyResponse handleIdempotencyKey(String key, Long userId, String endpoint){
        log.info("Checking idempotency - Key: {}, UserId: {}", key, userId);

        // Check if key already exists
        Optional<IdempotencyKey> existing = idempotencyKeyRepository
            .findByKeyAndUserId(key, userId);
            

        if(existing.isPresent()) {
            IdempotencyKey storedKey = existing.get();

            // if still processing, tell client to wait
            if(storedKey.getStatus() == IdempotencyStatus.PROCESSING) {
                log.warn("Request already processing - Key: {}", key);
                throw new RuntimeException("Request already being processed. Please retry.");
            }

            // Return cached response
            log.info("Returning cached response - Key: {}", key);
            return IdempotencyResponse.builder()
                .fromCache(true)
                .statusCode(storedKey.getHttpStatusCode())
                .responseBody(storedKey.getResponseBody())
                .build();
        }

        // New request - mark as PROCESSING
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        IdempotencyKey newEntry = IdempotencyKey.builder()
            .key(key)
            .user(user)
            .endpoint(endpoint)
            .status(IdempotencyStatus.PROCESSING)
            .httpStatusCode(0)
            .build();

        idempotencyKeyRepository.save(newEntry);
        log.info("New idempotency entry created - Key: {}", key);

        return IdempotencyResponse.builder()
            .fromCache(false)
            .build();
    }

    /**
     * Store success response
     */
    @Override
    public void markSuccess(String key, String responseBody, Integer statusCode) {
        IdempotencyKey entry = idempotencyKeyRepository.findByKey(key)
            .orElseThrow(() -> new RuntimeException("Idempotency key not found"));

        entry.setStatus(IdempotencyStatus.SUCCESS);
        entry.setResponseBody(responseBody);
        entry.setHttpStatusCode(statusCode);
        idempotencyKeyRepository.save(entry);

        log.info("Marked as SUCCESS - Key: {}", key);
    }

    /**
     * Store failure response
     */
    @Override
    public void markFailure(String key, String errorMessage, Integer statusCode) {
         IdempotencyKey entry = idempotencyKeyRepository.findByKey(key)
            .orElseThrow(() -> new RuntimeException("Idempotency key not found"));

        entry.setStatus(IdempotencyStatus.FAILED);
        entry.setResponseBody(errorMessage);
        entry.setHttpStatusCode(statusCode);
        idempotencyKeyRepository.save(entry);

        log.info("Marked as FAILED - Key: {}", key);
    }
}
