package com.devh.payment_sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devh.payment_sim.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
}
