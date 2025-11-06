package com.devh.payment_sim.service;

import com.devh.payment_sim.model.BankAccount;

public interface BankAccountService {
    BankAccount linkAccount(Long userId, String accountNumber, String bankName, String ifscCode);
}
