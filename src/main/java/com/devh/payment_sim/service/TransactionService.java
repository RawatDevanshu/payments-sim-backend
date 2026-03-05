package com.devh.payment_sim.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.devh.payment_sim.model.Transaction;

public interface TransactionService {
    Transaction sendMoney(String fromUpi, String toUpi, BigDecimal amount, String upiPin, String remarks);
    Transaction topUpFromBank(Long userId, String walletUpiHandle, String bankAccountNumber, BigDecimal amount, String bankPin, String remarks);
    Transaction withdrawFromWallet(Long userId, String walletUpiHandle, String bankAccountNumber, BigDecimal amount, String walletPin, String remarks);
    Page<Transaction> getTransactionsByWalletUpi(String walletUpiHandle, Pageable pageable);
}
