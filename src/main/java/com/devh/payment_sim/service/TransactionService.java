package com.devh.payment_sim.service;

import java.math.BigDecimal;

import com.devh.payment_sim.model.Transaction;

public interface TransactionService {
    Transaction sendMoney(String fromUpi, String toUpi, BigDecimal amount, String upiPin, String remarks);
}
