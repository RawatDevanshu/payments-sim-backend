package com.devh.payment_sim.service;

import java.util.List;

import com.devh.payment_sim.dto.OpenAccountRequest;
import com.devh.payment_sim.model.BankAccount;

public interface BankAccountService {
    BankAccount createAccount(OpenAccountRequest request);
    List<BankAccount> getBankAccountsByUserId(Long userId);
    BankAccount getBankAccountByAccountNumber(String accountNumber);
}
