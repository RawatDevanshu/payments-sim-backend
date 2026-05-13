package com.devh.payment_sim.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devh.payment_sim.model.IdempotencyKey;


public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long>{

    Optional<IdempotencyKey> findByKey(String key);
    Optional<IdempotencyKey> findByKeyAndUserId(String key, Long userId);
}
