package com.devh.payment_sim.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devh.payment_sim.model.UPIPin;
import com.devh.payment_sim.model.Wallet;

public interface UPIPinRepository  extends JpaRepository<UPIPin, Long>{
    Optional<UPIPin> findByWallet(Wallet wallet);
}
