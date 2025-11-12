package com.devh.payment_sim.service;

import java.util.List;

import com.devh.payment_sim.model.BankAccount;

public interface BankAccountService {
    BankAccount linkAccount(Long userId, String accountNumber, String bankName, String ifscCode);
    List<BankAccount> getBankAccountsByUserId(Long userId);
}
