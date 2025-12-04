package com.devh.payment_sim.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.devh.payment_sim.dto.OpenAccountRequest;
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
    public BankAccount createAccount(OpenAccountRequest request) {
       String hashedPin = BCrypt.hashpw(request.getBankPin(), BCrypt.gensalt());

       User user = userRepository.findById(request.getUserId())
            .orElseThrow(()-> new RuntimeException("User not found"));

        BankAccount account = BankAccount.builder()
                            .user(user)
                            .accountNumber(request.getAccountNumber())
                            .balance(BigDecimal.valueOf(10000))
                            .bankPinHash(hashedPin)
                            .build();

        return bankAccountRepository.save(account);
    }

    @Override
    public List<BankAccount> getBankAccountsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found"));
        
        return bankAccountRepository.findByUser(user);
    }
    
    @Override
    public BankAccount getBankAccountByAccountNumber(String accountNumber) {
               
        return bankAccountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new RuntimeException("Bank Account not found"));
                        
    }
}
