package com.devh.payment_sim.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devh.payment_sim.model.UPIPin;

public interface UPIPinRepository  extends JpaRepository<UPIPin, Long>{
    Optional<UPIPin> findByWalletId(Long walletId);
}
