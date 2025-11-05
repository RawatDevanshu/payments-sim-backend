package com.devh.payment_sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devh.payment_sim.model.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    
}
