package com.devh.payment_sim.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.devh.payment_sim.model.BankAccount;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.repository.BankAccountRepository;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.service.BankAccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Override
    public BankAccount linkAccount(Long userId, String accountNumber, String bankName, String ifscCode) {
       User user = userRepository.findById(userId)
            .orElseThrow(()-> new RuntimeException("User not found"));

        BankAccount account = BankAccount.builder()
                            .user(user)
                            .accountNumber(accountNumber)
                            .bankName(bankName)
                            .ifscCode(ifscCode)
                            .balance(BigDecimal.valueOf(10000))
                            .isLinked(true)
                            .build();

        return bankAccountRepository.save(account);
    }
    
}
