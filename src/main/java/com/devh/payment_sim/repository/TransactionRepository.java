package com.devh.payment_sim.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.Wallet;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromWalletOrToWallet(Wallet fromWallet, Wallet toWallet);
    Optional<Transaction> findByTransactionId(String transactionId);
}
