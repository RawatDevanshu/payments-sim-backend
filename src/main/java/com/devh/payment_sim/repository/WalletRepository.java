package com.devh.payment_sim.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.devh.payment_sim.model.Wallet;

import jakarta.persistence.LockModeType;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUpiHandle(String upiHandle);
    boolean existsByUpiHandle(String upiHandle);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.upiHandle = :upiHandle")
    Optional<Wallet> findByUpiHandleWithLock(String upiHandle);
}
