package com.devh.payment_sim.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devh.payment_sim.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
}
